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

package app

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.versions.VersionFSRepoService
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import grails.plugin.springsecurity.SpringSecurityUtils

import com.cabolabs.security.User
import com.cabolabs.security.UserRole
import com.cabolabs.ehrserver.notification.Notification
import com.cabolabs.ehrserver.reporting.ActivityLog

// test S3
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

class AppController {

   def springSecurityService
   def versionRepoService
   def optService
   def remoteNotificationsService

   // shows main dashboard
   def index()
   {
      // test S3
      /*
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                                               "${grailsApplication.config.aws.accessKey}",
                                               "${grailsApplication.config.aws.secretKey}")

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
         .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
         .withRegion("${grailsApplication.config.aws.region}")
         .build()

      // List buckets
      // def buckets = s3.listBuckets()
      // println "Your Amazon S3 buckets are:"
      // buckets.each { b ->
      //    println "* " + b.getName()
      // }

      // List objects
      def result = s3.listObjectsV2(grailsApplication.config.aws.bucket)
      def objects = result.getObjectSummaries()
      objects.each { os ->
         println "* " + os.getKey() +" "+ os.getSize()
      }

      if (s3.doesObjectExist(grailsApplication.config.aws.bucket, 'versions/'))
         println "versions exist"
      else
         println "versions doesnt exist"


      // test create object
      def putObjectResult = s3.putObject(
         grailsApplication.config.aws.bucket,
         grailsApplication.config.aws.folders.version_repo + "test/" + '3453435434.xml',
         'test content'
      )

      if (s3.doesObjectExist(
            grailsApplication.config.aws.bucket,
            grailsApplication.config.aws.folders.version_repo + "test/" + '3453435434.xml'))
         println "objecgt exists"
      else
         println "object doesnt exist"
      */



      // String fileName = 'commits' //Save this for future reference.
      // File file = {{FILE}}
      // s3.putObject(bucketName, fileName, file)

      // /test S3

      // Count EHRs
      def count_ehrs, count_contributions, count_queries, count_users
      def version_repo_sizes = [:] // org => versio repo size
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         count_ehrs = Ehr.count()
         count_contributions = Contribution.count()
         count_queries = Query.count()
         count_users = User.count()

         def orgs = Organization.list()

         orgs.each { __org ->
            version_repo_sizes << [(__org): versionRepoService.getRepoSizeInBytes(__org.uid) + optService.getRepoSizeInBytesOrg(__org.uid)]
         }

         // sort by usage, decreasing
         version_repo_sizes = version_repo_sizes.sort { -it.value }
      }
      else
      {
         def org = session.organization

         count_ehrs = Ehr.countByOrganizationUid(org.uid)
         count_contributions = Contribution.countByOrganizationUid(org.uid)

         def shares = QueryShare.findAllByOrganization(org)
         def c = Query.createCriteria()
         count_queries = c.count() {
            if (shares)
            {
               or {
                  eq('isPublic', true)
                  'in'('id', shares.query.id)
               }
            }
            else
            {
               eq('isPublic', true)
            }
         }

         def ur = UserRole.createCriteria()
         def urs = ur.list() {
            createAlias('user','user')
            projections {
               property("user.id", "user.id")
            }
            eq('organization', org)
         }
         count_users = urs.unique().size()

         version_repo_sizes << [(org): versionRepoService.getRepoSizeInBytes(org.uid) + optService.getRepoSizeInBytesOrg(org.uid)]
      }

      // ----------------------------------------------------------------------------------------------------------
      // Check for remote notifications
      // This is here because right after the login, the user goes to the dashboard.

      def loggedInUser = springSecurityService.currentUser

      // Get date of the last read of remote notifications by the current user, null if no reads were done
      // This avoids reading the same notifications twice by the same user
      def lastALogs = ActivityLog.findAllByActionAndUsername('remote_notifications', loggedInUser.username, [max: 1, sort: 'timestamp', order:'desc'])
      def from
      if (lastALogs.size() > 0) from = lastALogs[0].timestamp

      def notifications = remoteNotificationsService.getNotifications('ehrserver', session.lang, from)

      notifications.each { notification ->
         new Notification(name:'remote', language:session.lang, text:notification.nt, forUser:loggedInUser.id).save()
      }

      // Mark current read of the remote notifications
      new ActivityLog(username: loggedInUser.username, action: 'remote_notifications', sessionId: session.id.toString()).save()

      [
         count_ehrs:count_ehrs,
         count_contributions:count_contributions,
         count_queries: count_queries,
         version_repo_sizes: version_repo_sizes,
         count_users: count_users
      ]
   }

   def get_started()
   {
      []
   }
}
