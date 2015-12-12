package ehr

import static org.junit.Assert.*
import demographic.Person
import grails.test.mixin.*
import grails.test.mixin.support.*
import ehr.clinical_documents.*
import common.change_control.*
import common.generic.*
import org.junit.*
import org.springframework.mock.web.MockMultipartFile
import ehr.clinical_documents.data.*
import grails.util.Holders
import query.*
import com.cabolabs.security.Organization


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(RestController)
@Mock([ Ehr,Person,Organization,
        PatientProxy, DoctorProxy,
        OperationalTemplateIndex, IndexDefinition, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails,
        DataValueIndex, DvQuantityIndex, DvCountIndex, DvProportionIndex, DvTextIndex, DvCodedTextIndex, DvDateTimeIndex, DvBooleanIndex,
        Query, DataGet, DataCriteria
      ])
class RestController2Tests {

   private static String PS = System.getProperty("file.separator")
   private static String patientUid = 'a86ac702-980a-478c-8f16-927fd4a5e9ae'
   
   // grailsApplication is injected by the controller test mixin
   // http://stackoverflow.com/questions/18614893/how-to-access-grailsapplication-and-applicationcontext-in-functional-tests
   def config = grailsApplication.config.app //Holders.config.app
   
   void setUp()
	{
      println "setUp"
      

      controller.xmlService = new parsers.XmlService()
      controller.xmlService.xmlValidationService = new parsers.XmlValidationService()
      
      // Sample organizations
      def hospital = new Organization(name: 'Hospital de Clinicas', number: '1234')
      def clinic = new Organization(name: 'Clinica del Tratamiento del Dolor', number: '6666')
      def practice = new Organization(name: 'Cirugia Estetica', number: '5555')
      
      hospital.save(failOnError:true, flush:true)
      clinic.save(failOnError:true, flush:true)
      practice.save(failOnError:true, flush:true)
      
      
	   // Copiado de bootstrap porque no agarra las instancias en testing.
		def persons = [
         new Person(
            firstName: 'Pablo',
            lastName: 'Pazos',
            dob: new Date(81, 9, 24),
            sex: 'M',
            idCode: '4116238-0',
            idType: 'CI',
            role: 'pat',
            uid: patientUid,
            organizationUid: hospital.uid
         ),
         new Person(
            firstName: 'Barbara',
            lastName: 'Cardozo',
            dob: new Date(87, 2, 19),
            sex: 'F',
            idCode: '1234567-0',
            idType: 'CI',
            role: 'pat',
            uid: '22222222-1111-1111-1111-111111111111',
            organizationUid: hospital.uid
         ),
         new Person(
            firstName: 'Carlos',
            lastName: 'Cardozo',
            dob: new Date(80, 2, 20),
            sex: 'M',
            idCode: '3453455-0',
            idType: 'CI',
            role: 'pat',
            uid: '33333333-1111-1111-1111-111111111111',
            organizationUid: hospital.uid
         )
         ,
         new Person(
            firstName: 'Mario',
            lastName: 'Gomez',
            dob: new Date(64, 8, 19),
            sex: 'M',
            idCode: '5677565-0',
            idType: 'CI',
            role: 'pat',
            uid: '44444444-1111-1111-1111-111111111111',
            organizationUid: hospital.uid
         )
         ,
         new Person(
            firstName: 'Carla',
            lastName: 'Martinez',
            dob: new Date(92, 1, 5),
            sex: 'F',
            idCode: '84848884-0',
            idType: 'CI',
            role: 'pat',
            uid: '55555555-1111-1111-1111-111111111111',
            organizationUid: hospital.uid
         )
      ]
      
      persons.each { p ->
         
         if (!p.save())
         {
            println p.errors
         }
      }
	  
	  
	  // Crea EHRs para los pacientes de prueba
	  // Idem EhrController.createEhr
	  def ehr
	  persons.eachWithIndex { p, i ->
	  
	    if (p.role == 'pat')
		 {
			ehr = new Ehr(
			   subject: new PatientProxy(
			      value: p.uid
			   ),
            organizationUid: p.organizationUid
		    )
         
          if (!ehr.save()) println ehr.errors
		 }
	  }
     
     
     // Setup queries for testing
     
     def compositionQuery = new Query(
        name: "test query composition",
        type: "composition",
        where: [])
     if (!compositionQuery.save()) println compositionQuery.errors
   }

   void tearDown()
	{
      // Tear down logic here
      def version_repo = new File(config.version_repo)
      
      version_repo.eachFile {
         it.delete()
      }
   }

