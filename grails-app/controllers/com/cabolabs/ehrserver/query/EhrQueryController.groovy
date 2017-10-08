package com.cabolabs.ehrserver.query

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import com.cabolabs.ehrserver.openehr.ehr.Ehr

@Transactional(readOnly = true)
class EhrQueryController {

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def index(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      [list: EhrQuery.list(params), total: EhrQuery.count()]
   }
   
   
   def test1(long id)
   {
      def eq = EhrQuery.get(id)
      eq.execute(Ehr.get(1).uid, session.organization.uid)
      render "test1"
   }
   
   
   def test2(long id)
   {
      def eq = EhrQuery.get(id)
      eq.execute(null, session.organization.uid)
      render "test2"
   }

   
   def show(EhrQuery ehrQueryInstance) {
      respond ehrQueryInstance
   }

   def create() {
      respond new EhrQuery(params)
   }

   @Transactional
   def save(EhrQuery ehrQueryInstance) {
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

   def edit(EhrQuery ehrQueryInstance) {
      respond ehrQueryInstance
   }

   @Transactional
   def update(EhrQuery ehrQueryInstance) {
      if (ehrQueryInstance == null) {
         notFound()
         return
      }

      if (ehrQueryInstance.hasErrors()) {
         respond ehrQueryInstance.errors, view:'edit'
         return
      }

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
   def delete(EhrQuery ehrQueryInstance) {

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
