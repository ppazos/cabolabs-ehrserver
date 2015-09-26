package com.cabolabs.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;
//import org.springframework.security.authentication.AuthenticationManager
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.authentication.AuthenticationManager

import com.cabolabs.security.UserPassOrgAuthToken;

/**
Alternative to http://docs.spring.io/autorepo/docs/spring-security/3.2.3.RELEASE/apidocs/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html
that considers username, password and organization number for the login.
ref: http://stackoverflow.com/questions/32621369/customize-login-in-grails-spring-security-plugin
ref: http://www.oodlestechnologies.com/blogs/Adding-Custom-Spring-Security-Authentication
*/
public class AuthFilter extends AbstractAuthenticationProcessingFilter implements ApplicationEventPublisherAware {
 
  AuthenticationManager authenticationManager
  AuthProvider authProvider
 
  public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
  public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";
  
  /** @deprecated */
  @Deprecated
  public static final String SPRING_SECURITY_LAST_USERNAME_KEY = "SPRING_SECURITY_LAST_USERNAME";
  private String usernameParameter = "j_username";
  private String passwordParameter = "j_password";
  private String organisationParameter = "j_organisation";
  private boolean postOnly = true;

  public AuthFilter() {
      //super("/user/login");
      super("/j_ehrserver_security_check");
  }
  
  // http://www.oodlestechnologies.com/blogs/Adding-Custom-Spring-Security-Authentication
  void afterPropertiesSet() {
     assert authenticationManager != null, 'authenticationManager must be specified'
     //assert rememberMeServices != null, 'rememberMeServices must be specified'
  }
    
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException
  {
      if(this.postOnly && !request.getMethod().equals("POST"))
      {
          throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
      }
      else
      {
          String username = this.obtainUsername(request);
          String password = this.obtainPassword(request);
          String organization = this.obtainOrganisation(request);

          //regular implementation in spring security plugin   
        
          //UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
          //this.setDetails(request, authRequest);
          //return         this.getAuthenticationManager().authenticate(authRequest);
          
          // That calls this provider to authenticate the token:
          // https://github.com/spring-projects/spring-security/blob/master/core/src/main/java/org/springframework/security/authentication/dao/AbstractUserDetailsAuthenticationProvider.java
          
          // authenticate esta aca
          // https://github.com/spring-projects/spring-security/blob/master/core/src/main/java/org/springframework/security/authentication/ProviderManager.java
        
         
         
          // I might need to add the User.getAuthorities /roles/ to the token.
          // UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
          //  "User", "Password", AuthorityUtils.createAuthorityList("ROLE_USER")
          // );
          // org.springframework.security.core.authority.AuthorityUtils
          // createAuthorityList deberia recibir los roles del user como un array.
          // user.getAuthorities() Set<Role>
          // tengo que poner Role.authority en un array y pasarselo al constructor de UserPassOrgAuthToken como 4to argumento.
          
          // FIXME: this is not encoding!!!!
          
          Object springSecurityService = new grails.plugin.springsecurity.SpringSecurityService();
          String encodedPassword = springSecurityService.passwordEncoder ? springSecurityService.encodePassword(password) : password

          println password
          println encodedPassword
          
          UserPassOrgAuthToken auth = new UserPassOrgAuthToken(username, encodedPassword, organization);
          
          // If authentication fails, always throws an AuthenticationException.
        
          return this.getAuthenticationManager().authenticate(auth)
      }
      //Your custom implementation goes here(Authenticate on the basis of organisation as well). Here you need to customise authenticate as per your requirement so that it checks for organisation as well.
  }

  protected String obtainOrganisation(HttpServletRequest request) {
      return request.getParameter(this.organisationParameter);
  }    

  protected String obtainPassword(HttpServletRequest request) {
      return request.getParameter(this.passwordParameter);
  }

  protected String obtainUsername(HttpServletRequest request) {
      return request.getParameter(this.usernameParameter);
  }

  protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
      authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
  }

  public void setUsernameParameter(String usernameParameter) {
      Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
      this.usernameParameter = usernameParameter;
  }

  public void setPasswordParameter(String passwordParameter) {
      Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
      this.passwordParameter = passwordParameter;
  }

  public void setPostOnly(boolean postOnly) {
      this.postOnly = postOnly;
  }

  public final String getUsernameParameter() {
      return this.usernameParameter;
  }

  public final String getPasswordParameter() {
      return this.passwordParameter;
  }
  
  public final String getOrganizationParameter() {
      return this.organisationParameter;
  }
}