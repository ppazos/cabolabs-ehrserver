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

package com.cabolabs.ehrserver.account

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.security.*
import grails.converters.*
import com.cabolabs.ehrserver.openehr.ehr.Ehr


import java.math.RoundingMode

class StatsController {

   def versionRepoService
   def optService

   /**
    * Show stats dashboard for all the organizations accessible by the user.
    */
   def index()
   {
   }

   // server stats
   @SecuredStateless
   def stats()
   {
      // Memory report
      // =====================================================
      Runtime runtime = Runtime.getRuntime()

      // Run the garbage collector
      runtime.gc()

      // Calculate the used memory in bytes
      long total_memory = runtime.totalMemory()
      long free_memory  = runtime.freeMemory()
      long used_memory  = total_memory - free_memory


      // Version Repo Size (total)
      // =====================================================
      def version_repo_size = versionRepoService.getRepoSizeInBytes()


      // Determine repository service
      // =====================================================
      def repo_service = 'FS'
      if (versionRepoService instanceof com.cabolabs.ehrserver.versions.VersionS3RepoService)
      {
         repo_service = 'AWS S3'
      }


      // Total number of EHRs
      def total_ehr_count = Ehr.count()

      //  Total number of contributions
      def total_contribution_count = Contribution.count()


      // Memory in MB
      def stats = [
         total_memory: (total_memory / (1024L * 1024L)).setScale(1, RoundingMode.HALF_UP),
         used_memory: (used_memory / (1024L * 1024L)).setScale(1, RoundingMode.HALF_UP),
         free_memory: (free_memory / (1024L * 1024L)).setScale(1, RoundingMode.HALF_UP),
         max_memory: (runtime.maxMemory() / (1024L * 1024L)).setScale(1, RoundingMode.HALF_UP),
         available_processors: runtime.availableProcessors(),
         version_repo_size: (version_repo_size / (1024L * 1024L)).setScale(1, RoundingMode.HALF_UP),
         repo_service: repo_service,
         total_ehr_count: total_ehr_count,
         total_contribution_count: total_contribution_count,
         ehrserver_version: grailsApplication.metadata["info.app.version"]
      ]

      render stats as JSON
   }

   /**
    * Admin get stats for an account manager user
    * @return
    */
   @SecuredStateless
   def organizationAccountStats(String uid)
   {
      //def accmgt = User.findByEmail(username)
      //def organizations = accmgt.organizations
      def org = Organization.findByUid(uid)

      if (!org)
      {
         render(status:404, "Organization not found")
         return
      }

      // For now the period is just the current month, variable period later.
      long from = firstDayOfCurrentMonth()
      long to = firstDayOfNextMonth()
      def dfrom = new Date(from)
      def dto   = new Date(to)

      def stats = [
         from: from, /* TODO: from and to are rendered as timestamps, should be JSON dates in 8601 */
         to: to,
         stats: [
            transactions: Contribution.byOrgInPeriod(org.uid, dfrom, dto).count(),
            documents: Version.byOrgInPeriod(org.uid, dfrom, dto).count(),
            size: versionRepoService.getRepoSizeInBytesBetween(org.uid, dfrom, dto)
         ]
      ]

      // TODO: support XML by withFormat
      render stats as JSON
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
      //println params
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

      // if on current month, dfrom can be < now and we need to plan
      // active on now not on dfrom.
      def current_month = Calendar.getInstance().get(Calendar.MONTH)
      def now = new Date()
      def active_now = (current_month == dfrom.month)
      def active_plan_in = (active_now ? now : dfrom)


      // FIXME: should be current total size, not size in period, and
      // should be for the account that is the sum of all repos of all the account organizations
      def size = versionRepoService.getRepoSizeInBytesBetween(uid, dfrom, dto)

      // Active plan for the orgazination account
      def org = Organization.findByUid(uid)

      def plan_association = Plan.associatedOn(org.account, active_plan_in) // can be null!

      [transactions: Contribution.byOrgInPeriod(uid, dfrom, dto).count(),
       documents: Version.byOrgInPeriod(uid, dfrom, dto).count(),
       size: size,
       plan: plan_association?.plan,
       plan_association: plan_association,
       from: from, to: to]
   }

   // disk usage for all the organizations in the account
   // results are in kB (1000 bytes)
   def accountRepoUsage(Account account)
   {
      // If there is no current plan, the max repo size is set to the sum of the size of all repos
      def plan_association = Plan.active(account) // can be null!
      def max_repo_size = 0.0

      def plan_repo_total_size = false
      if (plan_association)
      {
         max_repo_size = plan_association.plan.repo_total_size_in_kb
         plan_repo_total_size = true
      }

      def stats = [:] // org name => repo size
      def size

      account.organizations.each { org ->

         size = ((versionRepoService.getRepoSizeInBytesOrg(org.uid) + optService.getRepoSizeInBytesOrg(org.uid)) / 1000).setScale(1,0)

         // size is set in kB = 1000 bytes
         stats[org.uid] = size

         // if there is no plan, we set the max to the current size
         if (!plan_repo_total_size) max_repo_size += size
      }

      render(text: [max_repo_size: max_repo_size, usage: stats] as JSON, contentType:"application/json", encoding:"UTF-8")
   }

   /**
    * Stats abuot the number of templates loaded, returns also the max templates per org.
    */
   def accountTemplatesLoaded(Account account)
   {
      def plan_association = Plan.active(account) // can be null!
      def max_opts_per_org = 0

      def plan_max_opts = false
      if (plan_association)
      {
         max_opts_per_org = plan_association.plan.max_opts_per_organization
         plan_max_opts = true
      }

      def stats = [:] // org name => repo size
      def count

      account.organizations.each { org ->

         count = OperationalTemplateIndex.forOrg(org).lastVersions.count()
         stats[org.uid] = count

         // if there is no plan, we set the max to the current amount
         if (!plan_max_opts) max_opts_per_org += count
      }

      render(text: [max_opts_per_org: max_opts_per_org, usage: stats] as JSON, contentType:"application/json", encoding:"UTF-8")
   }
}
