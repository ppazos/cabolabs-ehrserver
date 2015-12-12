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

    def cleanup() {
    }

    void "test nothing to index"()
    {
       when:
         def ehr = Ehr.get(1)
         job.execute()
         
       then:
         ehr != null
         DataValueIndex.count() == 0
    }
}
