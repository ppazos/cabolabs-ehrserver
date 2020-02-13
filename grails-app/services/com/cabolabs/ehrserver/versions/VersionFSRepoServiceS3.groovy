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

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

/**
 * Operations related to the file system based version repo.
 * @author Pablo Pazos <pablo.pazos@cabolabs.com>
 *
 */
class VersionFSRepoServiceS3 {

   def config = Holders.config.app
   def grailsApplication

   /*
    * Operations for the whole version repo.
    */
   def canWriteRepo()
   {
      return true
   }

   def repoExists()
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${grailsApplication.config.aws.accessKey}",
                                               "${grailsApplication.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${grailsApplication.config.aws.region}")
         .build()

      return s3.doesObjectExist(grailsApplication.config.aws.bucket, grailsApplication.config.aws.folders.version_repo)
   }

   /*
    * Operations for the version repo per organization.
    */
   def canWriteRepoOrg(String orguid)
   {
      return true
   }

   def repoExistsOrg(String orguid)
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${grailsApplication.config.aws.accessKey}",
                                               "${grailsApplication.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${grailsApplication.config.aws.region}")
         .build()

      return s3.doesObjectExist(grailsApplication.config.aws.bucket, grailsApplication.config.aws.folders.version_repo + orguid + '/')
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
      return getRepoSizeInBytesFiltered(orguid, filter_null)
   }

   def getRepoSizeInBytesBetween(String orguid, Date from, Date to)
   {
      return getRepoSizeInBytesFiltered(orguid, filter_file_last_modified_between.curry(from.time).curry(to.time))
   }

   private int getRepoSizeInBytesFiltered(String orguid, Closure filter)
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${grailsApplication.config.aws.accessKey}",
                                               "${grailsApplication.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${grailsApplication.config.aws.region}")
         .build()

      def s3Object = s3.getObject(
         grailsApplication.config.aws.bucket,
         grailsApplication.config.aws.folders.version_repo + orguid + '/')

      // TODO: not sure if this gives the size of the folder and contents
      return s3Object.getObjectMedatada().getContentLength()
   }


   def getRepoSizeInBytes()
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${grailsApplication.config.aws.accessKey}",
                                               "${grailsApplication.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${grailsApplication.config.aws.region}")
         .build()

      def s3Object = s3.getObject(
         grailsApplication.config.aws.bucket,
         grailsApplication.config.aws.folders.version_repo)

      // TODO: not sure if this gives the size of the folder and contents
      // check: https://stackoverflow.com/questions/15950032/calculate-s3-objectfolder-size-in-java
      return s3Object.getObjectMedatada().getContentLength()
   }

   /**
    * same as getRepoSizeInBytes but faster.
    */
   def getRepoSizeInBytesOrg(String orguid)
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${grailsApplication.config.aws.accessKey}",
                                               "${grailsApplication.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${grailsApplication.config.aws.region}")
         .build()

      def s3Object = s3.getObject(
         grailsApplication.config.aws.bucket,
         grailsApplication.config.aws.folders.version_repo + orguid + '/')

      // TODO: not sure if this gives the size of the folder and contents
      // check: https://stackoverflow.com/questions/15950032/calculate-s3-objectfolder-size-in-java
      return s3Object.getObjectMedatada().getContentLength()
   }

   /**
    * Sum of the version repos for all the orgs in the account.
    */
   def getRepoSizeInBytesAccount(Account account)
   {
      def total_size = 0
      account.organizations.each { org ->
         total_size += getRepoSizeInBytesOrg(org.uid)
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
      if (!repoExists() || !canWriteRepo())
      {
         throw new VersionRepoNotAccessibleException("Unable to write file ${config.aws.folders.version_repo}")
      }

      if (!repoExistsOrg(orguid))
      {
         throw new VersionRepoNotAccessibleException("Unable to write file ${config.aws.folders.version_repo + orguid}")
      }

      def f = new File(config.aws.folders.version_repo + orguid.withTrailSeparator() + version.fileUid +'.xml')
      if (!f.exists())
      {
         throw new FileNotFoundException("File ${f.absolutePath} doesn't exists")
      }
      return f.text
   }

   /**
    * Creates a version file that shouldnt exist
    * @param version_uid
    * @return
    */
   def createNewVersionFile(String orguid, Version version, String contents)
   {
      if (!repoExists() || !canWriteRepo())
      {
         throw new VersionRepoNotAccessibleException("Unable to write file ${config.aws.folders.version_repo}")
      }

      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${grailsApplication.config.aws.accessKey}",
                                               "${grailsApplication.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${grailsApplication.config.aws.region}")
         .build()

      // TODO: The orguid folder is created just the first time,
      // it might be better to create it whe nthe organization is created.
      if (!repoExistsOrg(orguid))
      {
         // Creates the orguid subfolder
         new File(config.aws.folders.version_repo + orguid).mkdir()
      }

      if (s3.doesObjectExist(grailsApplication.config.aws.bucket, grailsApplication.config.aws.folders.version_repo + orguid.withTrailSeparator() + version.fileUid +'.xml'))
      {
         throw new FileAlreadyExistsException("Object ${grailsApplication.config.aws.folders.version_repo + orguid.withTrailSeparator() + version.fileUid +'.xml'} already exists")
      }


      // creates empty object
      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#putObject-java.lang.String-java.lang.String-java.lang.String-
      def putObjectResult = s3.putObject(
         grailsApplication.config.aws.bucket,
         grailsApplication.config.aws.folders.version_repo + orguid.withTrailSeparator() + version.fileUid +'.xml',
         ''
      )

      // get created object
      def s3Object = s3.getObject(
         grailsApplication.config.aws.bucket,
         grailsApplication.config.aws.folders.version_repo + orguid.withTrailSeparator() + version.fileUid +'.xml')


      // FIXME: the caller expects File not S3Object
      //return returns3Object
   }
}
