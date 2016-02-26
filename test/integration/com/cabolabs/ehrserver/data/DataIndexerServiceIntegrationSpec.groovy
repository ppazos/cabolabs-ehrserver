package com.cabolabs.ehrserver.data

import grails.test.spock.IntegrationSpec
import com.cabolabs.ehrserver.ehr.clinical_documents.data.*
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.util.Holders
import groovy.io.FileType

class DataIndexerServiceIntegrationSpec extends IntegrationSpec {

   def dataIndexerService
   def xmlService
   
   private static String PS = System.getProperty("file.separator")
   
   
   def setup()
   {
      // used by the service, mock the version repo where commits are stored
      Holders.config.app.version_repo = "test"+ PS +"resources"+ PS +"temp_versions" + PS
   }

   def cleanup()
   {
      // empty the temp version store
      def temp = new File(Holders.config.app.version_repo)
      temp.eachFileMatch(FileType.FILES, ~/.*\.xml/) { it.delete() }
   }

   void "test simple count inbdex"()
   {
      // prepare a single commit, then try to index
      setup:
         def ehr = Ehr.get(1)
      
         def versionsXML = new File('test'+PS+'resources'+PS+'commit'+PS+'test_commit_1.xml').text
         versionsXML = versionsXML.replaceAll('\\[PATIENT_UID\\]', ehr.subject.value)
         
         def slurper = new XmlSlurper(false, false)
         def parsedVersions = slurper.parseText(versionsXML)
         
         xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
          
      when:
         dataIndexerService.generateIndexes()
          
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