	void testPatientList()
	{
	    // Personas creados en el setUp
	    assert Person.count() == 5
		
		// Formato XML por defecto
		controller.patientList()
		println groovy.xml.XmlUtil.serialize( controller.response.text )
		assert controller.response.xml.patients.patient.size() == 5
		response.reset()
		
		
		// Formato incorrecto
		params.format = 'text'
		controller.patientList()
		assert controller.response.xml.code.text() == "error"
		response.reset()
	}
	
/*
   void testCommitWithDvQuantity()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      //def opt = new File( "opts" + PS + "Test all datatypes_en.opt" )
      def opt = new File( "opts" + PS + "Signos.opt" )
      oti.index(opt)
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      //controller.request.content
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = $/
<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://schemas.openehr.org/v1 ../xsd/Version.xsd" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563778</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e36</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.signos.v1">
    <name>
      <value>Signos vitales</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.signos.v1</value>
      </archetype_id>
      <template_id>
        <value>Signos</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.blood_pressure.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <protocol xsi:type="ITEM_TREE" archetype_node_id="at0011">
        <name>
          <value>Tree</value>
        </name>
      </protocol>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0006">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>blood pressure</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0005">
              <name>
                <value>Diastolic</value>
              </name>
              <value xsi:type="DV_QUANTITY">
                <magnitude>55.0</magnitude>
                <units>mm[Hg]</units>
              </value>
            </items>
            <items xsi:type="ELEMENT" archetype_node_id="at0004">
              <name>
                <value>Systolic</value>
              </name>
              <value xsi:type="DV_QUANTITY">
                <magnitude>44.0</magnitude>
                <units>mm[Hg]</units>
              </value>
            </items>
          </data>
          <state xsi:type="ITEM_TREE" archetype_node_id="at0007">
            <name>
              <value>state structure</value>
            </name>
          </state>
        </events>
      </data>
    </content>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.body_temperature.v1">
      <name>
        <value>Body temperature</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <protocol xsi:type="ITEM_TREE" archetype_node_id="at0020"><!-- Protocol va antes de data -->
        <name>
          <value>Protocol</value>
        </name>
      </protocol>
      <data xsi:type="HISTORY" archetype_node_id="at0002">
        <name>
          <value>History</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0003">
          <name>
            <value>Any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
            <name>
              <value>Tree</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0004">
              <name>
                <value>Temperature</value>
              </name>
              <value xsi:type="DV_QUANTITY">
                <magnitude>36.0</magnitude>
                <units>°C</units>
              </value>
            </items>
          </data>
          <state xsi:type="ITEM_TREE" archetype_node_id="at0029">
            <name>
              <value>State</value>
            </name>
          </state>
        </events>
      </data>
    </content>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.pulse.v1">
      <name>
        <value>Pulso</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <protocol xsi:type="ITEM_TREE" archetype_node_id="at0010"><!-- Protocol va antes de data -->
        <name>
          <value>*List(en)</value>
        </name>
      </protocol>
      <data xsi:type="HISTORY" archetype_node_id="at0002">
        <name>
          <value>*history(en)</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0003">
          <name>
            <value>*Any event(en)</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
            <name>
              <value>*structure(en)</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0004">
              <name>
                <value>Frecuencia</value>
              </name>
              <value xsi:type="DV_QUANTITY">
                <magnitude>37.0</magnitude>
                <units>/min</units>
              </value>
            </items>
          </data>
          <state xsi:type="ITEM_TREE" archetype_node_id="at0012">
            <name>
              <value>*List(en)</value>
            </name>
          </state>
        </events>
      </data>
    </content>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.respiration.v1">
      <name>
        <value>Respirations</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>Any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>List</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0004">
              <name>
                <value>Rate</value>
              </name>
              <value xsi:type="DV_QUANTITY">
                <magnitude>25.0</magnitude>
                <units>/min</units>
              </value>
            </items>
          </data>
          <state xsi:type="ITEM_TREE" archetype_node_id="at0022">
            <name>
              <value>List</value>
            </name>
          </state>
        </events>
      </data>
    </content>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.body_weight.v1">
      <name>
        <value>Peso corporal</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <protocol xsi:type="ITEM_TREE" archetype_node_id="at0015"><!-- OBSERVATION tiene protocol antes de data -->
        <name>
          <value>*protocol structure(en)</value>
        </name>
      </protocol>
      <data xsi:type="HISTORY" archetype_node_id="at0002">
        <name>
          <value>*history(en)</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0003">
          <name>
            <value>Cualquier evento.</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0001"><!-- EVENT debe tener data antes de state-->
            <name>
              <value>*Simple(en)</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0004">
              <name>
                <value>Peso</value>
              </name>
              <value xsi:type="DV_QUANTITY">
                <magnitude>46.0</magnitude>
                <units>kg</units>
              </value>
            </items>
          </data>
          <state xsi:type="ITEM_TREE" archetype_node_id="at0008">
            <name>
              <value>*state structure(en)</value>
            </name>
          </state>
        </events>
      </data>
    </content>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.height.v1">
      <name>
        <value>Height/Length</value>
      </name>
      <language><!-- toda entry debe tener language, encoding y subject -->
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <protocol xsi:type="ITEM_TREE" archetype_node_id="at0007"><!-- protocol va antes que data porque es de ENTRY y data es de la subclase de ENTRY -->
        <name>
          <value>List</value>
        </name>
      </protocol>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002"><!-- EVENT es abstracto -->
          <name>
            <value>Any event</value>
          </name>
          <time><value>20140101</value></time><!-- todo event tiene que tener time DV_DATE_TIME -->
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Simple</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0004">
              <name>
                <value>Height/Length</value>
              </name>
              <value xsi:type="DV_QUANTITY">
                <magnitude>180.0</magnitude>
                <units>cm</units>
              </value>
            </items>
          </data>
          <state xsi:type="ITEM_TREE" archetype_node_id="at0013"><!-- data va antes que state -->
            <name>
              <value>Tree</value>
            </name>
          </state>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
      /$
      
      //println params.versions

      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println controller.response.contentAsString
      println controller.response.text
      
      Ehr.get(1).contributions.versions.each { version -> // version.data (CompositionIndex)
         
         println "template id: "+ version.data.templateId
      }
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA"
      
      // TODO: test the created data indexes
   }
*/
   
/*
   void testCommitWithDvProportion()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      // Test operational template index created
      assert ehr.clinical_documents.OperationalTemplateIndex.countByTemplateId('Test all datatypes') == 1
      
      // Test data indexes created
      IndexDefinition.list().each { di ->
         
         println "di: " + di.path
      }
      
      
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = $/<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://schemas.openehr.org/v1 ../xsd/Version.xsd" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563780</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e38::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0024">
              <name>
                <value>Proportion</value>
              </name>
              <value xsi:type="DV_PROPORTION">
                <numerator>3.5</numerator>
                <denominator>100</denominator>
                <type>2</type><!-- pk_percentage -->
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
      /$
      
      //println params.versions

      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      //println controller.response.contentAsString
      //println controller.response.text
      
      Ehr.get(1).contributions.versions.each { version -> // version.data (CompositionIndex)
      
         println "template id: "+ version.data.templateId
      }
      
      // Test response ok
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA"
      
      
      // Test data indexes
      
      def indexJob = new ehr.IndexDataJob()
      indexJob.execute()
      
      assert DataValueIndex.count() == 1
      
      def countIdx = DataValueIndex.get(1)
      
      assert countIdx.path == '/content[archetype_id=openEHR-EHR-OBSERVATION.test_all_datatypes.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0024]/value'
      
      assert countIdx.getClass().getSimpleName() == "DvProportionIndex"
      
      assert countIdx.numerator == 3.5f
      assert countIdx.denominator == 100
      assert countIdx.type == 2
      
      println countIdx.numerator
      
   } // DvProportion
   


   void testCommitWithDvCount()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      // Test operational template index created
      ehr.clinical_documents.OperationalTemplateIndex.list().each { opti ->
         
         println "opti: " + opti.templateId
      }
      
      // Test data indexes created
      IndexDefinition.list().each { di ->
         
         println "di: " + di.path
      }
      
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = $/<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://schemas.openehr.org/v1 ../xsd/Version.xsd" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563779</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0011">
              <name>
                <value>Count</value>
              </name>
              <value xsi:type="DV_COUNT">
                <magnitude>3</magnitude>
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
      /$
      
      //println params.versions

      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      //println controller.response.contentAsString
      //println controller.response.text
      
      Ehr.get(1).contributions.versions.each { version -> // version.data (CompositionIndex)
      
         println "template id: "+ version.data.templateId
      }
      
      // Test response ok
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA"
      
      
      // Test data indexes
      
      def indexJob = new ehr.IndexDataJob()
      indexJob.execute()
      
      assert DataValueIndex.count() == 1
      
      def countIdx = DataValueIndex.get(1)
      
      assert countIdx.path == '/content[archetype_id=openEHR-EHR-OBSERVATION.test_all_datatypes.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0011]/value'
      
      assert countIdx.getClass().getSimpleName() == "DvCountIndex"
      
      assert countIdx.magnitude == new Long(3)
      
      println countIdx.magnitude
      
   } // DvCount

   
   
   void testCommitUKSample()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "UK AoMRC Community Dental Final Assessment.opt" )
      oti.index(opt)
      
      // Test operational template index created
      assert ehr.clinical_documents.OperationalTemplateIndex.countByTemplateId('UK AoMRC Community Dental Final Assessment') == 1
      
      // Test data indexes created
      IndexDefinition.list().each { di ->
         
         println "di: " + di.path
      }
      
      
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = $/<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://schemas.openehr.org/v1 ../GitHub/cabolabs-ehrserver/xsd/Version.xsd" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563780</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e38::EMR_APP::1</value>
  </uid>
  <data xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.report.v1">
   <name>
      <value>Community Dental Final Assessment Letter</value>
   </name>
   <archetype_details>
      <archetype_id>
         <value>openEHR-EHR-COMPOSITION.report.v1</value>
      </archetype_id>
      <template_id>
         <value>UK AoMRC Community Dental Final Assessment</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
   </archetype_details>
   <language>
      <terminology_id>
         <value>ISO_639-1</value>
      </terminology_id>
      <code_string>en</code_string>
   </language>
   <territory>
      <terminology_id>
         <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>GB</code_string>
   </territory>
   <category>
      <value>event</value>
      <defining_code>
         <terminology_id>
            <value>openehr</value>
         </terminology_id>
         <code_string>433</code_string>
      </defining_code>
   </category>
   <composer xsi:type="PARTY_IDENTIFIED">
      <name>Rebecca Wassall</name>
   </composer>
   <context>
      <start_time>
         <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
         <value>other care</value>
         <defining_code>
            <terminology_id>
               <value>openehr</value>
            </terminology_id>
            <code_string>238</code_string>
         </defining_code>
      </setting>
      <health_care_facility>
         <external_ref>
            <id xsi:type="GENERIC_ID">
               <value>999999-345</value>
               <scheme>NHS</scheme>
            </id>
            <namespace>NHSE</namespace>
            <type>PARTY</type>
         </external_ref>
         <name>Northumbria Community NHS</name>
      </health_care_facility>
   </context>
   <content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.demographics_rcp.v1">
      <name>
         <value>Patient demographics</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-SECTION.demographics_rcp.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <items xsi:type="ADMIN_ENTRY" archetype_node_id="openEHR-EHR-ADMIN_ENTRY.key_contacts.v1">
         <name>
            <value>Relevant contacts</value>
         </name>
         <archetype_details>
            <archetype_id>
               <value>openEHR-EHR-ADMIN_ENTRY.key_contacts.v1</value>
            </archetype_id>
            <rm_version>1.0.2</rm_version>
         </archetype_details>
         <language>
            <terminology_id>
               <value>ISO_639-1</value>
            </terminology_id>
            <code_string>en</code_string>
         </language>
         <encoding>
            <terminology_id>
               <value>IANA_character-sets</value>
            </terminology_id>
            <code_string>UTF-8</code_string>
         </encoding>
         <subject xsi:type="PARTY_SELF"/>
         <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
            <name>
               <value>Tree</value>
            </name>
            <items xsi:type="CLUSTER" archetype_node_id="at0014">
               <name>
                  <value>Formal carer</value>
               </name>
               <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.individual_professional.v1">
                  <name>
                     <value>Primary school contact</value>
                  </name>
                  <archetype_details>
                     <archetype_id>
                        <value>openEHR-EHR-CLUSTER.individual_professional.v1</value>
                     </archetype_id>
                     <rm_version>1.0.2</rm_version>
                  </archetype_details>
                  <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.person_name.v1">
                     <name>
                        <value>Person name</value>
                     </name>
                     <archetype_details>
                        <archetype_id>
                           <value>openEHR-EHR-CLUSTER.person_name.v1</value>
                        </archetype_id>
                        <rm_version>1.0.2</rm_version>
                     </archetype_details>
                     <items xsi:type="ELEMENT" archetype_node_id="at0001">
                        <name>
                           <value>Contact name</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                           <value>Nurse Baw</value>
                        </value>
                     </items>
                  </items>
                  <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.telecom_uk.v1">
                     <name>
                        <value>Telecom details (UK)</value>
                     </name>
                     <archetype_details>
                        <archetype_id>
                           <value>openEHR-EHR-CLUSTER.telecom_uk.v1</value>
                        </archetype_id>
                        <rm_version>1.0.2</rm_version>
                     </archetype_details>
                     <items xsi:type="ELEMENT" archetype_node_id="at0002">
                        <name>
                           <value>Contact telecoms</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                           <value>Mobile 0775 343 46547</value>
                        </value>
                     </items>
                  </items>
                  <items xsi:type="ELEMENT" archetype_node_id="at0012">
                     <name>
                        <value>Primary school</value>
                     </name>
                     <value xsi:type="DV_TEXT">
                        <value>St Mungo Primary school</value>
                     </value>
                  </items>
               </items>
            </items>
         </data>
      </items>
   </content>
   <content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.outpatient_details_rcp.v1">
      <name>
         <value>Outpatient details</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-SECTION.outpatient_details_rcp.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <items xsi:type="ADMIN_ENTRY" archetype_node_id="openEHR-EHR-ADMIN_ENTRY.outpatient_details_rcp.v1">
         <name>
            <value>Outpatient administration</value>
         </name>
         <archetype_details>
            <archetype_id>
               <value>openEHR-EHR-ADMIN_ENTRY.outpatient_details_rcp.v1</value>
            </archetype_id>
            <rm_version>1.0.2</rm_version>
         </archetype_details>
         <language>
            <terminology_id>
               <value>ISO_639-1</value>
            </terminology_id>
            <code_string>en</code_string>
         </language>
         <encoding>
            <terminology_id>
               <value>IANA_character-sets</value>
            </terminology_id>
            <code_string>UTF-8</code_string>
         </encoding>
         <subject xsi:type="PARTY_SELF"/>
         <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
            <name>
               <value>Tree</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0002">
               <name>
                  <value>Contact type</value>
               </name>
               <value xsi:type="DV_CODED_TEXT">
                  <value>Scheduled follow-up contact</value>
                  <defining_code>
                     <terminology_id>
                        <value>local</value>
                     </terminology_id>
                     <code_string>at0008</code_string>
                  </defining_code>
               </value>
            </items>
         </data>
      </items>
   </content>
   <content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.history_rcp.v1">
   <name>
      <value>History</value>
   </name>
   <archetype_details>
      <archetype_id>
         <value>openEHR-EHR-SECTION.history_rcp.v1</value>
      </archetype_id>
      <rm_version>1.0.2</rm_version>
   </archetype_details>
   <items xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.story.v1">
      <name>
         <value>Story/History</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-OBSERVATION.story.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <language>
         <terminology_id>
            <value>ISO_639-1</value>
         </terminology_id>
         <code_string>en</code_string>
      </language>
      <encoding>
         <terminology_id>
            <value>IANA_character-sets</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_SELF"/>
      <data archetype_node_id="at0001">
         <name>
            <value>Event Series</value>
         </name>
         <origin>
            <value>20140901T232600,304-0300</value>
         </origin>
         <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
            <name>
               <value>Any event</value>
            </name>
            <time>
               <value>20140901T232600,304-0300</value>
            </time>
            <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
               <name>
                  <value>Tree</value>
               </name>
               <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.symptom.v1">
                  <name>
                     <value>Symptom</value>
                  </name>
                  <archetype_details>
                     <archetype_id>
                        <value>openEHR-EHR-CLUSTER.symptom.v1</value>
                     </archetype_id>
                     <rm_version>1.0.2</rm_version>
                  </archetype_details>
                  <items xsi:type="ELEMENT" archetype_node_id="at0001">
                     <name>
                        <value>Symptom name</value>
                     </name>
                     <value xsi:type="DV_CODED_TEXT">
                        <value>Painful mouth</value>
                        <defining_code>
                           <terminology_id>
                              <value>SNOMED-CT</value>
                           </terminology_id>
                           <code_string>102616008</code_string>
                        </defining_code>
                     </value>
                  </items>
               </items>
            </data>
         </events>
      </data>
   </items>
</content>
<content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.examination_findings_rcp.v1">
   <name>
      <value>Examination findings</value>
   </name>
   <archetype_details>
      <archetype_id>
         <value>openEHR-EHR-SECTION.examination_findings_rcp.v1</value>
      </archetype_id>
      <rm_version>1.0.2</rm_version>
   </archetype_details>
   <items xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.exam.v1">
      <name>
         <value>Physical Examination Findings</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-OBSERVATION.exam.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <language>
         <terminology_id>
            <value>ISO_639-1</value>
         </terminology_id>
         <code_string>en</code_string>
      </language>
      <encoding>
         <terminology_id>
            <value>IANA_character-sets</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_SELF"/>
      <data archetype_node_id="at0001">
         <name>
            <value>Event Series</value>
         </name>
         <origin>
             <value>20140901T232600,304-0300</value>
         </origin>
         <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
            <name>
               <value>Any event</value>
            </name>
            <time>
               <value>20140901T232600,304-0300</value>
            </time>
            <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
               <name>
                  <value>Tree</value>
               </name>
               <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.oral_exam_oduk.v1">
                  <name>
                     <value>Oral examination</value>
                  </name>
                  <archetype_details>
                     <archetype_id>
                        <value>openEHR-EHR-CLUSTER.oral_exam_oduk.v1</value>
                     </archetype_id>
                     <rm_version>1.0.2</rm_version>
                  </archetype_details>
                  <items xsi:type="ELEMENT" archetype_node_id="at0001">
                     <name>
                        <value>Plaque control</value>
                     </name>
                     <value xsi:type="DV_CODED_TEXT">
                        <value>Poor plaque control</value>
                        <defining_code>
                           <terminology_id>
                              <value>local</value>
                           </terminology_id>
                           <code_string>at0003</code_string>
                        </defining_code>
                     </value>
                  </items>
                  <items xsi:type="ELEMENT" archetype_node_id="at0004">
                     <name>
                        <value>Dental swelling</value>
                     </name>
                     <value xsi:type="DV_CODED_TEXT">
                        <value>Dental swelling absent</value>
                        <defining_code>
                           <terminology_id>
                              <value>local</value>
                           </terminology_id>
                           <code_string>at0006</code_string>
                        </defining_code>
                     </value>
                  </items>
                  <items xsi:type="ELEMENT" archetype_node_id="at0007">
                     <name>
                        <value>Teeth with decay</value>
                     </name>
                     <value xsi:type="DV_COUNT">
                        <magnitude>3</magnitude>
                     </value>
                  </items>
               </items>
            </data>
         </events>
      </data>
   </items>
</content>
<content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.assessment_scales_rcp.v1">
   <name>
      <value>Assessment scales</value>
   </name>
   <archetype_details>
      <archetype_id>
         <value>openEHR-EHR-SECTION.assessment_scales_rcp.v1</value>
      </archetype_id>
      <rm_version>1.0.2</rm_version>
   </archetype_details>
   <items xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.dental_rag_score.v1">
      <name>
         <value>Dental RAG score</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-OBSERVATION.dental_rag_score.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <language>
         <terminology_id>
            <value>ISO_639-1</value>
         </terminology_id>
         <code_string>en</code_string>
      </language>
      <encoding>
         <terminology_id>
            <value>IANA_character-sets</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_SELF"/>
      <data archetype_node_id="at0001">
         <name>
            <value>Event Series</value>
         </name>
         <origin>
       <value>20140901T232600,304-0300</value>
         </origin>
         <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
            <name>
               <value>Any event</value>
            </name>
            <time>
              <value>20140901T232600,304-0300</value>
            </time>
            <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
               <name>
                  <value>Tree</value>
               </name>
               <items xsi:type="CLUSTER" archetype_node_id="at0006">
                  <name>
                     <value>Caries (tooth decay)</value>
                  </name>
                  <items xsi:type="ELEMENT" archetype_node_id="at0011">
                     <name>
                        <value>Clinical factors</value>
                     </name>
                     <value xsi:type="DV_CODED_TEXT">
                        <value>Teeth with carious lesions</value>
                        <defining_code>
                           <terminology_id>
                              <value>local</value>
                           </terminology_id>
                           <code_string>at0025</code_string>
                        </defining_code>
                     </value>
                  </items>
                  <items xsi:type="ELEMENT" archetype_node_id="at0010">
                     <name>
                        <value>Patient factors</value>
                     </name>
                     <value xsi:type="DV_CODED_TEXT">
                        <value>Unsatisfactory Plaque control</value>
                        <defining_code>
                           <terminology_id>
                              <value>local</value>
                           </terminology_id>
                           <code_string>at0029</code_string>
                        </defining_code>
                     </value>
                  </items>
                  <items xsi:type="ELEMENT" archetype_node_id="at0010">
                     <name>
                        <value>Patient factors #2</value>
                     </name>
                     <value xsi:type="DV_CODED_TEXT">
                        <value>Sibling experience</value>
                        <defining_code>
                           <terminology_id>
                              <value>local</value>
                           </terminology_id>
                           <code_string>at0030</code_string>
                        </defining_code>
                     </value>
                  </items>
                  <items xsi:type="ELEMENT" archetype_node_id="at0018">
                     <name>
                        <value>Caries risk</value>
                     </name>
                     <value xsi:type="DV_ORDINAL">
                        <value>2</value>
                        <symbol>
                           <value>Red</value>
                           <defining_code>
                              <terminology_id>
                                 <value>local</value>
                              </terminology_id>
                              <code_string>at0024</code_string>
                           </defining_code>
                        </symbol>
                     </value>
                  </items>
               </items>
            </data>
         </events>
      </data>
   </items>
</content>
<content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.investigations_results_rcp.v1">
   <name>
      <value>Investigations and results</value>
   </name>
   <archetype_details>
      <archetype_id>
         <value>openEHR-EHR-SECTION.investigations_results_rcp.v1</value>
      </archetype_id>
      <rm_version>1.0.2</rm_version>
   </archetype_details>
   <items xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.imaging_exam.v1">
      <name>
         <value>Imaging examination result</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-OBSERVATION.imaging_exam.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <language>
         <terminology_id>
            <value>ISO_639-1</value>
         </terminology_id>
         <code_string>en</code_string>
      </language>
      <encoding>
         <terminology_id>
            <value>IANA_character-sets</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_SELF"/>
      <data archetype_node_id="at0001">
         <name>
            <value>Event Series</value>
         </name>
         <origin>
             <value>20140901T232600,304-0300</value>
         </origin>
         <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
            <name>
               <value>Any event</value>
            </name>
            <time>
          <value>20140901T232600,304-0300</value>
            </time>
            <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
               <name>
                  <value>Tree</value>
               </name>
               <items xsi:type="ELEMENT" archetype_node_id="at0004">
                  <name>
                     <value>Examination result name</value>
                  </name>
                  <value xsi:type="DV_CODED_TEXT">
                     <value>Radiography of teeth </value>
                     <defining_code>
                        <terminology_id>
                           <value>SNOMED-CT</value>
                        </terminology_id>
                        <code_string>22891007</code_string>
                     </defining_code>
                  </value>
               </items>
               <items xsi:type="ELEMENT" archetype_node_id="at0007">
                  <name>
                     <value>Overall result status</value>
                  </name>
                  <value xsi:type="DV_CODED_TEXT">
                     <value>Final</value>
                     <defining_code>
                        <terminology_id>
                           <value>local</value>
                        </terminology_id>
                        <code_string>at0011</code_string>
                     </defining_code>
                  </value>
               </items>
               <items xsi:type="CLUSTER" archetype_node_id="at0015">
                  <name>
                     <value>Result group</value>
                  </name>
                  <items xsi:type="CLUSTER" archetype_node_id="at0016">
                     <name>
                        <value>Decayed teeth</value>
                     </name>
                     <items xsi:type="ELEMENT" archetype_node_id="at0017">
                        <name>
                           <value>Decayed teeth</value>
                        </name>
                        <value xsi:type="DV_COUNT">
                           <magnitude>4</magnitude>
                        </value>
                     </items>
                  </items>
                  <items xsi:type="CLUSTER" archetype_node_id="at0016">
                     <name>
                        <value>Teeth with associated abscesses</value>
                     </name>
                     <items xsi:type="ELEMENT" archetype_node_id="at0017">
                        <name>
                           <value>Teeth with associated abscesses</value>
                        </name>
                        <value xsi:type="DV_COUNT">
                           <magnitude>3</magnitude>
                        </value>
                     </items>
                  </items>
               </items>
               <items xsi:type="ELEMENT" archetype_node_id="at0020">
                  <name>
                     <value>Radiological diagnosis</value>
                  </name>
                  <value xsi:type="DV_CODED_TEXT">
                     <value>No pathologic diagnosis</value>
                     <defining_code>
                        <terminology_id>
                           <value>SNOMED-CT</value>
                        </terminology_id>
                        <code_string>23875004</code_string>
                     </defining_code>
                  </value>
               </items>
            </data>
         </events>
      </data>
   </items>
</content>
<content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.plan_requested_actions_rcp.v1">
   <name>
      <value>Plan and requested actions</value>
   </name>
   <archetype_details>
      <archetype_id>
         <value>openEHR-EHR-SECTION.plan_requested_actions_rcp.v1</value>
      </archetype_id>
      <rm_version>1.0.2</rm_version>
   </archetype_details>
   <items xsi:type="EVALUATION" archetype_node_id="openEHR-EHR-EVALUATION.clinical_synopsis.v1">
      <name>
         <value>Plan and requested actions synopsis</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-EVALUATION.clinical_synopsis.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <language>
         <terminology_id>
            <value>ISO_639-1</value>
         </terminology_id>
         <code_string>en</code_string>
      </language>
      <encoding>
         <terminology_id>
            <value>IANA_character-sets</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_SELF"/>
      <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
         <name>
            <value>List</value>
         </name>
         <items xsi:type="ELEMENT" archetype_node_id="at0002">
            <name>
               <value>Synopsis</value>
            </name>
            <value xsi:type="DV_TEXT">
               <value>Preventitive dental care, Dental treatment under GA</value>
            </value>
         </items>
      </data>
   </items>
   <items xsi:type="INSTRUCTION" archetype_node_id="openEHR-EHR-INSTRUCTION.request-follow_up.v1">
      <name>
         <value>Follow Up Request</value>
      </name>
      <uid xsi:type="HIER_OBJECT_ID">
         <value>f36d8e67-6609-40be-ac0f-35f0fe900d1e</value>
      </uid>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-INSTRUCTION.request-follow_up.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <language>
         <terminology_id>
            <value>ISO_639-1</value>
         </terminology_id>
         <code_string>en</code_string>
      </language>
      <encoding>
         <terminology_id>
            <value>IANA_character-sets</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_SELF"/>
      <narrative>
         <value>Dental examination</value>
      </narrative>
      <activities archetype_node_id="at0001">
         <name>
            <value>Request</value>
         </name>
         <description xsi:type="ITEM_TREE" archetype_node_id="at0009">
            <name>
               <value>Tree</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0121.1">
               <name>
                  <value>Follow up Service Requested</value>
               </name>
               <value xsi:type="DV_CODED_TEXT">
                  <value>Dental examination</value>
                  <defining_code>
                     <terminology_id>
                        <value>SNOMED-CT</value>
                     </terminology_id>
                     <code_string>141954009</code_string>
                  </defining_code>
               </value>
            </items>
            <items xsi:type="ELEMENT" archetype_node_id="at0135.1">
               <name>
                  <value>Description of Follow Up</value>
               </name>
               <value xsi:type="DV_TEXT">
                  <value>Review in 4 weeks.</value>
               </value>
            </items>
         </description>
         <timing>
            <value>dummy</value>
            <formalism>dummy</formalism>
         </timing>
         <action_archetype_id>/./</action_archetype_id>
      </activities>
   </items>
</content>
<content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.clinical_summary_rcp.v1">
   <name>
      <value>Clinical Summary</value>
   </name>
   <archetype_details>
      <archetype_id>
         <value>openEHR-EHR-SECTION.clinical_summary_rcp.v1</value>
      </archetype_id>
      <rm_version>1.0.2</rm_version>
   </archetype_details>
   <items xsi:type="EVALUATION" archetype_node_id="openEHR-EHR-EVALUATION.clinical_synopsis.v1">
      <name>
         <value>Clinical Synopsis</value>
      </name>
      <archetype_details>
         <archetype_id>
            <value>openEHR-EHR-EVALUATION.clinical_synopsis.v1</value>
         </archetype_id>
         <rm_version>1.0.2</rm_version>
      </archetype_details>
      <language>
         <terminology_id>
            <value>ISO_639-1</value>
         </terminology_id>
         <code_string>en</code_string>
      </language>
      <encoding>
         <terminology_id>
            <value>IANA_character-sets</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_SELF"/>
      <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
         <name>
            <value>Tree</value>
         </name>
         <items xsi:type="ELEMENT" archetype_node_id="at0002">
            <name>
               <value>Synopsis</value>
            </name>
            <value xsi:type="DV_TEXT">
               <value>Significant dental issues.</value>
            </value>
         </items>
      </data>
   </items> 
</content>
</data>
<lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
      /$
      
      //println params.versions
      
      assert Ehr.count() == 5 : "No hay 5 EHRs como deberia"

      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer UK"
      controller.commit()
      
      //println controller.response.contentAsString
      //println controller.response.text
      
      assert Ehr.get(1).contributions.size() > 0 : "No hay contributions"
      
      Ehr.get(1).contributions.versions.each { version -> // version.data (CompositionIndex)
      
         println "template id: "+ version.data.templateId
      }
      
      // Test response ok
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA"
      
      
      // Test data indexes
      
      def indexJob = new ehr.IndexDataJob()
      indexJob.execute()
      
      assert DataValueIndex.count() == 19 // There are 19 data points indexed
      
      DataValueIndex.list().each {
         println it.getClass().getSimpleName()
      }
      
      
   } // commit uk sample
*/
   
   
   /**
    * 2 commits, en el segundo se cambia un valor COUNT de 3 a 5.
    * El segundo commit va con change_type = amendment
    */
   void testCommitNewVersion()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "tests" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      // Test operational template index created
      ehr.clinical_documents.OperationalTemplateIndex.list().each { opti ->
         
         println "opti: " + opti.templateId
      }
      
