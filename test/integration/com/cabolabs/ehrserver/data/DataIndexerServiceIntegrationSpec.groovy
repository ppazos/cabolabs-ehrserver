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
   
   
   def setup()
   {
      // used by the service, mock the version repo where commits are stored
      //Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"temp_versions" + PS
      def uid = '11111111-1111-1111-1111-111111111111'
      def ehr = new Ehr(
         uid: uid, // the ehr id is the same as the patient just to simplify testing
         subject: new PatientProxy(
            value: uid
         ),
         organizationUid: Organization.get(1).uid
      )
    
      if (!ehr.save()) println ehr.errors
   }

   def cleanup()
   {
      // empty the temp version store
      def temp = new File(Holders.config.app.version_repo)
      println "***** DELETE FROM "+ temp.path
      temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) {
         println it.path
         it.delete()
      }
   }

   void "test simple count inbdex"()
   {
      // prepare a single commit, then try to index
      setup:
         def ehr = Ehr.get(1)
         assert ehr != null

         def versionsXML = new File('test'+ PS +'resources'+ PS +'commit'+ PS +'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)

         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)
log.warn "pre commit"
         assert CompositionIndex.count() == 0
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
log.warm "post commit"
         assert CompositionIndex.count() == 1

      when:
println "E"
         def compoIndex = CompositionIndex.findByDataIndexed(false)
         assert compoIndex != null
         assert CompositionIndex.countByDataIndexed(false) == 1
         
         // The template for the instance is loaded
         assert OperationalTemplateIndex.countByTemplateId( compoIndex.templateId ) == 1
         
         println compoIndex.organizationUid
         assert Organization.get(1).uid == compoIndex.organizationUid
         
         // OperationalTemplateIndexer should generated the shares on bootstrap from indexAll
         println "shares "+ OperationalTemplateIndexShare.list()
         
         dataIndexerService.generateIndexes( CompositionIndex.findByDataIndexed(false) )
println "F"
         assert CompositionIndex.countByDataIndexed(true) == 1
          
      then:
         // commit was ok
         notThrown Exception // this shouldn't throw any exceptions
      
         assert Contribution.count() == 1
         assert Version.count() == 1
         assert VersionedComposition.count() == 1
         assert CompositionIndex.count() == 1
         
         
         // indexing was ok
         assert DataValueIndex.count() == 2
         assert DvCountIndex.count() == 1
         assert DvCountIndex.first().magnitude == 3
         assert DvCodedTextIndex.first().value == "event"
   }
}
