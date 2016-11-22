package com.cabolabs.ehrserver.ehr.clinical_documents

import org.springframework.dao.DataIntegrityViolationException
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import grails.util.Holders

class CompositionIndexController {

   def config = Holders.config.app
   
   def index()
   {
      redirect(action: "list", params: params)
   }

   def list(Integer max)
   {
      params.max = Math.min(max ?: config.list_max, 100)
      [compositionIndexInstanceList: CompositionIndex.list(params), total: CompositionIndex.count()]
   }

   def show(Long id)
   {
      def compositionIndexInstance = CompositionIndex.get(id)
      if (!compositionIndexInstance)
      {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), id])
         redirect(action: "list")
         return
      }

      [compositionIndexInstance: compositionIndexInstance]
   }
}
