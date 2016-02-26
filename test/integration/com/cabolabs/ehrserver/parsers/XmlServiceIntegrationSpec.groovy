package com.cabolabs.ehrserver.parsers

import grails.test.spock.IntegrationSpec
import groovy.util.slurpersupport.GPathResult

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.ehr.Ehr

import groovy.io.FileType
import spock.lang.Ignore
import grails.util.Holders

class XmlServiceIntegrationSpec extends IntegrationSpec {

   // it seems integration tests are transactional by default, so if an exception occurs, the session is rolledback at the end of each test case,
   // after we check the conditions, and we need the rollback to occur before we check the test conditions
   //static transactional = false
   
   def xmlService
   
   private static String PS = System.getProperty("file.separator")
   
   
   def setup()
   {
      // used by the service, mock the version repo where commits are stored
      Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"temp_versions" + PS
   }

   def cleanup()
   {
      println "cleanup"
      
      /*
       * org.springframework.dao.DataIntegrityViolationException: Hibernate operation: could not execute statement; SQL [n/a]; Cannot delete or update a p
arent row: a foreign key constraint fails (`ehrservertest`.`version`, CONSTRAINT `FK_qku5pv15ayvcge2p64ko7cvb4` FOREIGN KEY (`data_id`) REFERENCE
S `composition_index` (`id`)); nested exception is com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Cannot delete or u
pdate a parent row: a foreign key constraint fails (`ehrservertest`.`version`, CONSTRAINT `FK_qku5pv15ayvcge2p64ko7cvb4` FOREIGN KEY (`data_id`)
REFERENCES `composition_index` (`id`))
        at com.cabolabs.ehrserver.parsers.XmlServiceIntegrationSpec.cleanup(XmlServiceIntegrationSpec.groovy:46)
Caused by: com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Cannot delete or update a parent row: a foreign key constr
aint fails (`ehrservertest`.`version`, CONSTRAINT `FK_qku5pv15ayvcge2p64ko7cvb4` FOREIGN KEY (`data_id`) REFERENCES `composition_index` (`id`))
      
      // clean database
      VersionedComposition.list().each { it.delete() }
      Version.list().each { version ->
         //version.commitAudit.delete() // AuditDetails
         version.data.delete() // CompositionIndex
         version.contribution.removeFromVersions(version)
         version.delete()
      }
      Contribution.list().each { contrib ->
         //contrib.audit.delete() // AuditDetails
         contrib.ehr.removeFromContributions(contrib)
         contrib.delete()
      }
      */
      
      // empty the temp version store
      def temp = new File(Holders.config.app.version_repo)
      temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }

   
   void "commit single / valid version"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
   
   void "commit single / invalid version"()
   {
      setup:
         def ehr = Ehr.get(1)
      
         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1_invalid.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         
         def slurper = new XmlSlurper(false, false)
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
   }
   
   void "multiple / all valid versions"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
   
   void "multiple / one invalid version"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
   
   void "multiple / all invalid version"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
   
   void "commit same version twice"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
   
   void "commit 2 compos, and new version"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
   
   void "commit new version without previous version"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
   
   /**
    * there is an issue with this test, while the rollback is done correctly on functional testing, here is not detected and if gives 1 contribution.
    * asked here: http://stackoverflow.com/questions/35617951/grails-2-5-3-testing-service-rollback-on-integration-tests-with-spock
    */
   @Ignore
   void "commit with an existing file with the same version id on the version repo"()
   {
      setup:
         def ehr = Ehr.get(1)
      
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
   }
}
