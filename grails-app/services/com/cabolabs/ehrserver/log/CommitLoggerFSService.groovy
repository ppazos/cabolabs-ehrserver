/*
 * Copyright 2011-2020 CaboLabs Health Informatics
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

package com.cabolabs.ehrserver.log

import com.cabolabs.ehrserver.openehr.common.change_control.CommitLog
import grails.transaction.Transactional
import grails.util.Holders
import javax.servlet.http.HttpServletRequest

@Transactional
class CommitLoggerFSService {

   def config = Holders.config.app

   /*
    * Operations for the whole version repo.
    */
   private boolean canWriteRepo()
   {
      return new File(config.commit_logs).canWrite()
   }

   private boolean repoExists()
   {
      return new File(config.commit_logs).exists()
   }

   /*
    * Operations for the version repo per organization.
    */
   private boolean canWriteRepoOrg(String orguid)
   {
      return new File(config.commit_logs.withTrailSeparator() + orguid).canWrite()
   }

   private boolean repoExistsOrg(String orguid)
   {
      return new File(config.commit_logs.withTrailSeparator() + orguid).exists()
   }

   String getCommitContents(CommitLog commit)
   {
      return new File(commit.fileLocation).text
   }

   /**
    * If the content (xml or json) was read from the request, we won't be able to read it again,
    * reading twice from the request will result on a java.io.IOException "stream closed"
    */
   def log(HttpServletRequest request, String contributionUid, boolean success, String content, session, params)
   {
      // http://docs.oracle.com/javaee/1.4/api/javax/servlet/http/HttpServletRequest.html
      def clientLocale = request.locale
      def isSecure = request.isSecure() // it uses https?
      //def params = request.parameterMap.collectEntries{ [(it.key): it.value[0]] } // params is a map of lists, we need a map of strings
      def contentType = request.contentType
      def contentLength = request.contentLength
      def encoding = request.characterEncoding
      def cookies = request.cookies

      // Stateless username
      def authUser = request.securityStatelessMap.username


      // FIXME: file can be read once from the request ...
      // I think this can be changed to an if (content) .. else try to read.
      def logContent

      // contentType can include charset, and here we need to check only the MIME type
      // text/xml; charset=UTF-8
      def requestContentTypeOnly = contentType.split(';')[0]

      try
      {
         // this is used when there is no content read from the request.reader yet
         // if the content (xml or json commit) was read, it should come in the content param
         switch (requestContentTypeOnly)
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
      /*
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
      */

      // empty XML is a possible error so the commit should be saved to the
      // database but no xml file will be created

      def commit = new CommitLog(
         ehrUid:          params.ehrUid,
         locale:          clientLocale,
         params:          params,
         contentType:     contentType,
         contentLength:   contentLength,
         success:         success,
         username:        authUser,
         action:          params.controller +':'+params.action,
         objectClazz:     'Contribution',
         objectUid:       contributionUid, // can be null if !success
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURL:      request.requestURL,
         matchedURI:      request.forwardURI,
         sessionId:       session.id.toString()
      )

      if (!commit.validate()) println commit.errors

      commit.save(failOnError: true)

      if (logContent)
      {
         String orguid = request.securityStatelessMap.extradata.org_uid

         // save the json or xml to the commit log
         def ext = '.xml'
         if (contentType == 'application/json') ext = '.json'

         // saves the reference to the log file in the DB
         commit.fileLocation = newCommitFileLocation(orguid, contributionUid, ext)

         // ==================================================
         // creates parent subfolders if dont exist
         def containerFolder = new File(new File(commit.fileLocation).getParent())
         containerFolder.mkdirs()

         def commitLog = new File(commit.fileLocation)
         commitLog << logContent
      }

      // save log to DB
      if (!commit.validate()) println commit.errors
      commit.save(failOnError: true)
   }

   static String newCommitFileLocation(String orguid, String contributionUid, String ext)
   {
      if (!contributionUid) contributionUid = java.util.UUID.randomUUID()

      // TODO: this is XML only, for JSON versions we need to consider the content type of the commit adding a new parameter
      return Holders.config.app.commit_logs.withTrailSeparator() + orguid.withTrailSeparator() + contributionUid + ext
   }
}
