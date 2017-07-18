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

package com.cabolabs.ehrserver.ehr.clinical_documents

import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy

/**
 * Modela una instancei a de indice a nivel de documento (nivel 1).
 * Permite realizar busquedas de documentos sobre una estructura plana (mas rapido que usar datos estructurados).
 * 
 * @author pab
 *
 */
class CompositionIndex {

   String uid // uid de la composition
   
   String category // event o persistent
   
   Date startTime // de la composition.context (solo para compositions event)
   
   String subjectId // references an EHR.subject.uid, simplifies querying
   
   String ehrUid // uid del ehr del subjectId
   
   String templateId // se usa como "tipo de documento", es un arquetipo de COMPOSITION
   String archetypeId // archetype that defines the "definition" part of the template
   
   boolean dataIndexed = false // true cuando se crean los indices de DataValue para la composition
   boolean lastVersion = true // copy of the latestVersion attribute of the parent Version to avoid the use of old data in queries
   
   // multitenancy, copy of ehr.organizationUid
   String organizationUid
   
   DoctorProxy composer
   
   // TODO: name (de Locatable) para busqueda like %
   
   def getParent()
   {
      return Version.findByData(this)
   }
   
   static belongsTo = [Version]
   
   static constraints = {
      category(inList:['event','persistent'])
      startTime(nullable:true) // persistent no tienen context.startTime
   }
   
   static transients = ['parent']
}
