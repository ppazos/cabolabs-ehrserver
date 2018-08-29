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

@Transactional
class SyncParserService {

   def config = Holders.config.app

   def jsonService
   def xmlService

   /*
   sync order:

   Account
   Organization
   EHR
   OPT
   Contribution

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

      def ehr = Ehr.findByUid(j.ehrUid), // EHR should be synced before Contribution

      // Process commit to create versions and compo indexes, later data indexing will generate dvs
      def jsonCommit = j.commit

      // copied from RestController.commit
      def versionsXML = jsonService.json2xml(jsonCommit)
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
         subject: new PatientProxy(value: j.subjectUid)
         organizationUid: j.organizationUid,
         deleted: j.deleted,
         master: false
      )

      return ehr
   }

   Account fromJSONAccount(JSONObject j)
   {

   }

   User fromJSONUser(JSONObject j)
   {
      // TODO: to be able to set the password from the sync, the beforeInsert encodePassword()
      // should be disabled since the pass is already encoded, maybe adding a flag.
      // like this https://stackoverflow.com/questions/33150455/gorm-temporarily-disable-beforeinsert-event
   }

   Organization fromJSONOrganization(JSONObject j)
   {

   }

   UserRole fromJSONUserRole(JSONObject j)
   {

   }

   Role fromJSONRole(JSONObject r)
   {

   }

   Query fromJSONQuery(JSONObject q)
   {

   }

   DataGet fromJSONDataGet(JSONObject dg)
   {

   }

   DataCriteriaExpression toJSONDataCriteriaExpression(JSONObject dg)
   {

   }

   DataCriteriaString toJSONDataCriteriaString(JSONObject dg)
   {

   }

   DataCriteriaLOCATABLE_REF toJSONDataCriteriaLOCATABLE_REF(JSONObject dg)
   {

   }

   DataCriteriaDV_TEXT toJSONDataCriteriaDV_TEXT(JSONObject dg)
   {

   }

   DataCriteriaDV_QUANTITY toJSONDataCriteriaDV_QUANTITY(JSONObject dg)
   {

   }

   DataCriteriaDV_PROPORTION toJSONDataCriteriaDV_PROPORTION(JSONObject dg)
   {

   }

   DataCriteriaDV_PARSABLE toJSONDataCriteriaDV_PARSABLE(JSONObject dg)
   {

   }

   DataCriteriaDV_ORDINAL toJSONDataCriteriaDV_ORDINAL(JSONObject dg)
   {

   }

   DataCriteriaDV_MULTIMEDIA toJSONDataCriteriaDV_MULTIMEDIA(JSONObject dg)
   {

   }

   DataCriteriaDV_IDENTIFIER toJSONDataCriteriaDV_IDENTIFIER(JSONObject dg)
   {

   }

   DataCriteriaDV_DURATION toJSONDataCriteriaDV_DURATION(JSONObject dg)
   {

   }

   DataCriteriaDV_DATE toJSONDataCriteriaDV_DATE(JSONObject dg)
   {

   }

   DataCriteriaDV_DATE_TIME toJSONDataCriteriaDV_DATE_TIME(JSONObject dg)
   {

   }

   DataCriteriaDV_COUNT toJSONDataCriteriaDV_COUNT(JSONObject dg)
   {

   }

   DataCriteriaDV_CODED_TEXT toJSONDataCriteriaDV_CODED_TEXT(JSONObject dg)
   {

   }

   DataCriteriaDV_BOOLEAN toJSONDataCriteriaDV_BOOLEAN(JSONObject dg)
   {

   }

   EhrQuery toJSONEhrQuery(JSONObject dg)
   {

   }

   OperationalTemplateIndex OperationalTemplateIndex(JSONObject dg)
   {

   }

   Folder toJSONFolder(JSONObject dg)
   {

   }
}
