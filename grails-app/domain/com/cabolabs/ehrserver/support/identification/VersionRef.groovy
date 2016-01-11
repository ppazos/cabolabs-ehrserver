package com.cabolabs.ehrserver.support.identification

class VersionRef {

   // Referencia valida localmente
   String namespace = "local"
   
   // Apunta a una VERSION
   String type = "VERSION"
   
   // Identificador confiable de la VERSION
   // object_id::creating_system_id::version_tree_id
   String value
   
   static constraints = {
   }
}
