package com.cabolabs.ehrserver.parsers

import grails.test.spock.IntegrationSpec
import groovy.util.slurpersupport.GPathResult

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Commit
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.*
import com.cabolabs.ehrserver.account.*

import groovy.io.FileType
import spock.lang.Ignore
import grails.util.Holders

/**
 * This mainly tests commits without the controller, just goes directly to the service.
 *
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 *
 */
class XmlServiceIntegrationSpec extends IntegrationSpec {

   // it seems integration tests are transactional by default, so if an exception occurs, the session is rolledback at the end of each test case,
   // after we check the conditions, and we need the rollback to occur before we check the test conditions
   //static transactional = false

   def xmlService
   def config = Holders.config

   private static String PS  = System.getProperty("file.separator")
   private String ehrUid     = '11111111-1111-1111-1111-111111111123'
   private String patientUid = '11111111-1111-1111-1111-111111111145'
   private String orgUid     = '11111111-1111-1111-1111-111111111178'

   private createEHR()
   {
      def ehr = new Ehr(
         uid: ehrUid, // the ehr id is the same as the patient just to simplify testing
         subject: new PatientProxy(
            value: patientUid
         ),
         organizationUid: Organization.findByUid(orgUid).uid
      )

      ehr.save(failOnError: true)
   }

   def setup()
   {
      // 1. Account setup: create account manager user
      def accman = new User(
         username: 'testaccman',
         password: 'testaccman',
         email: 'testaccman@domain.com',
      ).save(failOnError:true, flush: true)

      // 2. Account setup: create account
      def account = new Account(contact: accman, companyName:'Test company')

      // 3. Account setup: create organization
      def org = new Organization(uid: orgUid, name: 'CaboLabs', number: '123456')
      account.addToOrganizations(org)
      account.save(failOnError: true) // saves the org

      // 4. Account setup: create ACCMAN role
      def accmanRole = new Role(authority: Role.AM).save(failOnError: true, flush: true)

      // 5. Account setup: create user role association
      UserRole.create( accman, accmanRole, org, true )


      createEHR()
   }

   def cleanup()
   {
      Contribution.list()*.delete()

      def ehr = Ehr.findByUid(ehrUid)
      ehr.delete(flush: true) // deletes all the contributions, versions, audit details, doctor proxies in cascade

      /* all zero!
      println Contribution.count()
      println Version.count()
      println VersionedComposition.count()
      println CompositionIndex.count()
      */

      Account.list()*.delete() // should delete the orgs

      UserRole.list()*.delete()
      User.list()*.delete()
      Role.list()*.delete()
   }


   void "commit single / valid version"()
   {
      setup: "gets the existing EHR and prepares the commit (simulates the controller commit)"
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)


      when: "does the commit"
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         notThrown Exception // this shouldn't throw any exceptions
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1
         //println "commits "+ Commit.count() // 0, Commit is not used...


      cleanup:
         println "test cleanup"

         // VersionedCompositions are not deleted in cascade in the global cleanup when ehr.delete
         VersionedComposition.list()*.delete(flush: true)