      // Test data indexes created
      IndexDefinition.list().each { di ->
         
         println "di: " + di.path
      }
      
      
      
      // =========================================================================
      // PRIMER COMMIT
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      request.contentType = 'text/xml'
      
      // dolar slashy allows GString variables in multiline Strings
      request.xml = $/<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<versions xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><version xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563779</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0011">
              <name>
                <value>Count</value>
              </name>
              <value xsi:type="DV_COUNT">
                <magnitude>3</magnitude>
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
</versions>
      /$
      
      //println params.versions

      
      println "========= COMMIT 1 ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT 1 ========="
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA"
      
      controller.response.reset()
      
      
      // FIN PRIMER COMMIT
      
      // ===============================================================
      // SEGUNDO COMMIT (CUIDADO! el id de la contribution debe ser distinto al de la contrib previa)
      
      
      params.versions = $/<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2562626</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>amendment</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>250</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0011">
              <name>
                <value>Count</value>
              </name>
              <value xsi:type="DV_COUNT">
                <magnitude>5</magnitude>
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
      /$
      
      println "========= COMMIT 2 ========="

      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT 2 ========="
      
      // FIN SEGUNDO COMMIT
      
      
      resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA"
      
      
      //println controller.response.contentAsString
      //println controller.response.text
      
      Ehr.get(1).contributions.versions.each { version -> // version.data (CompositionIndex)
      
         println "template id: "+ version.data.templateId
      }
      
