package com.cabolabs.ehrserver.openehr.common.generic

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType

/**
 * Emula a common.generic.PARTY_IDENTIFIED que hereda de PARTY_PROXY.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
class DoctorProxy {

   // Emula PARTY_PROXY.external_ref.namespace
   // La referencia (id) es valida localmente
   String namespace = "local"
   
   // Emula PARTY_PROXY.external_ref.type
   // Apunta a una persona
   String type = "PERSON"
   
   // Identificador confiable del medico (no es su cedula, documento o pasaporte), es asignado por el sistema al crear el doctor
   // Emula PARTY_PROXY.external_ref.id<OBJECT_ID>.value
   // Que a su vez emula un HIER_OBJECT_ID.root y su valor va a ser un UUID (java.util.UUID.randomUUID() as String)
   String value
   
   // PARTY_IDENTIFIED.name
   String name
   
   // PARTY_IDENTIFIED.identifiers esta representado en Person.(idCode,idType)
   
   static constraints = {
      namespace(nullable:true)
      type(nullable:true)
      value(nullable:true)
      name(nullable:false) // Debe venir name y se toma como identificador debil
   }
   
   // Para que AuditDetails salve su DoctorProxy en cascada
   static belongsTo = [AuditDetails]
}