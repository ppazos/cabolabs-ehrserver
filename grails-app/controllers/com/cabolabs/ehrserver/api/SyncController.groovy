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
package com.cabolabs.ehrserver.api

import com.cabolabs.ehrserver.sync.*
import grails.converters.*

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless
import org.codehaus.groovy.grails.web.json.JSONObject
import groovy.json.JsonSlurper

import com.cabolabs.ehrserver.reporting.ActivityLog
import com.cabolabs.ehrserver.ResourceService
import com.cabolabs.security.*
import com.cabolabs.ehrserver.account.ApiKey

/**
 * Controller that receives the sync operations
 */
class SyncController {

   static allowedMethods = [syncAccount: "POST",
                            syncEhr: "POST",
                            syncOpt: "POST",
                            syncContribution: "POST",
                            syncQuery: "POST"
                           ]

   def syncParserService
   def resourceService
   def statelessTokenProvider

   /**
    Web Console action to list sync API Tokens.
    */
    // TODO: enable only for admins
   def index()
   {
      def keys = ApiKey.findAllByScope('sync')
      def remotes = SyncClusterConfig.list()

      println remotes
      [keys: keys, remotes: remotes]
   }

   /**
    Just shows the view.
    */
   def create()
   {

   }

   // TODO: refactor
   // TODO: enable only for admins
   // similar to OrganizationController.generateApiKey
   def save(String systemId)
   {
      if (!systemId)
      {
         flash.message = message(code: 'synccontroller.save.error.systemIdIsRequired')
         return
      }

      // This user has no organizations
      // TODO: check if that is posible
      def virtualUser = new User(username: 'apikey'+String.random(50),
                                 password: String.uuid(),
                                 email: String.random(50) + '@apikey.com',
                                 isVirtual: true,
                                 enabled: true)

      virtualUser.save(failOnError: true)

      // this user is associated with a key that has scope sync that is global
      // so it can't be associated with an org, and UserRole requires an org
      //UserRole.create(virtualUser, Role.findByAuthority(Role.US), org, true)

      // https://github.com/kaleidos/grails-security-stateless/blob/master/src/groovy/net/kaleidos/grails/plugin/security/stateless/token/JwtStatelessTokenProvider.groovy#L30
      def key = new ApiKey(user: virtualUser,
                           systemId: systemId,
                           scope: 'sync',
                           token: statelessTokenProvider.generateToken(virtualUser.username, null, [scope: 'sync']))

      key.save(failOnError: true)

      redirect action:'index'
   }

   /**
    Just shows the view.
    */
   def createRemote()
   {

   }

   def saveRemote(String remoteAPIKey, String remoteServerName, int remoteServerPort,
                  String remoteServerPath, String remoteServerIP, boolean isActive)
   {
      println params
      //render "saveRemote TODO"

      /*
      [remoteAPIKey:sdfgsdfggsdgfsdf, remoteServerPort:5436,
      remoteServerName:sdfgs, remoteServerPath:/fhdfghd,
      remoteServerIP:345345, doit:Create, isActive:on, _isActive:,
      controller:sync, action:saveRemote]

      */
      /// TBD
      /*
      def sync1 = new SyncClusterConfig(
         remoteServerName: 'test mirth',
         remoteAPIKey: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFwaWtleWtwZXdoZW95dXppZW1vaWlkYXh3cHNpd21rc2lvYW9xcmxnbXBwcnZwdWxidGt4bXZnIiwiZXh0cmFkYXRhIjp7Im9yZ2FuaXphdGlvbiI6IjEyMzQ1NiIsIm9yZ191aWQiOiJlOWQxMzI5NC1iY2U3LTQ0ZTctOTYzNS04ZTkwNmRhMGM5MTQifSwiaXNzdWVkX2F0IjoiMjAxOC0wOS0wMVQwMzowMTo1Ny42NDRaIn0=.zBXBgRlKBuYWFgYsTv7P706Qf3h6IMTjgF2jwk/LQVo=',
         remoteServerIP: 'localhost',
         remoteServerPort: 4455,
         remoteServerPath: '/',
         isActive: true
      )
      sync1.save()
      */

      def sync = new SyncClusterConfig(params)
      if (!sync.save())
      {
         println "errors: "+ sync.errors.allErrors
      }

      flash.message = "Remote created"
      redirect action: 'index'
   }

   def editRemote(Long id)
   {
      def remote = SyncClusterConfig.get(id)
      if (!remote)
      {
         flash.message = "Remote doesn't exists"
         redirect action: 'index'
         return
      }

      render view: 'editRemote', model: [remote: remote]
   }

   def updateRemote(Long id)
   {
      def remote = SyncClusterConfig.get(id)
      if (!remote)
      {
         flash.message = "Remote doesn't exists"
         redirect action: 'index'
         return
      }
      remote.properties = params
      if (!remote.save())
      {
         println "errors: "+ remote.errors.allErrors
      }

      flash.message = "Remote updated"
      redirect action: 'index'
   }

