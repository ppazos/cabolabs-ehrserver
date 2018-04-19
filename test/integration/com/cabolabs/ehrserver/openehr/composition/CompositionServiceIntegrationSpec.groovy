package com.cabolabs.ehrserver.openehr.composition

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

import grails.test.spock.IntegrationSpec

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.*
import com.cabolabs.ehrserver.account.*
import com.cabolabs.util.DateParser

import grails.util.Holders

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.ChangeType
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy

import groovy.io.FileType

class CompositionServiceIntegrationSpec extends IntegrationSpec {

   def compositionService
   def versionFSRepoService

   private static String PS = System.getProperty("file.separator")

   def xml = $/<?xml version="1.0" encoding="UTF-8"?><version xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1" xsi:type="ORIGINAL_VERSION">
   <contribution>
     <id xsi:type="HIER_OBJECT_ID">
       <value>78363227-6655-4784-af29-e9b67afae01f</value>
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
       <name>Dr. House</name>
     </committer>
     <time_committed>
       <value>20160119T022429,000-0300</value>
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
     <value>13a9f2b9-81fe-432a-bcc8-d225377a13f1::EMR::1</value>
   </uid>
   <data archetype_node_id="openEHR-EHR-COMPOSITION.signos.v1" xsi:type="COMPOSITION">
     <name>
       <value>Signos vitales</value>
     </name>
     <uid xsi:type="HIER_OBJECT_ID">
       <value>b5ae930b-edff-468e-8a53-c3dd45b29a1f</value>
     </uid>
     <archetype_details>
       <archetype_id>
         <value>openEHR-EHR-COMPOSITION.signos.v1</value>
       </archetype_id>
       <template_id>
         <value>signos.es.v1</value>
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
       <external_ref>
         <id xsi:type="HIER_OBJECT_ID">
           <value>cc193f71-f5fe-438a-87f9-81ecb302eede</value>
         </id>
         <namespace>DEMOGRAPHIC</namespace>
         <type>PERSON</type>
       </external_ref>
       <name>Dr. House</name>
     </composer>
     <context>
       <start_time>
         <value>20160119T022429,000-0300</value>
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
     <content archetype_node_id="openEHR-EHR-OBSERVATION.blood_pressure.v1" xsi:type="OBSERVATION">
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
           <value>Unicode</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
       </encoding>
       <subject xsi:type="PARTY_SELF"/>
       <protocol archetype_node_id="at0011" xsi:type="ITEM_TREE">
         <name>
           <value>Tree</value>
         </name>
       </protocol>
       <data archetype_node_id="at0001" xsi:type="HISTORY">
         <name>
           <value>history</value>
         </name>
         <origin xsi:type="DV_DATE_TIME">
           <value>20160119T022429,000-0300</value>
         </origin>
         <events archetype_node_id="at0006" xsi:type="POINT_EVENT">
           <name>
             <value>any event</value>
           </name>
           <time xsi:type="DV_DATE_TIME">
             <value>20160119T022429,000-0300</value>
           </time>
           <data archetype_node_id="at0003" xsi:type="ITEM_TREE">
             <name>
               <value>blood pressure</value>
             </name>
             <items archetype_node_id="at0005" xsi:type="ELEMENT">
               <name>
                 <value>Diastolic</value>
               </name>
               <value xsi:type="DV_QUANTITY">
                 <magnitude>87</magnitude>
                 <units>mm[Hg]</units>
               </value>
             </items>
             <items archetype_node_id="at0004" xsi:type="ELEMENT">
               <name>
                 <value>Systolic</value>
               </name>
               <value xsi:type="DV_QUANTITY">
                 <magnitude>137</magnitude>
                 <units>mm[Hg]</units>
               </value>
             </items>
           </data>
           <state archetype_node_id="at0007" xsi:type="ITEM_TREE">
             <name>
               <value>state structure</value>
             </name>
           </state>
         </events>
       </data>
     </content>
     <content archetype_node_id="openEHR-EHR-OBSERVATION.body_temperature.v1" xsi:type="OBSERVATION">
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
           <value>Unicode</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
       </encoding>
       <subject xsi:type="PARTY_SELF"/>
       <protocol archetype_node_id="at0020" xsi:type="ITEM_TREE">
         <name>
           <value>Protocol</value>
         </name>
       </protocol>
       <data archetype_node_id="at0002" xsi:type="HISTORY">
         <name>
           <value>History</value>
         </name>
         <origin xsi:type="DV_DATE_TIME">
           <value>20160119T022429,000-0300</value>
         </origin>
         <events archetype_node_id="at0003" xsi:type="POINT_EVENT">
           <name>
             <value>Any event</value>
           </name>
           <time xsi:type="DV_DATE_TIME">
             <value>20160119T022429,000-0300</value>
           </time>
           <data archetype_node_id="at0001" xsi:type="ITEM_TREE">
             <name>
               <value>Tree</value>
             </name>
             <items archetype_node_id="at0004" xsi:type="ELEMENT">
               <name>
                 <value>Temperature</value>
               </name>
               <value xsi:type="DV_QUANTITY">
                 <magnitude>39</magnitude>
                 <units>°C</units>
               </value>
             </items>
           </data>
           <state archetype_node_id="at0029" xsi:type="ITEM_TREE">
             <name>
               <value>State</value>
             </name>
           </state>
         </events>
       </data>
     </content>
     <content archetype_node_id="openEHR-EHR-OBSERVATION.pulse.v1" xsi:type="OBSERVATION">
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
           <value>Unicode</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
       </encoding>
       <subject xsi:type="PARTY_SELF"/>
       <protocol archetype_node_id="at0010" xsi:type="ITEM_TREE">
         <name>
           <value>*List(en)</value>
         </name>
       </protocol>
       <data archetype_node_id="at0002" xsi:type="HISTORY">
         <name>
           <value>*history(en)</value>
         </name>
         <origin xsi:type="DV_DATE_TIME">
           <value>20160119T022429,000-0300</value>
         </origin>
         <events archetype_node_id="at0003" xsi:type="POINT_EVENT">
           <name>
             <value>*Any event(en)</value>
           </name>
           <time xsi:type="DV_DATE_TIME">
             <value>20160119T022429,000-0300</value>
           </time>
           <data archetype_node_id="at0001" xsi:type="ITEM_TREE">
             <name>
               <value>*structure(en)</value>
             </name>
             <items archetype_node_id="at0004" xsi:type="ELEMENT">
               <name xsi:type="DV_CODED_TEXT">
                 <value>Frecuencia cardiaca</value>
                 <defining_code>
                   <terminology_id>
                     <value>local</value>
                   </terminology_id>
                   <code_string>at1027</code_string>
                 </defining_code>
               </name>
               <value xsi:type="DV_QUANTITY">
                 <magnitude>51</magnitude>
                 <units>/min</units>
               </value>
             </items>
           </data>
           <state archetype_node_id="at0012" xsi:type="ITEM_TREE">
             <name>
               <value>*List(en)</value>
             </name>
           </state>
         </events>
       </data>
     </content>
     <content archetype_node_id="openEHR-EHR-OBSERVATION.respiration.v1" xsi:type="OBSERVATION">
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
           <value>Unicode</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
       </encoding>
       <subject xsi:type="PARTY_SELF"/>
       <data archetype_node_id="at0001" xsi:type="HISTORY">
         <name>
           <value>history</value>
         </name>
         <origin xsi:type="DV_DATE_TIME">
           <value>20160119T022429,000-0300</value>
         </origin>
         <events archetype_node_id="at0002" xsi:type="POINT_EVENT">
           <name>
             <value>Any event</value>
           </name>
           <time xsi:type="DV_DATE_TIME">
             <value>20160119T022429,000-0300</value>
           </time>
           <data archetype_node_id="at0003" xsi:type="ITEM_TREE">
             <name>
               <value>List</value>
             </name>
             <items archetype_node_id="at0004" xsi:type="ELEMENT">
               <name>
                 <value>Rate</value>
               </name>
               <value xsi:type="DV_QUANTITY">
                 <magnitude>26</magnitude>
                 <units>/min</units>
               </value>
             </items>
           </data>
           <state archetype_node_id="at0022" xsi:type="ITEM_TREE">
             <name>
               <value>List</value>
             </name>
           </state>
         </events>
       </data>
     </content>
     <content archetype_node_id="openEHR-EHR-OBSERVATION.body_weight.v1" xsi:type="OBSERVATION">
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
           <value>Unicode</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
       </encoding>
       <subject xsi:type="PARTY_SELF"/>
       <protocol archetype_node_id="at0015" xsi:type="ITEM_TREE">
         <name>
           <value>*protocol structure(en)</value>
         </name>
       </protocol>
       <data archetype_node_id="at0002" xsi:type="HISTORY">
         <name>
           <value>*history(en)</value>
         </name>
         <origin xsi:type="DV_DATE_TIME">
           <value>20160119T022429,000-0300</value>
         </origin>
         <events archetype_node_id="at0003" xsi:type="POINT_EVENT">
           <name>
             <value>Cualquier evento.</value>
           </name>
           <time xsi:type="DV_DATE_TIME">
             <value>20160119T022429,000-0300</value>
           </time>
           <data archetype_node_id="at0001" xsi:type="ITEM_TREE">
             <name>
               <value>*Simple(en)</value>
             </name>
             <items archetype_node_id="at0004" xsi:type="ELEMENT">
               <name>
                 <value>Peso</value>
               </name>
               <value xsi:type="DV_QUANTITY">
                 <magnitude>93</magnitude>
                 <units>kg</units>
               </value>
             </items>
           </data>
           <state archetype_node_id="at0008" xsi:type="ITEM_TREE">
             <name>
               <value>*state structure(en)</value>
             </name>
           </state>
         </events>
       </data>
     </content>
     <content archetype_node_id="openEHR-EHR-OBSERVATION.height.v1" xsi:type="OBSERVATION">
       <name>
         <value>Height/Length</value>
       </name>
       <language>
         <terminology_id>
           <value>ISO_639-1</value>
         </terminology_id>
         <code_string>es</code_string>
       </language>
       <encoding>
         <terminology_id>
           <value>Unicode</value>
         </terminology_id>
         <code_string>UTF-8</code_string>
       </encoding>
       <subject xsi:type="PARTY_SELF"/>
       <protocol archetype_node_id="at0007" xsi:type="ITEM_TREE">
         <name>
           <value>List</value>
         </name>
       </protocol>
       <data archetype_node_id="at0001" xsi:type="HISTORY">
         <name>
           <value>history</value>
         </name>
         <origin xsi:type="DV_DATE_TIME">
           <value>20160119T022429,000-0300</value>
         </origin>
         <events archetype_node_id="at0002" xsi:type="POINT_EVENT">
           <name>
             <value>Any event</value>
           </name>
           <time xsi:type="DV_DATE_TIME">
             <value>20160119T022429,000-0300</value>
           </time>
           <data archetype_node_id="at0003" xsi:type="ITEM_TREE">
             <name>
               <value>Simple</value>
             </name>
             <items archetype_node_id="at0004" xsi:type="ELEMENT">
               <name>
                 <value>Height/Length</value>
               </name>
               <value xsi:type="DV_QUANTITY">
                 <magnitude>120</magnitude>
                 <units>cm</units>
               </value>
             </items>
           </data>
           <state archetype_node_id="at0013" xsi:type="ITEM_TREE">
             <name>
               <value>Tree</value>
             </name>
           </state>
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


