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

import com.cabolabs.ehrserver.sync.SyncParserService
import grails.converters.*

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

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


   @SecuredStateless
   def syncAccount()
   {
      println "syncAccount " //+ request.securityStatelessMap.extradata

      // TODO: check if already exists
      // TOOD: check if this is an update, that should be a PUT, OK result should be 200
      //println request.JSON

      // account with contact user and list of organizations
      def account = syncParserService.fromJSONAccount(request.JSON.account)

      if (!account) println "NULL!!!!"

      if (!account.save(flush:true))
      {
         // TODO: handle error
         println account.errors.allErrors
      }

      // TODO: structure for the response
      render( status:201, text:[message: 'account synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }

   def syncEhr()
   {
      println "syncEhr"

      def ehr = syncParserService.fromJSONEhr(request.JSON.ehr)

      if (!ehr.save(flush:true))
      {
         // TODO: handle error
         println ehr.errors.allErrors
      }

      // TODO: structure for the response
      render( status:201, text:[message: 'ehr synced OK'] as JSON, contentType:"application/json", encoding:"UTF-8")
   }

   def syncOpt()
   {

   }

   def syncContribution()
   {

   }

   def syncQuery()
   {

   }
}
