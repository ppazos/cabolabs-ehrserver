package com.cabolabs.ehrserver.openehr.directory

import com.cabolabs.ehrserver.openehr.ehr.Ehr;

class Folder {

   // Attributes from LOCATABLE
   String name
   String uid = (java.util.UUID.randomUUID() as String)
   /* This will be enabled when we support archetyped Folders.
   path
   archetype_node_id
   archetype_id
   template_id
   */
   
   Folder parent
   List items = []
   static hasMany = [folders: Folder, items: String] // items is a list of UIDs of the VERSIONED_OBJECTS contained.
   
   // EHR in which the Folder is contained
   // Only root nodes have EHRs, so ehr != null only if parent == null
   // TODO: add that to constraints
   Ehr ehr
   static belongsTo = [Ehr, Folder]
   
   // multitenancy
   String organizationUid
   
   static constraints = {
      parent(nullable: true)
      name(empty: false)
      ehr(nullable: true)
   }
   
   static mapping = {
      items cascade: 'all-delete-orphan'
   }
}
