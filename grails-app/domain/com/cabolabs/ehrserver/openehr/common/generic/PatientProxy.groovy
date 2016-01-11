package com.cabolabs.ehrserver.openehr.common.generic

import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.openehr.demographic.Person

/**
 * Emula a common.generic.PARTY_SELF que hereda de PARTY_PROXY.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
class PatientProxy {

   // Emula PARTY_PROXY.external_ref.namespace
   // La referencia (id) es valida localmente
   String namespace = "local"
   
   // Emula PARTY_PROXY.external_ref.type
   // Apunta a una persona
   String type = "PERSON"
   
   // Identificador confiable del paciente (no es su cedula, documento o pasaporte), es asignado por el sistema al crear el paciente
   // Emula PARTY_PROXY.external_ref.id<OBJECT_ID>.value
   // Que a su vez emula un HIER_OBJECT_ID.root y su valor va a ser un UUID (java.util.UUID.randomUUID() as String)
   String value
   
   static constraints = {
   }
   
   // Para que salve en cascada al crear el Ehr
   // http://grails.org/doc/latest/guide/GORM.html#manyToOneAndOneToOne
   static belongsTo = [Ehr]
   
   static transients = ['person']
   
   /**
    * Devuelve la Person correspondiente al value de este PatientProxy.
    * @return
    */
   def getPerson()
   {
      return Person.findByUid(this.value)
   }
}