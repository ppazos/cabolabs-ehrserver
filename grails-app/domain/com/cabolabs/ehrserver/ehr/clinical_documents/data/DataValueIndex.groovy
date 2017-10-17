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

package com.cabolabs.ehrserver.ehr.clinical_documents.data

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

class DataValueIndex {

   // index
   String templateId
   String archetypeId
   String path
   String archetypePath
   String rmTypeName
   
   CompositionIndex owner
   
   // TODO: ver si puedo sacarle las xpaths al xml
   //       y guardarlas, para un path de arquetipo
   //       pueden haber varias xpaths, esto permite
   //       devolver a los sistemas clientes las xpaths
   //       donde encuentran los datos que buscan en los
   //       xmls que resulten de las queries.
   
   static mapping = {
      tablePerHierarchy false // tabla por subclase
      archetypeId index: 'arch_id_path_idx,arch_id_idx'
      // Grails is not creating an index on this because it is > 255 the max index size in MySQL, in Postgres it is 2713
      archetypePath index: 'arch_id_path_idx,arch_path_idx'
   }
   
   static constraints = {
      path(maxSize: 4096)
      archetypePath(maxSize: 2048)
   }
   
   
   public String toString()
   {
      return this.archetypeId + this.archetypePath
   }
}
