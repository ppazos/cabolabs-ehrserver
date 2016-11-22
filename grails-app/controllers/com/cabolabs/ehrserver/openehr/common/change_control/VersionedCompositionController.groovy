package com.cabolabs.ehrserver.openehr.common.change_control

import static org.springframework.http.HttpStatus.*

import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition

import grails.transaction.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.security.Organization
import grails.util.Holders

@Transactional(readOnly = true)
class VersionedCompositionController {

   def springSecurityService
   def config = Holders.config.app
   
   def index(int max, int offset, String sort, String order, String ehdUid)
   {
      max = Math.min(max ?: config.list_max, 100)
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def list
      def c = VersionedComposition.createCriteria()
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            if (ehdUid)
            {
               ehr {
                  like('uid', '%'+ehdUid+'%')
               }
            }
         }
      }
      else
      {
         // login info
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            ehr {
               eq("organizationUid", org.uid)
               if (ehdUid)
               {
                  like('uid', '%'+ehdUid+'%')
               }
            }
         }
      }
      
      respond list, model:[versionedCompositionInstanceCount: list.totalCount]
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
