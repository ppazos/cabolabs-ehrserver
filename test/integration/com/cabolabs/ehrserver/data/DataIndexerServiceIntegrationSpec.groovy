package com.cabolabs.ehrserver.data

import grails.test.spock.IntegrationSpec
import com.cabolabs.ehrserver.ehr.clinical_documents.data.*
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.util.Holders
import groovy.io.FileType
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexShare


import com.cabolabs.security.Organization
import java.util.logging.Logger

class DataIndexerServiceIntegrationSpec extends IntegrationSpec {

   def dataIndexerService
   def xmlService
   
   private static String PS = System.getProperty("file.separator")
   
   def log = Logger.getLogger('com.cabolabs.ehrserver.data.DataIndexerServiceIntegrationSpec')
   
   private String ehrUid = '11111111-1111-1111-1111-111111111123'
   private String patientUid = '11111111-1111-1111-1111-111111111145'
   private String orgUid = '11111111-1111-1111-1111-111111111178'
   
   private createOrganization()
   {
      def org = new Organization(uid: orgUid, name: 'Test', number: '111999')
      org.save(failOnError: true, flush: true)
   }
   
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
      // used by the service, mock the version repo where commits are stored
      //Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"temp_versions" + PS
      createOrganization()
      createEHR()
      
      
      // Load test OPTs
      // Always regenerate indexes in deploy
      if (OperationalTemplateIndex.count() == 0)
      {
        println "Indexing Operational Templates"
        
        def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
        ti.setupBaseOpts()
        ti.indexAll( Organization.findByUid(orgUid) )
      }
     
      // OPT loading
      def optMan = com.cabolabs.openehr.opt.manager.OptManager.getInstance( Holders.config.app.opt_repo )
      optMan.unloadAll()
      optMan.loadAll()
      // /Load test OPTs
   }

   def cleanup()
   {
      def ehr = Ehr.findByUid(ehrUid)
      ehr.delete(failOnError: true)
      
      def org = Organization.findByUid(orgUid)
      org.delete()

      
      // empty the temp version store, TODO: make the cleanup per test case
      def temp = new File(Holders.config.app.version_repo)
      println "***** DELETE FROM "+ temp.path
      temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) {
         println it.path
         it.delete()
      }
   }

   void "test simple count index"()
   {
      // prepare a single commit, then try to index
      setup: "simulates a commit of a test composition, creates a compoIndex, without indexing data"
      
         // Need template "Test all datatypes" to be shared with the org,
         // if is not shared the commit will say that the version cant ve indexed
      
         def ehr = Ehr.findByUid(ehrUid)
         assert ehr != null

         def versionsXML = new File('test'+ PS +'resources'+ PS +'commit'+ PS +'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)

         assert CompositionIndex.count() == 0
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         assert CompositionIndex.count() == 1
         assert CompositionIndex.countByDataIndexed(false) == 1 // there is 1 compoIndex and should not be indexed
         
         // The OPT associated with the compo index should be shared with the org or be public
         // Here we manke the OPT public.
         def compoIndex = CompositionIndex.findByDataIndexed(false)
         
         assert compoIndex != null
         
         def opt = OperationalTemplateIndex.findByTemplateId(compoIndex.templateId)
         
         if (!opt) println "OPT '${compoIndex.templateId}' not loaded"
         
         assert opt != null
         
         opt.isPublic = true
         opt.save(failOnError: true)

      when: "generate data indexes for the committed composition"
      
         assert OperationalTemplateIndex.count() > 0 // OPTs are loaded in bootstrap
         assert OperationalTemplateIndex.countByTemplateId( compoIndex.templateId ) == 1 // The template for the instance is loaded
         assert orgUid == compoIndex.organizationUid
         
         println "compoIndex.templateId "+ compoIndex.templateId
         
         // OperationalTemplateIndexer should generated the shares on bootstrap from indexAll
         //println "shares "+ OperationalTemplateIndexShare.list()
         
         dataIndexerService.generateIndexes( compoIndex )
         assert CompositionIndex.countByDataIndexed(true) == 1 // the compo is marked as indexed
          
      then: "check indexed data"
      
         // commit was ok
         notThrown Exception // this shouldn't throw any exceptions
      
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1
         
         
         // indexing was ok
         assert DataValueIndex.count() == 1
         assert DvCountIndex.count() == 1
         assert DvCountIndex.first().magnitude == 3
         
         // compo.category is no longer being indexed because of the change
         // on DataIndexerService.recursiveIndexData,
         // line: def type = DataValues.valueOfString(node.'@xsi:type'.text())
         // but it is OK because the category is saved on the compo index.
         //assert DvCodedTextIndex.first().value == "event"
   }
}
