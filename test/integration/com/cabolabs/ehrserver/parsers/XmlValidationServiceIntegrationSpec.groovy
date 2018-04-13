package com.cabolabs.ehrserver.parsers

import grails.test.spock.IntegrationSpec

class XmlValidationServiceIntegrationSpec extends IntegrationSpec {

   def xmlValidationService

   private static String PS = System.getProperty("file.separator")

   def valid_xml = $/<?xml version="1.0" encoding="UTF-8"?><version xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563775</value>
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
    <value>91cf9ded-e926-4848-aa3f-3257c1d89554::EMR_APP::1</value>
  </uid>
  <data archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1" xsi:type="COMPOSITION">
    <name>
      <value>Test all datatypes</value>
    </name>
    <uid xsi:type="HIER_OBJECT_ID">
      <value>d6fa1aa6-cfc7-4c28-ba51-555ee55b0ae1</value>
    </uid>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>test_all_datatypes.en.v1</value>
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
        <code_string>433</code_string>
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
    <content archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1" xsi:type="OBSERVATION">
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
      <subject xsi:type="PARTY_SELF" />
      <data archetype_node_id="at0001" xsi:type="HISTORY">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events archetype_node_id="at0002" xsi:type="POINT_EVENT">
          <name>
            <value>any event</value>
          </name>
          <time>
            <value>20140101</value>
          </time>
          <data archetype_node_id="at0003" xsi:type="ITEM_TREE">
            <name>
              <value>Arbol</value>
            </name>
            <items archetype_node_id="at0011" xsi:type="ELEMENT">
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
    <value>complete</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>/$

   def invalid_xml = $/<?xml version="1.0" encoding="UTF-8"?><version xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1" xsi:type="ORIGINAL_VERSION">
  <contribution>
    <id xsi:type="HIER_OBJECT_ID">
      <value>ad6866e1-fb08-4e9b-a93b-5095a2563775</value>
    </id>
    <namespace>EHR::COMMON</namespace>
    <type>CONTRIBUTION</type>
  </contribution>
  <uid>
    <value>91cf9ded-e926-4848-aa3f-3257c1d89554::EMR_APP::1</value>
  </uid>
  <data archetype_node_id="openEHR-EHR-COMPOSITION.test_all_datatypes.v1" xsi:type="COMPOSITION">
    <namex>
      <value>Test all datatypes</value>
    </namex>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.test_all_datatypes.v1</value>
      </archetype_id>
      <template_id>
        <value>test_all_datatypes.en.v1</value>
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
        <code_string>433</code_string>
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
    <content archetype_node_id="openEHR-EHR-OBSERVATION.test_all_datatypes.v1" xsi:type="OBSERVATION">
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
      <subject xsi:type="PARTY_SELF" />
      <data archetype_node_id="at0001" xsi:type="HISTORY">
        <name>
          <value>history</value>
        </name>
        <origin>
          <value>20140101</value>
        </origin>
        <events archetype_node_id="at0002" xsi:type="POINT_EVENT">
          <name>
            <value>any event</value>
          </name>
          <time>
            <value>20140101</value>
          </time>
          <data archetype_node_id="at0003" xsi:type="ITEM_TREE">
            <name>
              <value>Arbol</value>
            </name>
            <items archetype_node_id="at0011" xsi:type="ELEMENT">
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
    <value>complete</value>
    <defining_code>
      <terminology_id>
        <value>openehr</value>
      </terminology_id>
      <code_string>532</code_string>
    </defining_code>
  </lifecycle_state>
</version>/$

   def setup()
   {
   }

   def cleanup()
   {
   }

   void "validate OPT"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'opts'+PS+'Encuentro.opt'
         def opt = new File(path).text

      when:
         def valid = xmlValidationService.validateOPT(opt)

      then:
         assert valid
   }

   void "validate invalid OPT"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'opts'+PS+'Encuentro_invalid.opt'
         def opt = new File(path).text

      when:
         def valid = xmlValidationService.validateOPT(opt)

      then:
         assert !valid
         assert xmlValidationService.getErrors().size() > 0
   }

   void "validate version"()
   {
      setup:
         //def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1.xml'
         //def xml = new File(path).text
         def xml = valid_xml
         def slurper = new XmlSlurper(false, false)
         def parsedVersion = slurper.parseText(xml)
         def namespaces = [:]

      when:
         def valid = xmlValidationService.validateVersion(parsedVersion, namespaces)

      then:
         assert valid
   }

   void "validate invalid version"()
   {
      setup:
         //def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1_invalid.xml'
         //def xml = new File(path).text
         def xml = invalid_xml
         def slurper = new XmlSlurper(false, false)
         def parsedVersion = slurper.parseText(xml)
         def namespaces = [:]

      when:
         def valid = xmlValidationService.validateVersion(parsedVersion, namespaces)

      then:
         assert !valid
         assert xmlValidationService.getErrors().size() > 0
   }

   void "validate string version"()
   {
      setup:
         //def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1.xml'
         //def xml = new File(path).text
         def xml = valid_xml

      when:
         def valid = xmlValidationService.validateVersion(xml)

      then:
         assert valid
   }

   void "validate invalid string version"()
   {
      setup:
         //def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1_invalid.xml'
         //def xml = new File(path).text
         def xml = invalid_xml

      when:
         def valid = xmlValidationService.validateVersion(xml)

      then:
         assert !valid
         assert xmlValidationService.getErrors().size() > 0
   }
}
