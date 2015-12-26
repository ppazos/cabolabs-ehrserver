package com.cabolabs.ehrserver.openehr.demographic

//import javax.xml.bind.annotation.XmlAccessorType
//import javax.xml.bind.annotation.XmlAccessType

/**
 * Emula la clase demographic.PERSON.
 *
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
class Person {

   // Emula LOCATABLE.uid (porque PARTY es LOCATABLE y PERSON hereda de PERTY)
                       // uid referenciado desde PatienProxy.value
   String uid = java.util.UUID.randomUUID() as String
   
   // Emula PARTY.identities<PARTY_IDENTITY>
   String firstName    // primer nombre
   String lastName     // primer apellido
   Date dob            // fecha de nacimiento
   String sex          // M (male), F (female), U (unknown)
   
   // Emula el PARTY_IDENTIFIED.identifiers (solo un id), que referencia con
   // su PARTY_IDENTIFIED.external_ref<PARTY_REF> que referencia a esta PARTY.
   String idCode       // Numero o codigo de documento local, regional o nacional
   String idType       // Tipo de documento
   
   // Emula ACTOR.roles<ROLE> (uno solo)
   String role         // Paciente o Medico
   
   boolean deleted = false // logical delete
   
   String organizationUid
   
   static constraints = {
      sex(inList:['M','F','U'])
      idCode(nullable:true)
      idType(nullable:true)
      role(inList:['pat','doc'])
   }
   
   String toString()
   {
      return lastName +', '+ firstName
   }
}
