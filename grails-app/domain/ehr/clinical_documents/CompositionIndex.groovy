package ehr.clinical_documents

<<<<<<< HEAD
import common.change_control.Version

=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
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
   
   String ehrId // uid del ehr del subjectId
   
   String archetypeId // se usa como "tipo de documento", es un arquetipo de COMPOSITION
   
<<<<<<< HEAD
   boolean dataIndexed = false // true cuando se crean los indices de DataValue para la composition
   
   // TODO: composerName para busquedas like %
   // TODO: name (de Locatable) para busqueda like %
   
   // T0004: ahora CIndex se usa en Version
   static belongsTo = [Version]
   
=======
   // TODO: composerName para busquedas like %
   // TODO: name (de Locatable) para busqueda like %
   
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
   static constraints = {
      category(inList:['event','persistent'])
      startTime(nullable:true) // persistent no tienen context.startTime
   }
}