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
   int period

   // in bytes, for all the orgs in the account
   // This is the initial size of the repo when the plan is selected,
   // later customers can choose to add space to their account, that will
   // generate new custom plans or modelling add-ons that can be attached
   // to a plan for a specific account (will be attached to the plan assoc).
   // With add-ons extra services can be modeled and be used to know current
   // limits (with a 5Gb add-on the total repo size will be de basic plan
   // repo total size + 5GB of the add-on), and also help to calculate billing.
   int repo_total_size_in_kb

   int max_opts_per_organization
   int max_organizations
   int max_api_tokens_per_organization

   /**
    * other limits we might want to use in the future
    * - max_users_per_organization
    * - max_ehrs_per_organization
    * - max_compositions_per_organization
    * -
    * -
    */

   static constraints = {
      period( inList: periods.values() as List)
   }

   /**
    * Creates the PlanAssociation between Plan and Account.
    * The caller should check there is no active plan for the account on the same period.
    */
   def associate(Account account, Date from, int duration_in_days = 365)
   {
      if (!from)
      {
         // plan starts from the first day of current month
         from = DateUtils.toFirstDateOfMonth(new Date())
      }


      // By default, a new plan is inactive.
      // If there exists acurrently ACTIVE plan, the PlanAssociationStateUpdateJob will
      // update the states if the new plan should start today.
      // Also will activate this plan if today falls under the period and there is no ACTIVE plan

      // TODO: to assing plans in the future I need to check the period overlapping here and assign
      // state INACTIVE for the future one y there is currently an active one
      //def state = PlanAssociation.states.INACTIVE // if the start date is in the future, the plan is inactive until that date arrives, a job will update the states

/*
      if (from < new Date())
      {
         state = PlanAssociation.states.ACTIVE // if the start date is in the past, activate the plan
      }
*/

      def pa = new PlanAssociation(account: account, from: from, to: from+duration_in_days, plan: this)
      pa.save(failOnError: true)
   }

   /**
    * Gets the currently associated plan for the organization.
    * Checking if the current date falls into the plan validity period.
    * Doens't check if the status is active or suspended, etc.
    */
   static PlanAssociation associatedNow(Account account)
   {
      return associatedOn(account, new Date())
   }

   static PlanAssociation associatedOn(Account account, Date on)
   {
      def pa = PlanAssociation.withCriteria(uniqueResult: true) {
        le('from', on) // from <= on < to
        gt('to', on)
        eq('account', account)
      }

      return pa // can be null
   }

   // get currently active plan assoc, can be null
   static PlanAssociation active(Account account)
   {
      PlanAssociation.findByAccountAndState(account, PlanAssociation.states.ACTIVE)
   }

   // get currently inactive plan assoc, can be null
   static PlanAssociation inactive(Account account)
   {
      PlanAssociation.findByAccountAndState(account, PlanAssociation.states.INACTIVE)
   }
}
