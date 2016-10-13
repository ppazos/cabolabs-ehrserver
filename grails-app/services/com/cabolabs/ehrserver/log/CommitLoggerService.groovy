package com.cabolabs.ehrserver.log

import com.cabolabs.ehrserver.openehr.common.change_control.Commit
import grails.transaction.Transactional
import grails.util.Holders
import javax.servlet.http.HttpServletRequest

@Transactional
class CommitLoggerService {
   
   def config = Holders.config.app

   /**
    * If the content (xml or json) was read from the request, we won't be able to read it again,
    * reading twice from the request will result on a java.io.IOException "stream closed"
    */
   def log(HttpServletRequest request, String contributionUid, boolean success, String content)
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
      // I think this can be changed to an if (content) .. else try to read.
      def logContent
      
      try
      {
         // this is used when there is no content read from the request.reader yet
         // if the content (xml or json commit) was read, it should come in the content param
         switch (contentType)
         {
            // urlencoded not supported
            //case 'application/x-www-form-urlencoded':
            //break
            case 'multipart/form-data': // TODO: check if it us JSON or XML (we dont really know)
               def f = request.getFile('versions')
               logContent = f?.text
            break
            case ['application/xml', 'text/xml']:
               logContent = request.reader?.text
            break
            // TODO: add logger for json commits
            case 'application/json':
               logContent = request.reader?.text
            break
            default:
               println 'commit logger contentType '+ contentType +' not supported'
         }
      }
      catch(java.io.IOException e) // file already read by the controller
      {
         logContent = content // can be null or empty
      }
      
      // TODO: log specific errors thrown by the controller
      
      println "commmit log"
      println params.ehrUid
      println contributionUid
      println clientIP
      println clientLocale
      println params
      println contentType
      println contentLength
      println url
      println authUser
      
      // empty XML is a possible error so the commit should be saved to the
      // database but no xml file will be created
      
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
      
      if (logContent)
      {
         // save the json or xml to the commit log
         def ext = '.xml'
         if (contentType == 'application/json') ext = '.json'
         
         def commitLog = new File(config.commit_logs + commit.id.toString() + ext)
         commitLog << logContent
      }
   }
}
