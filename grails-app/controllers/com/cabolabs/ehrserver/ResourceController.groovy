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

package com.cabolabs.ehrserver

import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare
import com.cabolabs.security.Organization

class ResourceController {
   
   def resourceService
   
   // TODO: verify permissions of the user over the query & organizations
   
   def shareQuery(String uid)
   {
      def query = Query.findByUid(uid)
      def shares = QueryShare.findAllByQuery(query)
      render view:'/query/share', model:[query: query, organizations: shares.organization]
   }
   
   def saveSharesQuery()
   {
      Query query = params.query
      
      if (query.isPublic)
      {
         flash.message = "Can't change the shares of a public query"
         redirect action:'shareQuery', params:[uid:query.uid]
         return
      }
      
      // delete all shares but the one that belongs to the current org
      resourceService.cleanSharesQueryBut(query, session.organization)
      
      // share with selected orgs
      def organization
      def orgUids = params.list('organizationUid') - [null, '']
      
      orgUids.each { organizationUid ->
         organization = Organization.findByUid(organizationUid)
         resourceService.shareQuery(query, organization)
      }
      
      flash.message = "Query shares were updated correctly"
      redirect action:'shareQuery', params:[uid:query.uid]
   }
}
