package com.cabolabs.ehrserver.openehr.common.change_control

import static org.springframework.http.HttpStatus.*
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import grails.transaction.Transactional

import com.cabolabs.security.Organization

@Transactional(readOnly = true)
class VersionedCompositionController {

   def springSecurityService
   
   def index(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      if (!params.offset) params.offset = 0
      
      // login info
      def auth = springSecurityService.authentication
      def org = Organization.findByNumber(auth.organization)
      
      def c = VersionedComposition.createCriteria()
      def results = c.list (max: params.max, offset: params.offset) {
         ehr {
            eq("organizationUid", org.uid)
         }
      }
      
      respond results, model:[versionedCompositionInstanceCount: results.totalCount]
   }

   def show(String uid)
   {
      def versionedCompositionInstance = VersionedComposition.findByUid(uid)
      respond versionedCompositionInstance
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'versionedComposition.label', default: 'VersionedComposition'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
