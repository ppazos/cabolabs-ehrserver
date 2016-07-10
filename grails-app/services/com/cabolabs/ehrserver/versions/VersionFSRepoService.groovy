package com.cabolabs.ehrserver.versions

import grails.util.Holders
import com.cabolabs.ehrserver.openehr.common.change_control.Version

/**
 * Operations related to the file system based version repo.
 * @author Pablo Pazos <pablo.pazos@cabolabs.com>
 *
 */
class VersionFSRepoService {
   
   def config = Holders.config.app

   def getRepoSizeInBytes()
   {
      def r = new File(config.version_repo)
      return r.directorySize()
   }
   
   def getRepoSizeInBytes(String orguid)
   {
      def c = Version.createCriteria()
      def orgversions = c.list () {
         projections {
            property('uid')
         }
         contribution {
            eq('organizationUid', orguid)
         }
      }
      
      // if we add the size to the version on the DB we don't need to process the file system
      def v, size = 0
      orgversions.each { version_uid ->
         v = new File(config.version_repo + version_uid.replaceAll('::', '_') +'.xml')
         size += v.length()
      }
      
      return size
   }
}
