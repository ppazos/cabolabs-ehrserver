package com.cabolabs.ehrserver.log

import com.cabolabs.ehrserver.openehr.common.change_control.Commit
import grails.transaction.Transactional
import grails.util.Holders
import javax.servlet.http.HttpServletRequest

@Transactional
class CommitLoggerService {
   
   def config = Holders.config.app

   /**
    * If the XML was read from the request, we won't be able to read it again,
    * reading twice from the request will result on a java.io.IOException "stream closed"
    */
   def log(HttpServletRequest request, String contributionUid, boolean success, String readXML)
   {
      // http://docs.oracle.com/javaee/1.4/api/javax/servlet/http/HttpServletRequest.html
      def clientIP = request.remoteAddr
      def clientLocale = request.locale
      def isSecure = request.isSecure() // it uses https?
      def params = request.parameterMap.collectEntries{ [(it.key): it.value[0]] } // params is a map of lists, we need a map of strings
      def contentType = request.contentType
      def contentLength = request.contentLength
      def encoding = request.characterEncoding
      def cookies = request.cookies
      def url = request.requestURL

      // Stateless username
      def authUser = request.securityStatelessMap.username
         
      
      // FIXME: file can be read once from the request ...
      def versionsXML
      
      try
      {
         switch (contentType)
         {
            //case 'application/x-www-form-urlencoded':
            //break
            case 'multipart/form-data':
               def f = request.getFile('versions')
               versionsXML = f?.text
            break
            case ['application/xml', 'text/xml']:
               versionsXML = request.reader?.text
            break
            default:
               println 'contentType '+ contentType +' not supported'
         }
      }
      catch(java.io.IOException e) // file already read by the controller
      {
         versionsXML = readXML // can be null or empty
      }
      
      // TODO: log specific errors thrown by the controller
      
      println params.ehrUid
      println contributionUid
      println clientIP
      println clientLocale
      println params
      println contentType
      println contentLength
      println url
      println authUser
      
      // FIXME: empty XML is a possible error and it should be logged!
      if (versionsXML)
      {
         def commit = new Commit(
           ehrUid: params.ehrUid,
           contributionUid: contributionUid, // can be null if !success
           ip: clientIP,
           locale: clientLocale,
           params: params,
           contentType: contentType,
           contentLength: contentLength,
           url: url,
           username: authUser,
           success: success
         )
         
         commit.save(failOnError: true)
         
         // save the XML to the commit log
         def commitLog = new File(config.commit_logs + commit.id.toString() +'.xml')
         commitLog << versionsXML
      }
   }
}
