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

import com.cabolabs.security.*
import com.cabolabs.ehrserver.account.*
import java.util.logging.Logger

class DataIndexerServiceIntegrationSpec extends IntegrationSpec {

   def dataIndexerService
   def xmlService

   private static String PS = System.getProperty("file.separator")

   def log = Logger.getLogger('com.cabolabs.ehrserver.data.DataIndexerServiceIntegrationSpec')

   private String ehrUid     = '11111111-1111-1111-1111-111111111123'
   private String patientUid = '11111111-1111-1111-1111-111111111145'
   private String orgUid     = '11111111-1111-1111-1111-111111111178'

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


      // Load test OPTs
      // Always regenerate indexes in deploy
      if (OperationalTemplateIndex.count() == 0)
      {
        println "Indexing Operational Templates"

        def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
        ti.setupBaseOpts( org )
        ti.indexAll( org )

        println "Indexed ${OperationalTemplateIndex.count()} OPTs"
      }

      // OPT loading
      def optMan = com.cabolabs.openehr.opt.manager.OptManager.getInstance( Holders.config.app.opt_repo.withTrailSeparator() )
      optMan.unloadAll(orgUid)
      optMan.loadAll(orgUid)
      // /Load test OPTs
   }

   def cleanup()
   {
      def ehr = Ehr.findByUid(ehrUid)
      ehr.delete(failOnError: true)


      Account.list()*.delete() // should delete the orgs

      UserRole.list()*.delete()
      User.list()*.delete()
      Role.list()*.delete()


      // empty the temp version store, TODO: make the cleanup per test case
      def temp = new File(Holders.config.app.version_repo.withTrailSeparator() + orgUid.withTrailSeparator())
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
         def contribution = xmlService.processCommit(ehr, parsedVersions, 'CaboLabs EMR', new Date(), 'House, MD.')
         contribution.save() // contrib -> versions -> compoIndex
         assert CompositionIndex.count() == 1
         assert CompositionIndex.countByDataIndexed(false) == 1 // there is 1 compoIndex and should not be indexed

         // The OPT associated with the compo index should be shared with the org or be public
         // Here we manke the OPT public.
         def compoIndex = CompositionIndex.findByDataIndexed(false)

         assert compoIndex != null

         def opt = OperationalTemplateIndex.findByTemplateId(compoIndex.templateId)

         if (!opt) println "OPT '${compoIndex.templateId}' not loaded"

         assert opt != null

         opt.save(failOnError: true)

      when: "generate data indexes for the committed composition"

         assert OperationalTemplateIndex.count() > 0 // OPTs are loaded in bootstrap
         assert OperationalTemplateIndex.countByTemplateId( compoIndex.templateId ) == 1 // The template for the instance is loaded
         assert orgUid == compoIndex.organizationUid

         println "compoIndex.templateId "+ compoIndex.templateId


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
         /*
         indexes:
         - context.start_time
         - context.setting
         - history.origin
         - event.time
         - count.magnitude
         */
         assert DataValueIndex.count() == 5

         assert DvDateTimeIndex.count() == 3
         assert DvCountIndex.count() == 1
         assert DvCodedTextIndex.count() == 1

         assert DvCountIndex.first().magnitude == 3

         // compo.category is no longer being indexed because of the change
         // on DataIndexerService.recursiveIndexData,
         // line: def type = DataValues.valueOfString(node.'@xsi:type'.text())
         // but it is OK because the category is saved on the compo index.
         //assert DvCodedTextIndex.first().value == "event"
   }
}
