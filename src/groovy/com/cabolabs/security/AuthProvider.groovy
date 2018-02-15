
/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.security

import com.cabolabs.security.UserPassOrgAuthToken
import com.cabolabs.security.User
import com.cabolabs.ehrserver.account.Account

import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.aop.aspectj.RuntimeTestWalker.ThisInstanceOfResidueTestVisitor
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.util.Assert
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.logging.Logger
import grails.util.Holders

class AuthProvider implements AuthenticationProvider
{
    //def userDetailsService
    //def passwordEncoder // should be injected! but is not... I might need to configure something in the resources.groovy file.
    def passwordEncoder // = new BCryptPasswordEncoder(10) // wont do the config for now, FIXME: this should consider the current encoder config, if we change it, this should change.
    //passwordEncoder(BCryptPasswordEncoder, conf.password.bcrypt.logrounds) // 10
    
    def userService = Holders.grailsApplication.mainContext.getBean('userService')
    
    def log = Logger.getLogger('com.cabolabs.security.AuthProvider')
    
    Authentication authenticate(Authentication auth) throws AuthenticationException
    {
        Assert.isInstanceOf(UserPassOrgAuthToken.class, auth, "Only UserPassOrgAuthToken is supported")
                        
        UserPassOrgAuthToken authentication = (UserPassOrgAuthToken) auth
          
        // userDetailsService no se inyecta porque no hay bean 
        //def userDetails = userDetailsService.loadUserByUsername(auth.principal)
        //userDetails have the following properties like username, isEnabled..etc
        
        //println userDetails
        
        /*
        def user = User.findByUsername(auth.principal);
        if(userDetails != null && user.token == auth.credentials)
        {
            auth.authorities = userDetails.authorities;
            auth.principal = userDetails
            return auth;
        }
        */
        
        return doAuthentication(authentication)
    }
    
    @Override
    boolean supports(Class authentication)
    {
        return UserPassOrgAuthToken.class.isAssignableFrom(authentication)
    }
    
    // our custom authorization logic
    def doAuthentication(UserPassOrgAuthToken auth)
    {
       def username = auth.principal
       def password = auth.credentials // plain text entered by the user
       def organization_number = auth.organization
       
       def user
       User.withNewSession { 
          user = User.findByUsername(username)
       }
       if (user == null)
       {
          log.info("No matching account")
          throw new UsernameNotFoundException("No matching account")
       }
       
       // Status checks
       if (!user.enabled)
       {
          log.info("User account disabled")
          throw new DisabledException("User account disabled")
       }
       
       Account.withNewSession {
          if (!user.account.enabled)
          {
             log.info("Company account disabled")
             throw new DisabledException("Company account disabled")
          }
       }
       
       if (user.accountExpired)
       {
          log.info("Account expired")
          throw new AccountExpiredException("Account expired")
       }
       
       if (user.accountLocked)
       {
          log.info("Account locked")
          throw new LockedException("Account locked")
       }
       
       // Check password
       assert this.passwordEncoder != null
       
       if (!passwordEncoder.isPasswordValid(user.password, password, null))
       {
          log.info("Authentication failed - invalid password")
          throw new BadCredentialsException("Authentication failed")
       }
       
       if (!organization_number) // null or empty
       {
          log.info("Authentication failed - organization number not provided")
          throw new BadCredentialsException("Authentication failed - organization number not provided")
       }
       
       // Check organization
       Organization org
       Organization.withNewSession {
          org = Organization.findByNumber(organization_number)
       }
       
       if (org == null)
       {
          log.info("Authentication failed - organization doesnt exists")
          throw new BadCredentialsException("Authentication failed")
       }
       
       //println 'user orgs '+ user.organizations
       
       if (!user.organizations.find{ it.uid == org.uid })
       {
          log.info("Authentication failed - user not associated with existing organization")
          throw new BadCredentialsException("Authentication failed - check the organization number")
       }

       //auth.setAuthenticated(true)

       // sets authenticated true
       // TODO: do it in a userDetailsService
       // http://www.oodlestechnologies.com/blogs/Adding-Custom-Spring-Security-Authentication
       def userDetails = new GrailsUser(user.username,
                                        user.password,
                                        user.enabled,
                                        !user.accountExpired,
                                        !user.passwordExpired, // credentialsNonExpired
                                        !user.accountLocked,
                                        userService.getUserAuthorities(user, org),
                                        user.id)
       
       // The userDetails.authorities is not over our User is over the SpringSecurity User, so it doesnt require an org.
       // http://docs.spring.io/spring-security/site/docs/current/apidocs/org/springframework/security/core/userdetails/User.html
       auth = new UserPassOrgAuthToken(userDetails, password, organization_number, userDetails.authorities)
       
       return auth
    }
}
