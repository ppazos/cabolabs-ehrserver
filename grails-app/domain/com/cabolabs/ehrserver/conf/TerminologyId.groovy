package com.cabolabs.ehrserver.conf

class TerminologyId {

   String name
   String terminology_release
   String terminology_version

   static constraints = {
      terminology_release nullable: true
      terminology_version nullable: true
   }
}
