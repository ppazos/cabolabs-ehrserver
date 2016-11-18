package com.cabolabs.ehrserver.query

import com.cabolabs.security.Organization

class QueryShare {

   Query query // this query is shared with
   Organization organization // this organization
   
   static constraints = {
      query nullable: false
      organization: nullable: false
   }
}
