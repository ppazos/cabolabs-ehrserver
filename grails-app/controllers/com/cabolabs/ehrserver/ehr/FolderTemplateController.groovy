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
package com.cabolabs.ehrserver.ehr

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.converters.*

class FolderTemplateController {

   def configurationService

   def index(int offset, String sort, String order)
   {
      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def list
      def c = FolderTemplate.createCriteria()
      
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
      
      [list: list, total: list.totalCount]
   }
   
   def show(FolderTemplate folderTemplate)
   {
      def tree = folderTemplate as JSON
      render(view:'show', model:[folderTemplate: folderTemplate, foldersTree: tree])
   }
   
   def create()
   {
      def folderTemplate = new FolderTemplate()
      def tree = folderTemplate as JSON
      render(view:'create', model:[folderTemplate: folderTemplate, foldersTree: tree])
   }
   
   // AJAX/JSON
   def save()
   {
      request.JSON.folderTemplate.organizationUid = session.organization.uid
      def folderTemplate = FolderTemplate.newInstance(request.JSON.folderTemplate)
      
      if (!folderTemplate.save(flush:true))
      {
         println folderTemplate.errors.allErrors
         render(text:([status:"error", message:"Invalid data"] as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8", status:400)
      }

      // TODO: check errors and generate messages
      render(text:([status:"ok", message:"Created", ref:g.createLink(action:'show', id:folderTemplate.id)] as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
   }
   
   def edit()
   {
   }
   
   def update()
   {
   }
}
