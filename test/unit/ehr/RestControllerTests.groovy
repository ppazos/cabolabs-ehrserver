package ehr

import static org.junit.Assert.*
import demographic.Person
import ehr.Ehr
import grails.test.mixin.*
import grails.test.mixin.support.*
import ehr.clinical_documents.*
import common.change_control.*
import common.generic.*
import org.junit.*
import org.springframework.mock.web.MockMultipartFile
import ehr.clinical_documents.data.*
import org.codehaus.groovy.grails.commons.ApplicationHolder
import query.*


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(RestController)
@Mock([ Ehr,Person,
        PatientProxy, DoctorProxy,
        OperationalTemplateIndex, DataIndex, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails,
        DataValueIndex, DvQuantityIndex, DvCountIndex, DvProportionIndex, DvTextIndex, DvCodedTextIndex, DvDateTimeIndex, DvBooleanIndex,
        Query, DataGet, DataCriteria
      ])
class RestControllerTests {

   private static String PS = System.getProperty("file.separator")
   private static String patientUid = 'a86ac702-980a-478c-8f16-927fd4a5e9ae'
   def config = ApplicationHolder.application.config.app
   
   void setUp()
	{
      println "setUp"
      
      
      controller.xmlService = new parsers.XmlService()
      
      
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
            uid: patientUid
         ),
         new Person(
            firstName: 'Barbara',
            lastName: 'Cardozo',
            dob: new Date(87, 2, 19),
            sex: 'F',
            idCode: '1234567-0',
            idType: 'CI',
            role: 'pat'
         ),
         new Person(
            firstName: 'Carlos',
            lastName: 'Cardozo',
            dob: new Date(80, 2, 20),
            sex: 'M',
            idCode: '3453455-0',
            idType: 'CI',
            role: 'pat'
         )
         ,
         new Person(
            firstName: 'Mario',
            lastName: 'Gomez',
            dob: new Date(64, 8, 19),
            sex: 'M',
            idCode: '5677565-0',
            idType: 'CI',
            role: 'pat'
         )
         ,
         new Person(
            firstName: 'Carla',
            lastName: 'Martinez',
            dob: new Date(92, 1, 5),
            sex: 'F',
            idCode: '84848884-0',
            idType: 'CI',
            role: 'pat'
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
	  persons.each { p ->
	  
	     if (p.role == 'pat')
		 {
			ehr = new Ehr(
			   subject: new PatientProxy(
			      value: p.uid
			   )
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
      DataIndex.list().each { di ->
         
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
      DataIndex.list().each { di ->
         
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
      DataIndex.list().each { di ->
         
         println "di: " + di.path
      }
      
      
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = $/<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
      def opt = new File( "opts" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      // Test operational template index created
      ehr.clinical_documents.OperationalTemplateIndex.list().each { opti ->
         
         println "opti: " + opti.templateId
      }
      
      // Test data indexes created
      DataIndex.list().each { di ->
         
         println "di: " + di.path
      }
      
      
      
      // =========================================================================
      // PRIMER COMMIT
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = $/<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://schemas.openehr.org/v1 ../xsd/Version.xsd" xsi:type="ORIGINAL_VERSION">
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
      
      
      params.versions = $/<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<version xmlns="http://schemas.openehr.org/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://schemas.openehr.org/v1 ../xsd/Version.xsd" xsi:type="ORIGINAL_VERSION">
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
      
      
      // ====================================================
      // Verifica cardinalidades de los objetos creados
      assert VersionedComposition.count() == 1
      assert VersionedComposition.get(1).allVersions.size() == 2 /// TEST!!!!
      //assert VersionedComposition.get(1).latestVersion.uid == "91cf9ded-e926-4848-aa3f-3257c1d89e37::EMR_APP::2"
      
      
      
      // =========================================================
      // Crea data indexes para los commits previos
      def indexJob = new ehr.IndexDataJob()
      indexJob.execute()
      
      
      
      // ====================================================
      // Verifica los valores de los objetos creados      
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
      
      // Elimina archivos generados en el commit
      new File(config.composition_repo).eachFile { f ->
         f.delete()
      }
      
      
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
		println groovy.xml.XmlUtil.serialize( controller.response.text )
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
}