package com.cabolabs.ehrserver.query

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
    * 
    * When the ehrUid is given, want to check the conditions against that EHR.
    */
   def execute(String ehrUid, String organizationUid)
   {
      // Result is a set of matching EHRs
      if (!ehrUid)
      {
         def ehr_cis
         this.queries.each { query ->
            
            ehr_cis = query.executeComposition(null, null, null, organizationUid, 1000, 0, null, null, false, true)
            // the query should be:
            // SELECT ehr.uid, COUNT(ci.id)
            // FROM Ehr ehr, CompositionIndex ci
            // WHERE ... <<< filters and subqueries
            // GROUP BY ehr.uid
            
            // ehruid, count ci.id
            // [[11111111-1111-1111-1111-111111111111, 4], [22222222-1111-1111-1111-111111111111, 3]]
            println ehr_cis // ehr_cis*.ci.ehrUid
            // 1. store ehrUids for each result
            // 2. do the intersect of all the ehrUid lists
            // 3. the result is the intersection, since are al the ehrUids that had results on all the queries (is an AND)
         }
      }
      // Result is tru/false for the given EHR
      else
      {
         def ci
         this.queries.each { query ->
            
            ci = query.executeComposition(ehrUid, null, null, organizationUid, 1, 0, null, null)
            println ci // ci.ehrUid
            // if all queries return 1 result, return true, else false
            // we can detect the first empty result, break and return false
         }
      }
   }
}
