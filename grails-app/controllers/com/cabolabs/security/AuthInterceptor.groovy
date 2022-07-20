package com.cabolabs.security

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

class AuthInterceptor {

   int order = HIGHEST_PRECEDENCE + 100

   def authService

   public AuthInterceptor()
   {
      matchAll()
         //.excludes(controller:'auth')
         .excludes(controller:'restAuth')

   }

   // FIXME: copied from stateless plugin, should refactor to a common src class with static method

   // Checks if an action was annotated with SecuredStateless
   private boolean isSecuredStateless(String controllerName, String actionName, grailsApplication)
   {
      // when accessing to root I don't get the name of the controller if an urlmapping is not defined!
      if (!controllerName) return false

      def controller = grailsApplication.controllerClasses.find{controllerName.toLowerCase() == it.name.toLowerCase()} //WordUtils.uncapitalize(it.name)}
      if (controller) {
         def clazz = controller.clazz
         if (clazz.isAnnotationPresent(SecuredStateless)) {
            return true
         }
         if (!actionName) {
            actionName = controller.defaultAction
         }
         def method = clazz.methods.find{actionName == it.name}
         if (method) {
            return method.isAnnotationPresent(SecuredStateless)
         }
      }
      return false
   }

   private boolean isPublic(RequestMap rm)
   {
      if (rm && rm.configAttribute == 'OPEN_ACCESS')
      {
         log.info "open access to url "+ rm.url
         return true
      }
      return false
   }

   private boolean isLoggedIn()
   {
      def sessman = SessionManager.instance
      return sessman.hasSession(session.id.toString())
   }

   boolean before() {

      //println "AUTH INTERCEPTOR"

      // if SecuredStateless, the session check should not apply
      if (isSecuredStateless(controllerName, actionName, grailsApplication))
      {
         // Make session available in the session if comes from REST
         // TODO: with this we can change all request.securityStatelessMap.extradata.org_uid for sesison.organization.uid

         // ISSUE: if the stateless interceptor is not executed before this one, the request.securityStatelessMap is not set
         session.organization = Organization.findByUid(request.securityStatelessMap.extradata.org_uid)

         return true
      }

      //log.info "authInterceptor: c: ${controllerName}, a: ${actionName}"


      // Check access to current section by user role
      def path = request.requestURI

      /*
      request.attributeNames.each { println it +" "+ request.getAttribute(it) }
      */


      // TODO: dont query RequestMaps move to an in-memory singleton
      // TODO: check request method
      def rms = RequestMap.list() //findByUrl(path)
      def rm = rms.find {
         path.matches(it.url) // current path matches reges in RequestMap.url?
      }

      
      if (!rm)
      {
         log.info "${path} doesn't match any RequestMap URL"
         //println rms.url
         render view: "/noPermissions.gsp"
         return false // all URLs are closed by default!
      }
      

      log.info "${path} matches ${rm.url}"


      // FIXME: check the action is open in the RequestMap, similar to the checks in the toolkit
      // TODO: do the fix in atomik also
      
      if (!isPublic(rm))
      {
         if (!isLoggedIn())
         {
            //log.info "not public and not logged in, redirect to auth"
            flash.message = "Your session, please login."
            redirect controller: 'auth', action: 'login'
            return false
         }
         
         // verify role
         if (!authService.loggedInUserHasAnyRole(rm.configAttribute))
         {
            render view: "/noPermissions.gsp"
            return false // all URLs are closed by default!
         }

         /*
         if (onlyAdmin(controllerName, actionName) && authService.loggedInRole() != 'admin')
         {
            // I don't want to tell the user that section exists but he don't have access
            flash.message = "There was a problem with your request"
            redirect controller: 'app', action: 'dashboard'
            return false
         }
         */
      }
      else // public
      {
         if (isLoggedIn()) // if it's logged in, go to dashboard
         {
            redirect controller: 'app', action: 'index'
            return false
         }
         else
         {
            log.info "public, not logged in"
         }
      }

      true
   }

   boolean after() { true }

   void afterView() {
     // no-op
   }
}
