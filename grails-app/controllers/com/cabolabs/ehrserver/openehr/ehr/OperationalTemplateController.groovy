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

package com.cabolabs.ehrserver.openehr.ehr

import grails.util.Holders
import groovy.xml.MarkupBuilder
import net.pempek.unicode.UnicodeBOMInputStream
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.ehrserver.ehr.clinical_documents.*
import grails.converters.*
import com.cabolabs.ehrserver.account.Plan
import com.cabolabs.util.FileUtils

// test
import com.cabolabs.ehrserver.sync.SyncMarshallersService
import groovy.json.*

class OperationalTemplateController {

   def config = Holders.config.app
   def xmlValidationService
   def authService
   def configurationService
   def operationalTemplateIndexerService
   def optService
   def syncMarshallersService

   def index(int offset, String sort, String order, String concept, Boolean deleted)
   {
      /*
      def jb = new JsonBuilder()
      syncMarshallersService.toJSON(OperationalTemplateIndex.list(), jb)
      render jb.toString(), contentType: "application/json"
      */

      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'

      def org = session.organization
      def list
      if (!deleted)
      {
         list = OperationalTemplateIndex
                 .forOrg(org).likeConcept(concept).notDeleted.lastVersions
                 .list(max: max, offset: offset, sort: sort, order: order)
      }
      else
      {
         list = OperationalTemplateIndex
                .forOrg(org).likeConcept(concept).deleted.lastVersions
                .list(max: max, offset: offset, sort: sort, order: order)
      }

      [opts: list, total: list.totalCount]
   }

   /**
    * (re)generates indexes for all archetypes in the repo.
    * This is usefull to add archetypes to the repo and index them to generate new queries.
    */
   def generate()
   {
      operationalTemplateIndexerService.indexAll(session.organization)

      // load opt in manager cache
      def optMan = OptManager.getInstance()
      optMan.unloadAll(session.organization.uid)
      optMan.loadAll(session.organization.uid, true)

      println "loaded opts: " + optMan.getLoadedOpts(session.organization.uid)

      redirect(action: "index")
   }


