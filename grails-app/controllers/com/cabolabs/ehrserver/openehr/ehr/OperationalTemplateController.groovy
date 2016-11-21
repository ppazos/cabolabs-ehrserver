package com.cabolabs.ehrserver.openehr.ehr

import grails.util.Holders
import groovy.xml.MarkupBuilder
import net.pempek.unicode.UnicodeBOMInputStream
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.ehrserver.ehr.clinical_documents.*

class OperationalTemplateController {

   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   def xmlValidationService
   
   def list(int max, int offset, String sort, String order, String concept)
   {
      max = Math.min(max ?: 10, 100)
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
    * 
    * @param overwrite
    * @return
    */
   def upload(boolean overwrite)
   {
      //println "upload "+ params
      //println "destination folder "+ config.opt_repo
      
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
         
         def indexer = new com.cabolabs.archetype.OperationalTemplateIndexer()
         def opt = indexer.createOptIndex(template) // saves OperationalTemplateIndex
         
         // Prepare file
         def destination = config.opt_repo + opt.fileUid + '.opt' //f.getOriginalFilename()
         File fileDest = new File( destination )
         
         
         // FIXME: overwrite check should happen using the template uid not the filename.
         if (!overwrite && fileDest.exists())
         {
           // FIXME: overwrite might cause inconsistencies with currently saved data for the previous version of the template
           errors << "The OPT already exists"
           return [errors: errors]
         }
         
         
         // Hago lo mismo que el transferTo pero a mano.
         if (overwrite) // file exists and the user wants to overwrite
         {
            def copy = new File(destination + ".old")
            fileDest.renameTo(copy)
            copy.delete()
         }
         
         fileDest << xml
         
         // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/commons/CommonsMultipartFile.html#transferTo-java.io.File-
         // If the file exists, it will be deleted first
         //f.transferTo(fileDest)
         // Tira excepcion si el archivo existe:
         // Message: opts\Signos.opt (Access is denied)
         //   Line | Method
         //->>  221 | <init>   in java.io.FileOutputStream
         
         flash.message = g.message(code:"opt.upload.success")
         
         
         // Generates OPT and archetype item indexes for the uploaded OPT
         indexer.index(template)
         
         
         // load opt in manager cache
         // TODO: just load the newly created ones
         def optMan = OptManager.getInstance()
         optMan.unloadAll()
         optMan.loadAll()
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
      
      def opt_file = new File(config.opt_repo + opt.concept +".opt")
      
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
