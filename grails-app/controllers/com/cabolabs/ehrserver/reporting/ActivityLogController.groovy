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

package com.cabolabs.ehrserver.reporting

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.util.Holders
import grails.plugin.springsecurity.SpringSecurityUtils

@Transactional(readOnly = true)
class ActivityLogController {

   def configurationService
   
   def config = Holders.config.app
   
   def index(int offset, String sort, String order)
   {
      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'desc'
      
      def c = ActivityLog.createCriteria()
      def list
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
         }
      }
      else
      {
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            eq('organizationUid', session.organization.uid)
         }
      }
      
      //respond list, model:[activityLogInstanceCount: list.totalCount]
      
      render view:'index', model : [activityLogInstanceList: list.groupBy{it.sessionId}, activityLogInstanceCount: list.totalCount]
   }

   def show(ActivityLog activityLogInstance)
   {
      // admins can access all logs
      // filter by current org! because it is accessed by id
      if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") &&
          (!activityLogInstance || activityLogInstance.organizationUid != session.organization.uid))
      {
         flash.message = message(code:'activityLog.show.cantAccessLog')
         redirect action:'index'
         return
      }
      respond activityLogInstance
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'activityLog.label', default: 'ActivityLog'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
