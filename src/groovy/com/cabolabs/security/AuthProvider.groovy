package com.cabolabs.security

import com.cabolabs.security.UserPassOrgAuthToken
import com.cabolabs.security.User
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.util.Assert

class AuthProvider implements AuthenticationProvider
{
    def userDetailsService
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
        
        return doAuthorization(authentication)
    }
    
    @Override
    boolean supports(Class authentication)
    {
        return UserPassOrgAuthToken.class.isAssignableFrom(authentication)
    }
    
    // my custom authorization logic
    def doAuthorization(UserPassOrgAuthToken auth)
    {
       def username = auth.principal
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
       if (user.password != auth.credentials) // credentials is the encoded pass input from the user
       {
          System.out.println( "wrong password" );
          throw new BadCredentialsException("Authentication failed");
       }
       
       // Check organization
       Organization org = Organization.findByNumber(organization_number);
       if (org == null)
       {
          System.out.println( "organization is not associated with user" );
          throw new BadCredentialsException("Authentication failed");
       }
       
       if (!user.getOrganizations().contains( org.getUid() ))
       {
          System.out.println( "organization is not associated with user" );
          throw new BadCredentialsException("Authentication failed");
       }
     
       auth.setAuthenticated(true)
       
       return auth
    }
}
