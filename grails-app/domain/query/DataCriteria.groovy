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
   
   int spec // index of the criteria spec selected
   
   String alias // for the query
   
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
   
   def sqlOperand(String operandString)
   {
      return operandMap[operandString]
   }
   
   static belongsTo = [Query]
   
   
   String toString()
   {
      return "(archetypeId: "+ this.archetypeId +", path: "+ this.path +", rmTypeName: "+ this.rmTypeName +", class: "+ this.getClass().getSimpleName() +")"
   }
   
   String toSQL()
   {
      def specs = criteriaSpec()
      def spec = specs[this.spec] // spec used Map
      def attributes = spec.keySet()
      def sql = ""
      def operand
      def operandField
      def valueField
      def criteriaValueType // value, list, range ...
      def value
      
      attributes.each { attr ->
         operandField = attr+'Operand'
         operand = this."$operandField"
         valueField = attr+'Value'
         value = this."$valueField"
         
         criteriaValueType = spec[attr][operand]
         
         println this.getClass().getSimpleName()
         
         
         // TODO: if value is string, add quotes, if boolean change it to the DB boolean value
         if (criteriaValueType == 'value')
         {
            if (value instanceof List) // it can be a list but have just one value e.g. because it can also have a range
               sql += this.alias +'.'+ attr +' '+ sqlOperand(operand) +' '+ value[0].asSQLValue()
            else
               sql += this.alias +'.'+ attr +' '+ sqlOperand(operand) +' '+ value.asSQLValue()
         }
         else if (criteriaValueType == 'list')
         {
            assert operand == 'in_list'
            
            sql += this.alias +'.'+ attr +' IN ('
            
            value.each { singleValue ->
               sql += singleValue.asSQLValue() +','
            }
            sql = sql[0..-2] + ')' // removes last ,
         }
         else if (criteriaValueType == 'range')
         {
            assert operand == 'between'
            
            sql += this.alias +'.'+ attr +' BETWEEN '+ value[0].asSQLValue() +' AND '+ value[1].asSQLValue()
         }
         
         sql += ' AND '
      }
      
      sql = sql[0..-6] // removes the last AND
      
      return sql
   }
}
