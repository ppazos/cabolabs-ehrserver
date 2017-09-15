
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

package com.cabolabs.ehrserver.account

import grails.util.Holders
import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class PlanController {

   def configurationService

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
   
   def config = Holders.config.app

   def index() {
      params.max = configurationService.getValue('ehrserver.console.lists.max_items')
      respond Plan.list(params), model:[planInstanceCount: Plan.count()]
   }

   def show(Plan planInstance) {
      respond planInstance
   }

   def create() {
      respond new Plan(params)
   }

   @Transactional
   def save(Plan planInstance) {
      if (planInstance == null) {
         notFound()
         return
      }

      if (planInstance.hasErrors()) {
         respond planInstance.errors, view:'create'
         return
      }

      planInstance.save flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'plan.label', default: 'Plan'), planInstance.id])
            redirect planInstance
         }
         '*' { respond planInstance, [status: CREATED] }
      }
   }

   def edit(Plan planInstance) {
      respond planInstance
   }

   @Transactional
   def update(Plan planInstance) {
      if (planInstance == null) {
         notFound()
         return
      }

      if (planInstance.hasErrors()) {
         respond planInstance.errors, view:'edit'
         return
      }

      planInstance.save flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'Plan.label', default: 'Plan'), planInstance.id])
            redirect planInstance
         }
         '*'{ respond planInstance, [status: OK] }
      }
   }

   @Transactional
   def delete(Plan planInstance) {

      if (planInstance == null) {
         notFound()
         return
      }

      planInstance.delete flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'Plan.label', default: 'Plan'), planInstance.id])
            redirect action:"index", method:"GET"
         }
         '*'{ render status: NO_CONTENT }
      }
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'plan.label', default: 'Plan'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
