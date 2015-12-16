package com.cabolabs.ehrserver.identification

class PersonIdType {

   String name
   String code // can be an OID
   
   static constraints = {
      code unique: true
   }
}
