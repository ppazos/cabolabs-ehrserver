package com.cabolabs.ehrserver.ehr.clinical_documents

class OperationalTemplateIndex {

	String templateId
	String concept          // Concept name of the OPT
	String language         // en formato ISO_639-1::en
	String uid
   String archetypeId      // root archetype id
   String archetypeConcept // concept name for the archetype root node
   
   static hasMany = [referencedArchetypeNodes: ArchetypeIndexItem]
}
