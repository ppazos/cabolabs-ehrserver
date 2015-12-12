package ehr

import grails.test.mixin.TestMixin
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.test.mixin.support.GrailsUnitTestMixin

import spock.lang.Specification

import parsers.XmlService
import parsers.XmlValidationService

import demographic.Person
import com.cabolabs.security.Organization
import ehr.clinical_documents.*
import common.change_control.*
import common.generic.*
import ehr.Ehr
import ehr.clinical_documents.data.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(RestController)
@TestMixin(GrailsUnitTestMixin)
@Mock([ Ehr,Person,Organization,
   PatientProxy, DoctorProxy,
   OperationalTemplateIndex, IndexDefinition, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails,
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
      
      def patient = new Person(
         firstName: 'Pablo', lastName: 'Pazos',
         dob: new Date(81, 9, 24), sex: 'M',
         idCode: '4116238-0', idType: 'CI',
         role: 'pat',
         uid: patientUid,
         organizationUid: hospital.uid
      )
      patient.save(failOnError:true, flush:true)
      
      def ehr = new Ehr(
         subject: new PatientProxy(
            value: patient.uid
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
        params.ehrId = Ehr.get(1).ehrId
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        
      then:
        response.xml.type.code.text() == 'AA' // response is already xml
        Version.count() == 1
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
        params.ehrId = Ehr.get(1).ehrId
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
        params.ehrId = Ehr.get(1).ehrId
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
        params.ehrId = Ehr.get(1).ehrId
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
       
        println "Should report an error"
        println response.contentAsString
       
      then:
        response.xml.type.code.text() == 'AR' // response is already xml
        response.xml.message != null
        Version.count() == 1
   }
}
