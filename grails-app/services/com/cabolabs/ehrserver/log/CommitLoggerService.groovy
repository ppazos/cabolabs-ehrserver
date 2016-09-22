package com.cabolabs.ehrserver.log

import grails.transaction.Transactional
import grails.util.Holders
import javax.servlet.http.HttpServletRequest

@Transactional
class CommitLoggerService {
   
   def config = Holders.config.app

   def log(HttpServletRequest request)
   {
      // http://docs.oracle.com/javaee/1.4/api/javax/servlet/http/HttpServletRequest.html
      def clientIP = request.remoteAddr
      def clientLocale = request.locale
      def isSecure = request.isSecure() // it uses https?
      def params = request.parameterMap
      def contentType = request.contentType
      def contentLength = request.contentLength
      def encoding = request.characterEncoding
      def cookies = request.cookies
      def url = request.requestURL
      def authUser = request.userPrincipal
      
      if (contentType == 'application/x-www-form-urlencoded')
      {
         
      }
      if (contentType == 'multipart/form-data')
      {
         // request.getFile('paramname')
      }
      /* application/xml
       * def versionsXML = request.reader?.text
       * 
       */
   }
}
