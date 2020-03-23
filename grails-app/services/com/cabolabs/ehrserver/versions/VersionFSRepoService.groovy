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

package com.cabolabs.ehrserver.versions

import grails.util.Holders
import java.io.FileNotFoundException
import java.nio.file.FileAlreadyExistsException
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.exceptions.VersionRepoNotAccessibleException
import com.cabolabs.ehrserver.account.Account
import groovy.util.slurpersupport.GPathResult

/**
 * Operations related to the file system based version repo.
 * @author Pablo Pazos <pablo.pazos@cabolabs.com>
 */
class VersionFSRepoService {

   def config = Holders.config.app

   /*
    * Operations for the whole version repo.
    */
   def canWriteRepo()
   {
      return new File(config.version_repo).canWrite()
   }

   def repoExists()
   {
      return new File(config.version_repo).exists()
   }

   /*
    * Operations for the version repo per organization.
    */
   def canWriteRepoOrg(String orguid)
   {
      return new File(config.version_repo.withTrailSeparator() + orguid).canWrite()
   }


   /**
    * The following closures are for reusing the code to calculate the
    * size of an org repo.
    */
   def filter_file_last_modified_between = { min, max, f ->
      return min <= f.lastModified() && f.lastModified() < max
   }

   def filter_null = {
      return true
   }

   def getRepoSizeInBytes(String orguid)
   {
      // faster without reading each file
      def r = new File(config.version_repo.withTrailSeparator() + orguid)
      return r.directorySize()
      //return getRepoSizeInBytesFiltered(orguid, filter_null)
   }

   def getRepoSizeInBytesBetween(String orguid, Date from, Date to)
   {
      return getRepoSizeInBytesFiltered(orguid, filter_file_last_modified_between.curry(from.time).curry(to.time))
   }

   private int getRepoSizeInBytesFiltered(String orguid, Closure filter)
   {
      def c = Version.createCriteria()
      def orgversions = c.list () {
         projections {
            property('fileLocation') // we want just the file location to get the file
         }
         contribution {
            eq('organizationUid', orguid)
         }
      }

      // if we add the size to the version on the DB we don't need to process the file system
      def v, size = 0
      orgversions.each { fileLocation ->
         //v = new File(config.version_repo.withTrailSeparator() + orguid.withTrailSeparator() + fileUid +'.xml')
         v = new File(fileLocation)

         if (filter.call(v))
         {
            size += v.length()
         }
      }

      return size
   }

   // this is not used, might be useful for a full admin to get the whole size
   // of the version repo for all accounts
   def getRepoSizeInBytes()
   {
      def r = new File(config.version_repo.withTrailSeparator())
      return r.directorySize()
   }

   /**
    * Sum of the version repos for all the orgs in the account.
    */
   def getRepoSizeInBytesAccount(Account account)
   {
      def total_size = 0
      account.organizations.each { org ->
         total_size += getRepoSizeInBytes(org.uid)
      }
      return total_size
   }

   /**
    * Gets a version file that should be on the repo.
    * @param version_uid
    * @return
    *
    * Note: the exception is declared to avoid groovy wrap it in an UndeclaredThrowableException
    * ref http://stackoverflow.com/questions/19987720/exception-thrown-from-service-not-being-caught-in-controller
    */
   def getExistingVersionContents(String orguid, Version version)
   {
      if (!repoExists() || !canWriteRepo())
      {
         throw new VersionRepoNotAccessibleException("Unable to write file ${config.version_repo}")
      }

      //def f = new File(config.version_repo.withTrailSeparator() + orguid.withTrailSeparator() + version.fileUid +'.xml')
      def f = new File(version.fileLocation)
      if (!f.exists())
      {
         throw new FileNotFoundException("File ${f.absolutePath} doesn't exists")
      }

      return f.text
   }

   /**
    * Gets a version file that shouldn't be on the repo.
    * @param version_uid
    * @return
    */
   def storeVersionContents(String orguid, Version version, GPathResult contents)
   {
      def fileLocation = newVersionFileLocation(orguid)

      // creates all parent subfolder if dont exist
      def containerFolder = new File(new File(fileLocation).getParent())
      containerFolder.mkdirs()

      // double check just in case
      if (!repoExists() || !canWriteRepo())
      {
         throw new VersionRepoNotAccessibleException("Unable to write file ${config.version_repo}")
      }

      // the file location is  set on the Version
      version.fileLocation = fileLocation

      def f = new File(fileLocation)
      if (f.exists())
      {
         throw new FileAlreadyExistsException("File ${f.absolutePath} already exists")
      }

      // FIXME: check if the XML has the namespace declarations of the root node from the commit
      // FIXME: this is XML only, if we want to store the JSON versions we need to check
      //        the content type of the commit also, this will be required for implementing
      //        the openEHR API where XML to JSON transformations are not direct
      f << groovy.xml.XmlUtil.serialize(contents)
   }

   def newVersionFileLocation(String orguid)
   {
      // TODO: this is XML only, for JSON versions we need to consider the content type of the commit adding a new parameter
      return config.version_repo.withTrailSeparator() + orguid.withTrailSeparator() + java.util.UUID.randomUUID() +'.xml'
   }
}
