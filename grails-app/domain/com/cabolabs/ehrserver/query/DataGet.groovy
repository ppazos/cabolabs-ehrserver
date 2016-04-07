package com.cabolabs.ehrserver.query

/**
 * SELECT archId/path
 * 
 * Para consultas de datos (queryData)
 * 
 * @author pab
 *
 */
class DataGet {
   
   String archetypeId
   String path
   
   // TODO: poner name para mostrar en la definicion
   //       de la consulta, se saca de ArchetypeIndexItem o del
   //       arquetipo archetypeId para la path (que
   //       tiene el nodeId)
   
   static belongsTo = [Query]
}