      println "versioned compositions "+ VersionedComposition.count()
      println "versions "+ Version.count()
      println "cotribs "+ Contribution.count()
      
      // ====================================================
      // Verifica cardinalidades de los objetos creados
      assert VersionedComposition.count() == 1
      assert VersionedComposition.get(1).allVersions.size() == 2
      assert VersionedComposition.get(1).latestVersion.versionTreeId == '2'
      //assert VersionedComposition.get(1).latestVersion.uid == "91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::2"
      
      
      
      // =========================================================
      // Crea data indexes para los commits previos
      def indexJob = new IndexDataJob()
      indexJob.execute()
      
      
      // For each version, there are 2 datavalue indexes created
      // one for /category and one for the DV_COUNT ELEMENT.
      VersionedComposition.get(1).allVersions.each { version ->
         assert DataValueIndex.countByOwner(version.data) == 2
         
         /*
         DataValueIndex.findAllByOwner(version.data).each { dvi ->
            println dvi.path
         }
         */
      }
      
      
      // ====================================================
      // Verifica los valores de los objetos creados     
      /* 
      VersionedComposition.list().each { vc ->
         
         vc.allVersions.each { ver ->
          
            println ver.uid
            println ver.commitAudit.changeType
            println "last version ver and data: "+ ver.data.lastVersion
            
            
            // 1 dvi por compoidx
            assert DataValueIndex.countByOwner(ver.data) == 1
            
            
            DataValueIndex.findAllByOwner( ver.data ).each { dataIndex ->
               
               assert dataIndex.getClass().getSimpleName() == "DvCountIndex"
               
               println "count mag: "+ dataIndex.magnitude
            }
            println "============================"
         }
      }
      */
      
