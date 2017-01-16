package com.cabolabs.ehrserver.parsers

import grails.test.spock.IntegrationSpec
import groovy.util.slurpersupport.GPathResult

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization

import groovy.io.FileType
import spock.lang.Ignore
import grails.util.Holders

class XmlServiceIntegrationSpec extends IntegrationSpec {

   // it seems integration tests are transactional by default, so if an exception occurs, the session is rolledback at the end of each test case,
   // after we check the conditions, and we need the rollback to occur before we check the test conditions
   //static transactional = false
   
   def xmlService
   
   private static String PS = System.getProperty("file.separator")
   private String ehrUid = '11111111-1111-1111-1111-111111111123'
   private String patientUid = '11111111-1111-1111-1111-111111111145'
   private String orgUid = '11111111-1111-1111-1111-111111111178'
   
   private createOrganization()
   {
      def org = new Organization(uid: orgUid, name: 'CaboLabs', number: '123456')
      org.save(failOnError: true)
   }
   
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
      createOrganization()
      createEHR()
   }

   def cleanup()
   {
      def ehr = Ehr.findByUid(ehrUid)
      ehr.delete()
      
      def org = Organization.findByUid(orgUid)
      org.delete()
   }

   
   void "commit single / valid version"()
   {
      setup:
         def ehr = Ehr.findByUid(ehrUid)
      
         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         
         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)
         
         
      when:
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         
      then:
         notThrown Exception // this shouldn't throw any exceptions
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1
      
      cleanup:
         println "commit single / valid version DELETE CREATED FILES FROM "+ Holders.config.app.version_repo
         new File(Holders.config.app.version_repo).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert e.message == "There are errors in the XML versions"
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
         // no version files should be created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         
      then:
         Exception e = thrown() // TODO: use specific exception type
         
         println xmlService.validationErrors // stores all the validation errors
         
         assert e.message == "There are errors in the XML versions"
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
         // no version files should be created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         
      then:
         notThrown Exception // this shouldn't throw any exceptions
         assert Contribution.count() == 1
         assert Version.count() == 2
         assert VersionedComposition.count() == 2
         assert CompositionIndex.count() == 2
         
         // check that 2 version files were created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
                                                         .findAll { it.name ==~ /.*\.xml/ }
                                                         .size() == 2
      
      cleanup:
         println "multiple / all valid versions DELETE CREATED FILES FROM "+ Holders.config.app.version_repo
         new File(Holders.config.app.version_repo).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert xmlService.validationErrors.size() == 1
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
         // no version files should be created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
      
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert xmlService.validationErrors.size() == 2
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
         // no version files should be created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
      
         // second should throw an exception
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
      
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1
         
         
         // just one version file should be created in the filesystem, the one for the first commit
         assert new File(Holders.config.app.version_repo).listFiles()
                                                         .findAll { it.name ==~ /.*\.xml/ }
                                                         .size() == 1
      
      cleanup:
         println "commit same version twice DELETE CREATED FILES FROM "+ Holders.config.app.version_repo
         new File(Holders.config.app.version_repo).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }
   
   void "commit 2 compos, and new version"()
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         xmlService.processCommit(ehr, parsedVersions2, 'CaboLabs EMR', new Date(), 'House, MD.')
      
      then:
         notThrown Exception // this shouldn't throw any exceptions
         assert Contribution.count() == 2
         assert Version.count() == 2
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 2
         
         // check that 2 version files were created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
                                                         .findAll { it.name ==~ /.*\.xml/ }
                                                         .size() == 2
      
      cleanup:
         println "commit 2 compos, and new version DELETE CREATED FILES FROM "+ Holders.config.app.version_repo
         new File(Holders.config.app.version_repo).eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
         // no version files should be created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
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
         java.nio.file.Files.copy(source.toPath(), new File(Holders.config.app.version_repo + PS + "91cf9ded-e926-4848-aa3f-3257c1d89e37_EMR_APP_1.xml").toPath())
         
         // commit same file via the commit processing
         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         def parsedVersions = slurper.parseText(versionsXML)
         
      when:
         // should throw an exception
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
      
      then:
         Exception e = thrown() // TODO: use specific exception type
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
         // no version files should be created in the filesystem
         assert new File(Holders.config.app.version_repo).listFiles()
                                                         .findAll { it.name ==~ /.*\.xml/ }
                                                         .size() == 0
   }
}
