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


import com.cabolabs.ehrserver.openehr.ehr.Ehr

/**
 * Emula a common.generic.PARTY_SELF que hereda de PARTY_PROXY.
 *
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
class PartySelf {

   // Fields from PARTY_SELF.external_ref: PARTY_REF
   String namespace = "local"
   String type = "PERSON"

   // PARTY_SELF.external_ref.id.value
   String value

   // PARTY_SELF.external_ref.id.scheme, when id is GENERIC_ID
   String scheme

   String idType

   // all nullable because the external_ref is optional in PartySelf
   static constraints = {
      namespace nullable: true
      type nullable: true
      value nullable: true
      scheme nullable: true
      idType nullable: true, inList: ['HIER_OBJECT_ID', 'GENERIC_ID']
   }

   // Para que salve en cascada al crear el Ehr
   // http://grails.org/doc/latest/guide/GORM.html#manyToOneAndOneToOne
   static belongsTo = [Ehr]
}
