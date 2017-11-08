package com.cabolabs.ehrserver.query

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import static grails.async.Promises.*

class QueryGroup {

   String uid = java.util.UUID.randomUUID() as String
   String name
   String organizationUid

   static constraints = {
   }
   static mapping = {
     organizationUid index: 'org_uid_idx'
   }
   
   /*
    * Returns the number of EHRs that match with each single query.
    */
   def executeCount(String organizationUid)
   {
      def queries = Query.findAllByQueryGroup(this)
      
      // http://docs.grails.org/2.5.6/guide/async.html
      def tasks = []
      int max = Ehr.countByOrganizationUid(organizationUid)
      queries.each { query ->
         
         tasks << task {
            def res = [:]
            println "task "+ query.name
            Query.withTransaction {
               res[(query.uid)] = query.executeComposition(null, null, null, organizationUid, max, 0, null, null, false, true)
            }
            return res
         }
      }
      
      def res = [:]
      def all_results = waitAll(tasks)
      
      all_results.each { resMap -> // has one key and the list of results in that key
         // returns the first and only entry
         def entry = resMap.find{ key, value -> true }
         res[entry.key] = entry.value.size()
         
         /*
         println resMap.getClass()
         println resMap.keySet()
         println resMap.find{ key, value -> true }.key
         */
      }

      return res
   }
}
