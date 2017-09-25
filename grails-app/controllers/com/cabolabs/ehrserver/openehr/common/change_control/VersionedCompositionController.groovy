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

package com.cabolabs.ehrserver.openehr.common.change_control

import static org.springframework.http.HttpStatus.*

import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition

import grails.transaction.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.security.Organization
import com.cabolabs.security.User
import grails.util.Holders

@Transactional(readOnly = true)
class VersionedCompositionController {

   def springSecurityService
   def configurationService
   
   def config = Holders.config.app
   
   def index(int offset, String sort, String order, String ehdUid, String organizationUid)
   {
      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def list, orgs
      def c = VersionedComposition.createCriteria()
      def us = User.findByUsername(springSecurityService.authentication.principal.username)
          
      if (organizationUid)
      {
         if (Organization.countByUid(organizationUid) == 0)
         {
            flash.message = "versionedComposition.index.feedback.orgNotFoundShowingForCurrentOrg"
            organizationUid = null
         }
         else
         {
            // Have access to organizationUid?
            
            if (!us.organizations.uid.contains(organizationUid) && !SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
            {
               flash.message = "versionedComposition.index.feedback.cantAccessOrgShowingForCurrentOrg"
               organizationUid = null
            }
         }
      }
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            
            // for admins, if not organizationUid, display for all orgs
            
            if (organizationUid)
            {
               ehr {
                 eq("organizationUid", organizationUid)
               }
            }
            
            if (ehdUid)
            {
               ehr {
                  like('uid', '%'+ehdUid+'%')
               }
            }
         }
         
         orgs = Organization.list() // for the org filter
      }
      else
      {
         def org = session.organization
         
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            ehr {
               if (!organizationUid)
               {
                  flash.message = message(code:"versionedComposition.index.feedback.showingForCurrentOrg")
                  eq("organizationUid", org.uid)
               }
               else
               {
                  eq("organizationUid", organizationUid)
               }
               
               if (ehdUid)
               {
                  like('uid', '%'+ehdUid+'%')
               }
            }
         }
         
         orgs = us.organizations // for the org filter
      }
      
      respond list, model:[versionedCompositionInstanceCount: list.totalCount, organizations: orgs]
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
