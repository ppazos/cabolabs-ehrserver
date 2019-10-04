package com.cabolabs.ehrserver

import com.cabolabs.security.*
import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

class AuthInterceptor {

   int order = HIGHEST_PRECEDENCE + 100

   public AuthInterceptor()
   {
      matchAll().excludes(controller:'auth') // excludes the open actions (not login auth and not stateless secured)
      /*.excludes(controller:'rest')

                .excludes(controller:'restAuth')*/

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

   boolean before() {

      println "AUTH INTERCEPTOR"

      // if SecuredStateless, the session check should not apply
      if (isSecuredStateless(controllerName, actionName, grailsApplication))
      {
         // Make session available in the session if comes from REST
         // TODO: with this we can change all request.securityStatelessMap.extradata.org_uid for sesison.organization.uid
         session.organization = Organization.findByUid(request.securityStatelessMap.extradata.org_uid)

         return true
      }

      println "AuthInterceptor: c: ${controllerName}, a: ${actionName}"

      // Not logged in? Go to the login page
      def sessman = SessionManager.instance
      if (!sessman.hasSession(session.id.toString()))
      {
         println "redirects to auth"
         redirect controller: 'auth', action: 'login'
         return false
      }

      true
   }

   boolean after() { true }

   void afterView() {
     // no-op
   }
}
