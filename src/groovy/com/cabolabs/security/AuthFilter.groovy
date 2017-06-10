
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

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.util.Assert
//import org.springframework.security.authentication.AuthenticationManager
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.core.context.SecurityContextHolder
import com.cabolabs.security.UserPassOrgAuthToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.util.matcher.RequestMatcher

/**
Alternative to http://docs.spring.io/autorepo/docs/spring-security/3.2.3.RELEASE/apidocs/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html
that considers username, password and organization number for the login.
ref: http://stackoverflow.com/questions/32621369/customize-login-in-grails-spring-security-plugin
ref: http://www.oodlestechnologies.com/blogs/Adding-Custom-Spring-Security-Authentication
*/
public class AuthFilter extends AbstractAuthenticationProcessingFilter implements ApplicationEventPublisherAware {
 
  AuthProvider authProvider
  
  // redeclared because is private on superclass
  //AuthenticationManager authenticationManager
  //RememberMeServices rememberMeServices 
  //AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler()
  //AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler()
  //RequestMatcher requiresAuthenticationRequestMatcher
  
  
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
      
     String username = this.obtainUsername(request)
     String password = this.obtainPassword(request)
     String organization = this.obtainOrganisation(request)

     UserPassOrgAuthToken auth = new UserPassOrgAuthToken(username, password, organization)
     
     // from https://github.com/spring-projects/spring-security/blob/7b4a37f27e4ba7045bd63656e49ee0d5ee381ce5/web/src/main/java/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.java
     // Allow subclasses to set the "details" property
     //setDetails(request, auth) // dice que no existe el metodo, porque es del UsernamePasswordAuthenticationFilter
     // ese llama a authtoken.setDetails(authenticationDetailsSource.buildDetails(request)); en https://github.com/spring-projects/spring-security/blob/master/core/src/main/java/org/springframework/security/authentication/UsernamePasswordAuthenticationToken.java
     // que en realidad es heredado de https://github.com/spring-projects/spring-security/blob/master/core/src/main/java/org/springframework/security/authentication/AbstractAuthenticationToken.java
     
     // If authentication fails, always throws an AuthenticationException.
     
     // result is UserPassOrgAuthToken, and it's attr principal is GrailsUser
     // GrailsUser extends User and has an attr id with the id of the user
     // https://github.com/grails-plugins/grails-spring-security-core/blob/master/src/main/groovy/grails/plugin/springsecurity/userdetails/GrailsUser.groovy
     auth = this.getAuthenticationManager().authenticate(auth) // can throw AuthenticationException if auth fails
     
     // cant make queries without the transaction because there is no hibernate session
     User.withTransaction { status ->
        def user = User.get(auth.principal.id)
        if (user.getAuthorities(Organization.findByNumber(organization)).contains( new Role('ROLE_USER') ))
        {
           //println "valid user but cant login into the web console"
           throw new InsufficientAuthenticationException("Your role is not allowed to login through the web console.")
        }
     }
     
     // http://www.oodlestechnologies.com/blogs/Adding-Custom-Spring-Security-Authentication
     SecurityContextHolder.getContext().setAuthentication(auth)
//     rememberMeServices.onLoginSuccess(request, response, auth)
     
     // TODO: catch auth exception and handle the lines below, then rethrow the except
     // SecurityContextHolder.clearContext();
     // rememberMeServices.loginFail(request, response)
     //     
     return auth
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
  
  public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher)
  {
     this.eventPublisher = eventPublisher
  }
}