   /**
    * TODO: refactor to service, this should be transactional.
    * @param overwrite
    * @param versionOfTemplateUid UID of the template the user wants to add a version to.
    * @return
    */
   def upload(boolean overwrite, String versionOfTemplateUid)
   {
      if (params.doit)
      {
         def errors = []
         def res

         def user = authService.loggedInUser

         // TODO: move to a plan check interceptor
         // Repo size check and max opt check max_opts_per_organization
         def account = user.account
         def plan_assoc = Plan.associatedNow(account) // can be null on dev envs, size check is not done on that case.
         if (plan_assoc)
         {
            if (plan_assoc.plan.repo_total_size_in_kb <= account.totalRepoSizeInKb)
            {
               res = [status:'error', message:message(code:'opt.upload.error.insufficient_storage'), errors: errors]
               render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
               return
            }

            def opt_count = OperationalTemplateIndex.forOrg(session.organization).lastVersions.count()

            if (plan_assoc.plan.max_opts_per_organization <= opt_count)
            {
               res = [status:'error', message:message(code:'opt.upload.error.max_opt_reached'), errors: errors]
               render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
               return
            }
         }


         // PROCESS FILE

         // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/commons/CommonsMultipartFile.html
         def f = request.getFile('opt')

         // file empty check
         if(f.empty)
         {
            errors << message(code:"opt.upload.error.noOPT")

            res = [status:'error', message:'XML validation errors', errors: errors]
            render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
            return
         }

         def xml = FileUtils.removeBOM(f.getBytes())

         /*
         // Avoid BOM on OPT files (the Template Designer exports OPTs with BOM and that breaks the XML parser)
         def bytes = f.getBytes()
         def inputStream = new ByteArrayInputStream(bytes)
         def bomInputStream = new UnicodeBOMInputStream(inputStream)
         bomInputStream.skipBOM() // NOP if no BOM is detected

         // Read out
         def isr = new InputStreamReader(bomInputStream)
         def br = new BufferedReader(isr)
         def xml = br.text // getText from Groovy
         */

         // Validate XML
         if (!xmlValidationService.validateOPT(xml))
         {
            errors = xmlValidationService.getErrors()

            res = [status:'error', message:'XML validation errors', errors: errors]
            render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
            return
         }
         // /PROCESS FILE

         // ROOT VALIDATION
         // Parse to get the template id
         def slurper = new XmlSlurper(false, false)
         def template = slurper.parseText(xml)

         def root_rm_type = template.definition.rm_type_name.text()

         if (root_rm_type != 'COMPOSITION')
         {
            errors << message(code:"opt.upload.error.noComposition", args:[root_rm_type])

            res = [status:'error', message:'Incorrect root type', errors: errors]
            render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
            return
         }
         // /ROOT VALIDATION

         // COMPO.content should be present (is optional in the RM and doesn't make sense to upload such OPT)
         if (!template.definition.attributes.find{ it.rm_attribute_name.text() == 'content' })
         {
            res = [status:'error', message:'The COMPOSITION should have a non empty definition', errors: []]
            render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
            return
         }
         // /COMPO.content


         def opt_repo_org_path = config.opt_repo.withTrailSeparator() + session.organization.uid.withTrailSeparator()

         // OPT VERSIONING
         def setId, versionNumber

         // check existing by OPT uid or templateId
         // Check uniqueness of the OPT inside the org
         def opt_uid = template.uid.value.text()
         def opt_template_id = template.template_id.value.text()
         def alternatives = OperationalTemplateIndex.forOrg(session.organization)
                                            .matchExternalUidOrExternalTemplateId(opt_uid, opt_template_id)
                                            .lastVersions
                                            .list()
         if (alternatives.size() > 0)
         {
            if (!versionOfTemplateUid) // user needs to resolve revision
            {
               // start the new versioning process for OPTs
               res = [status:'resolve_duplicate', message:'Found some templates that might be the same or older versions of the one you try to upload. Choose one of the alternatives to upload a new version of that OPT or if it is a new OPT, change the the UID of the OPT you want to update.', alternatives: alternatives]
               render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
               return
            }
            else // revision choosen by user
            {
               def old_version = alternatives.find{ it.uid == versionOfTemplateUid }

               if (!old_version) // invalid uid
               {
                  res = [status:'resolve_duplicate', message:'OPT UID not found, please select one OPT from the list.', alternatives: alternatives]
                  render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
                  return
               }

               // the user selected an OPT different than the one that has the same internal UID?
               def same_uid_opts = alternatives.findAll{ it.externalUid == opt_uid }

               if (same_uid_opts.size() > 1 || same_uid_opts[0].id != old_version.id)
               {
                  // Select same_uid_opts[0] as version or change the uploaded OPT UID
                  res = [status:'resolve_duplicate', message:'There is another OPT '+ same_uid_opts[0].concept +' that has the same UID as the one you uploaded. Select that OPT to version or change the UID of the OPT you uploaded.', alternatives: alternatives]
                  render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
                  return
               }

               // old version update
               old_version.lastVersion = false
               //old_version.save() // the deactivation already saves

               operationalTemplateIndexerService._event_deactivate(old_version)

               // data for new version
               setId = old_version.setId
               versionNumber = old_version.versionNumber + 1

               // move old version outside the OPT repo
               optService.moveOldVersion(old_version)

               // create new version
               // DONE: the OPT.uid can't be equal to an existing OPT.uid that is not the selected versionOfTemplateUid, the user should change the OPT uid to do so.
               // DONE: if everything is OK, create the new version with the same setId and +1 versionNumber, put lastVersion in false to the previous version.
               // DONE: all uses of OPTs should get the latest version of the OPT using the lastVersion flag
               // DONE: move the older version OPT to another folder so OPTManager can load only the latest versions
            }
         }

         // Will index the opt nodes, and help deleting existing ones when updating

         // saves OperationalTemplateIndex to the DB and the file in the OPT repo
         def opt = operationalTemplateIndexerService.createOptIndex(template, session.organization, xml)

         flash.message = message(code:"opt.upload.success")

         // versioning if needed
         if (setId)
         {
            opt.setId = setId
            opt.versionNumber = versionNumber
         }

         // job will index
         // Generates OPT and archetype item indexes just for the uploaded OPT
         //operationalTemplateIndexerService.templateIndex = opt // avoids creating another opt index internally and use the one created here
         //operationalTemplateIndexerService.index(template, session.organization)


         // load opt in manager cache
         // TODO: just load the newly created/updated one
         /* done in the indexer job
         def optMan = OptManager.getInstance()
         optMan.unloadAll(session.organization.uid)
         optMan.loadAll(session.organization.uid, true)
         */


         // Recalculate OPT repo size for the account
         account.current_opt_repo_size = optService.getRepoSizeInBytesAccount(account)
         account.save(flush: true, failOnError: true)


         res = [status: 'ok', message: message(code:'opt.upload.success'), opt: opt]
         render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
      }
   }

