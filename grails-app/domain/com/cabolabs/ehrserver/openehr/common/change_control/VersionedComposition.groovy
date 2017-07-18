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

import com.cabolabs.ehrserver.openehr.ehr.Ehr

class VersionedComposition {

   // This is equivalent to the is_persistent method of VERSIONED_COMPOSITION from ehr package.
   boolean isPersistent
   
   // This will be set by the object_id of the first commit of an ORIGINAL_VERSION with change_type=creation
   String uid
   
   // This is equivalent to the owner_id attribute in the RM, just gave little more semantics to the name because we know the owner will be always the EHR.
   //String ehrUid
   Ehr ehr
   
   // When the first commit of a VERSION is received.
   Date timeCreated = new Date()
   
   static transients = ['allVersions', 'latestVersion']
   
   /*
    * Some Methods in the RM
    * 
    * allVersions
    * allVersionIds
    * versionCount
    * hasVersionId
    * isOriginalVersion(versionId)
    * versionWithId(id)
    * latestVersion
    * latestTrunkVersion
    * trunkLifecycleState
    * ...
    */
   
   //static transients = ['allVersions']
   
   List getAllVersions()
   {
      // Return all versions in which the uid prefix is the versioned object uid.
      return Version.findAllByUidLike(this.uid+"::%")
   }
   
   Version getLatestVersion()
   {
      def c = Version.createCriteria()
      def v = c.get {
         like('uid', this.uid + '::%')
         data {
            eq('lastVersion', Boolean.TRUE)
         }
      }
      return v
   }
}
