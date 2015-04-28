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
import grails.util.Holders
import query.*


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(RestController)
@Mock([ Ehr,Person,
        PatientProxy, DoctorProxy,
        OperationalTemplateIndex, IndexDefinition, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails,
        DataValueIndex, DvQuantityIndex, DvCountIndex, DvProportionIndex, DvTextIndex, DvCodedTextIndex, DvDateTimeIndex, DvBooleanIndex,
        Query, DataGet, DataCriteria
      ])
class RestControllerCommitXSDValidationTests {

   private static String PS = System.getProperty("file.separator")
   private static String patientUid = '11111111-1111-1111-1111-111111111111'
   def config = Holders.config.app
   
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
            uid: '11111111-1111-1111-1111-111111111111'
        ),
        new Person(
            firstName: 'Barbara',
            lastName: 'Cardozo',
            dob: new Date(87, 2, 19),
            sex: 'F',
            idCode: '1234567-0',
            idType: 'CI',
            role: 'pat',
            uid: '22222222-1111-1111-1111-111111111111'
        ),
        new Person(
            firstName: 'Carlos',
            lastName: 'Cardozo',
            dob: new Date(80, 2, 20),
            sex: 'M',
            idCode: '3453455-0',
            idType: 'CI',
            role: 'pat',
            uid: '33333333-1111-1111-1111-111111111111'
        ),
        new Person(
            firstName: 'Mario',
            lastName: 'Gomez',
            dob: new Date(64, 8, 19),
            sex: 'M',
            idCode: '5677565-0',
            idType: 'CI',
            role: 'pat',
            uid: '44444444-1111-1111-1111-111111111111'
        ),
        new Person(
            firstName: 'Carla',
            lastName: 'Martinez',
            dob: new Date(92, 1, 5),
            sex: 'F',
            idCode: '84848884-0',
            idType: 'CI',
            role: 'pat',
            uid: '55555555-1111-1111-1111-111111111111'
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
			   ehrId: p.uid, // the ehr id is the same as the patient just to simplify testing
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

   void testCommitInvalidVersion()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      
      
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
    <!-- territory REMOVED xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -->
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
      <!-- subject REMOVED xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -->
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
      
      // Pretty print 
      println "response \n"+ groovy.xml.XmlUtil.serialize(resp)
      
      /*
       * <result>
           <type>
             <code>AR</code>
             <codeSystem>HL7::TABLES::TABLE_8</codeSystem>
           </type>
           <message>Some versions do not validate against the XSD</message>
           <details>
             <item>
               Error for version #0 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'nameYYYYYYYYYY'. 
               One of '{"http://schemas.openehr.org/v1":name}' is expected. line #: 113 &gt;&gt;&gt; &lt;nameYYYYYYYYYY&gt;
             </item>
             <item>
               Error for version #0 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX'.
                One of '{"http://schemas.openehr.org/v1":uid, "http://schemas.openehr.org/v1":links, "http://schemas.openehr.org/v1":archetype_details, "
                http://schemas.openehr.org/v1":feeder_audit, "http://schemas.openehr.org/v1":value, "http://schemas.openehr.org/v1":null_flavour}' 
                is expected. line #: 125 &gt;&gt;&gt; &lt;valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX xsi:type="DV_COUNT"&gt;
              </item>
           </details>
         </result>
       */
      
      assert resp.type.code.text() == "AR" // REjected because it doesnt validate.
      
      //println controller.response.contentAsString
      //println controller.response.text
      
      controller.response.reset()
      
      // FIN COMMIT ============================================
      

      // ====================================================
      // No se deberia haber creado ningun objeto porque falla el commit
      assert VersionedComposition.count() == 0
      
   } // testCommitInvalidVersion
   
   void testCommit2InvalidVersion()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      
      
      // =========================================================================
      // PRIMER COMMIT
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = [
$/<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
    <!-- territory REMOVED xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -->
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
      <!-- subject REMOVED xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -->
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
/$,
$/<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
       <value>91cf9ded-e926-4848-aa3f-222222222222::EMR_APP::1</value>
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
           <value>20140901T122600,304-0300</value>
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
             <nameYYYYYYYYYY>
               <value>any event</value>
             </nameYYYYYYYYYY>
             <time><value>20140101</value></time>
             <data xsi:type="ITEM_TREE" archetype_node_id="at0003">
               <name>
                 <value>Arbol</value>
               </name>
               <items xsi:type="ELEMENT" archetype_node_id="at0011">
                 <name>
                   <value>Count</value>
                 </name>
                 <valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX xsi:type="DV_COUNT">
                   <magnitude>3</magnitude>
                 </valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX>
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
      ]
      
      //println params.versions

      
      println "========= COMMIT 1 ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT 1 ========="
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      // Pretty print
      println "response \n"+ groovy.xml.XmlUtil.serialize(resp)
      
      /*
       * <result>
             <type>
                 <code>AR</code>
                 <codeSystem>HL7::TABLES::TABLE_8</codeSystem>
             </type>
             <message>Some versions do not validate against the XSD</message>
             <details>
                 <item>Error for version #0 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'category'. One of '{"http://schemas.openehr.org/v1":territory}' is expected.
                 line #: 51
                 &gt;&gt;&gt; &lt;category&gt;</item>
                 <item>Error for version #0 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'data'. One of '{"http://schemas.openehr.org/v1":subject}' is expected.
                 line #: 94
                 &gt;&gt;&gt; &lt;data xsi:type="HISTORY" archetype_node_id="at0001"&gt;</item>
                 <item>Error for version #1 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'nameYYYYYYYYYY'. One of '{"http://schemas.openehr.org/v1":name}' is expected.
                 line #: 113
                 &gt;&gt;&gt; &lt;nameYYYYYYYYYY&gt;</item>
                 <item>Error for version #1 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX'. One of '{"http://schemas.openehr.org/v1":uid, "http://schemas.openehr.org/v1":links, "http://schemas.openehr.org/v1":archetype_details, "http://schemas.openehr.org/v1":feeder_audit, "http://schemas.openehr.org/v1":value, "http://schemas.openehr.org/v1":null_flavour}' is expected.
                 line #: 125
                 &gt;&gt;&gt; &lt;valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX xsi:type="DV_COUNT"&gt;</item>
             </details>
         </result>
       */
      
      assert resp.type.code.text() == "AR" // REjected because it doesnt validate.
      
      //println controller.response.contentAsString
      //println controller.response.text
      
      controller.response.reset()
      
      // FIN COMMIT ============================================
      
      // ====================================================
      // No se deberia haber creado ningun objeto porque falla el commit
      assert VersionedComposition.count() == 0
      
   } // testCommit2InvalidVersion
   
   void testCommit1Invalid1InvalidVersion()
   {
      def oti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      def opt = new File( "opts" + PS + "Test all datatypes_es.opt" )
      oti.index(opt)
      
      
      
      // =========================================================================
      // PRIMER COMMIT
      
      // https://github.com/gramant/grails-core-old/blob/master/grails-test/src/main/groovy/org/codehaus/groovy/grails/plugins/testing/GrailsMockHttpServletRequest.groovy
      //println "req: "+ request.class.toString()
      
      request.method = 'POST'
      controller.request.contentType = 'application/x-www-form-urlencoded'
      
      // dolar slashy allows GString variables in multiline Strings
      params.versions = [
$/<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
    <!-- territory REMOVED xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -->
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
      <!-- subject REMOVED xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -->
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
              <valueXXXXXXXXXXXXXXX xsi:type="DV_COUNT">
                <magnitude>3</magnitude>
              </valueXXXXXXXXXXXXXXX>
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
/$,
$/<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
       <value>91cf9ded-e926-4848-aa3f-222222222222::EMR_APP::1</value>
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
           <value>20140901T122600,304-0300</value>
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
      ]
      
      //println params.versions

      
      println "========= COMMIT 1 ========="
      
      params.ehrId = Ehr.get(1).ehrId
      params.auditSystemId = "TEST_SYSTEM_ID"
      params.auditCommitter = "Mr. Committer"
      controller.commit()
      
      println "========= FIN COMMIT 1 ========="
      
      def resp = new XmlSlurper().parseText( controller.response.contentAsString )
      
      // Pretty print
      println "response \n"+ groovy.xml.XmlUtil.serialize(resp)
      
      /*
       * <result>
             <type>
                 <code>AR</code>
                 <codeSystem>HL7::TABLES::TABLE_8</codeSystem>
             </type>
             <message>Some versions do not validate against the XSD</message>
             <details>
                 <item>Error for version #0 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'category'. One of '{"http://schemas.openehr.org/v1":territory}' is expected.
                 line #: 51
                 &gt;&gt;&gt; &lt;category&gt;</item>
                 <item>Error for version #0 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'data'. One of '{"http://schemas.openehr.org/v1":subject}' is expected.
                 line #: 94
                 &gt;&gt;&gt; &lt;data xsi:type="HISTORY" archetype_node_id="at0001"&gt;</item>
                 <item>Error for version #1 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'nameYYYYYYYYYY'. One of '{"http://schemas.openehr.org/v1":name}' is expected.
                 line #: 113
                 &gt;&gt;&gt; &lt;nameYYYYYYYYYY&gt;</item>
                 <item>Error for version #1 ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element 'valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX'. One of '{"http://schemas.openehr.org/v1":uid, "http://schemas.openehr.org/v1":links, "http://schemas.openehr.org/v1":archetype_details, "http://schemas.openehr.org/v1":feeder_audit, "http://schemas.openehr.org/v1":value, "http://schemas.openehr.org/v1":null_flavour}' is expected.
                 line #: 125
                 &gt;&gt;&gt; &lt;valueXXXXXXXXXXXXXXXXXXXXXXXXXXXXX xsi:type="DV_COUNT"&gt;</item>
             </details>
         </result>
       */
      
      assert resp.type.code.text() == "AR" // REjected because it doesnt validate.
      
      //println controller.response.contentAsString
      //println controller.response.text
      
      controller.response.reset()
      
      // FIN COMMIT ============================================
      
      // ====================================================
      // No se deberia haber creado ningun objeto porque falla el commit
      assert VersionedComposition.count() == 0
      
   } // testCommit1Invalid1InvalidVersion
}
