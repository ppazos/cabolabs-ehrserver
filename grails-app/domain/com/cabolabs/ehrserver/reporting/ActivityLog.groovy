/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 * at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, 
 * you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
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

class ActivityLog {

   Date timestamp = new Date()
   String username
   String organizationUid // used to login
   String action
   Long objectId    // when using db ids (we try to avoid this case)
   String objectUid // most ids will be uids
   String remoteAddr
   String clientIp
   String xForwardedFor
   String referer
   String requestURI // received url - /ehr/logs
   String matchedURI // internal matched url - /ehr/grails/activityLog/index.dispatch
   String sessionId // java session id, allows alog grouping
   
   static constraints = {
      username nullable: true
      objectId nullable: true
      objectUid nullable: true
      organizationUid nullable: true
      remoteAddr nullable: true
      clientIp nullable: true
      xForwardedFor nullable: true
      referer nullable: true
      requestURI nullable: true
      matchedURI nullable: true
   }
   
   static mapping = {
     organizationUid index: 'org_uid_idx'
   }
}
