package com.cabolabs.security

import grails.transaction.Transactional
import com.cabolabs.ehrserver.account.*
import com.cabolabs.ehrserver.query.QueryGroup
import grails.util.Holders

@Transactional
class OrganizationService {

   def config = Holders.config.app
   def notificationService

   def create(Account account, String name, boolean sendNotification = true)
   {
      // create org and set account
      def org = new Organization(name: name)
      account.addToOrganizations(org)
      account.save(flush: true, failOnError: true)

      // Create default QueryGroup per organization, see https://github.com/ppazos/cabolabs-ehrserver/issues/982
      new QueryGroup(name:'Ungrouped', organizationUid:org.uid).save()

      // There is no need of creating the file repos for the org since those will
      // be created the first time those are used

      if (sendNotification)
      {
         // notify the ACCMAN he has access to the new Organization on his account
         notificationService.sendNewOrganizationAssociatedEmail([account: account, organization: org])
      }

      // create folders
      def opt_repo = new File(config.opt_repo.withTrailSeparator() + org.uid)
      opt_repo.mkdirs()

      def version_repo = new File(config.version_repo.withTrailSeparator() + org.uid)
      version_repo.mkdirs()

      def commit_repo = new File(config.commit_logs.withTrailSeparator() + org.uid)
      commit_repo.mkdirs()

      return org
   }
}
