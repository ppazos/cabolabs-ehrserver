package ehr

import common.generic.PatientProxy
import common.change_control.VersionedComposition
import common.change_control.Contribution
import javax.xml.bind.annotation.*

/**
 * Representa el EHR de un paciente.
 *
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
class Ehr {

   // The id of the EHR system on which this EHR was created
   String systemId = "ISIS_EHR_SERVER"
   
   // Emula un HIER_OBJECT_ID.root y su valor va a ser un UUID (java.util.UUID.randomUUID() as String)
   // que se asigna en el momento que se crea el EHR
   String ehrId = java.util.UUID.randomUUID() as String
   
   // Emula timeCreated, se setea automaticamente por Grails en el momento de crear el EHR
   Date dateCreated = new Date()
   
   // Emula EHR.ehr_status...<OBJECT_REF>...subject
   PatientProxy subject
   
   
   List compositions = []
   List contributions = []
   static hasMany = [compositions:VersionedComposition, contributions:Contribution]
   
   
   static constraints = {
   }
   
   static mapping = {
      //subject cascade: 'save-update' // va con belongsTo en PatientProxy
      compositions cascade: 'save-update'
   }
   
   // For testing purposes
   def containsVersionedComposition(String uid)
   {
      def c = this.createCriteria()
      def res = c.list { // FIXME: use count
         compositions {
            eq('uid', uid)
         }
      }
      return res.size() == 1
   }
   
   String toString()
   {
      return "EHR of "+ this.subject.getPerson().toString()
   }
}