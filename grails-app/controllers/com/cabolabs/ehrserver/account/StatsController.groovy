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

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.security.*
import grails.converters.*

class StatsController {

   def versionFSRepoService

   /**
    * Show stats dashboard for all the organizations accessible by the user.
    */
   def index()
   {
   }
   
   /**
    * Admin get stats for an account manager user
    * @return
    */
   @SecuredStateless
   def userAccountStats(String username)
   {
      /*
      // 0. token should be for an admin
      // 1. username should be account manager
      def _username = request.securityStatelessMap.username
      def _user = User.findByUsername(_username)
      if (!_user.authoritiesContains(Role.AD))
      {
         // 0.
      }
      */
      def accmgt = User.findByUsername(username)
      /*
      if (!accmgt.authoritiesContains(Role.AM))
      {
         // 1.
      }
      */
      def organizations = accmgt.organizations
      
      // For now the period is just the current month, variable period later.
      long from = firstDayOfCurrentMonth()
      long to = firstDayOfNextMonth()
      def dfrom = new Date(from)
      def dto   = new Date(to)
      
      def stats = [from: from, to: to, organizations: [:]]
      organizations.each { org ->
         
         stats.organizations[org.uid] =
            [
              transactions: Contribution.byOrgInPeriod(org.uid, dfrom, dto).count(),
              documents: Version.byOrgInPeriod(org.uid, dfrom, dto).count(),
              size: versionFSRepoService.getRepoSizeInBytesBetween(org.uid, dfrom, dto)
            ]
      }
      
      return stats as JSON
   }
   
   private long firstDayOfCurrentMonth()
   {
      def cal = Calendar.getInstance()
      cal.set(Calendar.DATE, 1) // 1st day current month
      
      // zero time
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
     
      return cal.getTimeInMillis()
   }
   
   private long firstDayOfNextMonth()
   {
      def cal = Calendar.getInstance()
      
      cal.set(Calendar.DATE, 1) // 1st day current month
      
      // zero time
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      
      cal.add(Calendar.MONTH, 1) // next month 1st day
      return cal.getTimeInMillis()
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
         from = firstDayOfCurrentMonth()
         to = firstDayOfNextMonth()
      }
   
      def dfrom = new Date(from)
      def dto   = new Date(to)
      
      println dfrom
      println dto
      
      def size = versionFSRepoService.getRepoSizeInBytesBetween(uid, dfrom, dto)
      
      // Active plan for the orgazination
      def org = Organization.findByUid(uid)
      def plan_association = Plan.activeOn(org, dfrom) // can be null!
      
      [transactions: Contribution.byOrgInPeriod(organizations[0].uid, dfrom, dto).count(),
       documents: Version.byOrgInPeriod(organizations[0].uid, dfrom, dto).count(),
       size: size,
       plan: plan_association?.plan,
       plan_association: plan_association,
       from: from, to: to]
   }
}
