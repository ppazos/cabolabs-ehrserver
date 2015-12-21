package ehr

import com.cabolabs.ehrserver.data.DataIndexerService

/**
 * Indexa datos de compositions commiteadas.
 * 
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class IndexDataJob {
   
   static triggers = {
      simple repeatInterval: 45000l, startDelay:240000 // execute job once in 45 seconds
   }
   
   def dataIndexerService

   def execute()
   {
      println "IndexDataJob"
      // transactional service
      dataIndexerService.generateIndexes()
   }
}
