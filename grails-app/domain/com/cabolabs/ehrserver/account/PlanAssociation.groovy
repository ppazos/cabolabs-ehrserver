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

package com.cabolabs.ehrserver.account

/**
 * Association of a plan for an organization. If "to" is not null, the plan is inactive.
 */
class PlanAssociation {

   static Map states = [
     INACTIVE: 1,
     ACTIVE: 2,
     SUSPENDED: 3,
     CLOSED: 4
   ]
   
   //String organizationUid
   Account account
   Date from
   Date to
   Plan plan
   int state = states.INACTIVE
   
   static constraints = {
      to ( nullable: true )
      state( inList: states.values() as List)
   }
   
   static mapping = {
     from column: "pa_from" // avoid using reserved word FROM
     to column: "pa_to" // avoid using reserved word TO in MySQL
     organizationUid index: 'org_uid_idx'
   }
}
