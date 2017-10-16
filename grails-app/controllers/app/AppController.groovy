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

package app

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.versions.VersionFSRepoService
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import grails.plugin.springsecurity.SpringSecurityUtils

import com.cabolabs.security.User
import com.cabolabs.security.UserRole

class AppController {

   def springSecurityService
   def versionFSRepoService
   
   // shows main dashboard
   def index()
   {
      // Count EHRs
      def count_ehrs, count_contributions, count_queries, count_users
      def version_repo_sizes = [:] // org => versio repo size
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         count_ehrs = Ehr.count()
         count_contributions = Contribution.count()
         count_queries = Query.count()
         count_users = User.count()
         
         def orgs = Organization.list()
         
         orgs.each { __org ->
            version_repo_sizes << [(__org): versionFSRepoService.getRepoSizeInBytes(__org.uid)]
         }
         
         // sort by usage, decreasing
         version_repo_sizes = version_repo_sizes.sort { -it.value }
      }
      else
      {
         //def auth = springSecurityService.authentication
         //def org = Organization.findByNumber(auth.organization)
         def org = session.organization
         
         count_ehrs = Ehr.countByOrganizationUid(org.uid)
         count_contributions = Contribution.countByOrganizationUid(org.uid)
         
         
         def shares = QueryShare.findAllByOrganization(org)
         def c = Query.createCriteria()
         count_queries = c.count() {
            if (shares)
            {
               or {
                  eq('isPublic', true)
                  'in'('id', shares.query.id)
               }
            }
            else
            {
               eq('isPublic', true)
            }
         }
         
         def ur = UserRole.createCriteria()
         def urs = ur.list() {
            createAlias('user','user')
            projections {
               property("user.id", "user.id")
            }
            eq('organization', org)
         }
         count_users = urs.unique().size()
         
         version_repo_sizes << [(org): versionFSRepoService.getRepoSizeInBytes(org.uid)]
      }
      
      [
         count_ehrs:count_ehrs, 
         count_contributions:count_contributions, 
         count_queries: count_queries,
         version_repo_sizes: version_repo_sizes,
         count_users: count_users
      ]
   }
   
   def get_started()
   {
      []
   }
}