         println "commit single / valid version DELETE CREATED FILES FROM "+ config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()
         new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }



   void "commit single / invalid version"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1_invalid.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)


      when:
         // should throw an exception
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         Exception e = thrown() // TODO: use specific exception type
         assert e.message == "There are errors in the XML versions"
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0

         // no version files should be created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                   .findAll { it.name ==~ /.*\.xml/ }
                                                   .size() == 0
   }


   // for https://github.com/ppazos/cabolabs-ehrserver/issues/366
   void "commit single / invalid version with empty datatype nodes"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_empty_datatypes.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         // should throw an exception
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         Exception e = thrown() // TODO: use specific exception type

         println xmlService.validationErrors // stores all the validation errors

         assert e.message == "There are errors in the XML versions"
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0

         // no version files should be created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                   .findAll { it.name ==~ /.*\.xml/ }
                                                   .size() == 0
   }


   void "multiple / all valid versions"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_2_versions.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         notThrown Exception // this shouldn't throw any exceptions
         assert Contribution.count() == 1
         assert Version.count() == 2
         assert VersionedComposition.count() == 2
         assert CompositionIndex.count() == 2

         // check that 2 version files were created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                 .findAll { it.name ==~ /.*\.xml/ }
                                                 .size() == 2

      cleanup:
         println "test cleanup"

         // VersionedCompositions are not deleted in cascade in the global cleanup when ehr.delete
         VersionedComposition.list()*.delete(flush: true)


         println "multiple / all valid versions DELETE CREATED FILES FROM "+ config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()
         new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }


   void "multiple / one invalid version"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_2_versions_one_invalid.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         // should throw an exception
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         Exception e = thrown() // TODO: use specific exception type
         assert xmlService.validationErrors.size() == 1
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0

         // no version files should be created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                 .findAll { it.name ==~ /.*\.xml/ }
                                                 .size() == 0
   }


   void "multiple / all invalid version"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_2_versions_invalid.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         // should throw an exception
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         Exception e = thrown() // TODO: use specific exception type
         assert xmlService.validationErrors.size() == 2
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0

         // no version files should be created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                   .findAll { it.name ==~ /.*\.xml/ }
                                                   .size() == 0
   }


   void "multiple / duplicated compo.uid"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_2_versions_same_compo_uid.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         // should throw an exception
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         Exception e = thrown() // TODO: use specific exception type
         assert xmlService.validationErrors.size() == 0
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0

         // no version files should be created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                 .findAll { it.name ==~ /.*\.xml/ }
                                                 .size() == 0
   }



   void "commit same version twice"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         // ok first time
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

         // second should throw an exception
         contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1


         // just one version file should be created in the filesystem, the one for the first commit
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                 .findAll { it.name ==~ /.*\.xml/ }
                                                 .size() == 1

      cleanup:
         println "test cleanup"

         // VersionedCompositions are not deleted in cascade in the global cleanup when ehr.delete
         VersionedComposition.list()*.delete(flush: true)

         println "commit same version twice DELETE CREATED FILES FROM "+ config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()
         new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }


   void "commit 2 compo versions for the same document"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def slurper = new XmlSlurper(false, false)

         // first version
         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         def parsedVersions = slurper.parseText(versionsXML)

         // new version
         def versionsXML2 = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1_new_version.xml').text
         versionsXML2 = versionsXML2.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         def parsedVersions2 = slurper.parseText(versionsXML2)

      when:
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'EMR_APP', new Date(), 'House, MD.')
         contribution.save()


         // since the version.uid now is assigned by the server, need to get that from the first commit
         // to assign it to the second version.preceding_version_uid
         parsedVersions2.version[0].preceding_version_uid.value = contribution.versions[0].uid

         contribution = xmlService.processCommit(ehr, parsedVersions2, 'EMR_APP', new Date(), 'House, MD.')
         contribution.save()
      then:
         //notThrown Exception // this shouldn't throw any exceptions
         assert Contribution.count() == 2
         assert Version.count() == 2
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 2

         // check that 2 version files were created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                 .findAll { it.name ==~ /.*\.xml/ }
                                                 .size() == 2

      cleanup:
         println "test cleanup"

         // VersionedCompositions are not deleted in cascade in the global cleanup when ehr.delete
         VersionedComposition.list()*.delete(flush: true)

         println "commit 2 compos, and new version DELETE CREATED FILES FROM "+ config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()
         new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }


   void "commit new version without previous version"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def slurper = new XmlSlurper(false, false)

         // amendment with not previous version
         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_v2_amendment.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         // should throw an exception
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()

      then:
         Exception e = thrown() // TODO: use specific exception type
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0

         // no version files should be created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                 .findAll { it.name ==~ /.*\.xml/ }
                                                 .size() == 0
   }


   /**
    * there is an issue with this test, while the rollback is done correctly on functional testing, here is not detected and if gives 1 contribution.
    * asked here: http://stackoverflow.com/questions/35617951/grails-2-5-3-testing-service-rollback-on-integration-tests-with-spock
    */
   @Ignore
   void "commit with an existing file with the same version id on the version repo"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)

         def slurper = new XmlSlurper(false, false)

         // copy file into the version repo
         def source = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml')
         java.nio.file.Files.copy(source.toPath(), new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator() + "91cf9ded-e926-4848-aa3f-3257c1d89e37_EMR_APP_1.xml").toPath())

         // commit same file via the commit processing
         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         def parsedVersions = slurper.parseText(versionsXML)

      when:
         // should throw an exception
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save()
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0

         // no version files should be created in the filesystem
         assert new File(config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator()).listFiles()
                                                 .findAll { it.name ==~ /.*\.xml/ }
                                                 .size() == 0
   }
}