   private String ehrUid     = '11111111-1111-1111-1111-111111111123'
   private String patientUid = '11111111-1111-1111-1111-111111111145'
   private String orgUid     = '11111111-1111-1111-1111-111111111178'


   private createEHR()
   {
      def ehr = new Ehr(
         uid: ehrUid, // the ehr id is the same as the patient just to simplify testing
         subject: new PatientProxy(
            value: patientUid
         ),
         organizationUid: Organization.findByUid(orgUid).uid
      )

      ehr.save(failOnError: true)
   }


   def setup()
   {
      // 1. Account setup: create account manager user
      def accman = new User(
         username: 'testaccman',
         password: 'testaccman',
         email: 'testaccman@domain.com',
      ).save(failOnError:true, flush: true)

      // 2. Account setup: create account
      def account = new Account(contact: accman, companyName:'Test company')

      // 3. Account setup: create organization
      def org = new Organization(uid: orgUid, name: 'CaboLabs', number: '123456')
      account.addToOrganizations(org)
      account.save(failOnError: true) // saves the org

      // 4. Account setup: create ACCMAN role
      def accmanRole = new Role(authority: Role.AM).save(failOnError: true, flush: true)

      // 5. Account setup: create user role association
      UserRole.create( accman, accmanRole, org, true )



      createEHR()



      // get EHR
      def ehr = Ehr.findByUid(ehrUid)

      assert ehr != null


      // create CompositionIndex for an existing version XML
      def composer = new DoctorProxy(
         value: '5323452345-23452334-23452345',
         name: 'Dr. House'
      )


      // Load test Version
      //def path = 'test'+PS+'resources'+PS+'versions'+PS+'13a9f2b9-81fe-432a-bcc8-d225377a13f1_EMR_1.xml'
      //def xml = new File(path).text

      def parser = new XmlSlurper(false, false)
      def parsedVersion = parser.parseText(xml)

      //println parsedVersion.data.getClass() // class groovy.util.slurpersupport.NodeChildren
      //println parsedVersion.data[0].getClass() // class groovy.util.slurpersupport.NodeChild

      // ----------------------------------------------------------------------
      // Canonical transformation of the composition to a string to hash
      // FIXME: refactor, code from XmlService.parseCompositionIndex

      // need to add namespaces to avoid errors while serializing
      def namespaceMap = [ 'xmlns': 'http://schemas.openehr.org/v1', 'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance']
      def compo = parsedVersion.data[0]

      namespaceMap.each { ns, val ->

         compo."@$ns" = val
      }

      def compositionString = groovy.xml.XmlUtil.serialize( compo )

      // Removes the added namespaces to avoid saving them on store version
      namespaceMap.each { ns, val ->
         compo.attributes().remove(ns)
      }

      def byteSize = compositionString.size()
      def hash = compositionString.md5()

      def compoIndex = new CompositionIndex(
         uid:         parsedVersion.data.uid.value.text(),
         category:    parsedVersion.data.category.value.text(),
         startTime:   DateParser.tryParse( parsedVersion.data.context.start_time.text() ),
         timeCommitted: new Date(),
         subjectId:   ehr.subject.value,
         ehrUid:      ehr.uid,
         organizationUid: ehr.organizationUid,
         archetypeId: parsedVersion.data.@archetype_node_id.text(),
         templateId:  parsedVersion.data.archetype_details.template_id.value.text(),
         composer: composer,
         byteSize:      byteSize,
         hash:          hash
      )
      def change_type_code = parsedVersion.commit_audit.change_type.defining_code.code_string.text()
      def commitAudit = new AuditDetails(
         systemId:      parsedVersion.commit_audit.system_id.text(),
         timeCommitted: new Date(),
         changeType:    ChangeType.fromValue(change_type_code as short),
         committer: new DoctorProxy(
            name: parsedVersion.commit_audit.committer.name.text()
         )
      )

      // FIXME: now the version.uid is assigned only by the server
      def version = new Version(
         uid: (parsedVersion.uid.value.text()), // the 3 components come from the client.
         lifecycleState: parsedVersion.lifecycle_state.defining_code.code_string.text(),
         commitAudit: commitAudit,
         data: compoIndex
      )


      def contribution = new Contribution(
         uid: '78363227-6655-4784-af29-e9b67afae01f',
         ehr: ehr,
         organizationUid: ehr.organizationUid,
         audit: new AuditDetails(
            systemId: "CaboLabs EMR",
            timeCommitted: new Date(),
            committer: new DoctorProxy(
               name: "House, MD"
            )
         )
      )

      contribution.addToVersions(version)

      compoIndex.save(failOnError:true)
      commitAudit.save(failOnError:true)
      contribution.save(failOnError:true)

      // save version file
      def file = versionFSRepoService.getNonExistingVersionFile( ehr.organizationUid, version )
      file << xml
   }

