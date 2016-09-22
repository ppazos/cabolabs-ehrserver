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
      if (!to)
      {
         def cal = Calendar.getInstance()
         //cal.add(Calendar.MONTH, -1)
         cal.set(Calendar.DATE, 1)
         
         from = cal.getTimeInMillis()
         
         cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE))
         
         to = cal.getTimeInMillis()
      }
   
      def dfrom = new Date(from)
      def dto   = new Date(to)
   
      // Number of transactions
      def contributions = Contribution.withCriteria {
        
        projections {
           count()
        }
        
        eq('organizationUid', uid)
        audit {
          between('timeCommitted', dfrom, dto)
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
            between('timeCommitted', dfrom, dto)
         }
      }
      
      def size = versionFSRepoService.getRepoSizeInBytesBetween(uid, dfrom, dto)
      
      // Active plan for the orgazination
      def org = Organization.findByUid(uid)
      def plan_association = Plan.activeOn(org, dfrom)
      
      if (plan_association)
      {
         [transactions: contributions[0], documents: versions[0], size: size, plan: plan_association.plan, plan_association: plan_association, from: from, to: to]
      }
      else
      {
         render message(code:'stats.no_active_plan')
      }
   }
}
