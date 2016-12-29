package com.cabolabs.ehrserver.indexing

import com.cabolabs.ehrserver.data.DataIndexerService
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

/**
 * Indexa datos de compositions commiteadas.
 * 
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class IndexDataJob {
   
   def concurrent = false
   
   static triggers = {
      simple repeatInterval: 30000l // execute job once in 30 seconds
   }
   
   def dataIndexerService

   def execute()
   {
      // TODO: if indexing takes more than repeatInterval,
      //       the process will try to index the same version twice.
      //       we should lock the compo indexes.
      def compoIdxs = CompositionIndex.findAllByDataIndexed(false)
      
      compoIdxs.each { compoIndex ->
         // transactional service
         dataIndexerService.generateIndexes(compoIndex)
      }
   }
}
