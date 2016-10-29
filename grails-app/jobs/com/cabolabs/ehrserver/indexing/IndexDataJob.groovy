package com.cabolabs.ehrserver.indexing

import com.cabolabs.ehrserver.data.DataIndexerService

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
      // transactional service
      dataIndexerService.generateIndexes()
   }
}
