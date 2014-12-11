package common.change_control

import ehr.Ehr

class VersionedComposition {

   // This is equivalent to the is_persistent method of VERSIONED_COMPOSITION from ehr package.
   boolean isPersistent
   
   // This will be set by the object_id of the first commit of an ORIGINAL_VERSION with change_type=creation
   String uid
   
   // This is equivalent to the owner_id attribute in the RM, just gave little more semantics to the name because we know the owner will be always the EHR.
   String ehrId
   
   // When the first commit of a VERSION is received.
   Date timeCreated = new Date()
   
   static belongsTo = [Ehr]
   
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
   
   List allVersions()
   {
      // Return all versions in which the uid prefix is the versioned object uid.
      return Version.findAllByUidLike(this.uid+"::%")
   }
   
   Version latestVersion()
   {
      return Version.findAllByUidLikeAndIsLastVersion(this.uid+"::%", true)
   }
   
   /**
    * Devuelve el EHR con id ehdId.
    */
   def getEHR()
   {
      def ehr = Ehr.findByEhrId(this.ehrId)
      
      // Caso imposible porque el uid fue establecido segun un EHR existente
      if (!ehr)
      {
         throw new Exception("El EHR con uid '$ehrId' no existe")
      }
      
      return ehr
   }
}