   def cleanup()
   {
      Contribution.list()*.delete(flush: true)

      def ehr = Ehr.findByUid(ehrUid)
      ehr.delete(flush: true)

      //def org = Organization.findByUid(orgUid)
      //org.delete(flush: true)

      Account.list()*.delete() // should delete the orgs

      UserRole.list()*.delete()
      User.list()*.delete()
      Role.list()*.delete()
   }

   void "test composition as XML"()
   {
      setup:
         def uid = 'b5ae930b-edff-468e-8a53-c3dd45b29a1f'

      when:
         def xml = compositionService.compositionAsXml(uid)

      then:
         println xml

      cleanup:
         println "test composition as XML: DELETE CREATED FILES FROM "+ Holders.config.app.version_repo
         new File(Holders.config.app.version_repo).eachFileMatch(FileType.FILES, ~/.*\.xml/) {
           println it.path
           it.delete()
         }
   }

   void "test composition as JSON"()
   {
      setup:
         def uid = 'b5ae930b-edff-468e-8a53-c3dd45b29a1f'

      when:
         def json = compositionService.compositionAsJson(uid)

      then:
         println json

      cleanup:
         println "test composition as JSON: DELETE CREATED FILES FROM "+ Holders.config.app.version_repo
         new File(Holders.config.app.version_repo).eachFileMatch(FileType.FILES, ~/.*\.xml/) {
           println it.path
           it.delete()
         }
   }

   void "test composition as HTML"()
   {
      setup:
         def uid = 'b5ae930b-edff-468e-8a53-c3dd45b29a1f'

      when:
         def html = compositionService.compositionAsHtml(uid)

      then:
         println html

      cleanup:
         println "test composition as HTML: DELETE CREATED FILES FROM "+ Holders.config.app.version_repo
         new File(Holders.config.app.version_repo).eachFileMatch(FileType.FILES, ~/.*\.xml/) {
           println it.path
           it.delete()
         }
   }
}
