package common.change_control

import common.generic.AuditDetails
import ehr.clinical_documents.CompositionIndex
//import support.identification.CompositionRef // T0004 ya no se usa
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType

//@XmlAccessorType(XmlAccessType.FIELD)
class Version {

   // Now assigned by the client:
   // https://github.com/ppazos/cabolabs-ehrserver/issues/50
   //
   // FIXME: https://github.com/ppazos/cabolabs-ehrserver/issues/52
   //
   // Emula ORIGINAL_VERSION.uid, ORIGINAL_VERSION hereda de VERSION
   // owner_id (id del EHR), creating_system_id (identificador del sistema
   // donde se creó la versión) and version_tree_id (es el número del branch
   // que se crea cuando se piden datos para modificar (no creo que sea necesario
   // tener asignar números de branch cuando se piden datos solo para leer)).
   // Como no hay modificaciones (por ahora) el version_tree_id siempre va a ser 1
   // (debe ser asignado por el servidor cuando se hace commit de un documento).
   //
   // Ej. 591eb8e8-3a65-4630-a2e9-ffdeafc9bbba::10aec661-5458-4ff6-8e63-c2265537196d::1
   //
   // El id lo establece el EHR Server cuando recibe un commit.
   //
   String uid
   
   // Emula ORIGINAL_VERSION.lifecycle_state
   String lifecycleState
   
   AuditDetails commitAudit
   
   // Datos commiteados (referencia a la composition)
   //CompositionRef data
   // T0004: CompositionRef se deja de usar y se usa CompositionIndex
   CompositionIndex data
   
   Contribution contribution
   
   /**
    * id de la composition que contiene la version
    * 
    * @return String UUID
    */
   def getObjectId()
   {
      return uid.split("::")[0]  
   }
   
   /**
    * id del sistema que commitea los datos (donde fue creada la version)
    * 
    * @return String
    */
   def getCreatingSystemId()
   {
      return uid.split("::")[1]
   }
   
   /**
    * Devuelve el EHR con id getOwnerId()
    * 
    * @return
    */
   def getEHR()
   {
      def ehrId = getOwnerId()
      def ehr = Ehr.findByEhrId(ehrId)
      
      // Caso imposible porque el uid fue establecido segun un EHR existente
      if (!ehr)
      {
         throw new Exception("El EHR con uid '$ehrId' no existe")
      }
      
      return ehr
   }
   
   static transients = ['ownerId', 'creatingSystemId', 'EHR']
   
   static belongsTo = [Contribution]
   
   static constraints = {
      contribution(nullable:false) // La version debe estar dentro de una contribution
   }
}