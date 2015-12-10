package com.cabolabs.security

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.springframework.security.core.context.SecurityContextHolder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService

import org.springframework.security.core.userdetails.UserDetails

import grails.plugin.springsecurity.rest.RestAuthenticationFilter
import com.google.common.io.CharStreams
import groovy.json.JsonSlurper
import com.cabolabs.security.UserPassOrgAuthToken

/**
 * Custom implementation of the default RestAuthenticationFilter to use
 * the UserPassOrgToken to authenticate users.
 * @author pab
 *
 */
class RestAuthFilter extends RestAuthenticationFilter {
   
   private UserPassOrgAuthToken extractCredentialsFromJsonPayload(HttpServletRequest httpServletRequest)
   {
      String body = CharStreams.toString(httpServletRequest.reader)
      JsonSlurper slurper = new JsonSlurper()
      def jsonBody = slurper.parseText(body)
      if (jsonBody)
      {
         String username = jsonBody.username
         String password = jsonBody.password
         String orgnumber = jsonBody.organization

         log.debug "Extracted credentials from JSON payload. username: ${username}, password: ${password?.size()?'[PROTECTED]':'[MISSING]'}, organization: ${orgnumber},"

         return new UserPassOrgAuthToken(username, password, orgnumber)
      }
      else
      {
         log.debug "No JSON body sent in the request"
         return null
      }
   }
   
   @Override
   void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
       println "doFilter RestAuthFilter ***"
       
       HttpServletRequest httpServletRequest = request as HttpServletRequest
       HttpServletResponse httpServletResponse = response as HttpServletResponse

       def actualUri =  httpServletRequest.requestURI - httpServletRequest.contextPath

       logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

       //Only apply filter to the configured URL
       if (actualUri == endpointUrl)
       {
           log.debug "Applying authentication filter to this request"

           //Only POST is supported
           if (httpServletRequest.method != 'POST')
           {
               log.debug "${httpServletRequest.method} HTTP method is not supported. Setting status to ${HttpServletResponse.SC_METHOD_NOT_ALLOWED}"
               httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
               return
           }

           Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
           Authentication authenticationResult

           UserPassOrgAuthToken authenticationRequest = this.extractCredentialsFromJsonPayload(httpServletRequest)
           boolean authenticationRequestIsCorrect = (authenticationRequest?.principal && authenticationRequest?.credentials && authenticationRequest?.organization)
           
           if(authenticationRequestIsCorrect)
           {
               authenticationRequest.details = authenticationDetailsSource.buildDetails(httpServletRequest)
               
               try
               {
                   log.debug "Trying to authenticate the request"
                   authenticationResult = authenticationManager.authenticate(authenticationRequest)
             
                   if (authenticationResult.authenticated)
                   {
                       log.debug "Request authenticated. Storing the authentication result in the security context"
                       log.debug "Authentication result: ${authenticationResult}"
   
                       SecurityContextHolder.context.setAuthentication(authenticationResult)
                   }
               }
               catch (AuthenticationException ae)
               {
                   log.debug "Authentication failed: ${ae.message}"
                   authenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, ae)
               }
           }
           else
           {
               log.debug "Some credentials are missing, username, password and organization are required"
               if(!authentication)
               {
                   log.debug "Setting status to ${HttpServletResponse.SC_BAD_REQUEST}"
                   httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST)
                   return
               }
               else
               {
                   log.debug "Using authentication already in security context."
                   authenticationResult = authentication
               }
           }

           if (authenticationResult?.authenticated)
           {
               AccessToken accessToken = tokenGenerator.generateAccessToken(authenticationResult.principal as UserDetails)
               log.debug "Generated token: ${accessToken}"

               tokenStorageService.storeToken(accessToken.accessToken, authenticationResult.principal as UserDetails)

               authenticationEventPublisher.publishTokenCreation(accessToken)

               authenticationSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, accessToken)
           }
           else
           {
               log.debug "Not authenticated. Rest authentication token not generated."
           }
       }
       else // continue with next filter in chain
       {
           chain.doFilter(request, response)
       }
   }
}
