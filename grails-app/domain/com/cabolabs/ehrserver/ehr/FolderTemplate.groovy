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

/**
 * Generates a structure of folders templates with names that can be used to generate
 * many structures of concrete folders from the same folder template, to populate an
 * initial EHR.directory with that folder structure.
 * 
 * This class acts as the container of the structure, like a root. The name is to display
 * on the list of FolderTemplates. The structure is defined by the items.
 */
class FolderTemplate {

   String name
   String description
   String organizationUid
   
   List folders = []
   static hasMany = [folders: FolderTemplateItem]

   static constraints = {
      name(nullable: false, blank: false)
      organizationUid(nullable: false)
   }
   static mapping = {
     organizationUid index: 'org_uid_idx'
   }
   
   static def newInstance(org.codehaus.groovy.grails.web.json.JSONObject json)
   {
      def ftpl = new FolderTemplate()
      
      ftpl.updateInstance(json)
      
      return ftpl
   }
   
   def updateInstance(org.codehaus.groovy.grails.web.json.JSONObject json)
   {
      this.name            = json['name']
      this.description     = json['description']
      this.organizationUid = json['organizationUid']
      
      def traverse_item // needs to be declared before defined to work recursively
      
      traverse_item = { json_item ->
         def item = new FolderTemplateItem(name: json_item['name'])
         json_item['folders'].each { child ->
            println child
            item.folders << traverse_item(child)
         }
         return item
      }
      
      this.folders.clear()
      json['folders'].each {
         println it
         this.folders << traverse_item(it)
      }
   }
}
