package com.cabolabs.ehrserver.openehr.ehr

import grails.util.Holders
import groovy.xml.MarkupBuilder
import net.pempek.unicode.UnicodeBOMInputStream
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.archetype.OperationalTemplateIndexer
import com.cabolabs.ehrserver.ehr.clinical_documents.*

class OperationalTemplateController {

   def config = Holders.config.app
   def xmlValidationService
   
   def list(int max, int offset, String sort, String order, String concept)
   {
      max = Math.min(max ?: config.list_max, 100)
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def org = session.organization
      def shares = OperationalTemplateIndexShare.findAllByOrganization(org)
      
      def c = OperationalTemplateIndex.createCriteria()
      def list = c.list (max: max, offset: offset, sort: sort, order: order) {
         if (concept)
         {
            like('concept', '%'+concept+'%')
         }
         
         if (shares)
         {
            or {
               eq('isPublic', true)
               'in'('id', shares.opt.id)
            }
         }
         else
         {
            eq('isPublic', true)
         }
      }
      
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
            errors << "No OPT was uploaded"
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
         
         // check existing by OPT uid
         def opt_uid = template.uid.value.text()
         def existing_opt = OperationalTemplateIndex.findByUid(opt_uid)
         
         // check existing by templateId
         def opt_template_id
         if (!existing_opt)
         {
            opt_template_id = template.template_id.value.text()
            existing_opt = OperationalTemplateIndex.findByTemplateId( opt_template_id )
         }
         
         if (existing_opt)
         {
            if (overwrite) // OPT exists and the user wants to overwrite
            {
               def existing_file = new File(config.opt_repo + existing_opt.fileUid + '.opt')
               existing_file.delete()
               
               // delete all the indexes of the opt
               indexer.deleteOptReferences(existing_opt)
            }
            else
            {
               if (!opt_template_id)
                  errors << "The OPT ${opt_uid} already exists, change it's UID or select to overwrite"
               else
                  errors << "The OPT ${opt_template_id} already exists, change it's UID or select to overwrite"
               return [errors: errors]
            }
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
         flash.message = 'Template not found'
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
         flash.message = 'Template not found'
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