   @SecuredStateless
   def syncAccount()
   {
      println "syncAccount " //+ request.securityStatelessMap.extradata

      // TODO: check if already exists
      // TOOD: check if this is an update, that should be a PUT, OK result should be 200
      //println request.JSON
      // TODO: transaction!

      // account with contact user and list of organizations
      def json = request.JSON.account
      def account = syncParserService.fromJSONAccount(json)

      if (!account) println "NULL!!!!"

      if (!account.save(flush:true))
      {
         // TODO: handle error
         println account.errors.allErrors

         render(status:400, text: account.errors.allErrors as JSON, contentType:"application/json", encoding:"UTF-8")
         return
      }

      def plan_associations = syncParserService.fromJSONPlanAssociations(json.plans, account)

      plan_associations.each { plan_assoc ->
         // plan is not saved in cascade by plan assoc
         if (!plan_assoc.plan.save())
         {
            // TODO: handle errors
            println plan_assoc.plan.errors.allErrors
         }
         if (!plan_assoc.save())
         {
            // TODO: handle errors
            println plan_assoc.errors.allErrors
         }
      }

      def alog = new ActivityLog(
         username:        request.securityStatelessMap.username, // can be null
         organizationUid: null, /* sync is for the system not for a specific org */
         action:          controllerName+':'+actionName,
         objectId:        account.id,
         objectUid:       account.uid,
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURI:      request.forwardURI,
         matchedURI:      request.requestURI,
         sessionId:       session.id)


      // TODO: file log failure
      if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()


      // TODO: structure for the response
      render( status:201, text:[message: 'account synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }

   @SecuredStateless
   def syncEhr()
   {
      println "syncEhr"

      def ehr = syncParserService.fromJSONEhr(request.JSON.ehr)

      if (!ehr.save(flush:true))
      {
         // TODO: handle error
         println ehr.errors.allErrors
      }

      def alog = new ActivityLog(
         username:        request.securityStatelessMap.username, // can be null
         organizationUid: null, /* sync is for the system not for a specific org */
         action:          controllerName+':'+actionName,
         objectId:        ehr.id,
         objectUid:       ehr.uid,
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURI:      request.forwardURI,
         matchedURI:      request.requestURI,
         sessionId:       session.id)


      // TODO: file log failure
      if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()

      // TODO: structure for the response
      render( status:201, text:[message: 'ehr synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }

   @SecuredStateless
   def syncOpt()
   {
      println "syncOpt"

      def optIndex = syncParserService.toJSONOpt(request.JSON.template)

      if (!optIndex.save(flush:true))
      {
         // TODO: handle error
         println optIndex.errors.allErrors
      }

      def alog = new ActivityLog(
         username:        request.securityStatelessMap.username, // can be null
         organizationUid: null, /* sync is for the system not for a specific org */
         action:          controllerName+':'+actionName,
         objectId:        optIndex.id,
         objectUid:       optIndex.uid,
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURI:      request.forwardURI,
         matchedURI:      request.requestURI,
         sessionId:       session.id)


      // TODO: file log failure
      if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()

      // TODO: structure for the response
      render( status:201, text:[message: 'opt synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }

   @SecuredStateless
   def syncContribution()
   {
      println "syncContribution"

      // TODO: should catch validation errores and retrieve them to the client
      //println request.JSON // changes the order of the objects!!!!
      //def jo = new JSONObject(request.reader.text) // the issue is the JSONObject used by grails it is unordered!
      //println jo

      LinkedHashMap json = new JsonSlurper().parseText(request.reader.text)
      def jo = new JSONObject(json)
      println jo
      println "-----------"


      def contribution = syncParserService.fromJSONContribution(jo)

      if (!contribution.save(flush:true))
      {
         // TODO: handle error
         println contribution.errors.allErrors
      }

      def alog = new ActivityLog(
         username:        request.securityStatelessMap.username, // can be null
         organizationUid: null, /* sync is for the system not for a specific org */
         action:          controllerName+':'+actionName,
         objectId:        contribution.id,
         objectUid:       contribution.uid,
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURI:      request.forwardURI,
         matchedURI:      request.requestURI,
         sessionId:       session.id)


      // TODO: file log failure
      if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()

      // TODO: structure for the response
      render( status:201, text:[message: 'contribution synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }

   @SecuredStateless
   def syncQuery()
   {
      LinkedHashMap json = new JsonSlurper().parseText(request.reader.text)
      def jo = new JSONObject(json)
      println jo
      println "-----------"

      def query = syncParserService.fromJSONQuery(jo)

      // group is not saved in cascade so needs to be saved here
      if (query.queryGroup)
      {
         if (!query.queryGroup.save())
         {
            println query.queryGroup.errors.allErrors
         }
      }
      if (!query.save(flush:true))
      {
         // TODO: handle error
         println query.errors.allErrors
      }
      else
      {
         if (!query.isPublic)
         {
            resourceService.shareQuery(query, Organization.findByUid(query.organizationUid))
         }
      }

      def alog = new ActivityLog(
         username:        request.securityStatelessMap.username, // can be null
         organizationUid: null, /* sync is for the system not for a specific org */
         action:          controllerName+':'+actionName,
         objectId:        query.id,
         objectUid:       query.uid,
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURI:      request.forwardURI,
         matchedURI:      request.requestURI,
         sessionId:       session.id)


      // TODO: file log failure
      if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()

      // TODO: structure for the response
      render( status:201, text:[message: 'query synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }


   @SecuredStateless
   def syncEhrQuery()
   {
      LinkedHashMap json = new JsonSlurper().parseText(request.reader.text)
      def jo = new JSONObject(json)
      println jo
      println "-----------"

      def equery = syncParserService.toJSONEhrQuery(jo)

      if (!equery.save(flush:true))
      {
         // TODO: handle error
         println equery.errors.allErrors
      }

      def alog = new ActivityLog(
         username:        request.securityStatelessMap.username, // can be null
         organizationUid: null, /* sync is for the system not for a specific org */
         action:          controllerName+':'+actionName,
         objectId:        equery.id,
         objectUid:       equery.uid,
         remoteAddr:      request.remoteAddr,
         clientIp:        request.getHeader("Client-IP"), // can be null
         xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
         referer:         request.getHeader('referer'), // can be null
         requestURI:      request.forwardURI,
         matchedURI:      request.requestURI,
         sessionId:       session.id)


      // TODO: file log failure
      if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()

      // TODO: structure for the response
      render( status:201, text:[message: 'ehr query synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }
}
