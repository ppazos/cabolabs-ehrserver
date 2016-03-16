package com.cabolabs.ehrserver.ehr.clinical_documents

class ArchetypeIndexItem {

   String archetypeId   // archetype that defines the data point
   String path
   String rmTypeName
   
   // Name of the node by the archetype in the template language, can be empty.
   String name
   
   // if rmTypeName is DV_CODED_TEXT, it might define a reference to a terminology, for it's definition_code attribute.
   // In the OPT that value is in the referenceSetUri element, inside the defininig_code children element.
   // https://github.com/ppazos/cabolabs-ehrserver/issues/137
   String terminologyRef
   
   static constraints = {
      name(nullable:true)
      path(size:1..1023)
      terminologyRef(nullable:true)
   }
}
