package query

/**
 * Parametros n1 de la query:
 *  - ehrId    (== compoIndex.ehrId)
 *  - fromDate (<= compoIndex.startDate)
 *  - toDate   (>= compoIndex.startDate)
 *  - archetypeId (es parametro si no se especifica qarchetypeId en Query)
 * 
 * Parametros n2 de la query:
 *  - valores para cada DataCriteria (el tipo del valor depende del tipo del RM en DataCriteria.path)
 * 
 * @author pab
 * 
 * TODO: crear un servicio que devuelva la definicion de una consulta
 *       con nombres, tipos y obligatoriedad de parametros.
 *
 */
class Query {

   // Describe lo que hace la query
   String name
   
   // queryByData o queryData
   // lo que los diferencia es el resultado: composiciones o datos asociados a paths
   String type
   
   // Sino se especifica, por defecto es xml
   String format = 'xml'
   
   // Si es null, se puede especificar como parametro de la query
   // a modo de "tipo de documento", sino se especifica en ningun
   // caso, pide "cualquier tipo de documento". 
   String qarchetypeId
   
   // Si la consulta es de datos, se filtra por indices de nivel 1 y se usa DataGet para especificar que datos se quieren en el resultado.
   // Si la consulta es de compositions, se filtra por indices de nivel 1 y tambien por nivel 2 (para n2 se usa DataCriteria)
   // Los filtros/criterios de n1 y de n2 son parametros de la query.
   List select
   List where
   static hasMany = [select: DataGet, where: DataCriteria]
   
   // null, composition o path
   // Sirve para agrupar datos:
   //  composition: sirve para mostrar tablas, donde cada fila es una composition
   //  path: sirve para armar series de valores para graficar
   String group
   
   
   static constraints = {
      
      // para guardar la query debe tener nombre
      name(nullable:false, blank:false)
      
      // No creo que le guste null en inList, le pongo ''
      group(nullable:true, inList:['', 'composition', 'path'])
      qarchetypeId(nullable: true)
      format(inList:['xml','json'])
      type(inList:['composition','datavalue'])
   }
   
   static mapping = {
      group column: 'dg_group' // group es palabra reservada de algun dbms
   }
   
   /**
    * Genera la consulta en SQL.
    */
   public String toString()
   {
      
   }
}