   def show(String uid)
   {
      def opt = OperationalTemplateIndex.findByUidAndOrganizationUid(uid, session.organization.uid)
      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'index'
         return
      }
      if (!opt.lastVersion)
      {
         flash.message = message(code:"opt.common.error.templateNotLastVersion")
         redirect action:'index'
         return
      }

      def opt_xml = optService.getOPTContents(opt)

      // get all versions of the OPT, including last (current opt uid)
      def versions = OperationalTemplateIndex.findAllBySetId(opt.setId, [sort: 'versionNumber', order: 'desc'])

      [opt_xml: opt_xml, opt: opt, versions: versions]
   }

   def items(String uid, String sort, String order)
   {
      def opt = OperationalTemplateIndex.findByUidAndOrganizationUid(uid, session.organization.uid)

      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'index'
         return
      }

      if (!opt.isIndexed) flash.message = message(code:"opt.feedback.notIndexed")

      sort = sort ?: 'id'
      order = order ?: 'asc'

      def items = opt.templateNodes.sort{ it."${sort}" }
      if (order == 'desc') items.reverse(true)

      return [items: items, templateInstance: opt]
   }

   def archetypeItems(String uid, String sort, String order)
   {
      def opt = OperationalTemplateIndex.findByUidAndOrganizationUid(uid, session.organization.uid)

      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'index'
         return
      }

      if (!opt.isIndexed) flash.message = message(code:"opt.feedback.notIndexed")

      def items = opt.referencedArchetypeNodes as List

      sort = sort ?: 'id'
      order = order ?: 'asc'

      assert items instanceof List

      items.sort { it."$sort" }
      if (order == 'desc') items.reverse(true)

      return [items: items, templateInstance: opt]
   }

   /*
    * Logical delete of one OPT.
    */
   def delete(String uid)
   {
      def opt = OperationalTemplateIndex.findByUidAndOrganizationUid(uid, session.organization.uid)
      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'index'
         return
      }

      opt.isDeleted = true
      opt.save(flush:true, failOnError: true) // without the flush the instance is not updated

      // removes references
      operationalTemplateIndexerService.deleteOptReferences(opt, false)

      // removes from cache (avoids showing the OPT in the query builder)
      def optMan = OptManager.getInstance()
      optMan.removeOpt(opt.templateId, session.organization.uid)

      // If the OPT file is moved and the reindex executed, the OPTIndex is deleted
      // from the database and not listed on trash, we need the OPTIndex to be on
      // the DB and remove it when it is physically deleted.

      // Should this delete orphan AIIs and OPT items? a.k.a. reindex, but not delete
      // the opt from the DB because there it is marked as deleted, and the file is
      // moved to the deleted folder

      flash.message = message(code:"opt.delete.deleted.ok")
      redirect action:'index'
      return
   }

   def activate(String uid)
   {
      def opt = OperationalTemplateIndex.findByUidAndOrganizationUid(uid, session.organization.uid)

      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'index'
         return
      }

      operationalTemplateIndexerService._event_activate(opt)

      // show should be for the latest version
      def c = OperationalTemplateIndex.createCriteria()
      def latest_version_uid = c.get {
         eq('setId', opt.setId)
         eq('lastVersion', true)
         projections {
            property('uid')
         }
      }

      redirect action:'show', id:latest_version_uid
   }

   def deactivate(String uid)
   {
      def opt = OperationalTemplateIndex.findByUidAndOrganizationUid(uid, session.organization.uid)

      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'index'
         return
      }

      // show should be for the latest version
      def c = OperationalTemplateIndex.createCriteria()
      def latest_version_uid = c.get {
         eq('setId', opt.setId)
         eq('lastVersion', true)
         projections {
            property('uid')
         }
      }

      // Check there is no pending compo to be indexed before deactivating, see https://github.com/ppazos/cabolabs-ehrserver/issues/944
      def count = CompositionIndex.countByDataIndexedAndTemplateIdAndOrganizationUid(false, opt.templateId, session.organization.uid)
      if (count > 0)
      {
         flash.message = message(code:"opt.common.error.templateCantBeDeactivatedIndexingPending")
         redirect action:'show', id:latest_version_uid
         return
      }

      operationalTemplateIndexerService._event_deactivate(opt)

      redirect action:'show', id:latest_version_uid
   }

   /*
    * Physical delete all logically deleted items.
    */
   def empty_trash()
   {
      optService.emptyTrash(session.organization)

      flash.message = message(code:"opt.trash.emptied")
      redirect action: 'trash' // URLMapping maps trash to index?deleted=true
   }

   def opt_manager_status()
   {
      def optMan = OptManager.getInstance()
      return [optMap: optMan.getLoadedOpts(session.organization.uid)]
   }
}
