package com.cabolabs.ehrserver.ehr.clinical_documents

class OperationalTemplateIndex {

   String templateId
   String concept          // Concept name of the OPT
   String language         // en formato ISO_639-1::en
   String uid
   String archetypeId      // root archetype id
   String archetypeConcept // concept name for the archetype root node
   
   String fileUid = java.util.UUID.randomUUID() as String
   
   // true => shared with all the organizations
   boolean isPublic
   
   static hasMany = [referencedArchetypeNodes: ArchetypeIndexItem, 
                     templateNodes: OperationalTemplateIndexItem]
   
   static transients = ['lang']
   def getLang()
   {
      this.language.split('::')[1]
   }
   
   static namedQueries = {
      forOrg { org ->
         
         def shares = OperationalTemplateIndexShare.findAllByOrganization(org)
         
         if (shares)
         {
            or {
               eq('isPublic', true)
               'in'('id', shares.opt.id)
            }
         }
         else
         {
            eq('isPublic', true)
         }
      }
      
      likeConcept { concept ->
         if (concept)
         {
            like('concept', '%'+concept+'%')
         }
      }
   }
}
