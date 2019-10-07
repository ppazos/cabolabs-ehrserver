package com.cabolabs.ehrserver.reporting

import javax.servlet.http.HttpServletRequest
import grails.transaction.Transactional
import grails.util.Holders

@Transactional
class LogService {

   def config = Holders.config.app
   def authService

   def log(HttpServletRequest request, session, params)
   {
      // Get username and org_uid
      def username, organizationUid
      def weblogin = authService.loggedInUser
      if (weblogin)
      {
         username = weblogin.email
         organizationUid = session.organization.uid
      }
      else if (request.securityStatelessMap)
      {
         username = request.securityStatelessMap.username
         organizationUid = request.securityStatelessMap.extradata.org_uid
      }

      def alog = new ActivityLog(
         username:        username, // can be null
         organizationUid: organizationUid,
         action:          params.controller +':'+params.action,
         objectId:        params.id, // can be null
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

   def error(HttpServletRequest request, session, params, Exception e)
   {
      // Get username and org_uid
      def username, organizationUid
      def weblogin = authService.loggedInUser
      if (weblogin)
      {
         username = weblogin.email
         organizationUid = session.organization.uid
      }
      else if (request.securityStatelessMap)
      {
         username = request.securityStatelessMap.username
         organizationUid = request.securityStatelessMap.extradata.org_uid
      }

      def alog = new ErrorLog(
         username:        username, // can be null
         organizationUid: organizationUid,
         action:          params.controller +':'+params.action,
         objectId:        params.id, // can be null
         objectUid:       params.uid, // can be null
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURL:      request.requestURL,
         matchedURI:      request.forwardURI,
         sessionId:       session.id.toString(),

         message:         e.message.take(255),
         trace:           e.traceString(10).take(4095)
      )

      // TODO: file log failure
      if (!alog.save()) println "error log is not saving "+ alog.errors.toString()
   }
}
