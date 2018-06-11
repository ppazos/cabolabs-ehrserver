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

import grails.transaction.Transactional
import com.cabolabs.security.*
import com.cabolabs.util.DateParser
import grails.util.Holders

@Transactional(readOnly = true)
class AccountController {

   def notificationService
   def organizationService
   def config = Holders.config.app

   // Only admins can see the list of all the Accounts, each AccountManager
   // will see just his Account, other Roles won't have access to the Account.
   // Permissions are checked in the SecurityFilter.
   def index()
   {
      def accounts = Account.list()
      [accounts: accounts]
   }

   def show(Long id)
   {
      if (!id)
      {
         flash.message = message(code:'account.show.idIsRequired')
         redirect(url:request.getHeader('referer'))
         return
      }

      def account = Account.get(id)
      if (!account)
      {
         flash.message = message(code:'account.show.accountNotFound')
         redirect(url:request.getHeader('referer'))
         return
      }

      def plan_max_orgs
      def plan_assoc = Plan.associatedNow(account) // can be null in dev env, on this case, no constraints apply to org creation
      if (plan_assoc)
      {
         plan_max_orgs = plan_assoc.plan.max_organizations
      }

      [account: account, plan_max_orgs: plan_max_orgs]
   }

   def create()
   {
   }

   /**
    * Creates the account manager user, the account, the organization, and the user role.
    */
   @Transactional
   def save(Account account, String username, String email, String organization)
   {
      if (!account)
      {
         flash.message = message(code:'account.save.empty.error')
         render (view: 'create')
         return
      }


      // 1. Account setup: create account manager user
      def accman = new User(username: username, email: email, enabled: false)
      accman.setPasswordToken()
      account.contact = accman

      if (!accman.save(flush: true))
      {
         flash.message = message(code:'account.contact.save.error')
         render (view: 'create', model: [account: account])
         return
      }


      // 2. Account setup: create account
      if (!account.validate())
      {
         flash.message = message(code:'account.save.error')
         render (view: 'create', model: [account: account])
         return
      }


      // 3. Account setup: create organization
      // saves the account
      def org = organizationService.create(account, organization, false)

      /*
      if (!org.validate())
      {
         println org.errors
         flash.message = message(code:'account.organization.validate.error')
         render (view: 'create', model: [account: account, organization: org])
         return
      }
      */

      // THESE SHOULD BE IN THE organizationService.create

      // 4. Account setup: get ACCMAN role
      def accmanRole = Role.findByAuthority(Role.AM)

      // 5. Account setup: create user role association
      UserRole.create( accman, accmanRole, org, true )



      // send password reset email to the account manager
      // TODO: schedule emails
      // token to create the URL for the email is in the userInstance
      notificationService.sendUserRegisteredOrCreatedEmail( accman.email, [accman] )


      flash.message = message(code:'account.save.ok', args:[account.id])
      redirect action:'show', id: account.id
   }

   def edit(Account account)
   {
      if (!account)
      {
         flash.message = message(code:'account.edit.empty.error')
         render (view: 'index')
         return
      }

      [account: account]
   }

   /**
    * If plan_id is not null, the admin wants to set a plan to the account that
    * will be activated on plan_date_start (yyyy-MM-dd).
    */
   @Transactional
   def update(Account account, Long plan_id, String plan_date_start, String plan_date_end)
   {
      if (!account)
      {
         flash.message = message(code:'account.update.empty.error')
         render (view: 'edit')
         return
      }

      if (!account.save(flush:true))
      {
         flash.message = message(code:'account.update.error')
         render (view: 'edit', model: [account: account])
         return
      }


      // TODO: allow changing plan in the middle, need to prorrate by hand for now.


      // Want to associate a new plan
      if (plan_id)
      {
         // Check rules on https://github.com/ppazos/cabolabs-ehrserver/issues/906
         def inactive_plan = PlanAssociation.findByAccountAndState(account, PlanAssociation.states.INACTIVE)
         if (inactive_plan)
         {
           flash.message = message(code:'account.update.cantAssignNewPlanInactivePlanAlreadyExists')
           render (view: 'edit', model: [account: account])
           return
         }

         def plan = Plan.get(plan_id)
         if (!plan)
         {
            flash.message = message(code:'account.update.planNotFound')
            render (view: 'edit', model: [account: account])
            return
         }

         def from_date = new Date() // if no date was set, today is the start date
         if (plan_date_start)
         {
            try
            {
               from_date = DateParser.tryParse(plan_date_start)
            }
            catch (Exception e)
            {
               log.error( e.message )
               flash.message = message(code:'account.update.invalidPlanDateStart')
               render (view: 'edit', model: [account: account])
               return
            }
         }

         // association should start at 00:00 on the start date
         from_date.clearTime()

         // Do not allow to assign a new plan starting in the past
         // https://github.com/ppazos/cabolabs-ehrserver/issues/907
         if (from_date < new Date().clearTime())
         {
            flash.message = message(code:'account.update.newPlanCantStartInThePast')
            render (view: 'edit', model: [account: account])
            return
         }

         def to_date = from_date + 365 // default plan duration = 1Y
         if (plan_date_end)
         {
            try
            {
               to_date = DateParser.tryParse(plan_date_end)
            }
            catch (Exception e)
            {
               log.error( e.message )
               flash.message = message(code:'account.update.invalidPlanDateEnd')
               render (view: 'edit', model: [account: account])
               return
            }
         }

         // get current account plan, can be null if none
         // exists or if the expiry date already passed
         def plan_association = Plan.associatedNow(account)
         if (plan_association)
         {
            plan_association.to = from_date // current plan ends when the new starts
         }

         // if the current plan end date is older than today, close the plan,
         // if the current plan end date is in the future, it should be closed when that date arrives, need a
         // job to check daily if a new plan should be active and old plan should be closed.

/* The chck for active -> closed is done on the PlanAssociationStateUpdateJob
         if (plan_association.to < new Date())
         {
            println "A) active_plan_assoc.to < today "+ active_plan_assoc.to +" < "+ today
            plan_association.state = PlanAssociation.states.CLOSED
         }
*/

         try
         {
            plan.associate(account, from_date, to_date - from_date) // 3rd param is duration in days
         }
         catch (Exception e)
         {
            log.error( e.message )
            flash.message = message(code:'account.update.errorAssigningPlan')
            render (view: 'edit', model: [account: account])
            return
         }
      }

      flash.message = message(code:'account.update.ok', args:[account.id])
      redirect action:'show', id: account.id
   }

   // TODO: delete, we need to define rules

}
