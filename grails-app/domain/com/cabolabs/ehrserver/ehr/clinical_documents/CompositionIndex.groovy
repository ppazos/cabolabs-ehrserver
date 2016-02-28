package com.cabolabs.ehrserver.ehr.clinical_documents

import com.cabolabs.ehrserver.openehr.common.change_control.Version

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
   
   String subjectId // uid de la persona con rol paciente
   
   String ehrUid // uid del ehr del subjectId
   
   String templateId // se usa como "tipo de documento", es un arquetipo de COMPOSITION
   String archetypeId // archetype that defines the "definition" part of the template
   
   boolean dataIndexed = false // true cuando se crean los indices de DataValue para la composition
   boolean lastVersion = true // copy of the latestVersion attribute of the parent Version to avoid the use of old data in queries
   
   // multitenancy, copy of ehr.organizationUid
   String organizationUid
   
   // TODO: composerName para busquedas like %
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