      /* Can't test this because 
      // Test query versioned compositions
      assert Query.count() == 1, "A query should exist"
      
      def q = Query.get(1)
      def res = q.executeComposition(Ehr.get(1).ehrId, null, null)
      res.each { compoidx ->
         println compoidx
      }
      
      // FROM CompositionIndex ci
      // WHERE ci.lastVersion=true AND
      //       ci.ehrId = '1f95d3bb-a082-41e0-bbba-339d02dff218' AND 
      
      assert res.size() == 1, "The query should return just on composition index"
      
      
      
      /*
      assert DataValueIndex.count() == 1
      
      def countIdx = DataValueIndex.get(1)
      
      assert countIdx.path == '/content[archetype_id=openEHR-EHR-OBSERVATION.test_all_datatypes.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0011]/value'
      
      assert countIdx.getClass().getSimpleName() == "DvCountIndex"
      
      assert countIdx.magnitude == new Long(3)
      
      println countIdx.magnitude
      */
      
      
   } // Test new version
   
   
   
	
   void testEhrList()
	{
	    // EHRs creados en el setUp
	    assert Ehr.count() == 5
	
	
	    // sin format devuelve XML por defecto
		controller.ehrList()
		
		assert controller.response.contentType == "text/xml;charset=UTF-8"
		
		// pretty print del XML
		// Se puede cambiar la opcion en Config: 
		// test {
	    //   grails.converters.default.pretty.print = true
	    // }
		println groovy.xml.XmlUtil.serialize( controller.response.text )
		//println controller.response.text
		
		// ehrs debe tener 5 tags ehr
		// con .text es solo el texto, con .xml es el xml :)
		assert controller.response.xml.ehrs.ehr.size() == 5
		response.reset()
		
		
		// para que withFormat considere el param format> controller.request.format = 'json' 
		// http://grails.1312388.n4.nabble.com/withFormat-tests-in-ControllerUnitTestCase-td3343763.html
		params.format = 'json'
		controller.ehrList()
		assert controller.response.contentType == 'application/json;charset=UTF-8'
		println controller.response.text
		
		// json es un array y debe tener 5 objetos
		assert controller.response.json.ehrs.size() == 5
		response.reset()
		
		
		// Debe tirar error en XML porque no es un formato recocnocido
		params.format = 'text'
		controller.ehrList()
		//println controller.response.text
		//println groovy.xml.XmlUtil.serialize( controller.response.text )
      /*
       * <?xml version="1.0" encoding="UTF-8" ?><result>
           <code>error</code>
           <message>formato 'text' no reconocido, debe ser exactamente 'xml' o 'json'</message>
         </result>
       */
		assert controller.response.xml.code.text() == "error"
		response.reset()
		
		
		// Prueba paginacion con max=3
		params.format = 'xml'
		params.max = 3
		controller.ehrList()
		assert controller.response.xml.ehrs.ehr.size() == 3
		response.reset()
		
		// Prueba paginacion con offset=3 debe devolver 2 ehrs porque hay 5
		params.format = 'xml'
		params.offset = 3
		controller.ehrList()
		assert controller.response.xml.ehrs.ehr.size() == 2
		response.reset()
    }
   
   
   
   
   void testCommitAmendmentWithNoVersionedComposition()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "tests" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      

      // COMMIT
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = $/<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563779</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>amendment</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>250</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0011">
              <name>
                <value>Count</value>
              </name>
              <value xsi:type="DV_COUNT">
                <magnitude>3</magnitude>
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
      /$

      
      println "========= COMMIT ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT ========="
      
      println controller.response.contentAsString
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AR" // Application Reject
      
      println resp.message.text()

      // TODO: it should not create versions or contributions.
      
   } // Test commit non existing versioned object with change type=modification
   
   
   
   
   /**
    * One commit of two versions with different contributionUID (should return an error)
    */
   void testCommit2VersionWithDifferentContributionUID()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "tests" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      // Test operational template index created
      ehr.clinical_documents.OperationalTemplateIndex.list().each { opti ->
         
         println "opti: " + opti.templateId
      }
      
      // Test data indexes created
      IndexDefinition.list().each { di ->
         
         println "di: " + di.path
      }
      
      
      
      // =========================================================================
      // PRIMER COMMIT
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = [
$/<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563779</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0011">
              <name>
                <value>Count</value>
              </name>
              <value xsi:type="DV_COUNT">
                <magnitude>3</magnitude>
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>/$
,
$/<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
   <version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ORIGINAL_VERSION">
     <contribution>
       <id xsi:type="HIER_OBJECT_ID">
         <value>ad6866e1-fb08-4e9b-a93b-5095a2562626</value>
       </id>
       <namespace>EHR::COMMON</namespace>
       <type>CONTRIBUTION</type>
     </contribution>
     <commit_audit>
       <system_id>CABOLABS_EHR</system_id>
       <committer xsi:type="PARTY_IDENTIFIED">
         <name>Dr. Pablo Pazos</name>
       </committer>
       <time_committed>
         <value>20140901T233114,065-0300</value>
       </time_committed>
       <change_type>
         <value>amendment</value>
         <defining_code>
           <terminology_id>
             <value>openehr</value>
           </terminology_id>
           <code_string>250</code_string>
         </defining_code>
       </change_type>
     </commit_audit>
     <uid>
       <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
     </uid>
     <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
       <name>
         <value>Test all datatypes</value>
       </name>
       <archetype_details>
         <archetype_id>
           <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
         </archetype_id>
         <template_id>
           <value>Test all datatypes</value>
         </template_id>
         <rm_version>1.0.2</rm_version>
       </archetype_details>
       <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <territory>
         <terminology_id>
           <value>ISO_3166-1</value>
         </terminology_id>
         <code_string>UY</code_string>
       </territory>
       <category>
         <value>event</value>
         <defining_code>
           <terminology_id>
             <value>openehr</value>
           </terminology_id>
           <code_string>443</code_string>
         </defining_code>
       </category>
       <composer xsi:type="PARTY_IDENTIFIED">
         <name>Dr. Pablo Pazos</name>
       </composer>
       <context>
         <start_time>
           <value>20140901T232600,304-0300</value>
         </start_time>
         <setting>
           <value>Hospital Montevideo</value>
           <defining_code>
             <terminology_id>
               <value>openehr</value>
             </terminology_id>
             <code_string>229</code_string>
           </defining_code>
         </setting>
       </context>
       <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
         <name>
           <value>Blood Pressure</value>
         </name>
         <language>
            <terminology_id>
              <value>ISO_639-1</value>
            </terminology_id>
            <code_string>es</code_string>
          </language>
          <encoding>
            <terminology_id>
              <value>UNICODE</value>
            </terminology_id>
            <code_string>UTF-8</code_string>
         </encoding>
         <subject xsi:type="PARTY_IDENTIFIED">
           <external_ref>
             <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
             <namespace>DEMOGRAPHIC</namespace>
             <type>PERSON</type>
           </external_ref>
         </subject>
         <data xsi:type="HISTORY" archetype_node_id="at0001">
           <name>
             <value>history</value>
           </name>
           <origin>
             <value>20140101</value>
           </origin>
           <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
             <name>
               <value>any event</value>
             </name>
             <time><value>20140101</value></time>
             <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
               <name>
                 <value>Arbol</value>
               </name>
               <items xsi:type="ELEMENT" archetype_node_id="at0011">
                 <name>
                   <value>Count</value>
                 </name>
                 <value xsi:type="DV_COUNT">
                   <magnitude>5</magnitude>
                 </value>
               </items>
             </data>
           </events>
         </data>
       </content>
     </data>
     <lifecycle_state>
       <value>completed</value>
       <defining_code>
         <terminology_id>
           <value>openehr</value>
         </terminology_id>
         <code_string>532</code_string>
       </defining_code>
     </lifecycle_state>
   </version>/$
]
      
      //println params.versions

      
      println "========= COMMIT 1 ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT 1 ========="
      
      println "RESP: "+ controller.response.contentAsString
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AR"
      
      controller.response.reset()
      
      
      // FIN COMMIT
  
   } // Test 2 versions with different contribution uid
   
   
   void testCommitHugeCompo()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "tests" + PS + "RIPPLE - Conformance Test template.opt" )
      oti.index(opt)
      
      request.method = 'POST'
      request.contentType = 'text/xml'
      request.xml = $/<?xml version="1.0" encoding="UTF-8" ?>
