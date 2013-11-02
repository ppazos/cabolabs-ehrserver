package ehr.clinical_documents

/**
 * Definicion de indice para realizar busquedas a nivel de datos (nivel 2).
 * El indice se deriva de la definicion de la estructura de cada arquetipo.
 * A partir de estas definiciones se cren los indices sobre instancias de
 * datos de tipos concretos (DvDate, DvText, DvQuantity, etc).
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 */
class DataIndex {

   String archetypeId
   
   String path
   
   String rmTypeName
   
   // nombre del nodo segun el idioma por defecto del arquetipo
   // si es un atributo no arquetipable como uid, es un texto vacio
   // sino tiene definicion dentro del arquetipo, tambien es vacio
   String name
   
   static constraints = {
      name(nullable:true)
   }
}