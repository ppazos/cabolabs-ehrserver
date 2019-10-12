package com.cabolabs.ehrserver.reporting

import com.cabolabs.security.*

class ActivityLogInterceptor {

   int order = HIGHEST_PRECEDENCE + 200

   public ActivityLogInterceptor()
   {
      matchAll()
   }

   def avoidLog(controller, action)
   {
      def avoid_activity_log = [
        [controller:'activityLog', action:'*'], // avoid loading activity logs of loading activity logs :()
        [controller:'notification', action:'newNotifications'], // avoid checking for notifications via AJAX
        [controller:'notification', action:'dismiss'], // avoid logging the dismiss of a notification in the web console
        [controller:'rest',         action:'echo']
      ]

      def matching_controller_avoids = avoid_activity_log.findAll { it.controller == controller } // can be a list of rules
      def avoid = false
      matching_controller_avoids.each { rule ->
         if (rule.action == '*' || rule.action == action)
         {
            avoid = true
            return true
         }
      }

      return avoid
   }

   def doLog(when, request, response, session)
   {
      def username

      // check web console sessiondef sessman = SessionManager.instance
      def sessman = SessionManager.instance
      def sess = sessman.getSession(session.id.toString())
      if (sess) username = sess.userId
      else if (request.securityStatelessMap)
      {
         username = request.securityStatelessMap.username
      }
      else
      {
         // open actions that submit a username (email)
         if (['auth', 'forgotPassword', 'resetPasswordRequest'].contains(actionName))
            username = params.email
      }
      // Still username could be null if not logged in

      // println "activity log interceptor"
      // println session

      // only long ids are valid, sometimes we use uids in urls
      def the_id = params.id
      if (params.id instanceof String) the_id = null

      def alog = new ActivityLog(
                  username:        username, // can be null
                  organizationUid: session.organization?.uid,
                  action:          '('+ when +')'+controllerName+':'+actionName,
                  objectId:        the_id, // can be null
                  objectUid:       params.uid, // can be null
                  remoteAddr:      request.remoteAddr,
                  clientIp:        request.getHeader("Client-IP"), // can be null
                  xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
                  referer:         request.getHeader('referer'), // can be null
                  requestURL:      request.requestURL,
                  matchedURI:      request.forwardURI,
                  sessionId:       session.id.toString())


      // TODO: file log failure
      if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()
   }

   boolean before()
   {
      if (avoidLog(controllerName, actionName)) return true

      doLog('before', request, response, session)

      true
   }

   boolean after()
   {
      if (avoidLog(controllerName, actionName)) return true

      doLog('after', request, response, session)

      true
   }

   void afterView() {
      // no-op
   }
}
