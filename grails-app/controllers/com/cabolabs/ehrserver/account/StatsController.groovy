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