<versions xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<version xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563789</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <external_ref>
        <id xsi:type="HIER_OBJECT_ID">
          <value>cc193f71-f5fe-438a-87f9-81ecb302eede</value>
        </id>
        <namespace>DEMOGRAPHIC</namespace>
        <type>PERSON</type>
      </external_ref>
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>f16dd9db-b2cd-4e68-b08d-38bea43751b9::ripple_osi.ehrscape.c4h::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1">
        <name>
            <value>Encounter</value>
        </name>
        <uid xsi:type="OBJECT_VERSION_ID">
            <value>f16dd9db-b2cd-4e68-b08d-38bea43751b9::ripple_osi.ehrscape.c4h::1</value>
        </uid>
        <archetype_details>
            <archetype_id>
                <value>openEHR-EHR-COMPOSITION.encounter.v1</value>
            </archetype_id>
            <template_id>
                <value>RIPPLE - Conformance Test template</value>
            </template_id>
            <rm_version>1.0.1</rm_version>
        </archetype_details>
        <language>
            <terminology_id>
                <value>ISO_639-1</value>
            </terminology_id>
            <code_string>en</code_string>
        </language>
        <territory>
            <terminology_id>
                <value>ISO_3166-1</value>
            </terminology_id>
            <code_string>GB</code_string>
        </territory>
        <category>
            <value>event</value>
            <defining_code>
                <terminology_id>
                    <value>openehr</value>
                </terminology_id>
                <code_string>433</code_string>
            </defining_code>
        </category>
        <composer xsi:type="PARTY_IDENTIFIED">
            <name>Silvia Blake</name>
        </composer>
        <context>
            <start_time>
                <value>2015-12-02T17:41:56.809Z</value>
            </start_time>
            <setting>
                <value>other care</value>
                <defining_code>
                    <terminology_id>
                        <value>openehr</value>
                    </terminology_id>
                    <code_string>238</code_string>
                </defining_code>
            </setting>
            <other_context xsi:type="ITEM_TREE" archetype_node_id="at0001">
                <name>
                    <value>Tree</value>
                </name>
                <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.composition_context_detail.v1">
                    <name>
                        <value>Context detail</value>
                    </name>
                    <archetype_details>
                        <archetype_id>
                            <value>openEHR-EHR-CLUSTER.composition_context_detail.v1</value>
                        </archetype_id>
                        <rm_version>1.0.1</rm_version>
                    </archetype_details>
                    <items xsi:type="ELEMENT" archetype_node_id="at0001">
                        <name>
                            <value>Period of care identifier</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Ident. 52</value>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0008">
                        <name>
                            <value>Tags</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Tags 96</value>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0008">
                        <name>
                            <value>Tags #2</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Tags 96</value>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0009">
                        <name>
                            <value>Attachment</value>
                        </name>
                        <value xsi:type="DV_MULTIMEDIA">
                            <alternate_text>alternate text</alternate_text>
                            <uri>
                                <value>http://med.tube.com/sample</value>
                            </uri>
                            <media_type>
                                <terminology_id>
                                    <value>IANA_media-types</value>
                                </terminology_id>
                                <code_string>video/mp4</code_string>
                            </media_type>
                            <size>504903212</size>
                        </value>
                    </items>
                </items>
            </other_context>
            <health_care_facility>
                <external_ref>
                    <id xsi:type="GENERIC_ID">
                        <value>9091</value>
                        <scheme>HOSPITAL-NS</scheme>
                    </id>
                    <namespace>HOSPITAL-NS</namespace>
                    <type>PARTY</type>
                </external_ref>
                <name>Hospital</name>
            </health_care_facility>
        </context>
        <content xsi:type="SECTION" archetype_node_id="openEHR-EHR-SECTION.adhoc.v1">
            <name>
                <value>Ad hoc heading</value>
            </name>
            <archetype_details>
                <archetype_id>
                    <value>openEHR-EHR-SECTION.adhoc.v1</value>
                </archetype_id>
                <rm_version>1.0.1</rm_version>
            </archetype_details>
            <items xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.pulse.v1">
                <name>
                    <value>Pulse</value>
                </name>
                <archetype_details>
                    <archetype_id>
                        <value>openEHR-EHR-OBSERVATION.pulse.v1</value>
                    </archetype_id>
                    <rm_version>1.0.1</rm_version>
                </archetype_details>
                <language>
                    <terminology_id>
                        <value>ISO_639-1</value>
                    </terminology_id>
                    <code_string>en</code_string>
                </language>
                <encoding>
                    <terminology_id>
                        <value>IANA_character-sets</value>
                    </terminology_id>
                    <code_string>UTF-8</code_string>
                </encoding>
                <subject xsi:type="PARTY_SELF"/>
                <protocol xsi:type="ITEM_TREE" archetype_node_id="at0010">
                    <name>
                        <value>List</value>
                    </name>
                    <items xsi:type="ELEMENT" archetype_node_id="at1019">
                        <name>
                            <value>Method</value>
                        </name>
                        <value xsi:type="DV_CODED_TEXT">
                            <value>Device</value>
                            <defining_code>
                                <terminology_id>
                                    <value>local</value>
                                </terminology_id>
                                <code_string>at1034</code_string>
                            </defining_code>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at1037">
                        <name>
                            <value>Findings Location</value>
                        </name>
                        <value xsi:type="DV_CODED_TEXT">
                            <value>Femoral Artery - Left</value>
                            <defining_code>
                                <terminology_id>
                                    <value>local</value>
                                </terminology_id>
                                <code_string>at1043</code_string>
                            </defining_code>
                        </value>
                    </items>
                    <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.device.v1">
                        <name>
                            <value>Medical Device</value>
                        </name>
                        <archetype_details>
                            <archetype_id>
                                <value>openEHR-EHR-CLUSTER.device.v1</value>
                            </archetype_id>
                            <rm_version>1.0.1</rm_version>
                        </archetype_details>
                        <items xsi:type="ELEMENT" archetype_node_id="at0001">
                            <name>
                                <value>Device name</value>
                            </name>
                            <value xsi:type="DV_TEXT">
                                <value>Device name 33</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0021">
                            <name>
                                <value>Unique device identifier (UDI)</value>
                            </name>
                            <value xsi:type="DV_IDENTIFIER">
                                <issuer>Issuer</issuer>
                                <assigner>Assigner</assigner>
                                <id>59466e78-d15c-4765-87da-20e56f1413a6</id>
                                <type>Prescription</type>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0004">
                            <name>
                                <value>Manufacturer</value>
                            </name>
                            <value xsi:type="DV_TEXT">
                                <value>Manufacturer 6</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0005">
                            <name>
                                <value>Date of manufacture</value>
                            </name>
                            <value xsi:type="DV_DATE_TIME">
                                <value>2015-12-02T17:41:56.811Z</value>
                            </value>
                        </items>
                    </items>
                </protocol>
                <data archetype_node_id="at0002">
                    <name>
                        <value>history</value>
                    </name>
                    <origin>
                        <value>2015-12-02T17:41:56.809Z</value>
                    </origin>
                    <events xsi:type="POINT_EVENT" archetype_node_id="at0003">
                        <name>
                            <value>First event</value>
                        </name>
                        <time>
                            <value>2015-12-02T17:41:56.809Z</value>
                        </time>
                        <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
                            <name>
                                <value>structure</value>
                            </name>
                            <items xsi:type="ELEMENT" archetype_node_id="at0004">
                                <name xsi:type="DV_CODED_TEXT">
                                    <value>Heart Rate</value>
                                    <defining_code>
                                        <terminology_id>
                                            <value>local</value>
                                        </terminology_id>
                                        <code_string>at1027</code_string>
                                    </defining_code>
                                </name>
                                <value xsi:type="DV_QUANTITY">
                                    <magnitude>53.0</magnitude>
                                    <units>/min</units>
                                    <precision>0</precision>
                                </value>
                            </items>
                            <items xsi:type="ELEMENT" archetype_node_id="at0005">
                                <name>
                                    <value>Regularity</value>
                                </name>
                                <value xsi:type="DV_CODED_TEXT">
                                    <value>Regularly Irregular</value>
                                    <defining_code>
                                        <terminology_id>
                                            <value>local</value>
                                        </terminology_id>
                                        <code_string>at0007</code_string>
                                    </defining_code>
                                </value>
                            </items>
                            <items xsi:type="ELEMENT" archetype_node_id="at1022">
                                <name>
                                    <value>Clinical Description</value>
                                </name>
                                <value xsi:type="DV_TEXT">
                                    <value>Clinical Description 14</value>
                                </value>
                            </items>
                            <items xsi:type="ELEMENT" archetype_node_id="at1023">
                                <name>
                                    <value>Clinical Interpretation</value>
                                </name>
                                <value xsi:type="DV_TEXT">
                                    <value>Clinical Interpretation 23</value>
                                </value>
                            </items>
                        </data>
                    </events>
                    <events xsi:type="POINT_EVENT" archetype_node_id="at0003">
                        <name>
                            <value>Second event</value>
                        </name>
                        <time>
                            <value>2015-12-02T17:41:56.809Z</value>
                        </time>
                        <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
                            <name>
                                <value>structure</value>
                            </name>
                            <items xsi:type="ELEMENT" archetype_node_id="at1005">
                                <name>
                                    <value>Pulse Presence</value>
                                </name>
                                <value xsi:type="DV_CODED_TEXT">
                                    <value>Present</value>
                                    <defining_code>
                                        <terminology_id>
                                            <value>local</value>
                                        </terminology_id>
                                        <code_string>at1024</code_string>
                                    </defining_code>
                                </value>
                            </items>
                            <items xsi:type="ELEMENT" archetype_node_id="at0004">
                                <name xsi:type="DV_CODED_TEXT">
                                    <value>Pulse Rate</value>
                                    <defining_code>
                                        <terminology_id>
                                            <value>local</value>
                                        </terminology_id>
                                        <code_string>at1026</code_string>
                                    </defining_code>
                                </name>
                                <value xsi:type="DV_QUANTITY">
                                    <magnitude>3.0</magnitude>
                                    <units>/min</units>
                                    <precision>0</precision>
                                </value>
                            </items>
                            <items xsi:type="ELEMENT" archetype_node_id="at0005">
                                <name>
                                    <value>Regularity</value>
                                </name>
                                <value xsi:type="DV_CODED_TEXT">
                                    <value>Regularly Irregular</value>
                                    <defining_code>
                                        <terminology_id>
                                            <value>local</value>
                                        </terminology_id>
                                        <code_string>at0007</code_string>
                                    </defining_code>
                                </value>
                            </items>
                            <items xsi:type="ELEMENT" archetype_node_id="at1023">
                                <name>
                                    <value>Clinical Interpretation</value>
                                </name>
                                <value xsi:type="DV_TEXT">
                                    <value>Clinical Interpretation 34</value>
                                </value>
                            </items>
                        </data>
                    </events>
                    <events xsi:type="INTERVAL_EVENT" archetype_node_id="at1036">
                        <name>
                            <value>Maximum</value>
                        </name>
                        <time>
                            <value>2015-12-02T17:41:56.809Z</value>
                        </time>
                        <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
                            <name>
                                <value>structure</value>
                            </name>
                            <items xsi:type="ELEMENT" archetype_node_id="at0004">
                                <name xsi:type="DV_CODED_TEXT">
                                    <value>Heart Rate</value>
                                    <defining_code>
                                        <terminology_id>
                                            <value>local</value>
                                        </terminology_id>
                                        <code_string>at1027</code_string>
                                    </defining_code>
                                </name>
                                <value xsi:type="DV_QUANTITY">
                                    <magnitude>16.0</magnitude>
                                    <units>/min</units>
                                    <precision>0</precision>
                                </value>
                            </items>
                        </data>
                        <state xsi:type="ITEM_TREE" archetype_node_id="at0012">
                            <name>
                                <value>List</value>
                            </name>
                            <items xsi:type="ELEMENT" archetype_node_id="at0013">
                                <name>
                                    <value>Position</value>
                                </name>
                                <value xsi:type="DV_CODED_TEXT">
                                    <value>Sitting</value>
                                    <defining_code>
                                        <terminology_id>
                                            <value>local</value>
                                        </terminology_id>
                                        <code_string>at1001</code_string>
                                    </defining_code>
                                </value>
                            </items>
                            <items xsi:type="ELEMENT" archetype_node_id="at1018">
                                <name>
                                    <value>Confounding Factors</value>
                                </name>
                                <value xsi:type="DV_TEXT">
                                    <value>Confounding Factors 16</value>
                                </value>
                            </items>
                        </state>
                        <width>
                            <value>P1DT11H11M</value>
                        </width>
                        <math_function>
                            <value>maximum</value>
                            <defining_code>
                                <terminology_id>
                                    <value>openehr</value>
                                </terminology_id>
                                <code_string>144</code_string>
                            </defining_code>
                        </math_function>
                    </events>
                </data>
            </items>
            <items xsi:type="EVALUATION" archetype_node_id="openEHR-EHR-EVALUATION.cpr_decision_uk.v1">
                <name>
                    <value>CPR decision</value>
                </name>
                <archetype_details>
                    <archetype_id>
                        <value>openEHR-EHR-EVALUATION.cpr_decision_uk.v1</value>
                    </archetype_id>
                    <rm_version>1.0.1</rm_version>
                </archetype_details>
                <language>
                    <terminology_id>
                        <value>ISO_639-1</value>
                    </terminology_id>
                    <code_string>en</code_string>
                </language>
                <encoding>
                    <terminology_id>
                        <value>IANA_character-sets</value>
                    </terminology_id>
                    <code_string>UTF-8</code_string>
                </encoding>
                <subject xsi:type="PARTY_SELF"/>
                <protocol xsi:type="ITEM_TREE" archetype_node_id="at0010">
                    <name>
                        <value>Tree</value>
                    </name>
                    <items xsi:type="ELEMENT" archetype_node_id="at0014">
                        <name>
                            <value>Discussion with informal carer</value>
                        </name>
                        <value xsi:type="DV_CODED_TEXT">
                            <value>Resuscitation not discussed with informal carer</value>
                            <defining_code>
                                <terminology_id>
                                    <value>local</value>
                                </terminology_id>
                                <code_string>at0024</code_string>
                            </defining_code>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0009">
                        <name>
                            <value>Date for review of CPR decision</value>
                        </name>
                        <value xsi:type="DV_DATE_TIME">
                            <value>2015-12-02T17:41:56.811Z</value>
                        </value>
                    </items>
                </protocol>
                <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
                    <name>
                        <value>Tree</value>
                    </name>
                    <items xsi:type="ELEMENT" archetype_node_id="at0003">
                        <name>
                            <value>CPR decision</value>
                        </name>
                        <value xsi:type="DV_CODED_TEXT">
                            <value>CPR decision status unknown</value>
                            <defining_code>
                                <terminology_id>
                                    <value>local</value>
                                </terminology_id>
                                <code_string>at0022</code_string>
                            </defining_code>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0002">
                        <name>
                            <value>Date of CPR decision</value>
                        </name>
                        <value xsi:type="DV_DATE_TIME">
                            <value>2015-12-02T17:41:56.811Z</value>
                        </value>
                    </items>
                </data>
            </items>
            <items xsi:type="INSTRUCTION" archetype_node_id="openEHR-EHR-INSTRUCTION.request-procedure.v1">
                <name>
                    <value>Procedure request</value>
                </name>
                <uid xsi:type="HIER_OBJECT_ID">
                    <value>9a3871f8-8105-44f9-a06c-5626acaf40a3</value>
                </uid>
                <archetype_details>
                    <archetype_id>
                        <value>openEHR-EHR-INSTRUCTION.request-procedure.v1</value>
                    </archetype_id>
                    <rm_version>1.0.1</rm_version>
                </archetype_details>
                <language>
                    <terminology_id>
                        <value>ISO_639-1</value>
                    </terminology_id>
                    <code_string>en</code_string>
                </language>
                <encoding>
                    <terminology_id>
                        <value>IANA_character-sets</value>
                    </terminology_id>
                    <code_string>UTF-8</code_string>
                </encoding>
                <subject xsi:type="PARTY_SELF"/>
                <other_participations>
                    <function>
                        <value>performer</value>
                    </function>
                    <performer xsi:type="PARTY_IDENTIFIED">
                        <name>Nurse Bailey</name>
                    </performer>
                    <mode>
                        <value>not specified</value>
                        <defining_code>
                            <terminology_id>
                                <value>openehr</value>
                            </terminology_id>
                            <code_string>193</code_string>
                        </defining_code>
                    </mode>
                </other_participations>
                <protocol xsi:type="ITEM_TREE" archetype_node_id="at0008">
                    <name>
                        <value>Tree</value>
                    </name>
                    <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.distribution.v1">
                        <name>
                            <value>Distribution</value>
                        </name>
                        <archetype_details>
                            <archetype_id>
                                <value>openEHR-EHR-CLUSTER.distribution.v1</value>
                            </archetype_id>
                            <rm_version>1.0.1</rm_version>
                        </archetype_details>
                        <items xsi:type="ELEMENT" archetype_node_id="at0008">
                            <name>
                                <value>Group category</value>
                            </name>
                            <value xsi:type="DV_TEXT">
                                <value>Group category 26</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0012">
                            <name>
                                <value>Urgent</value>
                            </name>
                            <value xsi:type="DV_BOOLEAN">
                                <value>true</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0006">
                            <name>
                                <value>Date sent</value>
                            </name>
                            <value xsi:type="DV_DATE_TIME">
                                <value>2015-12-02T17:41:56.812Z</value>
                            </value>
                        </items>
                    </items>
                </protocol>
                <narrative>
                    <value>Human readable instruction narrative</value>
                </narrative>
                <activities archetype_node_id="at0001">
                    <name>
                        <value>Request</value>
                    </name>
                    <description xsi:type="ITEM_TREE" archetype_node_id="at0009">
                        <name>
                            <value>Tree</value>
                        </name>
                        <items xsi:type="ELEMENT" archetype_node_id="at0121">
                            <name>
                                <value>Procedure requested</value>
                            </name>
                            <value xsi:type="DV_TEXT">
                                <value>Procedure requested 79</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0144">
                            <name>
                                <value>Latest date service required</value>
                            </name>
                            <value xsi:type="DV_DATE_TIME">
                                <value>2015-12-02T17:41:56.812Z</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0076">
                            <name>
                                <value>Supplementary information to follow</value>
                            </name>
                            <value xsi:type="DV_BOOLEAN">
                                <value>true</value>
                            </value>
                        </items>
                    </description>
                    <timing>
                        <value>R2/2015-12-02T17:00:00Z/P3M</value>
                        <formalism>timing</formalism>
                    </timing>
                    <action_archetype_id>/.*/</action_archetype_id>
                </activities>
                <activities archetype_node_id="at0001">
                    <name>
                        <value>Request #3</value>
                    </name>
                    <description xsi:type="ITEM_TREE" archetype_node_id="at0009">
                        <name>
                            <value>Tree</value>
                        </name>
                        <items xsi:type="ELEMENT" archetype_node_id="at0121">
                            <name>
                                <value>Procedure requested</value>
                            </name>
                            <value xsi:type="DV_TEXT">
                                <value>Procedure requested 84</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0144">
                            <name>
                                <value>Latest date service required</value>
                            </name>
                            <value xsi:type="DV_DATE_TIME">
                                <value>2015-12-02T17:41:56.812Z</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0076">
                            <name>
                                <value>Supplementary information to follow</value>
                            </name>
                            <value xsi:type="DV_BOOLEAN">
                                <value>true</value>
                            </value>
                        </items>
                    </description>
                    <timing>
                        <value>R2/2015-12-02T17:00:00Z/P1M</value>
                        <formalism>timing</formalism>
                    </timing>
                    <action_archetype_id>/.*/</action_archetype_id>
                </activities>
            </items>
            <items xsi:type="ACTION" archetype_node_id="openEHR-EHR-ACTION.procedure.v1">
                <name>
                    <value>Procedure</value>
                </name>
                <archetype_details>
                    <archetype_id>
                        <value>openEHR-EHR-ACTION.procedure.v1</value>
                    </archetype_id>
                    <rm_version>1.0.1</rm_version>
                </archetype_details>
                <language>
                    <terminology_id>
                        <value>ISO_639-1</value>
                    </terminology_id>
                    <code_string>en</code_string>
                </language>
                <encoding>
                    <terminology_id>
                        <value>IANA_character-sets</value>
                    </terminology_id>
                    <code_string>UTF-8</code_string>
                </encoding>
                <subject xsi:type="PARTY_SELF"/>
                <protocol xsi:type="ITEM_TREE" archetype_node_id="at0053">
                    <name>
                        <value>Tree</value>
                    </name>
                    <items xsi:type="ELEMENT" archetype_node_id="at0054">
                        <name>
                            <value>Requestor order identifier</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Ident. 38</value>
                        </value>
                    </items>
                    <items xsi:type="CLUSTER" archetype_node_id="openEHR-EHR-CLUSTER.person_name.v1">
                        <name>
                            <value>Person name</value>
                        </name>
                        <archetype_details>
                            <archetype_id>
                                <value>openEHR-EHR-CLUSTER.person_name.v1</value>
                            </archetype_id>
                            <rm_version>1.0.1</rm_version>
                        </archetype_details>
                        <items xsi:type="ELEMENT" archetype_node_id="at0006">
                            <name>
                                <value>Name type</value>
                            </name>
                            <value xsi:type="DV_CODED_TEXT">
                                <value>AKA</value>
                                <defining_code>
                                    <terminology_id>
                                        <value>local</value>
                                    </terminology_id>
                                    <code_string>at0010</code_string>
                                </defining_code>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0022">
                            <name>
                                <value>Preferred name</value>
                            </name>
                            <value xsi:type="DV_BOOLEAN">
                                <value>true</value>
                            </value>
                        </items>
                        <items xsi:type="ELEMENT" archetype_node_id="at0001">
                            <name>
                                <value>Unstructured name</value>
                            </name>
                            <value xsi:type="DV_TEXT">
                                <value>Unstructured name 16</value>
                            </value>
                        </items>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0056">
                        <name>
                            <value>Receiver order identifier</value>
                        </name>
                        <value xsi:type="DV_IDENTIFIER">
                            <issuer>Issuer</issuer>
                            <assigner>Assigner</assigner>
                            <id>8fb20ec3-b285-4ab8-acd4-25bcd58cbd24</id>
                            <type>Prescription</type>
                        </value>
                    </items>
                </protocol>
                <time>
                    <value>2015-12-02T17:41:56.813Z</value>
                </time>
                <description xsi:type="ITEM_TREE" archetype_node_id="at0001">
                    <name>
                        <value>Tree</value>
                    </name>
                    <items xsi:type="ELEMENT" archetype_node_id="at0002">
                        <name>
                            <value>Procedure name</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Procedure name 40</value>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0065">
                        <name xsi:type="DV_CODED_TEXT">
                            <value>Run-time coded name</value>
                            <defining_code>
                                <terminology_id>
                                    <value>SNOMED-CT</value>
                                </terminology_id>
                                <code_string>70901006</code_string>
                            </defining_code>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Method 0</value>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0065">
                        <name>
                            <value>Method #1</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Method #1 94</value>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0066">
                        <name>
                            <value>Scheduled date/time</value>
                        </name>
                        <value xsi:type="DV_DATE_TIME">
                            <value>2015-12-02T17:41:56.812Z</value>
                        </value>
                    </items>
                </description>
                <ism_transition>
                    <current_state>
                        <value>initial</value>
                        <defining_code>
                            <terminology_id>
                                <value>openehr</value>
                            </terminology_id>
                            <code_string>524</code_string>
                        </defining_code>
                    </current_state>
                </ism_transition>
            </items>
            <items xsi:type="ADMIN_ENTRY" archetype_node_id="openEHR-EHR-ADMIN_ENTRY.inpatient_admission_uk.v1">
                <name>
                    <value>Inpatient admission</value>
                </name>
                <archetype_details>
                    <archetype_id>
                        <value>openEHR-EHR-ADMIN_ENTRY.inpatient_admission_uk.v1</value>
                    </archetype_id>
                    <rm_version>1.0.1</rm_version>
                </archetype_details>
                <language>
                    <terminology_id>
                        <value>ISO_639-1</value>
                    </terminology_id>
                    <code_string>en</code_string>
                </language>
                <encoding>
                    <terminology_id>
                        <value>IANA_character-sets</value>
                    </terminology_id>
                    <code_string>UTF-8</code_string>
                </encoding>
                <subject xsi:type="PARTY_SELF"/>
                <data xsi:type="ITEM_TREE" archetype_node_id="at0001">
                    <name>
                        <value>Tree</value>
                    </name>
                    <items xsi:type="ELEMENT" archetype_node_id="at0002">
                        <name>
                            <value>Date of admission</value>
                        </name>
                        <value xsi:type="DV_DATE_TIME">
                            <value>2015-12-02T17:41:56.813Z</value>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0003">
                        <name>
                            <value>Admission method</value>
                        </name>
                        <value xsi:type="DV_CODED_TEXT">
                            <value>D.80 description</value>
                            <defining_code>
                                <terminology_id>
                                    <value>external_terminology</value>
                                </terminology_id>
                                <code_string>D.80</code_string>
                            </defining_code>
                        </value>
                    </items>
                    <items xsi:type="ELEMENT" archetype_node_id="at0009">
                        <name>
                            <value>Source of admission</value>
                        </name>
                        <value xsi:type="DV_TEXT">
                            <value>Source of admission 42</value>
                        </value>
                    </items>
                </data>
            </items>
            <items xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.demo.v1">
                <name>
                    <value>Demonstration</value>
                </name>
                <archetype_details>
                    <archetype_id>
                        <value>openEHR-EHR-OBSERVATION.demo.v1</value>
                    </archetype_id>
                    <rm_version>1.0.1</rm_version>
                </archetype_details>
                <language>
                    <terminology_id>
                        <value>ISO_639-1</value>
                    </terminology_id>
                    <code_string>en</code_string>
                </language>
                <encoding>
                    <terminology_id>
                        <value>IANA_character-sets</value>
                    </terminology_id>
                    <code_string>UTF-8</code_string>
                </encoding>
                <subject xsi:type="PARTY_SELF"/>
                <data archetype_node_id="at0001">
                    <name>
                        <value>Event Series</value>
                    </name>
                    <origin>
                        <value>2015-12-02T17:41:56.809Z</value>
                    </origin>
                    <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
                        <name>
                            <value>Any event</value>
                        </name>
                        <time>
                            <value>2015-12-02T17:41:56.809Z</value>
                        </time>
                        <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
                            <name>
                                <value>Tree</value>
                            </name>
                            <items xsi:type="CLUSTER" archetype_node_id="at0004">
                                <name>
                                    <value>Heading1</value>
                                </name>
                                <items xsi:type="ELEMENT" archetype_node_id="at0005">
                                    <name>
                                        <value>Free text or coded</value>
                                    </name>
                                    <value xsi:type="DV_TEXT">
                                        <value>Free text or coded 33</value>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0006">
                                    <name>
                                        <value>Text that uses Internal codes</value>
                                    </name>
                                    <value xsi:type="DV_CODED_TEXT">
                                        <value>Reclining</value>
                                        <defining_code>
                                            <terminology_id>
                                                <value>local</value>
                                            </terminology_id>
                                            <code_string>at0008</code_string>
                                        </defining_code>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0011">
                                    <name>
                                        <value>Text that is sourced from an external terminology</value>
                                    </name>
                                    <value xsi:type="DV_CODED_TEXT">
                                        <value>T.38 description</value>
                                        <defining_code>
                                            <terminology_id>
                                                <value>external_terminology</value>
                                            </terminology_id>
                                            <code_string>T.38</code_string>
                                        </defining_code>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0012">
                                    <name>
                                        <value>Quantity</value>
                                    </name>
                                    <value xsi:type="DV_QUANTITY">
                                        <magnitude>96.63</magnitude>
                                        <units>cm</units>
                                        <precision>1</precision>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0023">
                                    <name>
                                        <value>Interval of Quantity</value>
                                    </name>
                                    <value xsi:type="DV_INTERVAL">
                                        <lower xsi:type="DV_QUANTITY">
                                            <magnitude>5.66</magnitude>
                                            <units>cm</units>
                                        </lower>
                                        <upper xsi:type="DV_QUANTITY">
                                            <magnitude>62.91</magnitude>
                                            <units>cm</units>
                                        </upper>
                                        <lower_unbounded>false</lower_unbounded>
                                        <upper_unbounded>false</upper_unbounded>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0013">
                                    <name>
                                        <value>Count</value>
                                    </name>
                                    <value xsi:type="DV_COUNT">
                                        <magnitude>908</magnitude>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0022">
                                    <name>
                                        <value>Interval of Integer</value>
                                    </name>
                                    <value xsi:type="DV_INTERVAL">
                                        <lower xsi:type="DV_COUNT">
                                            <magnitude>10</magnitude>
                                        </lower>
                                        <upper xsi:type="DV_COUNT">
                                            <magnitude>4</magnitude>
                                        </upper>
                                        <lower_unbounded>false</lower_unbounded>
                                        <upper_unbounded>false</upper_unbounded>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0028">
                                    <name>
                                        <value>Proportion</value>
                                    </name>
                                    <value xsi:type="DV_PROPORTION">
                                        <numerator>58.0</numerator>
                                        <denominator>100.0</denominator>
                                        <type>2</type>
                                        <precision>0</precision>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0014">
                                    <name>
                                        <value>Date/Time</value>
                                    </name>
                                    <value xsi:type="DV_DATE_TIME">
                                        <value>2015-12-02T17:41:56.816Z</value>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0024">
                                    <name>
                                        <value>Interval of Date</value>
                                    </name>
                                    <value xsi:type="DV_INTERVAL">
                                        <lower xsi:type="DV_DATE_TIME">
                                            <value>2015-12-02T17:41:56.817Z</value>
                                        </lower>
                                        <upper xsi:type="DV_DATE_TIME">
                                            <value>2015-12-02T17:41:56.817Z</value>
                                        </upper>
                                        <lower_unbounded>false</lower_unbounded>
                                        <upper_unbounded>false</upper_unbounded>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0021">
                                    <name>
                                        <value>Duration</value>
                                    </name>
                                    <value xsi:type="DV_DURATION">
                                        <value>P1DT11H11M</value>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0015">
                                    <name>
                                        <value>Ordinal</value>
                                    </name>
                                    <value xsi:type="DV_ORDINAL">
                                        <value>1</value>
                                        <symbol>
                                            <value>Slight pain</value>
                                            <defining_code>
                                                <terminology_id>
                                                    <value>local</value>
                                                </terminology_id>
                                                <code_string>at0039</code_string>
                                            </defining_code>
                                        </symbol>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0026">
                                    <name>
                                        <value>Multimedia</value>
                                    </name>
                                    <value xsi:type="DV_MULTIMEDIA">
                                        <alternate_text>alternate text</alternate_text>
                                        <uri>
                                            <value>http://med.tube.com/sample</value>
                                        </uri>
                                        <media_type>
                                            <terminology_id>
                                                <value>IANA_media-types</value>
                                            </terminology_id>
                                            <code_string>text/xml</code_string>
                                        </media_type>
                                        <size>504903212</size>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0027">
                                    <name>
                                        <value>URI - resource identifier</value>
                                    </name>
                                    <value xsi:type="DV_URI">
                                        <value>http://example.com/path/resource</value>
                                    </value>
                                </items>
                                <items xsi:type="ELEMENT" archetype_node_id="at0044">
                                    <name>
                                        <value>Identifier</value>
                                    </name>
                                    <value xsi:type="DV_IDENTIFIER">
                                        <issuer>Issuer</issuer>
                                        <assigner>Assigner</assigner>
                                        <id>0ffc464a-d954-46f0-9f77-05ed6877ccf5</id>
                                        <type>Prescription</type>
                                    </value>
                                </items>
                            </items>
                        </data>
                    </events>
                </data>
            </items>
        </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
