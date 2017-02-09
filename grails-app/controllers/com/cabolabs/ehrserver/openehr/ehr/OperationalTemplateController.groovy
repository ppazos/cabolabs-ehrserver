/*
 * Copyright 2011-2017 CaboLabs Health Informatics
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
import com.cabolabs.archetype.OperationalTemplateIndexer
import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexShare

class OperationalTemplateController {

   def config = Holders.config.app
   def xmlValidationService
   def springSecurityService
   
   def list(int max, int offset, String sort, String order, String concept)
   {
      max = Math.min(max ?: config.list_max, 100)
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def org = session.organization
      def list = OperationalTemplateIndex.forOrg(org).likeConcept(concept).list (max: max, offset: offset, sort: sort, order: order)
      
      [opts: list, total: list.totalCount]
   }
   
   /**
    * (re)generates indexes for all archetypes in the repo.
    * This is usefull to add archetypes to the repo and index them to generate new queries.
    */
   def generate()
   {
      def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      ti.indexAll(session.organization)
      
      redirect(action: "list")
   }
   
   
   /**
    * TODO: refactor to service, this should be transactional.
    * @param overwrite
    * @return
    */
   def upload(boolean overwrite)
   {
      if (params.doit)
      {
         def errors = []
         
         // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/commons/CommonsMultipartFile.html
         def f = request.getFile('opt')
         
         // Add file empty check
         if(f.empty)
         {
            errors << message(code:"opt.upload.error.noOPT")
            return [errors: errors]
         }

         // Avoid BOM on OPT files (the Template Designer exports OPTs with BOM and that breaks the XML parser)
         def bytes = f.getBytes()
         def inputStream = new ByteArrayInputStream(bytes)
         def bomInputStream = new UnicodeBOMInputStream(inputStream)
         bomInputStream.skipBOM() // NOP if no BOM is detected
         
         // Read out
         def isr = new InputStreamReader(bomInputStream)
         def br = new BufferedReader(isr)
         def xml = br.text // getText from Groovy
         //def xml = new String( f.getBytes() )
         
         // Validate XML
         if (!xmlValidationService.validateOPT(xml))
         {
           errors = xmlValidationService.getErrors() // Important to keep the correspondence between version index and error reporting.
           return [errors: errors]
         }
         
         // Will index the opt nodes, and help deleting existing ones when updating
         def indexer = new OperationalTemplateIndexer()
         
         // Parse to get the template id
         def slurper = new XmlSlurper(false, false)
         def template = slurper.parseText(xml)
         
         
         // check existing by OPT uid or templateId, shared with an org of the current user
         def opt_uid = template.uid.value.text()
         def opt_template_id = template.template_id.value.text()
         
         def user = springSecurityService.currentUser
         def orgs = user.organizations
         def c = OperationalTemplateIndexShare.createCriteria()
         def shares = c.list {
            // exists an OPT with uid or template id?
            opt {
               or {
                  eq('uid', opt_uid)
                  eq('templateId', opt_template_id)
               }
            }
            // only for the current user orgs
            'in'('organization', orgs)
         }
         
         // 1. there is one share, with the session org => overwrite if specified
         // 2. there is one share, with another org of the current user => can't overwrite, should upload the OPT and overwrite while logged with that org
         // 3. there are many shares => can't overwrite, should remove the shares first
         
         if (shares.size() == 1)
         {
            //println "shares size is 1"
            if (shares[0].organization.id != session.organization.id)
            {
               errors << message(code:"opt.upload.error.alreadyExistsNotInOrg", args:[shares[0].organization.name, session.organization.name]) //"There exists an OPT with the same uid or templateId shared with another of your organizations (${shares[0].organization.name}), login with that organization to overwrite or share the OPT from that org iwth the current organization (${session.organization.name})"
               return [errors: errors]
            }
            
            // Can overwrite if it was specified
            if (overwrite) // OPT exists and the user wants to overwrite
            {
               def existing_opt = shares[0].opt
               def existing_file = new File(config.opt_repo + existing_opt.fileUid + '.opt')
               existing_file.delete()
               
               // delete all the indexes of the opt
               indexer.deleteOptReferences(existing_opt)
            }
            else
            {
               errors << message(code:"opt.upload.error.alreadyExists")
               return [errors: errors]
            }
         }
         else if (shares.size() > 1)
         {
            //println "shares size is > 1"
            errors << message(code:"opt.upload.error.alreadyExistsInManyOrgs", args:[session.organization.name])
            return [errors: errors]
         }
         else
         {
            //println "shares size is 0"
         }
         
         def opt = indexer.createOptIndex(template, session.organization) // saves OperationalTemplateIndex
         
         // Prepare file
         def destination = config.opt_repo + opt.fileUid + '.opt' //f.getOriginalFilename()
         File fileDest = new File( destination )
         fileDest << xml
         
         // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/commons/CommonsMultipartFile.html#transferTo-java.io.File-
         // If the file exists, it will be deleted first
         //f.transferTo(fileDest)
         // Tira excepcion si el archivo existe:
         // Message: opts\Signos.opt (Access is denied)
         //   Line | Method
         //->>  221 | <init>   in java.io.FileOutputStream
         
         flash.message = g.message(code:"opt.upload.success")
         
         opt.isPublic = (params.isPublic != null)

         // Generates OPT and archetype item indexes just for the uploaded OPT
         indexer.templateIndex = opt // avoids creating another opt index internally and use the one created here
         indexer.index(template, null, session.organization, true)

         
         // load opt in manager cache
         // TODO: just load the newly created ones
         def optMan = OptManager.getInstance()
         optMan.unloadAll()
         optMan.loadAll()
         
         redirect action:'upload'
      }
   }
   
   def show(String uid)
   {
      def opt = OperationalTemplateIndex.findByUid(uid)
      if (!opt)
      {
         flash.message = 'Template not found'
         redirect action:'list'
         return
      }
      
      def opt_file = new File(config.opt_repo + opt.fileUid +".opt")
      
      [opt_xml: opt_file.getText(), opt: opt]
   }
   
   def items(String uid, String sort, String order)
   {
      def opt = OperationalTemplateIndex.findByUid(uid)
      
      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'list'
         return
      }
      
      sort = sort ?: 'id'
      order = order ?: 'asc'
      
      def items = opt.templateNodes.sort{ it."${sort}" }
      if (order == 'desc') items.reverse(true)
      
      return [items: items, templateInstance: opt]
   }
   
   def archetypeItems(String uid, String sort, String order)
   {
      def opt = OperationalTemplateIndex.findByUid(uid)
      
      if (!opt)
      {
         flash.message = message(code:"opt.common.error.templateNotFound")
         redirect action:'list'
         return
      }
      
      def items = opt.referencedArchetypeNodes as List
      
      sort = sort ?: 'id'
      order = order ?: 'asc'
      
      assert items instanceof List
      
      items.sort { it."$sort" }
      if (order == 'desc') items.reverse(true)
      
      return [items: items, templateInstance: opt]
   }
}
