package com.cabolabs.ehrserver.openehr.composition

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import grails.test.spock.IntegrationSpec
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.util.DateParser
import grails.util.Holders
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.demographic.Person

class CompositionServiceIntegrationSpec extends IntegrationSpec {

   def compositionService
   
   private static String PS = System.getProperty("file.separator")
   
   def setup()
   {
      /*
      println Ehr.list()
      println Ehr.list().uid
      println Ehr.list().subject.value
      */
      
      // 1. Load test Version
      def path = 'test'+PS+'resources'+PS+'versions'+PS+'13a9f2b9-81fe-432a-bcc8-d225377a13f1_EMR_1.xml'
      def xml = new File(path).text
      def parser = new XmlSlurper()
      def parsedVersion = parser.parseText(xml)
      
      // 2. get EHR
      def ehr = Ehr.get(1)
      
      
      // 3. create CompositionIndex for an existing version XML

      def compoIndex = new CompositionIndex(
         uid:         parsedVersion.data.uid.value.text(),
         category:    parsedVersion.data.category.value.text(),
         startTime:   DateParser.tryParse( parsedVersion.data.context.start_time.text() ),
         subjectId:   ehr.subject.value,
         ehrUid:      ehr.uid,
         organizationUid: ehr.organizationUid,
         archetypeId: parsedVersion.data.@archetype_node_id.text(),
         templateId:  parsedVersion.data.archetype_details.template_id.value.text()
      )
      
      def commitAudit = new AuditDetails(
         systemId:      parsedVersion.commit_audit.system_id.text(),
         timeCommitted: new Date(),
         changeType:    parsedVersion.commit_audit.change_type.value.text(),
         committer: new DoctorProxy(
            name: parsedVersion.commit_audit.committer.name.text()
         )
      )
      def version = new Version(
         uid: (parsedVersion.uid.value.text()), // the 3 components come from the client.
         lifecycleState: parsedVersion.lifecycle_state.value.text(),
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
      ehr.addToContributions( contribution )
      
      compoIndex.save(failOnError:true)
      commitAudit.save(failOnError:true)
      contribution.save(failOnError:true)
      version.save(failOnError:true)
   }

   def cleanup()
   {
   }

   void "test composition as XML"()
   {
      setup:
         def uid = 'b5ae930b-edff-468e-8a53-c3dd45b29a1f'
         // used by the service, mock the version repo
         Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"versions" + PS
         
      when:
         def xml = compositionService.compositionAsXml(uid)
         
      then:
         println xml
   }
   
   void "test composition as JSON"()
   {
      setup:
         def uid = 'b5ae930b-edff-468e-8a53-c3dd45b29a1f'
         // used by the service, mock the version repo
         Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"versions" + PS
         
      when:
         def json = compositionService.compositionAsJson(uid)
         
      then:
         println json
   }
   
   void "test composition as HTML"()
   {
      setup:
         def uid = 'b5ae930b-edff-468e-8a53-c3dd45b29a1f'
         // used by the service, mock the version repo
         Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"versions" + PS
         
      when:
         def html = compositionService.compositionAsHtml(uid)
         
      then:
         println html
   }
}
