/*
 * Copyright 2011-2020 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.sync

import grails.transaction.Transactional
import com.cabolabs.ehrserver.parsers.JsonService
import com.cabolabs.ehrserver.parsers.XmlService
import grails.util.Holders
import org.grails.web.json.JSONObject
import org.grails.web.json.JSONArray
import groovy.json.JsonBuilder
import com.cabolabs.ehrserver.openehr.ehr.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*
import com.cabolabs.ehrserver.account.*
import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.query.datatypes.*
import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.openehr.directory.*
import java.text.SimpleDateFormat
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.security.User

@Transactional
class SyncParserService {

   def config = Holders.config.app

   def jsonService
   def xmlService
   def operationalTemplateIndexerService

   /*
   sync order:
   User
   EHR (needs Org)
   OPT (needs Org)
   Contribution (needs EHR and OPT for each Version in the Commit)
   Query (needs User)
   */

   Contribution fromJSONContribution(JSONObject j)
   {
      // this is needed because the controller has problems with changed orders in the JSON
      def jsonContribution = j.contribution

      // no need to make the biding, since XmlService already does that from the commit payload
      def contribution /* = new Contribution(
         uid: j.uid,
         audit: fromJSONAuditDetails(j.audit),
         ehr: Ehr.findByUid(j.ehrUid), // EHR should be synced before Contribution
         master: false
      )
      */

      // TODO: OPTs should be synced before commits referencing those templates

      def ehr = Ehr.findByUid(jsonContribution.ehrUid) // EHR should be synced before Contribution

      // TODO: should check the EHR exists (was synced)

      // Process commit to create versions and compo indexes, later data indexing will generate dvs
      def jsonCommit = jsonContribution.commit

      //println "commit class "+ jsonCommit.getClass() // groovy.json.internal.Map
      //println jsonCommit.toString() // json string

      //def testfile = new File('./cucucu.json')
      //testfile << new JsonBuilder(jsonCommit).toString() //jsonCommit.toString() // this is not valid JSON!

      //println jsonCommit.toString() // not valid JSON

      // copied from RestController.commit
      def versionsXML = jsonService.json2XmlV2(new JsonBuilder(jsonCommit).toString())

      def slurper = new XmlSlurper(false, false)
      def _parsedVersions = slurper.parseText(versionsXML)

      // can throw validation errors!
      contribution = xmlService.processCommit(ehr, _parsedVersions, new Date(), jsonContribution.audit.committer.name)
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
         subject: fromJSONPartySelf(j.subject), // subject can be null
         organizationUid: j.organizationUid,
         deleted: j.deleted,
         master: false
      )

      return ehr
   }

   PartySelf fromJSONPartySelf(JSONObject j)
   {
      def partySelf = new PartySelf(
         value: j.value,
         type: j.type,
         namespace: j.namespace,
         idType: j.idType,
         scheme: j.scheme
      )

      return partySelf
   }

   User fromJSONUser(JSONObject j)
   {
      // TODO: to be able to set the password from the sync, the beforeInsert encodePassword()
      // should be disabled since the pass is already encoded, maybe adding a flag.
      // like this https://stackoverflow.com/questions/33150455/gorm-temporarily-disable-beforeinsert-event
      def user = new User(
         avoidBeforeInsert: true, // do not encode pass, is already encoded
         uid: j.uid,
         role: j.role,
         password: j.password, // already encoded
         email: j.email,
         enabled: j.enabled,
         accountExpired: j.accountExpired,
         accountLocked: j.accountLocked,
         passwordExpired: j.passwordExpired
      )

      return user
   }

   /*
    * PRE: orgs and users should be synced before
    */
   Query fromJSONQuery(JSONObject j)
   {
      def jq = j.query

      // Check query group
      def qg
      if (jq.queryGroup)
      {
         qg = QueryGroup.findByUid(jq.queryGroup.uid)
         if (!qg)
         {
            qg = new QueryGroup(jq.queryGroup) // TODO: Check if the binding is done correctly
         }
      }

      // FIXME: sprinc sec was removed
      // FIXME: this will fail if user is admin, since admin is not synced,
      // will be null when get by uid
      /*
      def author = User.findByUid(jq.author.uid)

      // if admin is the author, because admins are not synced, it wont find it by uid
      // so we need to check for username, and we consider the admin to be the same
      // across the sync cluster.
      if (!author)
      {
         if (jq.author.email == 'admin@cabolabs.com')
         {
            author = User.findByEmail('admin@cabolabs.com')
         }
      }
      */
      def author

      def q = new Query(
         uid: jq.uid,
         name: jq.name,
         type: jq.type,
         isCount: jq.isCount,
         format: jq.format,
         templateId: jq.templateId,
         group: jq.group,
         isDeleted: jq.isDeleted,
         queryGroup: qg,
         author: author,
         master: false
      )

      jq.select.each { dget ->
         q.addToSelect( fromJSONDataGet(dget) )
      }

      jq.where.each { cexpr ->
         q.addToWhere( toJSONDataCriteriaExpression(cexpr) )
      }

      return q
   }

   DataGet fromJSONDataGet(Map j)
   {
      new DataGet(j)
   }

   DataCriteriaExpression toJSONDataCriteriaExpression(Map j)
   {
      def toJSONDataCriteria = 'toJSON' + j.criteria_class
      def expr = new DataCriteriaExpression(
         left_assoc: j.left_assoc,
         right_assoc: j.right_assoc,
         criteria: "$toJSONDataCriteria"(j.criteria)
      )
      return expr
   }

   DataCriteriaString toJSONDataCriteriaString(Map j)
   {
      new DataCriteriaString(j)
   }

   DataCriteriaLOCATABLE_REF toJSONDataCriteriaLOCATABLE_REF(Map j)
   {
      new DataCriteriaLOCATABLE_REF(j)
   }

   DataCriteriaDV_TEXT toJSONDataCriteriaDV_TEXT(Map j)
   {
      new DataCriteriaDV_TEXT(j)
   }

   DataCriteriaDV_QUANTITY toJSONDataCriteriaDV_QUANTITY(Map j)
   {
      new DataCriteriaDV_QUANTITY(j)
   }

   DataCriteriaDV_PROPORTION toJSONDataCriteriaDV_PROPORTION(Map j)
   {
      new DataCriteriaDV_PROPORTION(j)
   }

   DataCriteriaDV_PARSABLE toJSONDataCriteriaDV_PARSABLE(Map j)
   {
      new DataCriteriaDV_PARSABLE(j)
   }

   DataCriteriaDV_ORDINAL toJSONDataCriteriaDV_ORDINAL(Map j)
   {
      new DataCriteriaDV_ORDINAL(j)
   }

   DataCriteriaDV_MULTIMEDIA toJSONDataCriteriaDV_MULTIMEDIA(Map j)
   {
      new DataCriteriaDV_MULTIMEDIA(j)
   }

   DataCriteriaDV_IDENTIFIER toJSONDataCriteriaDV_IDENTIFIER(Map j)
   {
      new DataCriteriaDV_IDENTIFIER(j)
   }

   DataCriteriaDV_DURATION toJSONDataCriteriaDV_DURATION(Map j)
   {
      new DataCriteriaDV_DURATION(j)
   }

   DataCriteriaDV_DATE toJSONDataCriteriaDV_DATE(Map j)
   {
      new DataCriteriaDV_DATE(j)
   }

   DataCriteriaDV_DATE_TIME toJSONDataCriteriaDV_DATE_TIME(Map j)
   {
      new DataCriteriaDV_DATE_TIME(j)
   }

   DataCriteriaDV_COUNT toJSONDataCriteriaDV_COUNT(Map j)
   {
      new DataCriteriaDV_COUNT(j)
   }

   DataCriteriaDV_CODED_TEXT toJSONDataCriteriaDV_CODED_TEXT(Map j)
   {
      new DataCriteriaDV_CODED_TEXT(j)
   }

   DataCriteriaDV_BOOLEAN toJSONDataCriteriaDV_BOOLEAN(Map j)
   {
      new DataCriteriaDV_BOOLEAN(j)
   }

   EhrQuery toJSONEhrQuery(JSONObject j)
   {
      def eq = new EhrQuery(
         uid: j.uid,
         name: j.name,
         description: j.description,
         master: false
      )

      def q
      j.queries.each { qj ->
         q = fromJSONQuery(qj)
         eq.addToQueries(q)
      }

      return eq
   }

   OperationalTemplateIndex toJSONOpt(JSONObject j)
   {
      def templateIndex = new OperationalTemplateIndex(
         templateId:       j.templateId,
         localTemplateId:  j.localTemplateId,
         concept:          j.concept,
         language:         j.language,
         uid:              j.uid,
         localUid:         j.localUid,
         archetypeId:      j.archetypeId,
         archetypeConcept: j.archetypeConcept,
         isActive:         j.isActive,
         master:           false,
         dateCreated:      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(j.dateCreated),
         setId:            j.setId,
         versionNumber:    j.versionNumber,
         lastVersion:      j.lastVersion,
         fileLocation:     j.fileLocation
      )

      def xml = j.opt
      File fileDest = new File(templateIndex.fileLocation)
      fileDest << xml

      // Generates OPT and archetype item indexes just for the uploaded OPT
      operationalTemplateIndexerService.templateIndex = templateIndex // avoids creating another opt index internally and use the one created here

      def slurper = new XmlSlurper(false, false)
      def template = slurper.parseText(xml)
      operationalTemplateIndexerService.index(template, Organization.findByUid(j.organizationUid))

      // load opt in manager cache
      // TODO: just load the newly created/updated one
      def optMan = OptManager.getInstance()
      optMan.unloadAll(j.organizationUid)
      optMan.loadAll(j.organizationUid, true)

      return templateIndex
   }

   Folder toJSONFolder(JSONObject j)
   {
      def f = new Folder(
         uid: j.uid,
         name: j.name,
         master: false,
         items: j.items // List<String>
      )

      if (j.ehrUid)
      {
         f.ehr = Ehr.findByUid(h.ehrUid)
      }

      def subf
      j.folder.each { jf ->
         subf = toJSONFolder(jf)
         f.addToFolders(subf)
      }

      return f
   }
}
