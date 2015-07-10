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
   //String operand
   
   // value va a depender del tipo del RM en la path
   // value es parametro de la query sino se setea aqui
   // se setea aqui cuando se hace la consulta como una regla (ej. para ver valores fuera de rango)
   //Map values // valores del criterio que se interpretan segun el tipo de dato
   /**
    * DV_PROPORTION:
    *   values = [value1, value2] se puede usar para operador between sobre magnitude
    * 
    * DV_CODED_TEXT:
    *   values = [code::terminology, code::terminology, ...] se puede usar para saber si un dato esta dentro de una lista.
    */
   
   boolean negation = false // Negation = true agrega un NOT al inicio de la condicion.
   
   String rmTypeName
   //String rmAttrName // atributo paticular del datatype al que se le aplica la condicion ej.DV_PROPORTION.enumerator
   
   // TODO: poner name para mostrar en la definicion
   //       de la consulta, se saca de IndexDefinition o del
   //       arquetipo archetypeId para la path (que
   //       tiene el nodeId)
   
   static constraints = {
      //operand(inList:['eq','neq','lt','gt','le','ge','in_list','contains','between'])
      //value(nullable:true)
      rmTypeName(inList:['DV_DATE_TIME', 'DV_QUANTITY', 'DV_CODED_TEXT', 'DV_TEXT', 'DV_ORDINAL', 'DV_BOOLEAN', 'DV_COUNT', 'DV_PROPORTION', 'DV_DURATION'])
   }
   
   static Map operandMap = [
     'eq': '=',
     'lt': '<',
     'gt': '>',
     'neq': '<>', // http://stackoverflow.com/questions/723195/should-i-use-or-for-not-equal-in-tsql
     'le': '<=',
     'ge': '>=',
     'in_list': 'IN',
     'contains': 'ILIKE',
     'between': 'BETWEEN'
   ]
   
   def sqlOperand()
   {
      return operandMap[this.operand]
   }
   
   static belongsTo = [Query]
   
   
   String toString()
   {
      return "archetypeId: "+ this.archetypeId +", path: "+ this.path +", rmTypeName: "+ this.rmTypeName +", class: "+ this.getClass().getSimpleName()
   }
}
