// Place your Spring DSL code here
//import com.cabolabs.security.AbstractAuthenticationProcessingFilter
import com.cabolabs.security.AuthFilter
import com.cabolabs.security.AuthProvider
beans = {

   authProvider(AuthProvider) {
      passwordEncoder = ref("passwordEncoder") // from plugin
   }

   authFilter(AuthFilter) {
      // properties
      // http://www.oodlestechnologies.com/blogs/Adding-Custom-Spring-Security-Authentication
      authProvider = ref("authProvider")
      
      
      // AbstractAuthenticationProcessingFilter copied from https://github.com/grails-plugins/grails-spring-security-core/blob/ced4c539dbd0c6ea2cb2ac06349c952accbd8142/src/main/groovy/grails/plugin/springsecurity/SpringSecurityCoreGrailsPlugin.groovy
      // see authenticationProcessingFilter bean.
      
      // Calls methods from https://github.com/spring-projects/spring-security/blob/master/web/src/main/java/org/springframework/security/web/authentication/AbstractAuthenticationProcessingFilter.java
      
      // calls AbstractAuthenticationProcessingFilter.setAuthenticationManager
      authenticationManager = ref("authenticationManager")
      sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
      
      // calls AbstractAuthenticationProcessingFilter.setAuthenticationSuccessHandler
      authenticationSuccessHandler = ref('authenticationSuccessHandler')
      
      // calls AbstractAuthenticationProcessingFilter.setAuthenticationFailureHandler
      authenticationFailureHandler = ref('authenticationFailureHandler')
      
      // calls AbstractAuthenticationProcessingFilter.setRememberMeServices
      rememberMeServices = ref("rememberMeServices")
      
      // calls AbstractAuthenticationProcessingFilter.setRequiresAuthenticationRequestMatcher
      requiresAuthenticationRequestMatcher = ref('filterProcessUrlRequestMatcher')
   }
}