</versions>/$
      
      println "========= COMMIT ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT ========="
      
      println controller.response.contentAsString
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA" // Application Reject
      
      println resp.message.text()
   }
   
   
   
   void test2CommitsWithSameUIDAndBothChangeTypesAreCreation()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "tests" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      

      // COMMIT
      request.method = 'POST'
      request.contentType = 'text/xml'
      request.xml = $/<?xml version="1.0" encoding="UTF-8" ?>
<versions xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<version xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563779</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0011">
              <name>
                <value>Count</value>
              </name>
              <value xsi:type="DV_COUNT">
                <magnitude>3</magnitude>
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
</versions>/$

      
      println "========= COMMIT ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT ========="
      
      println controller.response.contentAsString
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AA" // Application Reject
      
      println resp.message.text()

      
      
      controller.response.reset()
      //controller.request.reset()
      
      // Second commit
      
      println "second commit"
      
      request.xml = $/<?xml version="1.0" encoding="UTF-8" ?>
<versions xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<version xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563780</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <commit_audit>
    <system_id>CABOLABS_EHR</system_id>
    <committer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </committer>
    <time_committed>
      <value>20140901T233114,065-0300</value>
    </time_committed>
    <change_type>
      <value>creation</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>249</code_string>
      </defining_code>
    </change_type>
  </commit_audit>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::1</value>
  </uid>
  <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1">
    <name>
      <value>Test all datatypes</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>Test all datatypes</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
    <language>
      <terminology_id>
        <value>ISO_639-1</value>
      </terminology_id>
      <code_string>es</code_string>
    </language>
    <territory>
      <terminology_id>
        <value>ISO_3166-1</value>
      </terminology_id>
      <code_string>UY</code_string>
    </territory>
    <category>
      <value>event</value>
      <defining_code>
        <terminology_id>
          <value>openehr</value>
        </terminology_id>
        <code_string>443</code_string>
      </defining_code>
    </category>
    <composer xsi:type="PARTY_IDENTIFIED">
      <name>Dr. Pablo Pazos</name>
    </composer>
    <context>
      <start_time>
        <value>20140901T232600,304-0300</value>
      </start_time>
      <setting>
        <value>Hospital Montevideo</value>
        <defining_code>
          <terminology_id>
            <value>openehr</value>
          </terminology_id>
          <code_string>229</code_string>
        </defining_code>
      </setting>
    </context>
    <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1">
      <name>
        <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>UNICODE</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
      </encoding>
      <subject xsi:type="PARTY_IDENTIFIED">
        <external_ref>
          <id xsi:type="HIER_OBJECT_ID"><value>${patientUid}</value></id>
          <namespace>DEMOGRAPHIC</namespace>
          <type>PERSON</type>
        </external_ref>
      </subject>
      <data xsi:type="HISTORY" archetype_node_id="at0001">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events xsi:type="POINT_EVENT" archetype_node_id="at0002">
          <name>
            <value>any event</value>
          </name>
          <time><value>20140101</value></time>
          <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
            <name>
              <value>Arbol</value>
            </name>
            <items xsi:type="ELEMENT" archetype_node_id="at0011">
              <name>
                <value>Count</value>
              </name>
              <value xsi:type="DV_COUNT">
                <magnitude>3</magnitude>
              </value>
            </items>
          </data>
        </events>
      </data>
    </content>
  </data>
  <lifecycle_state>
    <value>completed</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>
</versions>/$

      println "========= COMMIT ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT ========="
      
      println controller.response.contentAsString
      
      resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      assert resp.type.code.text() == "AR" // Application Reject
      
      println resp.message.text()
   }
}
