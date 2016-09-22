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

   static constraints = {
     period( inList: periods.values() as List)
   }
   
   def associate(Organization org)
   {
      // TODO: check the org doesn't have an active plan
      def from = DateUtils.toFirstDateOfMonth(new Date()) // plans go from the first day of a month
      def pa = new PlanAssociation(organizationUid: org.uid, from: from, to: from+365, plan:this)
      pa.save(failOnError: true)
   }
   
   /**
    * Gets the active plan for the organization.
    */
   static PlanAssociation active(Organization org)
   {
      def pa = PlanAssociation.withCriteria(uniqueResult: true) {
        def now = new Date()
        lte('from', now)
        gt('to', now)
        eq('organizationUid', org.uid)
      }
      
      return pa // can be null
   }
   
   static PlanAssociation activeOn(Organization org, Date on)
   {
      def pa = PlanAssociation.withCriteria(uniqueResult: true) {
        lte('from', on)
        gt('to', on)
        eq('organizationUid', org.uid)
      }
      
      return pa // can be null
   }
}
