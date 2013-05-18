package query

/**
 * WHERE archId/path operand value 
 * 
 * Para consultas de compositions (queryByData)
 * 
 * @author pab
 *
 */
class DataCriteria {

   String archetypeId
   String path
   String operand
   
   // value va a depender del tipo del RM en la path
   // value es parametro de la query sino se setea aqui
   // se setea aqui cuando se hace la consulta como una regla (ej. para ver valores fuera de rango)
   String value
   
   // TODO: poner name para mostrar en la definicion
   //       de la consulta, se saca de DataIndex o del
   //       arquetipo archetypeId para la path (que
   //       tiene el nodeId)
   
   static constraints = {
      operand(inList:['eq','neq','lt','gt','le','ge'])
      value(nullable:true)
   }
   
   static belongsTo = [Query]
}