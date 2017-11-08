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

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import java.text.SimpleDateFormat
import com.cabolabs.ehrserver.openehr.ehr.Ehr

/**
 * La contribution queda pendiente/incompleta hasta que no se envien todas las versiones referenciadas.
 * 
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 *
 */
class Contribution {

   Ehr ehr // puntero para atras al ehr que contiene esta contribution
   AuditDetails audit
   
   // uid is set by the client:
   // https://github.com/ppazos/cabolabs-ehrserver/issues/51
   String uid //= (java.util.UUID.randomUUID() as String)
   
   // Emula CONTRIBUTION.versions(Set<OBJECT_REF>) usando relaciones directas a
   // las versiones contenidas en la Contribution en lugar de OBJECT_REFs
   List versions = []
   static hasMany = [versions:Version]
   
   // Internal to simplify querying and grouping
   int dateGroup      // yyyymmdd to group by day
   int yearMonthGroup // yyyymm to group by month
   int yearGroup      // yyyy to group by year
   
   
   // multitenancy
   // copy of the EHR.organizationUid
   String organizationUid
   
   
   static constraints = {
      uid(nullable: false)
   }
   
   static mapping = {
      audit column: 'contrib_audit' // En algunos dbms audit o audit_id son reservados
      audit cascade: 'all' //'save-update'
      versions cascade: 'all' //'save-update'
      organizationUid index: 'org_uid_idx'
   }
   
   //static belongsTo = [Ehr]
   
   
   def beforeInsert()
   {
      def d = this.audit.timeCommitted
      this.dateGroup      = Integer.parseInt( new SimpleDateFormat("yyyyMMdd").format(d) )
      this.yearMonthGroup = Integer.parseInt( new SimpleDateFormat("yyyyMM").format(d) )
      this.yearGroup      = Integer.parseInt( new SimpleDateFormat("yyyy").format(d) )
   }
   
   static namedQueries = {
      byOrgInPeriod { uid, from, to ->
         eq('organizationUid', uid)
         audit {
           ge('timeCommitted', from) // dfrom <= timeCommitted < dto
           lt('timeCommitted', to)
         }
      }
   }
}
