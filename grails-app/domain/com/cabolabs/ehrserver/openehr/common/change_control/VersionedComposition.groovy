package com.cabolabs.ehrserver.openehr.common.change_control

import com.cabolabs.ehrserver.openehr.ehr.Ehr

class VersionedComposition {

   // This is equivalent to the is_persistent method of VERSIONED_COMPOSITION from ehr package.
   boolean isPersistent
   
   // This will be set by the object_id of the first commit of an ORIGINAL_VERSION with change_type=creation
   String uid
   
   // This is equivalent to the owner_id attribute in the RM, just gave little more semantics to the name because we know the owner will be always the EHR.
   String ehrUid
   
   // When the first commit of a VERSION is received.
   Date timeCreated = new Date()
   
   static belongsTo = [Ehr]
   
   static transients = ['allVersions', 'latestVersion']
   
   /*
    * Some Methods in the RM
    * 
    * allVersions
    * allVersionIds
    * versionCount
    * hasVersionId
    * isOriginalVersion(versionId)
    * versionWithId(id)
    * latestVersion
    * latestTrunkVersion
    * trunkLifecycleState
    * ...
    */
   
   //static transients = ['allVersions']
   
   List getAllVersions()
   {
      // Return all versions in which the uid prefix is the versioned object uid.
      return Version.findAllByUidLike(this.uid+"::%")
   }
   
   Version getLatestVersion()
   {
      def c = Version.createCriteria()
      def v = c.get {
         like('uid', this.uid + '::%')
         data {
            eq('lastVersion', Boolean.TRUE)
         }
      }
      return v
   }
   
   /**
    * Devuelve el EHR con id ehdId.
    */
   def getEHR()
   {
      def ehr = Ehr.findByUid(this.ehrUid)
      
      // Caso imposible porque el uid fue establecido segun un EHR existente
      if (!ehr)
      {
         throw new Exception("El EHR con uid '$ehrUid' no existe")
      }
      
      return ehr
   }
}
