package query

import ehr.clinical_documents.*
import ehr.clinical_documents.data.*
import grails.util.Holders

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

   String uid = java.util.UUID.randomUUID() as String

   // Describe lo que hace la query
   String name
   
   // queryByData (composition) o queryData (datavalue)
   // lo que los diferencia es el resultado: composiciones o datos asociados a paths
   String type
   
   // Sino se especifica, por defecto es xml
   String format = 'xml'
   
   // Si es null, se puede especificar como parametro de la query
   // a modo de "tipo de documento", sino se especifica en ningun
   // caso, pide "cualquier tipo de documento".
   //String qtemplateId
   
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
      //qarchetypeId(nullable: true)
      format(inList:['xml','json'])
      type(inList:['composition','datavalue'])
   }
   
   static mapping = {
      group column: 'dg_group' // group es palabra reservada de algun dbms
      select cascade: "all-delete-orphan" // cascade delete
      where cascade: "all-delete-orphan" // cascade delete
   }
   
   def execute(String ehrId, Date from, Date to)
   {
      if (this.type == 'datavalue') return executeDatavalue(ehrId, from, to)
      return executeComposition(ehrId, from, to)
   }
   
   def executeDatavalue(String ehrId, Date from, Date to)
   {
      // Query data
      def res = DataValueIndex.withCriteria {
         
         // SELECT
         or { // matchea algun par archId+path
            this.select.each { dataGet ->
               
               and {
                  eq('archetypeId', dataGet.archetypeId)
                  eq('archetypePath', dataGet.path)
               }
            }
         }
         
         // WHERE level 1 filters
         owner { // CompositionIndex
            eq('ehrId', ehrId) // Ya se verifico que viene el param y que el ehr existe
            
            /*
            if (qarchetypeId)
            {
               eq('archetypeId', qarchetypeId) // Arquetipo de composition
            }
            */
            
            if (from) ge('startTime', from) // greater or equal
            if (to) le('startTime', to) // lower or equal
            
            eq('lastVersion', true) // query only latest versions
         }
      }
      
      //println ". "
      //println res
      //println "group $group"
      
      // Group
      if (this.group == 'composition')
      {
         res = queryDataGroupComposition(res)
      }
      else if (this.group == 'path')
      {
         res = queryDataGroupPath(res)
      }
      
      return res
   }
   
   /**
    * Usada por queryData para agrupar por composition
    */
   private queryDataGroupComposition(res)
   {
      def resHeaders = [:]
      def dataidx
      
      // =========================================================================
      // TODO: obtener el nombre del arquetipo en cada path para usar de header
      // =========================================================================
      
      // Headers para la tabla: 1 col por path, y dentro de cada path 1 col por atributo del DataValue
      // h1: | path1 (DvQuantity) | path2 (DvCodedText) | ... |
      // h2: | magnitude | units  |   code   |  value   | ... |
      //
      // [
      //  path1: [ type:'DV_QUANTITY', attrs:['magnitude','units'] ],
      //  path2: [ type:'DV_CODED_TEXT', attrs:['code','value'],
      //  ...
      // ]
      
      // Usa ruta absoluta para agrupar.
      String absPath
      
      this.select.each { dataGet ->
         
         // Usa ruta absoluta para agrupar.
         absPath = dataGet.archetypeId + dataGet.path
         
         // Lookup del tipo de objeto en la path para saber los nombres de los atributos
         // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
         dataidx = IndexDefinition.findByArchetypeIdAndArchetypePath(dataGet.archetypeId, dataGet.path)
         
         // FIXME: usar archId + path como key
         resHeaders[absPath] = [:]
         resHeaders[absPath]['type'] = dataidx.rmTypeName
         resHeaders[absPath]['name'] = dataidx.name
         
         switch (dataidx.rmTypeName)
         {
            case ['DV_QUANTITY', 'DvQuantity']:
               resHeaders[absPath]['attrs'] = ['magnitude', 'units']
            break
            case ['DV_CODED_TEXT', 'DvCodedText']:
               resHeaders[absPath]['attrs'] = ['value', 'code']
            break
            case ['DV_TEXT', 'DvText']:
               resHeaders[absPath]['attrs'] = ['value']
            break
            case ['DV_DATE_TIME', 'DvDateTime']:
               resHeaders[absPath]['attrs'] = ['value']
            break
            case ['DV_BOOLEAN', 'DvBoolean']:
               resHeaders[absPath]['attrs'] = ['value']
            break
            case ['DV_COUNT', 'DvCount']:
               resHeaders[absPath]['attrs'] = ['magnitude']
            break
            case ['DV_PROPORTION', 'DvProportion']:
               resHeaders[absPath]['attrs'] = ['numerator', 'denominator', 'type', 'precision']
            break
            default:
               throw new Exception("type "+dataidx.rmTypeName+" not supported")
         }
      }
      
      
      // Filas de la tabla
      def resGrouped = [:]
      
      
      // DEBUG
      //println res as grails.converters.JSON
      

      // dvis por composition (Map[compo.id] = [dvi, dvi, ...])
      // http://groovy.codehaus.org/groovy-jdk/java/util/Collection.html#groupBy(groovy.lang.Closure)
      def rows = res.groupBy { it.owner.id } // as grails.converters.JSON
      
      //println rows
      
      def dvi
      def col // lista de valores de una columna
      rows.each { compoId, dvis ->
         
         //println compoId + ": " + dvis
         
         resGrouped[compoId] = [:]
         
         // Datos de la composition
         // FIXME: deberia haber por lo menos un dvi, sino esto da error
         resGrouped[compoId]['date'] = dvis[0].owner.startTime
         resGrouped[compoId]['uid']  = dvis[0].owner.uid
         resGrouped[compoId]['cols'] = []
         
         // Las columnas no incluyen la path porque se corresponden en el indice con la path en resHeaders
         // Cada columna de la fila
         resHeaders.each { _absPath, colData -> // colData = [type:'XX', attrs:['cc','vv']]
            
            //println "header: " + path + " " + colData
            //resGrouped[compoId]['cols']['type'] = idxtype
            
            col = [type: colData['type'], path: _absPath] // pongo la path para debug
            
            // dvi para la columna actual
            dvi = dvis.find{ (it.archetypeId + it.archetypePath) == _absPath && it.owner.id == compoId}
            
            if (dvi)
            {
               // Datos de cada path seleccionada dentro de la composition
               switch (colData['type'])
               {
                  case ['DV_QUANTITY', 'DvQuantity']:
                     col['magnitude'] = dvi.magnitude
                     col['units'] = dvi.units
                  break
                  case ['DV_CODED_TEXT', 'DvCodedText']:
                     col['value'] = dvi.value
                     col['code'] = dvi.code
                  break
                  case ['DV_TEXT', 'DvText']:
                     col['value'] = dvi.value
                  break
                  case ['DV_DATE_TIME', 'DvDateTime']:
                     col['value'] = dvi.value
                  break
                  case ['DV_BOOLEAN', 'DvBoolean']:
                     col['value'] = dvi.value
                  break
                  case ['DV_COUNT', 'DvCount']:
                     col['magnitude'] = dvi.magnitude
                  break
                  case ['DV_PROPORTION', 'DvProportion']:
                     col['numerator'] = dvi.numerator
                     col['denominator'] = dvi.denominator
                     col['type'] = dvi.type
                     col['precision'] = dvi.precision
                  break
                  default:
                     throw new Exception("type "+colData['type']+" not supported")
               }
               
               resGrouped[compoId]['cols'] << col
            }
         }
      }
      
      return [resHeaders, resGrouped]
      
   } // queryDataGroupComposition
   
   
   /**
    * Usada por queryData para agrupar por path
    */
   private queryDataGroupPath(res)
   {
      // En este caso los headers son las filas
      //def resHeaders = [:]
      def dataidx
      
      // Columnas de la tabla (series)
      def resGrouped = [:]
      
      
      // TODO: necesito la fecha de la composition para cada item de la serie,
      //       el mismo indice en distintas series corresponde la misma fecha
      //       la fecha identifica la fila, y cada serie es una columna.
      

      // Estructura auxiliar para recorrer y armar la agrupacion en series.
      def cols = res.groupBy { it.archetypeId + it.archetypePath }
      

      // Usa ruta absoluta para agrupar.
      String absPath
      
      this.select.each { dataGet ->
         
         // Usa ruta absoluta para agrupar.
         absPath = dataGet.archetypeId + dataGet.path
         

         // Lookup del tipo de objeto en la path para saber los nombres de los atributos
         // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
         dataidx = IndexDefinition.findByArchetypeIdAndArchetypePath(dataGet.archetypeId, dataGet.path)
         

         resGrouped[absPath] = [:]
         resGrouped[absPath]['type'] = dataidx.rmTypeName // type va en cada columna
         resGrouped[absPath]['name'] = dataidx.name // name va en cada columna, nombre asociado a la path por la que se agrupa
         
         // FIXME: hay tipos de datos que no deben graficarse
         // TODO: entregar solo valores segun el tipo de dato, en lugar de devolver DataValueIndexes
         //resGrouped[paths[i]]['serie'] = cols[paths[i]]
         
         resGrouped[absPath]['serie'] = []
         
         cols[absPath].each { dvi ->
            
            println "dvi: "+ dvi + " rmTypeName: "+ dataidx.rmTypeName
            
            // Datos de cada path seleccionada dentro de la composition
            switch (dataidx.rmTypeName)
            {
               case ['DV_QUANTITY', 'DvQuantity']: // FIXME: this is a bug on adl parser it uses Java types instead of RM ones
                  resGrouped[absPath]['serie'] << [magnitude: dvi.magnitude,
                                                   units:     dvi.units,
                                                   date:      dvi.owner.startTime]
               break
               case ['DV_CODED_TEXT', 'DvCodedText']:
                  resGrouped[absPath]['serie'] << [code:      dvi.code,
                                                   value:     dvi.value,
                                                   date:      dvi.owner.startTime]
               break
               case ['DV_TEXT', 'DvText']:
                  resGrouped[absPath]['serie'] << [value:     dvi.value,
                                                   date:      dvi.owner.startTime]
               break
               case ['DV_DATE_TIME', 'DvDateTime']:
                  resGrouped[absPath]['serie'] << [value:     dvi.value,
                                                   date:      dvi.owner.startTime]
               break
               case ['DV_BOOLEAN', 'DvBoolean']:
                  resGrouped[absPath]['serie'] << [value:     dvi.value,
                                                   date:      dvi.owner.startTime]
               break
               case ['DV_COUNT', 'DvCount']:
                  resGrouped[absPath]['serie'] << [magnitude: dvi.magnitude,
                                                   date:      dvi.owner.startTime]
               break
               case ['DV_PROPORTION', 'DvProportion']:
                  resGrouped[absPath]['serie'] << [numerator:   dvi.numerator,
                                                   denominator: dvi.denominator,
                                                   type:        dvi.type,
                                                   precision:   dvi.precision,
                                                   date:        dvi.owner.startTime]
               break
               default:
                  throw new Exception("type "+dataidx.rmTypeName+" not supported")
            }
            
            // para cada fila quiero fecha y uid de la composition
         }
      }
      
      return resGrouped
      
   } // queryDataGroupPath
   
   
   def executeComposition(String ehrId, Date from, Date to)
   {
      def formatterDateDB = new java.text.SimpleDateFormat( Holders.config.app.l10n.db_date_format )
      
      // Armado de la query
      String q = "FROM CompositionIndex ci WHERE ci.lastVersion=true AND " // Query only latest versions
      
      // ===============================================================
      // Criteria nivel 1 ehrId
      if (ehrId) q += "ci.ehrId = '" + ehrId + "' AND "
       
      // Criteria nivel 1 archetypeId (solo de composition)
      //if (qarchetypeId) q += "ci.archetypeId = '" + qarchetypeId +"' AND "
       
      // Criterio de rango de fechas para ci.startTime
      // Formatea las fechas al formato de la DB
      if (from) q += "ci.startTime >= '"+ formatterDateDB.format( from ) +"' AND " // higher or equal
      if (to) q += "ci.startTime <= '"+ formatterDateDB.format( to ) +"' AND " // lower or equal
       
      //
      // ===============================================================
       
      /**
        * FIXME: issue #6
        * si en el create se verifican las condiciones para que a aqui no
        * llegue una path a un tipo que no corresponde, el error de tipo
        * no sucederia nunca, asi no hay que tirar except aca.
        */
      def dataidx
      def idxtype
       
      this.where.eachWithIndex { dataCriteria, i ->
          
         // Aux to build the query FROM
         def fromMap = ['DataValueIndex': 'dvi']
          
         // Lookup del tipo de objeto en la path para saber los nombres de los atributos
         // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
          
         println "archId "+ dataCriteria.archetypeId
         println "path "+ dataCriteria.path
          
         dataidx = IndexDefinition.findByArchetypeIdAndArchetypePath(dataCriteria.archetypeId, dataCriteria.path)
         idxtype = dataidx?.rmTypeName
          
         // ================================================================
         // TODO:
         // Since GRAILS 2.4 it seems that exists can be done in Criteria,
         // should use that instead of HQL.
         // https://jira.grails.org/browse/GRAILS-9223
         // ================================================================
          
          
         // Subqueries sobre los DataValueIndex de los CompositionIndex
         q += " EXISTS ("
          
          // dvi.owner.id = ci.id
          // Asegura de que todos los EXISTs se cumplen para el mismo CompositionIndex
          // (los criterios se consideran AND, sin esta condicion es un OR y alcanza que
          // se cumpla uno de los criterios que vienen en params)
         def subq = "SELECT dvi.id FROM "
          
         //"  FROM DataValueIndex dvi" + // FROM is set below
         def where = $/
             WHERE dvi.owner.id = ci.id AND
                   dvi.archetypeId = '${dataCriteria.archetypeId}' AND
                   dvi.archetypePath = '${dataCriteria.path}'
         /$
          
         // Consulta sobre atributos del IndexDefinition dependiendo de su tipo
         switch (idxtype)
         {
             // ADL Parser bug: uses Java class names instead of RM Type Names...
            case ['DV_DATE_TIME', 'DvDateTime']:
                fromMap['DvDateTimeIndex'] = 'ddti'
                where += " AND ddti.id = dvi.id "
                where += " AND ddti.value "+ dataCriteria.sqlOperand() +" "+  dataCriteria.value // TODO: verificar formato, transformar a SQL
            break
            case ['DV_QUANTITY', 'DvQuantity']:
                fromMap['DvQuantityIndex'] = 'dqi'
                where += " AND dqi.id = dvi.id "
                where += " AND dqi.magnitude "+ dataCriteria.sqlOperand() +" "+  new Float(dataCriteria.value)
            break
            case ['DV_CODED_TEXT', 'DvCodedText']:
                fromMap['DvCodedTextIndex'] = 'dcti'
                where += " AND dcti.id = dvi.id "
                where += " AND dcti.code "+ dataCriteria.sqlOperand() +" '"+ dataCriteria.value+"'"
            break
            case ['DV_TEXT', 'DvText']:
                fromMap['DvTextIndex'] = 'dti'
                where += " AND dti.id = dvi.id "
                where += " AND dti.value "+ dataCriteria.sqlOperand() +" '"+ dataCriteria.value+"'"
            break
            case ['DV_BOOLEAN', 'DvBoolean']:
                fromMap['DvBooleanIndex'] = 'dbi'
                where += " AND dbi.id = dvi.id "
                where += " AND dbi.value "+ dataCriteria.sqlOperand() +" "+  new Boolean(dataCriteria.value)
            break
            case ['DV_COUNT', 'DvCount']:
                fromMap['DvCountIndex'] = 'dci'
                where += " AND dci.id = dvi.id "
                where += " AND dci.magnitude "+ dataCriteria.sqlOperand() +" "+  new Long(dataCriteria.value)
            break
            case ['DV_PROPORTION', 'DvProportion']:
                fromMap['DvProportionIndex'] = 'dpi'
                where += " AND dpi.id = dvi.id "
                where += " AND dpi.numerator "+ dataCriteria.sqlOperand() +" "+ new Double(dataCriteria.numerator)
                /*
                 * FIXME: data criteria sobre proportion deberia decir si es sobre numerator o denominator.
                 *        https://github.com/ppazos/cabolabs-ehrserver/issues/53
                resGrouped[absPath]['serie'] << [numerator:   dvi.,
                                                 denominator: dvi.denominator,
                                                 type:        dvi.type,
                                                 precision:   dvi.precision,
                                                 date:        dvi.owner.startTime]
                */
            break
            default:
               throw new Exception("type $idxtype not supported")
         }
         
         fromMap.each { index, alias ->
             
            subq += index +' '+ alias +' , '
         }
         subq = subq.substring(0, subq.size()-2)
         subq += where
         
         q += subq
         q += ")"
         
         
         // TEST
         // TEST
         //println "SUBQ DVI: "+ subq.replace("dvi.owner.id = ci.id AND ", "")
         //println DataValueIndex.executeQuery(subq.replace("dvi.owner.id = ci.id AND ", ""))
         

         
         //       EXISTS (
         //         SELECT dvi.id
         //         FROM IndexDefinition dvi
         //         WHERE dvi.owner.id = ci.id
         //               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
         //               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value
         //               AND dvi.magnitude>140.0
         //       ) AND EXISTS (
         //         SELECT dvi.id
         //         FROM IndexDefinition dvi
         //         WHERE dvi.owner.id = ci.id
         //               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
         //               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value
         //               AND dvi.magnitude<130.0
         //       ) AND EXISTS (
         //         SELECT dvi.id
         //         FROM IndexDefinition dvi
         //         WHERE dvi.owner.id = ci.id
         //               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
         //               AND dvi.path = /content/data[at0001]/origin
         //               AND dvi.value>20080101
         //       )
         
         
         // Agrega ANDs para los EXISTs, menos el ultimo
         if (i+1 < this.where.size()) q += " AND "
      }
      
      println "hql query: " + q
      
      def cilist = CompositionIndex.executeQuery( q )
      
      println "cilist: "+ cilist
      
      return cilist
   }
}