
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

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.security.Organization

class StatsController {

   def versionFSRepoService

   /**
    * Show stats dashboard for all the organizations accessible by the user.
    */
   def index()
   {
   }
   
   /**
    * Show detailed stats for an organization in an interval of time.
    * Dates come as epoch times.
    */
   def organization(String uid, long from, long to)
   {
      // If not date range is set, set the rante to the current month
      // Range will be checked like: from <= timeCommitted < to
      // so "to" should be next months 1st day on time 0
      if (!to)
      {
         def cal = Calendar.getInstance()
         cal.set(Calendar.DATE, 1) // 1st day current month
         
         // zero time
         cal.set(Calendar.HOUR_OF_DAY, 0)
         cal.set(Calendar.MINUTE, 0)
         cal.set(Calendar.SECOND, 0)
        
         from = cal.getTimeInMillis()
         
         //cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE))
         cal.add(Calendar.MONTH, 1) // next month 1st day
         
         to = cal.getTimeInMillis()
      }
   
      def dfrom = new Date(from)
      def dto   = new Date(to)
      
      println dfrom
      println dto
   
      // Number of transactions
      def contributions = Contribution.withCriteria {
        
        projections {
           count()
        }
        
        eq('organizationUid', uid)
        audit {
          //between('timeCommitted', dfrom, dto)
          ge('timeCommitted', dfrom) // dfrom <= timeCommitted < dto
          lt('timeCommitted', dto)
        }
      }
      
      // Number of documents and size in bytes (more than one document per transaction is allowed)
      def versions = Version.withCriteria {
         
         projections {
           count()
         }
        
         contribution {
            eq('organizationUid', uid)
         }
         commitAudit {
            //between('timeCommitted', dfrom, dto)
            ge('timeCommitted', dfrom) // dfrom <= timeCommitted < dto
            lt('timeCommitted', dto)
         }
      }
      
      def size = versionFSRepoService.getRepoSizeInBytesBetween(uid, dfrom, dto)
      
      // Active plan for the orgazination
      def org = Organization.findByUid(uid)
      def plan_association = Plan.activeOn(org, dfrom) // can be null!
      
      [transactions: contributions[0], documents: versions[0], size: size, plan: plan_association?.plan, plan_association: plan_association, from: from, to: to]
   }
}
