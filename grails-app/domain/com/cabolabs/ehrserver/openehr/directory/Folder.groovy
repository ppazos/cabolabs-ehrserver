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

package com.cabolabs.ehrserver.openehr.directory

import com.cabolabs.ehrserver.openehr.ehr.Ehr

class Folder {

   // Attributes from LOCATABLE
   String name
   String uid = (java.util.UUID.randomUUID() as String)
   /* This will be enabled when we support archetyped Folders.
   path
   archetype_node_id
   archetype_id
   template_id
   */
   
   Folder parent
   List items = []
   static hasMany = [folders: Folder, items: String] // items is a list of UIDs of the VERSIONED_OBJECTS contained.
   
   // EHR in which the Folder is contained
   // Only root nodes have EHRs, so ehr != null only if parent == null
   // TODO: add that to constraints
   Ehr ehr
   static belongsTo = [Ehr, Folder]
   
   // multitenancy
   String organizationUid
   
   static constraints = {
      parent(nullable: true)
      name(nullable: true, blank: false)
      ehr(nullable: true)
   }
   
   static mapping = {
      items cascade: 'all-delete-orphan'
      organizationUid index: 'org_uid_idx'
   }
}
