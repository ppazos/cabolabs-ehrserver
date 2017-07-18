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

package com.cabolabs.ehrserver.openehr.common.generic

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

/**
 * Emula a common.generic.PARTY_IDENTIFIED que hereda de PARTY_PROXY.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 */
class DoctorProxy {

   // Emula PARTY_PROXY.external_ref.namespace
   // La referencia (id) es valida localmente
   String namespace = "local"
   
   // Emula PARTY_PROXY.external_ref.type
   // References an external PERSON record
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
   static belongsTo = [AuditDetails, CompositionIndex]
}
