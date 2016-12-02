package com.cabolabs.security

class Organization {

   String uid = java.util.UUID.randomUUID() as String
   String name
   String number // identifier of the organization to be used for user registration
   String preferredLanguage

   static constraints = {
      number nullable: true
      preferredLanguage nullable: true
   }
   
   def beforeInsert()
   {
      if (!this.number) assignNumber()
   }
   
   def beforeUpdate()
   {
      if (!this.number) assignNumber()
   }
   
   private void assignNumber()
   {
      def _number = String.randomNumeric(6)
      
      while (Organization.countByNumber(_number) == 1) // avoids repeated number
      {
         _number = String.randomNumeric(6)
      }
      
      this.number = _number
   }
}
