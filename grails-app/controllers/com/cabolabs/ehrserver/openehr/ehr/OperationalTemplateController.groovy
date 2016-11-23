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
      
      def list
      def c = OperationalTemplateIndex.createCriteria()
      
      list = c.list (max: max, offset: offset, sort: sort, order: order) {
         if (concept)
         {
            like('concept', '%'+concept+'%')
         }
      }
      
      return [opts: list,
              total: list.totalCount]
   }
   
   /**
    * (re)generates indexes for all archetypes in the repo.
    * This is usefull to add archetypes to the repo and index them to generate new queries.
    */
   def generate()
   {
      def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      ti.indexAll()
      
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
         
         
         // Parse to get the template id
         def slurper = new XmlSlurper(false, false)
         def template = slurper.parseText(xml)
         
         // OPT already uploaded, check if it needs to overwrite existing
         def was_overwritten = false
         
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
               
               // if was_overwritten, we do an indexAll, and that deletes all the OPT, 
               // OPT items and archetype items, so there is mo need of deleting stuff here.
               was_overwritten = true
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
         
         
         def indexer = new OperationalTemplateIndexer()
         def opt = indexer.createOptIndex(template) // saves OperationalTemplateIndex
         
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
         
         
         if (was_overwritten)
         {
            indexer.indexAll()
         }
         else
         {
            // If there is no overwrite
            // Generates OPT and archetype item indexes just for the uploaded OPT
            indexer.index(template)
         }
         
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
      
      def items = OperationalTemplateIndexItem.findAllByTemplateId(opt.templateId, [sort: sort, order: order])
      
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
      
      if (order == 'desc') items = items.reverse()
      
      return [items: items, templateInstance: opt]
   }
}
