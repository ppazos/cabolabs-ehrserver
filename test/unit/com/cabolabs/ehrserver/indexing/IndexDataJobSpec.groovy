package com.cabolabs.ehrserver.indexing

import grails.test.mixin.TestMixin
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import com.cabolabs.ehrserver.parsers.XmlService
import com.cabolabs.ehrserver.parsers.XmlValidationService
import com.cabolabs.ehrserver.openehr.ehr.Ehr

import com.cabolabs.ehrserver.api.RestController
import com.cabolabs.ehrserver.data.DataIndexerService
import com.cabolabs.security.Organization

import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*

import com.cabolabs.ehrserver.openehr.ehr.Ehr

import com.cabolabs.ehrserver.ehr.clinical_documents.data.*
import com.cabolabs.ehrserver.query.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestFor(IndexDataJob)
@TestFor(RestController)
@TestMixin(GrailsUnitTestMixin)
@Mock([ Ehr,Organization,
   PatientProxy, DoctorProxy,
   OperationalTemplateIndex, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails,
   DataValueIndex, DvQuantityIndex, DvCountIndex, DvProportionIndex, DvTextIndex, DvCodedTextIndex, DvDateTimeIndex, DvBooleanIndex
 ])
class IndexDataJobSpec extends Specification {

   private static String PS = System.getProperty("file.separator")
   private static String patientUid = 'a86ac702-980a-478c-8f16-927fd4a5e9ae'
   def config = grailsApplication.config.app //Holders.config.app
   
   
   def setup()
   {
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

   def cleanup()
   {
      def version_repo = new File(config.version_repo)
      version_repo.eachFile {
        it.delete()
      }
   }

   void "test nothing to index"()
   {
      setup:
        def job = new IndexDataJob()
        // https://github.com/grails/grails-core/issues/1501
        
        // service for job
        job.dataIndexerService = new DataIndexerService()
        job.dataIndexerService.transactionManager = getTransactionManager() // workaround to not get null from service that has @Transaction (Grails bug)
        
      when:
        job.execute()
       
      then:
        DataValueIndex.count() == 0
   }
   
   void "test commit and index"()
   {
      setup:
        def job = new IndexDataJob()
        
        // service for job
        job.dataIndexerService = new DataIndexerService()
        job.dataIndexerService.transactionManager = getTransactionManager() // workaround to not get null from service that has @Transaction (Grails bug)
        
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
        params.ehrUid = Ehr.get(1).uid
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
        dvct.code == '433'
   }
   
   
   void "test commit and index and query data"()
   {
      setup:        
        def job = new IndexDataJob()
        
        // service for job
        job.dataIndexerService = new DataIndexerService()
        job.dataIndexerService.transactionManager = getTransactionManager() // workaround to not get null from service that has @Transaction (Grails bug).transactionManager = getTransactionManager() // workaround to not get null from service that has @Transaction (Grails bug)
        
        
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
       
        def query = new Query(name:'get data', type:'datavalue', format:'json', select:[
          new DataGet(archetypeId:'openEHR-EHR-OBSERVATION.test_all_datatypes.v1', path:'/data[at0001]/events[at0002]/data[at0003]/items[at0011]/value')
        ])
       
      when:
        request.method = 'POST'
        request.contentType = 'text/xml'
        request.xml = content
        params.ehrUid = Ehr.get(1).uid
        params.auditSystemId = "TEST_SYSTEM_ID"
        params.auditCommitter = "Mr. Committer"
        controller.commit()
        job.execute() // creates indexes
       
        DataValueIndex.list().each { println it.getClass() }
       
        println query.toString()
       
        def queryResult = query.execute(Ehr.get(1).uid, null, null, null, Organization.get(1).uid)
       
      then:
        queryResult.size() == 1
        queryResult[0].instanceOf( DvCountIndex )
        queryResult[0].magnitude == 3
        queryResult[0].archetypeId == 'openEHR-EHR-OBSERVATION.test_all_datatypes.v1'
        queryResult[0].archetypePath == '/data[at0001]/events[at0002]/data[at0003]/items[at0011]/value'
   }
}
