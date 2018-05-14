package com.cabolabs.ehrserver.account

import com.cabolabs.security.User

class PlanAssociationStateUpdateJob {

   def concurrent = false
   def mailService
   def grailsApplication

   static triggers = {
     simple repeatInterval: 60000l, startDelay: 240000l // runs each 12 hours 1000*60*60*12 = 43200000l
   }

   def execute() {
      log.info('updating plan association states per account')

      // TODO: move logic to service for testability
      def active_plan_assoc
      def inactive_plan_assoc
      def today = new Date() //.clearTime()

      Account.list().each { account ->

         // Currently active
         active_plan_assoc = PlanAssociation.findByAccountAndState(account, PlanAssociation.states.ACTIVE) //account.currentPlan

         //println "active_plan_assoc "+ active_plan_assoc

         // If there is no active plan, check for inactive starting today and change that to active,
         // else do nothing, the installation might not be using plans at all.
         // The first case applies when a the first plan is assigned to the account, it is inactive
         // by default, and this job makes it active.
         if (!active_plan_assoc)
         {
            inactive_plan_assoc = PlanAssociation.findByAccountAndState(account, PlanAssociation.states.INACTIVE)
            if (inactive_plan_assoc)
            {
               if (inactive_plan_assoc.from <= today)
               {
                  inactive_plan_assoc.state = PlanAssociation.states.ACTIVE
                  inactive_plan_assoc.save(failOnError: true)
               }
            }

            return // continues with next account
         }

         // current active plan ended?
         if (active_plan_assoc.to < today)
         {
            //println "active_plan_assoc.to < today "+ active_plan_assoc.to +" < "+ today

            // has inactive plan for today?
            inactive_plan_assoc = PlanAssociation.findByAccountAndState(account, PlanAssociation.states.INACTIVE)
            //println "inactive_plan_assoc "+ inactive_plan_assoc

            if (inactive_plan_assoc)
            {
               // if should start today! if not we have <current active> pediod_of_time <new inactive>
               // and there shouldn't be gaps between active and future inactive plans,
               // TODO: need to add a check for that on the account edit.
               if (today < inactive_plan_assoc.from)
               {
                  log.error('inactive plan start date is set in the future and current active plan ends today')
               }
               else
               {
                  active_plan_assoc.state = PlanAssociation.states.CLOSED
                  inactive_plan_assoc.state = PlanAssociation.states.ACTIVE

                  active_plan_assoc.save(failOnError: true)
                  inactive_plan_assoc.save(failOnError: true)
               }
            }
            else // current plan ends and there is no new inactive plan to take it's place
            {
               // TODO: limit emails to admins
               // TODO: add feature for auto-renew plans

               // For now we don't close the current plan automatically, we'll need to send some notifications to the account
               // contact before closing the plan. TODO.
               //active_plan_assoc.state = PlanAssociation.states.CLOSED

               def message = "The account "${account.companyName}" has an active plan that ends today but no new plan was assigned.<br/><br/>Need to contact ${account.contact.email} to verify a plan extension, upgrade or closing the account."
               def admins = User.allForRole('ROLE_ADMIN')
               def title = 'Account plan expiration'
               admins.each { admin ->
                  mailService.sendMail {
                     from    grailsApplication.config.grails.mail.default.from
                     to      admin.email
                     subject title
                     html    view: "/messaging/email",
                             model: [title: title, preview: '', salute: '',
                                    message: message, actions: '', closing: '', bye: '']
                  }
               }
            }
         }
         else
         {
            //println "active_plan_assoc.to >= today "+ active_plan_assoc.to +" >= "+ today
         }
      }
   }
}
