package com.cabolabs.ehrserver.versions

import grails.util.Holders
import java.io.FileNotFoundException
import java.nio.file.FileAlreadyExistsException
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
   
   /**
    * Gets a version file that should be on the repo.
    * @param version_uid
    * @return
    * 
    * Note: the exception is declared to avoid groovy wrap it in an UndeclaredThrowableException
    * ref http://stackoverflow.com/questions/19987720/exception-thrown-from-service-not-being-caught-in-controller
    */
   def getExistingVersionFile(String version_uid) throws FileNotFoundException
   {
      def f = new File(config.version_repo + version_uid.replaceAll('::', '_') +'.xml')
      if (!f.exists())
      {
         throw new FileNotFoundException("File ${f.path} doesn't exists")
      }
      return f
   }
   
   /**
    * Gets a version file that shouldn't be on the repo.
    * @param version_uid
    * @return
    */
   def getNonExistingVersionFile(String version_uid) throws FileAlreadyExistsException
   {
      def f = new File(config.version_repo + version_uid.replaceAll('::', '_') +'.xml')
      if (f.exists())
      {
         throw new FileAlreadyExistsException("File ${f.path} already exists")
      }
      return f
   }
}
