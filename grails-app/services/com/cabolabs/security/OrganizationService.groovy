package com.cabolabs.security

import grails.transaction.Transactional
import com.cabolabs.ehrserver.account.*
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

      // create repos

      // create namespace repo for org OPTs
      def opt_repo_org = new File(config.opt_repo.withTrailSeparator() + org.uid)
      opt_repo_org.mkdir()

      // create older OPT version repo for the org (needed for versioning)
      def old_versions_opt_repo_org = new File(opt_repo_org.path.withTrailSeparator() + 'older_versions')
      old_versions_opt_repo_org.mkdir()

      def deleted_opt_repo_org = new File(opt_repo_org.path.withTrailSeparator() + 'deleted')
      deleted_opt_repo_org.mkdir()

      // org version repo
      def version_repo = new File(config.version_repo.withTrailSeparator() + org.uid)
      version_repo.mkdir()

      // org commit logs repo
      def commit_logs_repo = new File(config.commit_logs.withTrailSeparator() + org.uid)
      commit_logs_repo.mkdir()

      if (sendNotification)
      {
         // notify the ACCMAN he has access to the new Organization on his account
         notificationService.sendNewOrganizationAssociatedEmail([account: account, organization: org])
      }

      return org
   }
}
