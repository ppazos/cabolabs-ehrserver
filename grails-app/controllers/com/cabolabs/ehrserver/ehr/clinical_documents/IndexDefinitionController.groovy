package com.cabolabs.ehrserver.ehr.clinical_documents

import org.springframework.dao.DataIntegrityViolationException
import com.cabolabs.archetype.*
import com.cabolabs.ehrserver.ehr.clinical_documents.IndexDefinition

class IndexDefinitionController {

   def index()
   {
      redirect(action: "list", params: params)
   }

   def list(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      //Añado pageCurrent para indicar al paginatorTagLib que pagina debe marcar como actual. Si no se pierde.
      [indexDefinitionInstanceList: IndexDefinition.list(params), total: IndexDefinition.count()]
   }

   def show(Long id)
   {
      def indexDefinitionInstance = IndexDefinition.get(id)
      if (!indexDefinitionInstance)
      {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), id])
         redirect(action: "list")
         return
      }

      [indexDefinitionInstance: indexDefinitionInstance]
   }

   /**
    * (re)generates indexes for all archetypes in the repo.
    * This is usefull to add archetypes to the repo and index them to generate new queries.   
    */
   def generate() {

      // delete current
      def indexes = IndexDefinition.list()
      indexes.each {
       it.delete()
      }
      
      def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
      ti.indexAll()
      
      redirect(action: "list")
   }
}
