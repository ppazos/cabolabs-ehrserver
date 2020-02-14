package com.cabolabs.ehrserver.openehr

import grails.transaction.Transactional
import grails.util.Holders
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.account.Account
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.archetype.OperationalTemplateIndexer

@Transactional
class OPTService {

   // TODO: refactor, these are the same as the ones in VersionFSRepoService, just the repo field changes.

   static String getOPTContents(OperationalTemplateIndex opt)
   {
      return new File(opt.fileLocation).text
   }

   static String newOPTFileLocation(String orguid)
   {
      // TODO: this is XML only, for JSON versions we need to consider the content type of the commit adding a new parameter
      return Holders.config.app.opt_repo.withTrailSeparator() + orguid.withTrailSeparator() + java.util.UUID.randomUUID() +'.opt'
   }

   static String newOPTFileLocation(String orguid, String givenUid)
   {
      // TODO: this is XML only, for JSON versions we need to consider the content type of the commit adding a new parameter
      return Holders.config.app.opt_repo.withTrailSeparator() + orguid.withTrailSeparator() + givenUid +'.opt'
   }

   def moveOldVersion(OperationalTemplateIndex old_version_opt)
   {
      def old_version_file = new File(old_version_opt.fileLocation)
      old_version_file.renameTo(new File(old_version_opt.fileLocation + '.old'))
   }

   def emptyTrash(Organization org)
   {
      def ti = new OperationalTemplateIndexer()
      def opts = OperationalTemplateIndex.forOrg(org).deleted.list()
      opts.each { opt ->

         // move file to deleted folder, we don't actually delete the file, just in case!
         def deleted_file = new File(opt.fileLocation)
         def moved = deleted_file.renameTo(new File(opt.fileLocation + '.deleted'))
         if (!moved) println "DELETED OPT NOT MOVED!"

         // deletes OPT and references from DB
         ti.deleteOptReferences(opt, true)

         // load opt in manager cache
         // TODO: just unload the deleted OPT
         def optMan = OptManager.getInstance()
         optMan.unloadAll(org.uid)
         optMan.loadAll(org.uid, true)
      }
   }

   /**
    * size of opts in an org
    */
   def getRepoSizeInBytesOrg(String orguid)
   {
      def r = new File(Holders.config.app.opt_repo.withTrailSeparator() + orguid)
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
