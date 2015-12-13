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
import ehr.IndexDataJob
import ehr.clinical_documents.data.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestFor(IndexDataJob)
@TestFor(RestController)
@TestMixin(GrailsUnitTestMixin)
@Mock([ Ehr,Person,Organization,
   PatientProxy, DoctorProxy,
   OperationalTemplateIndex, IndexDefinition, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails,
   DataValueIndex, DvQuantityIndex, DvCountIndex, DvProportionIndex, DvTextIndex, DvCodedTextIndex, DvDateTimeIndex, DvBooleanIndex
 ])
class IndexDataJobSpec extends Specification {

   def job = new IndexDataJob()
   private static String PS = System.getProperty("file.separator")
   private static String patientUid = 'a86ac702-980a-478c-8f16-927fd4a5e9ae'
   def config = grailsApplication.config.app //Holders.config.app
   
   
   def setup()
   {
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

   def cleanup()
   {
      def version_repo = new File(config.version_repo)
      version_repo.eachFile {
        it.delete()
      }
   }

   void "test nothing to index"()
   {
      when:
       job.execute()
       
      then:
       DataValueIndex.count() == 0
   }
   
   void "test commit and index"()
   {
      setup:
       // load template that will be used for indexing
       def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
       def opt = new File( "opts" + PS + "tests" + PS + "Test all datatypes_es.opt" )
       oti.index(opt)
      
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
       job.execute() // creates indexes
       
       DataValueIndex.list().each { println it.getClass() }
       
      then:
       DataValueIndex.count() == 2
       
       DvCountIndex.count() == 1
       DvCountIndex.first().magnitude == 3
       
       DvCodedTextIndex.count() == 1
       def dvct = DvCodedTextIndex.first()
       dvct.value == 'event'
       dvct.code == '443'
       
       /*
       response.xml.type.code.text() == 'AA' // response is already xml
       Version.count() == 1
       Contribution.count() == 1
       CompositionIndex.count() == 1
       VersionedComposition.count() == 1
       Ehr.get(1).compositions.size() == 1 // versioned composition
       Ehr.get(1).contributions.size() == 1
       Contribution.get(1).ehr == Ehr.get(1)
       */
   }
}
