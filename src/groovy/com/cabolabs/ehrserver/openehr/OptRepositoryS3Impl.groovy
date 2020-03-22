package com.cabolabs.ehrserver.openehr

// From openEHR-OPT
import com.cabolabs.openehr.opt.manager.OptRepository

import grails.util.Holders
import com.cabolabs.util.FileUtils

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

/**
 * Implements the interface to access OPT files via S3.
 */
class OptRepositoryS3Impl implements OptRepository {

   private AmazonS3 s3

   def OptRepositoryS3Impl(AmazonS3 s3)
   {
      this.s3 = s3
   }

   /**
    * location is a key used to reference one OPT, could be an absolute file path,
    * or an S3 object key. The namespace is already included in the location as a
    * prefix.
    * Returns null if no OPT was found.
    */
   String getOptContents(String location)
   {
      String opt_text

      try
      {
         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#getObjectAsString-java.lang.String-java.lang.String-
         opt_text = s3.getObjectAsString(
            Holders.config.aws.bucket,
            location // key
         )

         opt_text = FileUtils.removeBOM(opt_text.bytes)
      }
      catch (Exception e)
      {
         log.error "There was a problem getting OPT contents in S3 "+ e.message
         return false
      }

      return opt_text
   }

   /**
    * Does a search in the namespace and finds the first OPT that matches the templateId.
    * Returns null if no OPT was found.
    */
   String getOptContentsByTemplateId(String templateId, String namespace)
   {
      def normalizedTemplateId = templateId.normalizeStrangeCharacters().toCamelCase()
      def location = Holders.config.aws.folders.opt_repo.withTrailSeparator() +
                     namespace.withTrailSeparator() +
                     normalizedTemplateId + '.opt'
      return getOptContents(location)
   }

   /**
    * Returns the contents of all the OPTs under the given namespace.
    * Returns an empty list if there are no OPTs.
    */
   List<String> getAllOptContents(String namespace)
   {
      def result = []
      def list_objectSummary

      try
      {
         // similar to OptS3Service.getRepoSizeInBytesOrg
         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#listObjectsV2-java.lang.String-java.lang.String-
         def listObjectsV2Result = this.s3.listObjectsV2(
            Holders.config.aws.bucket,
            Holders.config.aws.folders.opt_repo.withTrailSeparator() + namespace.withTrailSeparator())

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/ListObjectsV2Result.html
         list_objectSummary = listObjectsV2Result.getObjectSummaries()
      }
      catch (Exception e)
      {
         log.error "There was a problem getting OPT contents in S3 "+ e.message
         return false
      }

      def key, contents
      list_objectSummary.each { s3ObjectSummary ->

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/S3ObjectSummary.html#getKey--
         key = s3ObjectSummary.getKey()

         // avoid returning .deleted and .old OPTs
         if (!key.endsWith('.opt')) return

         // gets the OPT contens by key
         contents = getOptContents(key)

         contents = FileUtils.removeBOM(contents.bytes)

         result << contents
      }

      return result
   }

   /**
    * Similar to getAllOptContents, but returns the key (location) of each OPT.
    */
   Map<String, String> getAllOptKeysAndContents(String namespace)
   {
      def result = [:]
      def list_objectSummary

      try
      {
         // similar to OptS3Service.getRepoSizeInBytesOrg
         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html#listObjectsV2-java.lang.String-java.lang.String-
         def listObjectsV2Result = this.s3.listObjectsV2(
            Holders.config.aws.bucket,
            Holders.config.aws.folders.opt_repo.withTrailSeparator() + namespace.withTrailSeparator())

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/ListObjectsV2Result.html
         list_objectSummary = listObjectsV2Result.getObjectSummaries()
      }
      catch (Exception e)
      {
         log.error "There was a problem getting OPT contents in S3 "+ e.message
         return false
      }

      def key, contents
      list_objectSummary.each { s3ObjectSummary ->

         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/S3ObjectSummary.html#getKey--
         key = s3ObjectSummary.getKey()

         // avoid returning .deleted and .old OPTs
         if (!key.endsWith('.opt')) return

         // gets the OPT contens by key
         contents = getOptContents(key)

         contents = FileUtils.removeBOM(contents.bytes)

         result[key] = contents
      }

      return result
   }
}
