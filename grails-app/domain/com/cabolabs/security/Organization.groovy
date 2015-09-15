package com.cabolabs.security

class Organization {

   String uid = java.util.UUID.randomUUID() as String
   
   String name
   String pin

   static constraints = {
   }
}
