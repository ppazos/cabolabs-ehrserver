package com.cabolabs.ehrserver.openehr.demographic

//import javax.xml.bind.annotation.XmlAccessorType
//import javax.xml.bind.annotation.XmlAccessType
import com.cabolabs.swagger.annotations.ApiDescription
import com.cabolabs.swagger.annotations.ApiProperty
/**
 * Emula la clase demographic.PERSON.
 *
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
@ApiDescription(nameElementDefinitions="Person",typeElementDefinitions="object",description = "Emula la clase demographic.PERSON..")
class Person {

   // Emula LOCATABLE.uid (porque PARTY es LOCATABLE y PERSON hereda de PERTY)
                       // uid referenciado desde PatienProxy.value
   String uid = java.util.UUID.randomUUID() as String
   
   // Emula PARTY.identities<PARTY_IDENTITY>
   @ApiProperty(description = "primer nombre.",type="string")
   String firstName    // primer nombre
   @ApiProperty(description = "segundo apellido.",type="string")
   String lastName     // segundo apellido
   @ApiProperty(description = "fecha de nacimiento",type="string",format="date")
   Date dob            // fecha de nacimiento
   @ApiProperty(description = "M male, F female, U unknown.",type="string")
   String sex          // M (male), F (female), U (unknown)
   // Emula el PARTY_IDENTIFIED.identifiers (solo un id), que referencia con
   // su PARTY_IDENTIFIED.external_ref<PARTY_REF> que referencia a esta PARTY.
   @ApiProperty(description = "Numero o codigo de documento local, regional o nacional.",type="string")
   String idCode       // Numero o codigo de documento local, regional o nacional
   @ApiProperty(description = "Tipo de documento",type="string")
   String idType       // Tipo de documento
   
   // Emula ACTOR.roles<ROLE> (uno solo)
    @ApiProperty(description = "Paciente o Medico",type="string")
   String role         // Paciente o Medico
    @ApiProperty(description = "logical delete",type="boolean")
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
