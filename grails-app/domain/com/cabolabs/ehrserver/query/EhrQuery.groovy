package com.cabolabs.ehrserver.query

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import static grails.async.Promises.*

/**
 * This type of query allows:
 *
 * 1. to verify a set of conditions against a given EHR, for example:
 * does the EHR contains a compo with diagnosis "diabetes" and compo with and admin entry
 * with gender = "female"? The result will be boolean.
 *
 * 2. to query for EHRs that complies a set of conditions. This case uses the same kind
 * of conditions as 1. but the result is a set of EHRs.
 *
 */
class EhrQuery {

   String uid = java.util.UUID.randomUUID() as String
   String name
   String description
   
   List queries = []
   static hasMany = [queries: Query]

   static constraints = {
      description nullable: true
      queries minSize: 1, validator: {val, obj ->
      
         val.every { query -> query.type == 'composition' } ?: 'allQueriesShouldBeCompoQueries'
      }
   }
   
   
   /**
    * returns true if the EHR complies with the criteria of the EhrQuery, false otherwise.
    */
   boolean checkEhr(String ehrUid)
   {
      println "ehr check"
      def matching_compo_index_counts = []
      this.queries.each { query ->
         
         matching_compo_index_counts << query.executeComposition(ehrUid, null, null, null, 1, 0, null, null, true)
      }
      
      //println matching_compo_index_counts // [[1], [0]]
      //println matching_compo_index_counts.flatten() // [1, 0]
      
      // the count should be > 0 on all results to return true
      return matching_compo_index_counts.flatten().every { it > 0 }
   }
   
   /**
    * Get max EHR uids that complies with the criteria of the EhrQuery.
    */
   def getEhrUids(String organizationUid)
   {
      // Result is a set of matching EHR uids
      def ehr_cis = Collections.synchronizedList([])
      List<Thread> threads = []
      
      this.queries.each { query ->
         
         threads << Thread.start {
         
            def res
         
            // the query should be executed against all EHRs
            // TODO: queries can be executed in parallel then joined to get the final result
            Query.withTransaction { // adds hibernate session to current thread
               res = query.executeComposition(null, null, null, organizationUid, Ehr.count(), 0, null, null, false, true)
            }
            
            synchronized (ehr_cis) { // writes to the result list are done in parallel and should be synchronized (lock access at the same time)
               ehr_cis << res
            }
            
            // the query should be:
            // SELECT ehr.uid, COUNT(ci.id)
            // FROM Ehr ehr, CompositionIndex ci
            // WHERE ... <<< filters and subqueries
            // GROUP BY ehr.uid
            
            // ehruid, count ci.id
            // [[11111111-1111-1111-1111-111111111111, 4], [22222222-1111-1111-1111-111111111111, 3]]
            //println ehr_cis
         }
      }
      
      // wait for all queries to finish
      threads*.join()
      
      def ehrUids = []

      // 1. ehr_cis always has one result since at least one query is required.
      // 2. result final result is the intersection, so should included in the ehrUids of the first result.
      // 3. the first result might be empty, on that case, the final result is empty.
      
      // first result
      ehrUids = ehr_cis[0]*.getAt(0)

      // if more than one query was executed, do the result intersection of the ehrUids and returns that.
      if (ehr_cis.size()>1)
      {
         for (int i = 1; i<ehr_cis.size(); i++)
         {
            ehrUids = ehrUids.intersect( ehr_cis[i]*.getAt(0) )
         }
      }
      
      return ehrUids
   }
   
   def getEhrUids2(String organizationUid)
   {
      // http://docs.grails.org/2.5.6/guide/async.html
      def tasks = []
      int max = Ehr.countByOrganizationUid(organizationUid)
      this.queries.each { query ->
         
         tasks << task {
            def res
            println "task "+ query.name
            Query.withTransaction {
               res = query.executeComposition(null, null, null, organizationUid, max, 0, null, null, false, true)
            }
            return res
         }
      }

      def ehrUids = []
      
      def ehr_cis = waitAll(tasks)
      
      //println "results "+ ehr_cis +" "+ ehr_cis*.size()
   
      // this executes a logical AND between all results
      // first result
      ehrUids = ehr_cis[0]*.getAt(0)

      // if more than one query was executed, do the result intersection of the ehrUids and returns that.
      if (ehr_cis.size()>1)
      {
         for (int i = 1; i<ehr_cis.size(); i++)
         {
            ehrUids = ehrUids.intersect( ehr_cis[i]*.getAt(0) )
         }
      }

      return ehrUids
   }
}
