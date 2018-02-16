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

import com.cabolabs.security.Organization
import com.cabolabs.util.DateUtils

/**
 * Plan with quota information that can be assigned to many organizations through PlanAssociation.
 */
class Plan {

   static Map periods = [
     WEEKLY: 1,
     MONTHLY: 2,
     QUARTERLY: 3,
     SEMESTERLY: 4,
     YEARLY: 5
   ]

   String name
   
   // Limits for the plan in the period
   int maxTransactions
   int maxDocuments
   int repositorySize // in bytes
   int totalRepositorySize // in bytes, independent of the period, for the whole plan association duration
   int period
   
   
   // Limits for the plan, globally
   
   /* not always maxUsersTotal = maxUsersPerOrganization * maxOrganizations,
    * for some plans, we might have a constraint for the total users and not
    * for the users per org (=null) */
   int maxUsersTotal = 1 // adding all the users from all the organizations should not pass this boundary
   int maxUsersPerOrganization = 1
   int maxOrganizations = 1
   
   int maxEhrs = 50

   
   static constraints = {
     period( inList: periods.values() as List)
   }
   
   /**
    * Creates the PlanAssociation between Plan and Account.
    */
   def associate(Account account)
   {
      // TODO: check the org doesn't have an active plan
      def from = DateUtils.toFirstDateOfMonth(new Date()) // plans go from the first day of a month
      def pa = new PlanAssociation(account: account, from: from, to: from+365, plan:this)
      pa.save(failOnError: true)
   }
   
   /**
    * Gets the active plan for the organization.
    */
   static PlanAssociation active(Account account)
   {
      return activeOn(new Date())
   }
   
   static PlanAssociation activeOn(Account account, Date on)
   {
      def pa = PlanAssociation.withCriteria(uniqueResult: true) {
        le('from', on) // from <= on < to
        gt('to', on)
        eq('account', account)
      }
      
      return pa // can be null
   }
}
