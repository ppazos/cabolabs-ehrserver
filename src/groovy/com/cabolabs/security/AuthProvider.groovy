package com.cabolabs.security

import com.cabolabs.security.UserPassOrgAuthToken
import com.cabolabs.security.User

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
import grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthProvider implements AuthenticationProvider
{
    //def userDetailsService
    //def passwordEncoder // should be injected! but is not... I might need to configure something in the resources.groovy file.
    def passwordEncoder = new BCryptPasswordEncoder(10) // wont do the config for now, FIXME: this should consider the current encoder config, if we change it, this should change.
    //passwordEncoder(BCryptPasswordEncoder, conf.password.bcrypt.logrounds) // 10
    
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
       
       def user = User.findByUsername(username);
       if (user == null)
       {
          System.out.println( "wrong username" );
          throw new UsernameNotFoundException("No matching account"); 
       }
       
       
       // Status checks
       if (!user.enabled)
       {
          System.out.println( "account disabled" );
          throw new DisabledException("Account disabled");
       }
       
       if (user.accountExpired)
       {
          System.out.println( "account expired" );
          throw new AccountExpiredException("Account expired");
       }
       
       if (user.accountLocked)
       {
          System.out.println( "account locked" );
          throw new LockedException("Account locked");
       }
       
       
       // Check password
       assert this.passwordEncoder != null
       
       if (!passwordEncoder.isPasswordValid(user.password, password, null))
       {
          System.out.println( "wrong password" );
          throw new BadCredentialsException("Authentication failed");
       }
       
       println 'orgn '+ organization_number
       
       // Check organization
       Organization org = Organization.findByNumber(organization_number)
       
       println 'org '+ org
       
       if (org == null)
       {
          System.out.println( "organization is not associated with user 1" );
          throw new BadCredentialsException("Authentication failed");
       }
       
       println 'user orgs '+ user.organizations
       
       if (!user.organizations.contains( org.getUid() ))
       {
          System.out.println( "organization is not associated with user 2" );
          throw new BadCredentialsException("Authentication failed");
       }
       
       /*
       Message: failed to lazily initialize a collection of role: com.cabolabs.security.User.organizations, could not initialize proxy - no Session
       Line | Method
       ->>  115 | doAuthentication      in com.cabolabs.security.AuthProvider
       */
     
       auth.setAuthenticated(true)
       
       return auth
    }
}
