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
class RoleController {

   def configurationService
   
   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def config = Holders.config.app
   
   def index() {
      params.max = configurationService.getValue('ehrserver.console.lists.max_items')
      respond Role.list(params), model:[roleInstanceCount: Role.count()]
   }

   def show(Role roleInstance) {
      respond roleInstance
   }

   def create() {
      respond new Role(params)
   }

   @Transactional
   def save(Role roleInstance) {
      if (roleInstance == null) {
         notFound()
         return
      }

      if (roleInstance.hasErrors()) {
         respond roleInstance.errors, view:'create'
         return
      }

      roleInstance.save flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'role.label', default: 'Role'), roleInstance.id])
            redirect roleInstance
         }
         '*' { respond roleInstance, [status: CREATED] }
      }
   }

   def edit(Role roleInstance)
   {
      // can't edit core roles
      if (Role.coreRoles().contains(roleInstance.authority))
      {
         redirect action: 'index'
         return
      }
      respond roleInstance
   }

   @Transactional
   def update(Role roleInstance)
   {
      if (roleInstance == null) {
         notFound()
         return
      }
      
      // can't edit core roles
      if (Role.coreRoles().contains(roleInstance.authority))
      {
         redirect action: 'index'
         return
      }

      if (roleInstance.hasErrors())
      {
         respond roleInstance.errors, view:'edit'
         return
      }

      roleInstance.save flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'Role.label', default: 'Role'), roleInstance.id])
            redirect roleInstance
         }
         '*'{ respond roleInstance, [status: OK] }
      }
   }

   @Transactional
   def delete(Role roleInstance) {

      if (roleInstance == null) {
         notFound()
         return
      }

      roleInstance.delete flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'Role.label', default: 'Role'), roleInstance.id])
            redirect action:"index", method:"GET"
         }
         '*'{ render status: NO_CONTENT }
      }
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'role.label', default: 'Role'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
