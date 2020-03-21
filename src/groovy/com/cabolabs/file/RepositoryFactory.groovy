package com.cabolabs.file

import grails.util.Holders

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import com.cabolabs.openehr.opt.manager.OptRepository
import com.cabolabs.ehrserver.openehr.OptRepositoryS3Impl
import com.cabolabs.openehr.opt.manager.OptRepositoryFSImpl

class RepositoryFactory {

   private static instance = new RepositoryFactory()

   static def optService = Holders.grailsApplication.mainContext.getBean 'optService'

   private RepositoryFactory() {

   }

   static RepositoryFactory getInstance()
   {
      return instance
   }

   OptRepository getOPTRepository()
   {
      def repo
      if (optService instanceof com.cabolabs.ehrserver.openehr.OptFSService) // File System Config
      {
         repo = new OptRepositoryFSImpl(Holders.config.app.opt_repo.withTrailSeparator())
      }
      else if (optService instanceof com.cabolabs.ehrserver.openehr.OptS3Service) // S3 Config
      {
         BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                                 "${Holders.config.aws.accessKey}",
                                                 "${Holders.config.aws.secretKey}")

         AmazonS3 s3 = AmazonS3ClientBuilder.standard()
           .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
           .withRegion("${Holders.config.aws.region}")
           .build()

         repo = new OptRepositoryS3Impl(s3)
      }
      else
      {
         throw new Exception("OPT Service not configured")
      }

      return repo
   }

   // TODO: add other repos here

}
