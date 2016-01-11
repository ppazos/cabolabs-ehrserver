package com.cabolabs.ehrserver.openehr.ehr

import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.directory.Folder
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
   String uid = java.util.UUID.randomUUID() as String
   
   // Emula timeCreated, se setea automaticamente por Grails en el momento de crear el EHR
   Date dateCreated = new Date()
   
   // Emula EHR.ehr_status...<OBJECT_REF>...subject
   PatientProxy subject
   
   // Root of the directory tree
   Folder directory
   // Causes errors when creating Folders with ehr null
   //static hasOne = [directory: Folder] // needed to set 1 to 1 rel with Folder, Folder has: Ehr ehr. See http://grails.github.io/grails-doc/2.2.1/ref/Domain%20Classes/hasOne.html
   
   // multitenancy
   String organizationUid
   
   
   List compositions = []
   List contributions = []
   static hasMany = [compositions:VersionedComposition, contributions:Contribution]
   
   
   static constraints = {
      directory(nullable: true) // directory is optional
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