
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
//      rememberMeServices = ref("rememberMeServices")
      
      // calls AbstractAuthenticationProcessingFilter.setRequiresAuthenticationRequestMatcher
      requiresAuthenticationRequestMatcher = ref('filterProcessUrlRequestMatcher')
      
      continueChainBeforeSuccessfulAuthentication = false // test
   }
}
