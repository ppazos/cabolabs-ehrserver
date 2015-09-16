package com.cabolabs.security

class Organization {

   String uid = java.util.UUID.randomUUID() as String
   String name
   String number // identifier of the organization to be used for user registration

   static constraints = {
      number nullable: true
   }
}
