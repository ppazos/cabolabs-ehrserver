package com.cabolabs.ehrserver.sync

import grails.transaction.Transactional
import com.cabolabs.ehrserver.parsers.JsonService
import com.cabolabs.ehrserver.parsers.XmlService
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONObject

import com.cabolabs.ehrserver.openehr.ehr.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*
import com.cabolabs.ehrserver.account.*
import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.query.datatypes.*
import com.cabolabs.security.*
import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.openehr.directory.*

import com.cabolabs.archetype.OperationalTemplateIndexer

import java.text.SimpleDateFormat

import com.cabolabs.openehr.opt.manager.OptManager

@Transactional
class SyncParserService {

   def config = Holders.config.app

   def jsonService
   def xmlService

   /*
   sync order:

   Account
   Organization (needs Account)
   UserRole, User (needs Org)
   EHR (needs Org)
   OPT (needs Org)
   Contribution (needs EHR and OPT for each Version in the Commit)
   Query (needs User)
   */

   Contribution fromJSONContribution(JSONObject j)
   {
      // no need to make the biding, since XmlService already does that from the commit payload
      def contribution /* = new Contribution(
         uid: j.uid,
         organizationUid: j.organizationUid,
         audit: fromJSONAuditDetails(j.audit),
         ehr: Ehr.findByUid(j.ehrUid), // EHR should be synced before Contribution
         master: false
      )
      */

      // TODO: OPTs should be synced before commits referencing those templates

      def ehr = Ehr.findByUid(j.ehrUid) // EHR should be synced before Contribution

      // Process commit to create versions and compo indexes, later data indexing will generate dvs
      def jsonCommit = j.commit

      //println "commit class "+ jsonCommit.getClass() // JSONObject
      //println jsonCommit.toString() // json string

      def testfile = new File('./cucucu.json')
      testfile << jsonCommit.toString()

      // copied from RestController.commit
      def versionsXML = jsonService.json2xml(jsonCommit.toString())

println versionsXML

      def slurper = new XmlSlurper(false, false)
      def _parsedVersions = slurper.parseText(versionsXML)
      contribution = xmlService.processCommit(ehr, _parsedVersions, j.audit.system_id, new Date(), j.audit.committer.name)
      contribution.master = false

      return contribution
   }

   AuditDetails fromJSONAuditDetails(JSONObject j)
   {

   }

   Ehr fromJSONEhr(JSONObject j)
   {
      def ehr = new Ehr(
         systemId: j.systemId,
         uid: j.uid,
         dateCreated: new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(j.dateCreated),
         subject: new PatientProxy(value: j.subjectUid),
         organizationUid: j.organizationUid,
         deleted: j.deleted,
         master: false
      )

      return ehr
   }

   Account fromJSONAccount(JSONObject j)
   {
      def organizations = []
      j.organizations.each{ org ->
         organizations << fromJSONOrganization(org)
      }

      if (!organizations) println "no orgs"
      def contact = User.findByUid(j.contact.uid) // if already synced
      if (!contact)
      {
         contact = fromJSONUser(j.contact)
      }
      if (!contact) println "no contact"

      def account = new Account(
         uid: j.uid,
         companyName: j.companyName,
         enabled: j.enabled,
         contact: contact,
         master: false
         //,
         //organizations: organizations
      )

      organizations.each {
         account.addToOrganizations(it)
      }

      return account
   }

   User fromJSONUser(JSONObject j)
   {
      // TODO: to be able to set the password from the sync, the beforeInsert encodePassword()
      // should be disabled since the pass is already encoded, maybe adding a flag.
      // like this https://stackoverflow.com/questions/33150455/gorm-temporarily-disable-beforeinsert-event
      def user = new User(
         avoidBeforeInsert: true, // do not encode pass, is already encoded
         uid: j.uid,
         username: j.username,
         password: j.password, // already encoded
         email: j.email,
         isVirtual: j.isVirtual,
         enabled: j.enabled,
         accountExpired: j.accountExpired,
         accountLocked: j.accountLocked,
         passwordExpired: j.passwordExpired
      )

      return user
   }

   Organization fromJSONOrganization(JSONObject j)
   {
      def org = new Organization(
         uid: j.uid,
         name: j.name,
         number: j.number
      )

      // controller should parse the j.user_roles array to get the users of the org

      return org
   }

