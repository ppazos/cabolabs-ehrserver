package com.cabolabs.ehrserver.openehr

import grails.transaction.Transactional
import grails.util.Holders
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.account.Account
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.openehr.opt.manager.OptManager

//@Transactional
class OptFSService {

   def operationalTemplateIndexer

   // TODO: refactor, these are the same as the ones in VersionFSRepoService, just the repo field changes.

   String getOPTContents(OperationalTemplateIndex opt, int max_bytes_to_return = 1000000)
   {
      // avoid returning files that are too big
      if (new File(opt.fileLocation).length() > max_bytes_to_return)
      {
         return
      }
      return new File(opt.fileLocation).text
   }

   boolean storeOPTContents(String fileLocation, String fileContents)
   {
      // creates parent subfolders if dont exist
      // parent is the organization folder
      def containerFolder = new File(new File(fileLocation).getParent())
      containerFolder.mkdirs()

      File fileDest = new File(fileLocation)

      if (fileDest.exists())
      {
         log.warn "File "+ fileLocation +" already exists, overwriting"
      }

      fileDest.write(fileContents) // overwrites if file exists

      return true // TODO check errors
   }

   // def newOPTFileLocation(String orguid)
   // {
   //    return Holders.config.app.opt_repo.withTrailSeparator() + orguid.withTrailSeparator() + java.util.UUID.randomUUID() +'.opt'
   // }

   def newOPTFileLocation(String orguid, String template_id)
   {
      //String normalized_template_id = template_id.normalizeStrangeCharacters().toCamelCase()
      //return Holders.config.app.opt_repo.withTrailSeparator() + orguid.withTrailSeparator() + normalized_template_id +'.opt'
      if (!isNormalizedTemplateId(template_id))
      {
         template_id = normalizeTemplateId(template_id)
      }

      Holders.config.app.opt_repo.withTrailSeparator() +
      orguid.withTrailSeparator() +
      template_id + '.opt'
   }

   // from openEHR-OPT OptRepositoryFSImpl
   // TODO: refactor
   String normalizeTemplateId(String templateId)
   {
      // https://gist.github.com/ppazos/12f3efc4eb178e43ff73a0c989a2e1d7
      String normalized = java.text.Normalizer.normalize(templateId, java.text.Normalizer.Form.NFD).replaceAll(/\p{InCombiningDiacriticalMarks}+/, '')
      // The issue with sname is for ABCDE template it generates a_b_c_d_e_template
      //String snake      = normalized.replaceAll( / ([A-Z])/, /$1/ ).replaceAll( /([A-Z])/, /_$1/ ).replaceAll(/\s/, '_').toLowerCase().replaceAll( /^_/, '' )

      // lowercase, no spaces, remove non (letters or numbers), remove beginning/ending underscores if there is any
      String snake      = normalized.toLowerCase().replaceAll(/\s/, '_').replaceAll(/[^a-zA-Z0-9]+/,'_').replaceAll( /^_/, '' ).replaceAll( /_$/, '' )
      String removeDots = snake.replaceAll(/\./, '_')
      //String plusLAndV  = removeDots +'.'+ language +'.v1'
      //return plusLAndV
      return removeDots
   }

   // from openEHR-OPT OptRepositoryFSImpl
   // TODO: refactor
   boolean isNormalizedTemplateId(String templateId)
   {
      // very strict regex doesnt allow hyphens or parentheses in the name
      //(templateId ==~ /([a-z]+(_[a-z0-9]+)*)\.([a-z]{2})\.v([0-9]+[0-9]*(\.[0-9]+[0-9]*(\.[0-9]+[0-9]*)?)?)/)

      // relaxed regex only checks it ends with .en.v1
      //(templateId ==~ /.*\.([a-z]{2})\.v([0-9]+[0-9]*(\.[0-9]+[0-9]*(\.[0-9]+[0-9]*)?)?)$/)

      // just checks snake case
      (templateId ==~ /^([a-z]+(_[a-z0-9]+)*)$/)
   }

   def moveOldVersion(OperationalTemplateIndex old_version_opt)
   {
      def old_version_file = new File(old_version_opt.fileLocation)
      old_version_file.renameTo(
         new File(
            old_version_opt.fileLocation +
            '.r'+ old_version_opt.versionNumber +'.old' // needs the version number to avoid name conflicts
         )
      )
   }

   def emptyTrash(Organization org)
   {
      def opts = OperationalTemplateIndex.forOrg(org).deleted.list()
      opts.each { opt ->

         // move file to deleted folder, we don't actually delete the file, just in case!
         def deleted_file = new File(opt.fileLocation)
         def moved = deleted_file.renameTo(
            new File(
               opt.fileLocation +'.'+ Date.nowInIsoBasicUtc() + '.deleted' // timestamp is needed to avoid name collisions
            )
         )
         if (!moved) println "DELETED OPT NOT MOVED!"

         // deletes OPT and references from DB
         operationalTemplateIndexer.deleteOptReferences(opt, true)

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

      // no OPTs where uploaded yet, the folders are created with the first upload
      if (!r.exists()) return 0

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
