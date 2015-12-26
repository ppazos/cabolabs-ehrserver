package com.cabolabs.security

class Organization {

   String uid = java.util.UUID.randomUUID() as String
   String name
   String number // identifier of the organization to be used for user registration

   static constraints = {
      number nullable: true
   }
   
   def beforeInsert()
   {
      println "before insert "+ this +" "+ this.name
      if (!this.number) assignNumber()
   }
   
   def beforeUpdate()
   {
      println "before update"
      if (!this.number) assignNumber()
   }
   
   private void assignNumber()
   {
      def number = String.randomNumeric(6)
      
      while (Organization.countByNumber(number) == 1) // avoids repeated number
      {
         println "while"
         number = String.randomNumeric(6)
      }
      
      this.number = number
   }
}
