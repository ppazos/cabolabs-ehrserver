package com.cabolabs.ehrserver.openehr

import grails.transaction.Transactional
import grails.util.Holders
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.account.Account
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.archetype.OperationalTemplateIndexer

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

// @Transactional
class OptS3Service {

   // TODO: refactor, these are the same as the ones in VersionFSRepoService, just the repo field changes.

   String getOPTContents(OperationalTemplateIndex opt)
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${Holders.config.aws.accessKey}",
                                               "${Holders.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${Holders.config.aws.region}")
         .build()

      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#getObjectAsString-java.lang.String-java.lang.String-
      return s3.getObjectAsString(
         Holders.config.aws.bucket,
         opt.fileLocation // key
      )
   }

   boolean storeOPTContents(String fileLocation, String fileContents)
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${Holders.config.aws.accessKey}",
                                               "${Holders.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${Holders.config.aws.region}")
         .build()

      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#putObject-java.lang.String-java.lang.String-java.lang.String-
      def putObjectResult = s3.putObject(
         Holders.config.aws.bucket,
         fileLocation,
         fileContents
      )

      return true // TODO check errors
   }

   def newOPTFileLocation(String orguid)
   {
      return Holders.config.aws.folders.opt_repo + orguid.withTrailSeparator() + java.util.UUID.randomUUID() +'.opt'
   }

   def newOPTFileLocation(String orguid, String givenUid)
   {
      return Holders.config.aws.folders.opt_repo + orguid.withTrailSeparator() + givenUid +'.opt'
   }

   def moveOldVersion(OperationalTemplateIndex old_version_opt)
   {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${Holders.config.aws.accessKey}",
                                               "${Holders.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${Holders.config.aws.region}")
         .build()

      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#getObject-java.lang.String-java.lang.String-
      def s3Object = s3.getObject(
         Holders.config.aws.bucket,
         old_version_opt.fileLocation // key
      )

      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/S3Object.html#setKey-java.lang.String-
      s3Object.setKey(old_version_opt.fileLocation + '.old')
   }

   def emptyTrash(Organization org)
   {
      def ti = new OperationalTemplateIndexer()
      def opts = OperationalTemplateIndex.forOrg(org).deleted.list()

      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${Holders.config.aws.accessKey}",
                                               "${Holders.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${Holders.config.aws.region}")
         .build()

      def s3Object

      opts.each { opt ->

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#getObject-java.lang.String-java.lang.String-
         s3Object = s3.getObject(
            Holders.config.aws.bucket,
            opt.fileLocation // key
         )

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/S3Object.html#setKey-java.lang.String-
         s3Object.setKey(opt.fileLocation + '.deleted')

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
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${Holders.config.aws.accessKey}",
                                               "${Holders.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${Holders.config.aws.region}")
         .build()

      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#listObjectsV2-java.lang.String-java.lang.String-
      def listObjectsV2Result = s3.listObjectsV2(
         Holders.config.aws.bucket,
         Holders.config.aws.folders.opt_repo + orguid + '/')

      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/ListObjectsV2Result.html
      def list_objectSummary = listObjectsV2Result.getObjectSummaries()

      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/S3ObjectSummary.html
      def total = 0
      list_objectSummary.each { s3ObjectSummary ->

         if (filter.call(s3ObjectSummary))
         {
            total += s3ObjectSummary.getSize()
         }
      }

      return total
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
