package com.cabolabs.ehrserver.api

import grails.test.mixin.TestMixin
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import com.cabolabs.ehrserver.parsers.XmlService
import com.cabolabs.ehrserver.parsers.XmlValidationService

import com.cabolabs.ehrserver.api.RestController
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization

import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*
import com.cabolabs.ehrserver.ehr.clinical_documents.data.*

import grails.util.Holders

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(RestController)
@TestMixin(GrailsUnitTestMixin)
@Mock([ Ehr,Organization,
   PatientProxy, DoctorProxy,
   OperationalTemplateIndex, OperationalTemplateIndexItem, ArchetypeIndexItem, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails,
   DataValueIndex, DvQuantityIndex, DvCountIndex, DvProportionIndex, DvTextIndex, DvCodedTextIndex, DvDateTimeIndex, DvBooleanIndex
 ])
class RestControllerSpec extends Specification {

   private static String PS = System.getProperty("file.separator")
   private static String patientUid = 'a86ac702-980a-478c-8f16-927fd4a5e9ae'
   def config = grailsApplication.config.app //Holders.config.app
   
   /**
    * creates patient, org and ehr.
    */
   def setup()
   {
      assert config.version_repo != null
      
      def hospital = new Organization(name: 'Hospital de Clinicas', number: '1234')
      hospital.save(failOnError:true, flush:true)
      

      def ehr = new Ehr(
         subject: new PatientProxy(
            value: patientUid
         ),
         organizationUid: patient.organizationUid
      )
      ehr.save(failOnError:true, flush:true)
   }
   
   /**
    * deleted the version files created by the commit.
    */
   def cleanup()
   {
      println "+++ RestControllerSpec config.version_repo "+ config.version_repo // dice /versions ...
      def version_repo = new File(config.version_repo)
      version_repo.eachFile {
         it.delete()
      }
   }

   void "test commit"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
        content = content.replaceAll('\\[PATIENT_UID\\]', patientUid)
        
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
      then:
        response.xml.type.code.text() == 'AA' // response is already xml
        Version.count() == 1
        Contribution.count() == 1
        CompositionIndex.count() == 1
        VersionedComposition.count() == 1
        Ehr.get(1).compositions.size() == 1 // versioned composition
        Ehr.get(1).contributions.size() == 1
        Contribution.get(1).ehr == Ehr.get(1)
   }
   
   void "test invalid commit"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1_invalid.xml').text
        content = content.replaceAll('\\[PATIENT_UID\\]', patientUid)
        
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
        println "Should report a XML validation error"
        println response.contentAsString
        
      then:
        response.xml.type.code.text() == 'AR' // response is already xml
        response.xml.message != null
        Version.count() == 0
   }
   
   void "test 2 commits with diff contrib UID but same version UID and change type creation"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content1 = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
        content1 = content1.replaceAll('\\[PATIENT_UID\\]', patientUid)
        
        def content2 = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1_diff_contrib_uid.xml').text
        content2 = content2.replaceAll('\\[PATIENT_UID\\]', patientUid)
      
      
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content1
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
      then:
        response.xml.type.code.text() == 'AA' // response is already xml
        Version.count() == 1
        
      when:
        controller.response.reset()
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content2
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
       
        println "Should report an error"
        println response.contentAsString
       
      then:
        response.xml.type.code.text() == 'AR' // response is already xml
        Version.count() == 1
   }
   
   
   void "test 2 commits with new version for amendment"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content1 = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
        content1 = content1.replaceAll('\\[PATIENT_UID\\]', patientUid)
        
        def content2 = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1_new_version.xml').text
        content2 = content2.replaceAll('\\[PATIENT_UID\\]', patientUid)
      
      
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content1
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
      then:
        response.xml.type.code.text() == 'AA' // response is already xml
        Version.count() == 1
        
      
      when:
        controller.response.reset()
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content2
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
       
      then:
        response.xml.type.code.text() == 'AA' // response is already xml
        response.xml.message != null
        Version.count() == 2
   }
   
   
   void "test commit amendment with no previous version"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1_new_version.xml').text
        content = content.replaceAll('\\[PATIENT_UID\\]', patientUid)
      
      
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
        println "Should report an error"
        println response.contentAsString
        
      then:
        response.xml.type.code.text() == 'AR'
        
        // Known issue: still saves the version https://github.com/ppazos/cabolabs-ehrserver/issues/216
        // UPDATE: the rollback now seems to work since we moved all the logic to a service, but it seems
        //         from a test it is not rolling back so we see data in the database. But on run-app it
        //         is rolling back ok, so no Version or Contribution is created for this test case.
        //         Might be related to: http://www.34m0.com/2012/08/grails-testing-rollback-of.html
        Version.count() == 0
        
        
        VersionedComposition.count() == 0
        Contribution.count() == 0
   }
   
   
   /**
    * versions have same contribution uid and different version uid.
    */
   void "test commit of 2 versions"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_2_versions.xml').text
        content = content.replaceAll('\\[PATIENT_UID\\]', patientUid)
      
      
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
        println response.contentAsString
        
      then:
        response.xml.type.code.text() == 'AA'
        Version.count() == 2
   }
   
   
   /**
    * versions have different contribution uid.
    */
   void "test commit of 2 versions with diff contrib id"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_2_versions_diff_contrib.xml').text
        content = content.replaceAll('\\[PATIENT_UID\\]', patientUid)
      
      
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
        println response.contentAsString
        
      then:
        response.xml.type.code.text() == 'AR'
        Version.count() == 0
   }
   
   
   /**
    * 2 not valid versions against xsds
    */
   void "test commit of 2 versions with errors"()
   {
      setup:
        // setup services for controller
        def xmlService = new XmlService()
        xmlService.xmlValidationService = new XmlValidationService()
        controller.xmlService = xmlService
        
        // content to commit
        def content = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_2_versions_invalid.xml').text
        content = content.replaceAll('\\[PATIENT_UID\\]', patientUid)
      
      
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
        println response.contentAsString
        
      then:
        response.xml.type.code.text() == 'AR'
        Version.count() == 0
   }
   
}
