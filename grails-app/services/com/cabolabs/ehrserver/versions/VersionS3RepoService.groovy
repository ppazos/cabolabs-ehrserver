/*
 * Copyright 2011-2020 CaboLabs Health Informatics
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

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import javax.annotation.PostConstruct

/**
 * Operations related to the file system based version repo.
 * @author Pablo Pazos <pablo.pazos@cabolabs.com>
 *
 */
class VersionS3RepoService {

   def config = Holders.config.app
   def grailsApplication
   AmazonS3 s3

   // this initalizes the S3 connection when the service is created
   @PostConstruct
   def init()
   {
      //your initialization code goes here. e.g connect to some Messaging Service

      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${Holders.config.aws.accessKey}",
                                               "${Holders.config.aws.secretKey}")

      this.s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${Holders.config.aws.region}")
         .build()
   }

   /*
    * Operations for the whole version repo.
    */
   def canWriteRepo()
   {
      return this.s3.doesBucketExistV2(Holders.config.aws.bucket)
   }

   def repoExists()
   {
      return this.s3.doesObjectExist(Holders.config.aws.bucket, Holders.config.aws.folders.version_repo)
   }

   /*
    * Operations for the version repo per organization.
    */
   def canWriteRepoOrg(String orguid)
   {
      // TODO
      return true
   }


   /**
    * The following closures are for reusing the code to calculate the
    * size of an org repo.
    */
   def filter_file_last_modified_between = { min, max, s3ObjectSummary ->
      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/S3ObjectSummary.html#getLastModified--
      return min <= s3ObjectSummary.getLastModified().time && s3ObjectSummary.getLastModified().time < max
   }

   def filter_null = {
      return true
   }

   def getRepoSizeInBytes(String orguid)
   {
      return getRepoSizeInBytesFiltered(orguid, filter_null)
   }

   def getRepoSizeInBytesBetween(String orguid, Date from, Date to)
   {
      return getRepoSizeInBytesFiltered(orguid, filter_file_last_modified_between.curry(from.time).curry(to.time))
   }

   private int getRepoSizeInBytesFiltered(String orguid, Closure filter)
   {
      def total = 0
      def list_objectSummary
      try
      {
         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#listObjectsV2-java.lang.String-java.lang.String-
         def listObjectsV2Result = this.s3.listObjectsV2(
            Holders.config.aws.bucket,
            Holders.config.aws.folders.version_repo + orguid + '/'
         )

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/ListObjectsV2Result.html
         list_objectSummary = listObjectsV2Result.getObjectSummaries()

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/S3ObjectSummary.html
         list_objectSummary.each { s3ObjectSummary ->

            if (filter.call(s3ObjectSummary))
            {
               total += s3ObjectSummary.getSize()
            }
         }
      }
      catch (Exception e)
      {
         log.error "There was a problem getting version summaries in S3 "+ e.message
      }

      return total
   }


   // this is not used, might be useful for a full admin to get the whole size
   // of the version repo for all accounts
   def getRepoSizeInBytes()
   {
      return 0
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
   def getExistingVersionContents(String orguid, Version version) throws FileNotFoundException, VersionRepoNotAccessibleException
   {
      def contents

      if (!repoExists() || !canWriteRepo())
      {
         throw new VersionRepoNotAccessibleException("Unable to write object ${Holders.config.aws.folders.version_repo}")
      }

      try
      {
         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#getObjectAsString-java.lang.String-java.lang.String-
         contents = this.s3.getObjectAsString(
            Holders.config.aws.bucket,
            version.fileLocation // key
         )
      }
      catch (Exception e)
      {
         log.error "There was a problem getting version contents in S3 "+ e.message
      }

      return contents
   }

   /**
    * Creates a version file that shouldnt exist
    * @param version_uid
    * @return
    */
   def storeVersionContents(String orguid, Version version, GPathResult contents)
   {
      if (!repoExists() || !canWriteRepo())
      {
         throw new VersionRepoNotAccessibleException("Unable to write object ${Holders.config.aws.folders.version_repo}")
      }

      def fileLocation = newVersionFileLocation(orguid)

      // if (s3.doesObjectExist(grailsApplication.config.aws.bucket, grailsApplication.config.aws.folders.version_repo + orguid.withTrailSeparator() + version.fileUid +'.xml'))
      // {
      //    throw new FileAlreadyExistsException("Object ${grailsApplication.config.aws.folders.version_repo + orguid.withTrailSeparator() + version.fileUid +'.xml'} already exists")
      // }

      // the file location is  set on the Version
      version.fileLocation = fileLocation

      // FIXME: check if the XML has the namespace declarations of the root node from the commit
      // FIXME: this is XML only, if we want to store the JSON versions we need to check
      //        the content type of the commit also, this will be required for implementing
      //        the openEHR API where XML to JSON transformations are not direct
      String text = groovy.xml.XmlUtil.serialize(contents)

      try
      {
         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#putObject-java.lang.String-java.lang.String-java.lang.String-
         def putObjectResult = this.s3.putObject(
            Holders.config.aws.bucket,
            fileLocation,
            text
         )
      }
      catch (Exception e)
      {
         log.error "There was a problem storing version in S3 "+ e.message
         return false
      }
   }

   def newVersionFileLocation(String orguid)
   {
      // TODO: this is XML only, for JSON versions we need to consider the content type of the commit adding a new parameter
      return Holders.config.aws.folders.version_repo + orguid.withTrailSeparator() + java.util.UUID.randomUUID() +'.xml'
   }
}
