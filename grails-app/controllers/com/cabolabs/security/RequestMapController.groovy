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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.security

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.util.Holders

@Transactional(readOnly = true)
class RequestMapController {

   def configurationService
   def springSecurityService
   
   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def config = Holders.config.app
   
   def index() {
      params.max = configurationService.getValue('ehrserver.console.lists.max_items')
      respond RequestMap.list(params), model:[requestMapInstanceCount: RequestMap.count()]
   }

   def show(RequestMap requestMapInstance) {
      respond requestMapInstance
   }

   def create()
   {
      def roles = (Role.list() - Role.findByAuthority(Role.US)).authority
      roles << 'IS_AUTHENTICATED_ANONYMOUSLY'
   
      respond new RequestMap(params), model: [roles: roles]
   }

   @Transactional
   def save(RequestMap requestMapInstance) {
      if (requestMapInstance == null) {
         notFound()
         return
      }

      if (requestMapInstance.hasErrors()) {
         respond requestMapInstance.errors, view:'create'
         return
      }

      requestMapInstance.save flush:true
      
      // 5.6.1 https://grails-plugins.github.io/grails-spring-security-core/latest/#requestmapInstances
      springSecurityService.clearCachedRequestmaps()

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'requestMap.label', default: 'RequestMap'), requestMapInstance.id])
            redirect requestMapInstance
         }
         '*' { respond requestMapInstance, [status: CREATED] }
      }
   }

   def edit(RequestMap requestMapInstance)
   {
      def roles = (Role.list() - Role.findByAuthority(Role.US)).authority
      roles << 'IS_AUTHENTICATED_ANONYMOUSLY'
      respond requestMapInstance, model: [roles: roles]
   }

   @Transactional
   def update(RequestMap requestMapInstance) {
      if (requestMapInstance == null) {
         notFound()
         return
      }

      if (requestMapInstance.hasErrors()) {
         respond requestMapInstance.errors, view:'edit'
         return
      }

      requestMapInstance.save flush:true
      
      // 5.6.1 https://grails-plugins.github.io/grails-spring-security-core/latest/#requestmapInstances
      springSecurityService.clearCachedRequestmaps()

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'RequestMap.label', default: 'RequestMap'), requestMapInstance.id])
            redirect requestMapInstance
         }
         '*'{ respond requestMapInstance, [status: OK] }
      }
   }

   @Transactional
   def delete(RequestMap requestMapInstance) {

      if (requestMapInstance == null) {
         notFound()
         return
      }

      requestMapInstance.delete flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'RequestMap.label', default: 'RequestMap'), requestMapInstance.id])
            redirect action:"index", method:"GET"
         }
         '*'{ render status: NO_CONTENT }
      }
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'requestMap.label', default: 'RequestMap'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
