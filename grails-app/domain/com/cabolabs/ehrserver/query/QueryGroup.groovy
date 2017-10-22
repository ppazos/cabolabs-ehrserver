package com.cabolabs.ehrserver.query

class QueryGroup {

   String uid = java.util.UUID.randomUUID() as String
   String name
   String organizationUid

   static constraints = {
   }
}
