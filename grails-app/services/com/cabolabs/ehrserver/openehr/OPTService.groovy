package com.cabolabs.ehrserver.openehr

import grails.transaction.Transactional
import grails.util.Holders
import com.cabolabs.ehrserver.account.Account

@Transactional
class OPTService {

   def config = Holders.config.app

   // TODO: refactor, these are the same as the ones in VersionFSRepoService, just the repo field changes.

   static File getOPTFile(String fileUid, String orguid)
   {
      return new File(Holders.config.app.opt_repo.withTrailSeparator() + orguid.withTrailSeparator() + fileUid +'.opt')
   }

   /**
    * size of opts in an org
    */
   def getRepoSizeInBytesOrg(String orguid)
   {
      def r = new File(config.opt_repo.withTrailSeparator() + orguid)
      return r.directorySize()
   }

   /**
    * Sum of the opt repos for all the orgs in the account.
    */
   def getRepoSizeInBytesAccount(Account account)
   {
      def total_size = 0
      account.organizations.each { org ->
         total_size += getRepoSizeInBytesOrg(org.uid)
      }
      return total_size
   }
}
