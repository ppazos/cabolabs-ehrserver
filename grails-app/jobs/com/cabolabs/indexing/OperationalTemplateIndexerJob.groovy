package com.cabolabs.indexing

import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.security.Organization

class OperationalTemplateIndexerJob {

   def operationalTemplateIndexerService
   def optService
   
   static concurrent = false

   static triggers = {
      simple repeatInterval: 30000l, startDelay: 240000l // execute job once in 60 seconds
   }

   def execute()
   {
      println "OperationalTemplateIndexerJob"

      def opts = OperationalTemplateIndex.findAllByIsIndexed(false)
      def template, xml
      def slurper = new XmlSlurper(false, false)
         

      opts.each { optIndex ->

         println "Indexing ${optIndex.templateId}"

         // FIXME: this is a terrible way of saying "do not create the OPTIndex", that should be a parameter or something else...
         // Generates OPT and archetype item indexes just for the uploaded OPT
         operationalTemplateIndexerService.templateIndex = optIndex // avoids creating another opt index internally and use the one created here


         xml = optService.getOPTContents(optIndex)
         template = slurper.parseText(xml)


         operationalTemplateIndexerService.index(template, Organization.findByUid(optIndex.organizationUid))


         // load opt in manager cache
         def optMan = OptManager.getInstance()
         println optIndex
         println optIndex.templateId
         println optIndex.externalTemplateId
         
         println optMan.status()
         optMan.load(optIndex.templateId, optIndex.organizationUid, true)
         println optMan.status()

         // the mark as indexed happens in the service.index
         //optIndex.isIndexed = true
         //if (!optIndex.save(flush:true)) println optIndex.errors
      }
   }
}
