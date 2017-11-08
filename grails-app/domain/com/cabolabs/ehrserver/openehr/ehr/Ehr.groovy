/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.openehr.ehr

import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.directory.Folder
import javax.xml.bind.annotation.*

/**
 * Representa el EHR de un paciente.
 *
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 *
 */
class Ehr {

   // The id of the EHR system on which this EHR was created
   String systemId = "CABOLABS_EHR_SERVER"
   
   // Emula un HIER_OBJECT_ID.root y su valor va a ser un UUID (java.util.UUID.randomUUID() as String)
   // que se asigna en el momento que se crea el EHR
   String uid = java.util.UUID.randomUUID() as String
   
   // Emula timeCreated, se setea automaticamente por Grails en el momento de crear el EHR
   Date dateCreated = new Date()
   
   // Emula EHR.ehr_status...<OBJECT_REF>...subject
   PatientProxy subject
   
   // Root of the directory tree
   Folder directory

   // multitenancy
   String organizationUid
   
   boolean deleted = false // logical delete
   
   
   //List contributions = []
   //static hasMany = [contributions:Contribution]
   
   static transients = ['compositions', 'contributions']
   
   static constraints = {
      directory(nullable: true) // directory is optional
   }
   
   static mapping = {
      //contributions cascade: 'all' //'save-update'
      organizationUid index: 'org_uid_idx'
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
   
   def getContributions()
   {
      Contributions.findAllByEhr(this)
   }
   
   String toString()
   {
      return "EHR ("+ this.uid +") of subject ("+ this.subject.value +")"
   }
}
