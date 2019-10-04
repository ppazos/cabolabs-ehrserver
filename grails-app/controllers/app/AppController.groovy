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

import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.versions.VersionFSRepoService
import com.cabolabs.security.*

class AppController {

   def versionFSRepoService

   // dashboard
   def index()
   {
      def count_ehrs, count_contributions, count_queries, count_users, version_repo_size, opt_count
      def version_repo_sizes = [:] // org => versio repo size

      count_ehrs = Ehr.count()
      count_contributions = Contribution.count()
      count_users = User.count()
      opt_count = OperationalTemplateIndex.count()
      count_queries = Query.count()

      def orgs = Organization.list()
      orgs.each { __org ->
         version_repo_sizes << [(__org): versionFSRepoService.getRepoSizeInBytes(__org.uid)]
      }

      // sort by usage, decreasing
      version_repo_sizes = version_repo_sizes.sort { -it.value }

      //version_repo_size = versionFSRepoService.getRepoSizeInBytes()

      // ---------------------------------------------
      // TODO: check for remote notifications
      // ---------------------------------------------

      return [
         count_ehrs:count_ehrs,
         count_contributions:count_contributions,
         count_queries: count_queries,
         version_repo_sizes: version_repo_sizes,
         count_users: count_users,
         opt_count: opt_count
      ]
   }

   def get_started()
   {
      render view:'get_started'
   }
}