   /**
    * called only if the UserRole for the user, org and role doesnt exists.
    */
   UserRole fromJSONUserRole(JSONObject j)
   {
      def user = User.findByUid(j.uid)
      if (!user)
      {
         user = fromJSONUser(j.user)
      }

      // Roles are always on the DB since are created by bootstrap
      def role = Role.findByAuthority(j.authority)

      // Organization should be synced before user roles
      def org = Organization.findByUid(j.organizationUid)

      if (!org) throw new Exception("Organization not synced "+ j.organizationUid)

      def ur = new UserRole(user, role, org)
      return ur
   }

   /*
   Role fromJSONRole(JSONObject r)
   {

   }
   */

   Query fromJSONQuery(JSONObject j)
   {
      // cant use the Query parser becuse the Query Builder structure has the tree
      // on Query.where, not the list of DataCriteriaExpression as comes on the sync
      // JSON object, se we need to parse that structure here.
      //def query = Query.newInstance(q)

      // TODO: author
      // TODO: uid

      //return query
   }

   DataGet fromJSONDataGet(JSONObject j)
   {

   }

   DataCriteriaExpression toJSONDataCriteriaExpression(JSONObject j)
   {

   }

   DataCriteriaString toJSONDataCriteriaString(JSONObject j)
   {

   }

   DataCriteriaLOCATABLE_REF toJSONDataCriteriaLOCATABLE_REF(JSONObject j)
   {

   }

   DataCriteriaDV_TEXT toJSONDataCriteriaDV_TEXT(JSONObject j)
   {

   }

   DataCriteriaDV_QUANTITY toJSONDataCriteriaDV_QUANTITY(JSONObject j)
   {

   }

   DataCriteriaDV_PROPORTION toJSONDataCriteriaDV_PROPORTION(JSONObject j)
   {

   }

   DataCriteriaDV_PARSABLE toJSONDataCriteriaDV_PARSABLE(JSONObject j)
   {

   }

   DataCriteriaDV_ORDINAL toJSONDataCriteriaDV_ORDINAL(JSONObject j)
   {

   }

   DataCriteriaDV_MULTIMEDIA toJSONDataCriteriaDV_MULTIMEDIA(JSONObject j)
   {

   }

   DataCriteriaDV_IDENTIFIER toJSONDataCriteriaDV_IDENTIFIER(JSONObject j)
   {

   }

   DataCriteriaDV_DURATION toJSONDataCriteriaDV_DURATION(JSONObject j)
   {

   }

   DataCriteriaDV_DATE toJSONDataCriteriaDV_DATE(JSONObject j)
   {

   }

   DataCriteriaDV_DATE_TIME toJSONDataCriteriaDV_DATE_TIME(JSONObject j)
   {

   }

   DataCriteriaDV_COUNT toJSONDataCriteriaDV_COUNT(JSONObject j)
   {

   }

   DataCriteriaDV_CODED_TEXT toJSONDataCriteriaDV_CODED_TEXT(JSONObject j)
   {

   }

   DataCriteriaDV_BOOLEAN toJSONDataCriteriaDV_BOOLEAN(JSONObject j)
   {

   }

   EhrQuery toJSONEhrQuery(JSONObject j)
   {

   }

   OperationalTemplateIndex toJSONOpt(JSONObject j)
   {
      def templateIndex = new OperationalTemplateIndex(
         templateId: j.templateId,
         concept: j.concept,
         language: j.language,
         uid: j.uid,
         externalUid: j.externalUid,
         archetypeId: j.archetypeId,
         archetypeConcept: j.archetypeConcept,
         organizationUid: j.organizationUid,
         isActive: j.isActive,
         master: false,
         dateCreated: new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(j.dateCreated),
         setId: j.setId,
         versionNumber: j.versionNumber,
         lastVersion: j.lastVersion
      )

      def xml = j.opt

      def opt_repo_org_path = config.opt_repo.withTrailSeparator() + j.organizationUid.withTrailSeparator()
      def destination = opt_repo_org_path + templateIndex.fileUid + '.opt'
      File fileDest = new File( destination )
      fileDest << xml

      // Generates OPT and archetype item indexes just for the uploaded OPT
      def indexer = new OperationalTemplateIndexer()
      indexer.templateIndex = templateIndex // avoids creating another opt index internally and use the one created here

      def slurper = new XmlSlurper(false, false)
      def template = slurper.parseText(xml)
      indexer.index(template, null, Organization.findByUid(j.organizationUid))

      // load opt in manager cache
      // TODO: just load the newly created/updated one
      def optMan = OptManager.getInstance()
      optMan.unloadAll(j.organizationUid)
      optMan.loadAll(j.organizationUid)

      return templateIndex
   }

   Folder toJSONFolder(JSONObject j)
   {

   }
}
