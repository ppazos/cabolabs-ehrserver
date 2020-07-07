/*
 * Copyright 2011-2020 CaboLabs Health Informatics
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

package com.cabolabs.ehrserver.ehr.clinical_documents

class OperationalTemplateIndex {

   String templateId       // formatted template id
   String concept          // Concept name of the OPT
   String language         // en formato 'en', antes era ISO_639-1::en pero https://github.com/ppazos/cabolabs-ehrserver/issues/878
   String uid = java.util.UUID.randomUUID() as String
   String externalUid      // from the OPT file
   String externalTemplateId // from the OPT file
   String archetypeId      // root archetype id
   String archetypeConcept // concept name for the archetype root node

   String organizationUid  // OPT multitenancy

   // internal versioning #776
   String setId = java.util.UUID.randomUUID() as String
   int versionNumber = 1
   boolean lastVersion = true // to simplify queries

   String fileLocation

   Date dateCreated
   Date lastUpdated

   boolean isDeleted = false
   boolean isActive = true
   boolean isIndexed = false

   // sync
   boolean master = true

   static hasMany = [referencedArchetypeNodes: ArchetypeIndexItem,
                     templateNodes: OperationalTemplateIndexItem]

   static constraints = {
      fileLocation(maxSize:1024)
   }

   static transients = ['lang']

   // alias to language
   def getLang()
   {
      this.language
   }

   static namedQueries = {
      forOrg { org ->

         eq('organizationUid', org.uid)
      }
      likeConcept { concept ->
         if (concept)
         {
            like('concept', '%'+concept+'%')
         }
      }
      lastVersions {

         eq('lastVersion', true)
      }
      matchExternalUidOrExternalTemplateId { externalUid, templateId ->
         or {
            eq('externalUid', externalUid)
            eq('externalTemplateId', templateId)
         }
      }
      notDeleted {
         eq('isDeleted', false)
      }
      deleted {
         eq('isDeleted', true)
      }
      active {
         eq('isActive', true)
      }
      indexed {
         eq('isIndexed', true)
      }
   }

   static mapping = {
      templateNodes cascade: "all-delete-orphan" // delete nodes when opti is deleted
      master column:'sync_master'
   }
}
