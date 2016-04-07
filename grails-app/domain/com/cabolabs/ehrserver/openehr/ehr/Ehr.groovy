package com.cabolabs.ehrserver.openehr.ehr

import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.directory.Folder
import javax.xml.bind.annotation.*

import com.cabolabs.swagger.annotations.ApiDescription
import com.cabolabs.swagger.annotations.ApiProperty
/**
 * Representa el EHR de un paciente.
 *
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
@ApiDescription(nameElementDefinitions="Ehr",typeElementDefinitions="object",description = "Representa el EHR de un paciente.")
class Ehr {

   // The id of the EHR system on which this EHR was created
   @ApiProperty(description = "The id of the EHR system on which this EHR was created",type="string")
   String systemId = "ISIS_EHR_SERVER"
   
   // Emula un HIER_OBJECT_ID.root y su valor va a ser un UUID (java.util.UUID.randomUUID() as String)
   // que se asigna en el momento que se crea el EHR
   @ApiProperty(description = "Emula un HIER_OBJECT_ID.root y su valor va a ser un UUID (java.util.UUID.randomUUID() as String) que se asigna en el momento que se crea el EHR",type="string")
   String uid = java.util.UUID.randomUUID() as String
   
   // Emula timeCreated, se setea automaticamente por Grails en el momento de crear el EHR
   @ApiProperty(description = "Emula timeCreated, se setea automaticamente por Grails en el momento de crear el EHR",type="string",format="date")
   Date dateCreated = new Date()
   
   // Emula EHR.ehr_status...<OBJECT_REF>...subject
   @ApiProperty(description = "Emula EHR.ehr_status...<OBJECT_REF>...subject",type="string")
   PatientProxy subject
   
   // Root of the directory tree
   Folder directory
   // Causes errors when creating Folders with ehr null
   //static hasOne = [directory: Folder] // needed to set 1 to 1 rel with Folder, Folder has: Ehr ehr. See http://grails.github.io/grails-doc/2.2.1/ref/Domain%20Classes/hasOne.html
   
   // multitenancy
   @ApiProperty(description = "organizationUid",type="string")
   String organizationUid
   
   
   List contributions = []
   static hasMany = [contributions:Contribution]
   
   static transients = ['compositions']
   
   static constraints = {
      directory(nullable: true) // directory is optional
   }
   
   static mapping = {
   }
   
   // For testing purposes
   def containsVersionedComposition(String uid)
   {
      /*
      def c = this.createCriteria()
      def res = c.list { // FIXME: use count
         compositions {
            eq('uid', uid)
         }
      }
      return res.size() == 1
      */
      VersionedComposition.countByUidAndEhr(uid, this) != 0
   }
   
   def getCompositions()
   {
      VersionedComposition.findAllByEhr(this)
   }
   
   String toString()
   {
      return "EHR of "+ this.subject.getPerson().toString()
   }
}
