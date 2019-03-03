package com.cabolabs.ehrserver.query

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.converters.*

// test
import com.cabolabs.ehrserver.sync.SyncMarshallersService
import groovy.json.*

@Transactional(readOnly = true)
class EhrQueryController {

   def syncMarshallersService

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def index(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      [list: EhrQuery.list(params), total: EhrQuery.count()]

      /*
      def jb = new JsonBuilder()
      syncMarshallersService.toJSON(EhrQuery.list(params), jb)
      render jb.toString(), contentType: "application/json"
      */
   }

   def show(EhrQuery ehrQueryInstance)
   {
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

      if (ehrQueryInstance.hasErrors()) {
         respond ehrQueryInstance.errors, view:'create'
         return
      }

      ehrQueryInstance.save flush:true

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
      respond ehrQueryInstance
   }

   @Transactional
   def update(EhrQuery ehrQueryInstance)
   {
      println params
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
