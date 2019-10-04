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
package com.cabolabs.ehrserver.query

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.converters.*

// test
import groovy.json.*

@Transactional(readOnly = true)
class EhrQueryController {

   def configurationService

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def index(Integer max, int offset, String sort, String order)
   {
      max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'

      def c = EhrQuery.createCriteria()

      // filders by org even if the user is admin because it is confusing for demos
      // to have EhrQueries that depend on queries that are not in the current org
      def list = c.list (max: max, offset: offset, sort: sort, order: order) {
         eq('organizationUid', session.organization.uid)
      }

      [list: list, total: list.totalCount]
   }

   def show(EhrQuery ehrQueryInstance)
   {
      // TODO: check the query belongs to the current org
      respond ehrQueryInstance
   }

   def create()
   {
      respond new EhrQuery(params)
   }

   @Transactional
   def save(EhrQuery ehrQueryInstance)
   {
      if (ehrQueryInstance == null) {
         notFound()
         return
      }

      ehrQueryInstance.organizationUid = session.organization.uid

      if (!ehrQueryInstance.save(flush:true))
      {
         respond ehrQueryInstance.errors, view:'create'
         return
      }

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'ehrQuery.label', default: 'EhrQuery'), ehrQueryInstance.id])
            redirect ehrQueryInstance
         }
         '*' { respond ehrQueryInstance, [status: CREATED] }
      }
   }

   def edit(EhrQuery ehrQueryInstance)
   {
      // TODO: check the query belongs to the current org
      respond ehrQueryInstance
   }

   @Transactional
   def update(EhrQuery ehrQueryInstance)
   {
      // TODO: check the query belongs to the current org
      if (ehrQueryInstance == null) {
         notFound()
         return
      }

      if (ehrQueryInstance.hasErrors()) {
         respond ehrQueryInstance.errors, view:'edit'
         return
      }

      ehrQueryInstance.queries.clear()
      ehrQueryInstance.queries = Query.getAll(params.queries)
      ehrQueryInstance.save flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'EhrQuery.label', default: 'EhrQuery'), ehrQueryInstance.id])
            redirect ehrQueryInstance
         }
         '*'{ respond ehrQueryInstance, [status: OK] }
      }
   }

   @Transactional
   def delete(EhrQuery ehrQueryInstance)
   {
      // TODO: check the query belongs to the current org

      if (ehrQueryInstance == null) {
         notFound()
         return
      }

      ehrQueryInstance.delete flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'EhrQuery.label', default: 'EhrQuery'), ehrQueryInstance.id])
            redirect action:"index", method:"GET"
         }
         '*'{ render status: NO_CONTENT }
      }
   }

   def execute(EhrQuery ehrQueryInstance)
   {
      // TODO: check the query belongs to the current org
      if (ehrQueryInstance == null) {
         notFound()
         return
      }

      def ehrUids = ehrQueryInstance.getEhrUids2(session.organization.uid)
      render (ehrUids as JSON)
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'ehrQuery.label', default: 'EhrQuery'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
