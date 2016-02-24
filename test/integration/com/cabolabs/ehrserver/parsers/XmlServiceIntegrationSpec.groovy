package com.cabolabs.ehrserver.parsers

import grails.test.spock.IntegrationSpec
import groovy.util.slurpersupport.GPathResult

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.ehr.Ehr

import groovy.io.FileType

import grails.util.Holders

class XmlServiceIntegrationSpec extends IntegrationSpec {

   def xmlService
   
   private static String PS = System.getProperty("file.separator")
   
   def setup()
   {
      // used by the service, mock the version repo where commits are stored
      Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"temp_versions" + PS
   }

   def cleanup()
   {
      
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
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1
         
      cleanup:
         // empty the temp version store
         def temp = new File(Holders.config.app.version_repo)
         temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
         try {
            xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         }
         catch (Exception e)
         {
            println "ok, exception handled "+ e.message
         }
      then:
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
      cleanup:
         // empty the temp version store
         def temp = new File(Holders.config.app.version_repo)
         temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
         assert Contribution.count() == 1
         assert Version.count() == 2
         assert VersionedComposition.count() == 2
         assert CompositionIndex.count() == 2
         
      cleanup:
         // empty the temp version store
         def temp = new File(Holders.config.app.version_repo)
         temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
         try {
            xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         } catch (Exception e) {
            println e.cause.message
         }
         
      then:
         assert xmlService.validationErrors.size() == 1
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
      cleanup:
         // empty the temp version store
         def temp = new File(Holders.config.app.version_repo)
         temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
         try {
            xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         } catch (Exception e) {
            println e.cause.message
         }
      
      then:
         assert xmlService.validationErrors.size() == 2
         assert Contribution.count() == 0
         assert Version.count() == 0
         assert VersionedComposition.count() == 0
         assert CompositionIndex.count() == 0
         
      cleanup:
         // empty the temp version store
         def temp = new File(Holders.config.app.version_repo)
         temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
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
      
         // second shoudl return an error
         try {
            xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         } catch (Exception e) {
            println e.message
         }
      
      then:
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1
         
      cleanup:
         // empty the temp version store
         def temp = new File(Holders.config.app.version_repo)
         temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }
}
