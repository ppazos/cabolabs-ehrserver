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

package com.cabolabs.ehrserver.openehr.common.change_control

// FIXME: move to another package like helpers or commit

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.reporting.ActivityLog

/**
 * Clase auxiliar para mantener los commits pendientes de ser finalizados.
 * Esta clase no es parte del modelo de openEHR es parte de la implementacion del EHR Server.
 * Se elimina cuando se finaliza el commit o se hace rollback.
 *
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
class CommitLog extends ActivityLog {

   String ehrUid

   // client/request data
   String locale
   Map params
   String contentType
   int contentLength

   // commit file location, could be null
   String fileLocation

   boolean success // false if an error hapenned in the commit

   static transients = ['ehr', 'contribution']

   static constraints = {
      ehrUid nullable: true // null if the request doesnt include it, this is a client error
      contentType nullable: true
      fileLocation nullable: true, maxSize: 1024
   }

   def getEhr()
   {
      return Ehr.findByUid(this.ehrUid)
   }

   def getContribution()
   {
      return Contribution.findByUid(this.objectUid)
   }

}
