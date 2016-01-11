package com.cabolabs.ehrserver.openehr.common.generic

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType

//@XmlAccessorType(XmlAccessType.FIELD)
class AuditDetails {

   // Identificador del sistema al que fue commiteado el cambio en el EHR
   String systemId = "ISIS_EHR" // FIXME: debe salir de config
   
   // Lo establece el servidor cuando recibe un commit
   Date timeCommitted
   
   String changeType //= "creation" // otros valores: deleted, amendment, modification, attestation, addition
   
   DoctorProxy committer
   
   static constraints = {
      // is nullable for Contribution.audit
      changeType(nullable:true, inList:["creation","amendment","modification","synthesis","deleted","attestation","unknown"])
   }
   
   // Para que Contribution salve su AuditDetails en cascada
   static belongsTo = [Contribution, Version]
   
   public String toString()
   {
      return this.getClass().getSimpleName() +' '+ changeType +' '+ systemId +' '+ timeCommitted
   }
}