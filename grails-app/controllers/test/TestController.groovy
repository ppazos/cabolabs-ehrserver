package test

//import com.thoughtworks.xstream.XStream
import ehr.Ehr
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.DataIndex
import common.change_control.Contribution
import common.change_control.Version
import common.generic.AuditDetails
import query.DataCriteria
import query.Query
//import support.identification.CompositionRef // T0004
import common.generic.DoctorProxy
import ehr.clinical_documents.data.DataValueIndex
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.commons.ApplicationHolder

class TestController {

   def xmlService
   
   // Para acceder a las opciones de localizacion
   def config = ApplicationHolder.application.config.app
   
   // Para hacer consultas en la base
   def formatterDateDB = new SimpleDateFormat( ApplicationHolder.application.config.app.l10n.db_date_format )
   
   
   
   /**
    * UI test
    */
   /*
   def rollbackContribution(String uid)
   {
      if (!uid)
      {
         throw new Exception("UID de la contribution es obligatorio")
      }
      
      // TODO
   }
   */
   
   
   /**
    * Utiliza CompositionIndex para buscar entre las compositions y devuelve el XML de las compositions que matchean.
    * 
    * @param ehrId
    * @param subjectId
    * @param fromDate yyyyMMdd
    * @param toDate yyyyMMdd
    * @param archetypeId
    * @return
    */
   def findCompositions(String ehrId, String subjectId, 
                        String fromDate, String toDate, 
                        String archetypeId, String category)
   {
      
      // 1. Todos los parametros son opcionales pero debe venir por lo menos 1
      // 2. La semantica de pasar 2 o mas parametros es el criterio de and
      // 3. Para implementar como un OR se usaria otro parametro booleano (TODO)
      //
      
      def dFromDate
      def dToDate
      
      // FIXME: cuando sea servicio no hay ui
      if (!ehrId && !subjectId && !fromDate && !toDate && !archetypeId && !category)
      {
         return // muestro ui para testear busqueda
         //throw new Exception("Debe enviar por lo menos un dato para el criterio de busqueda")
      }
      
      // FIXME: Si el formato esta mal va a tirar una except!
      if (fromDate)
      {
         dFromDate = Date.parse(config.l10n.date_format, fromDate)
      }
      
      if (toDate)
      {
         dToDate = Date.parse(config.l10n.date_format, toDate)
      }
      
      //println dFromDate
      //println dToDate
      
      def idxs = CompositionIndex.withCriteria {
         
         if (ehrId)
            eq('ehrId', ehrId)
         
         if (subjectId)
            eq('subjectId', subjectId)
         
         if (archetypeId)
            eq('archetypeId', archetypeId)
         
         if (category)
            eq('category', category)
            
         if (dFromDate)
            ge('startTime', dFromDate) // greater or equal
         
         if (dToDate)
            le('startTime', dToDate) // lower or equal
      }
      
      // TODO: ui o xml o json (solo index o contenido), ahora tira solo index y en XML
      render(text: idxs as grails.converters.XML, contentType:"text/xml", encoding:"UTF-8")
   }
   
   
   /**
    * Busqueda de datos simples dentro de compositions que cumplen cierto criterio.
    * Datos de nivel 2 por criterio nivel 1.
    * Se utiliza para mostrar datos tabulados y graficas.
    * 
    * @param archetypeId arquetipo donde esta la path al dato que se busca, uno o mas
    * @param path ruta dentro del arquetipo al dato que se busca, una o mas
    * @param qehrId id del ehr (obligatorio, los datos deben ser del mismo ehr/paciente)
    * @param qarchetypeId tipo de composition donde buscar (opcional)
    * @param format xml o json, xml por defecto
    * 
    * @return List<DataValueIndex>
    */
   def queryData(String qehrId, String qarchetypeId, String fromDate, String toDate, String format, String group)
   {
      // muestra gui
      if (!params.doit)
      {
         return
      }
      
      //println params
      
      // FIXME: si format es json, el error deberia devolverse como json!
      
      // Verifica parametros
      if (!params.qehrId)
      {
         render(status: 500, text:'<error>debe venir un qehrId</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // TODO: verificar que el ehrId existe
      
      
      if (!params.archetypeId)
      {
         render(status: 500, text:'<error>debe venir por lo menos un archetypeId</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      if (!params.path)
      {
         render(status: 500, text:'<error>debe venir por lo menos una path</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      
      // En una consulta EQL archetypeId+path seria el SELECT
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      
      
      if (archetypeIds.size() == 0)
      {
         render(status: 500, text:'<error>debe venir por lo menos un archetypeId</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      if (paths.size() == 0)
      {
         render(status: 500, text:'<error>debe venir por lo menos una path</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      
      // parse de dates
      Date qFromDate
      Date qToDate

      if (fromDate)
      {
         qFromDate = Date.parse(config.l10n.date_format, fromDate)
      }
      
      if (toDate)
      {
         qToDate = Date.parse(config.l10n.date_format, toDate)
      }
      
      
      def res = DataValueIndex.withCriteria {
         
         // SELECT
         or { // matchea algun par archId+path
            archetypeIds.eachWithIndex { archId, i ->
               
               and {
                  eq('archetypeId', archId)
                  eq('path', paths[i])
               }
            }
         }
         
         
         // WHERE
         owner { // CompositionIndex
            eq('ehrId', qehrId) // Ya se verifico que viene el param y que el ehr existe
            
            if (qarchetypeId)
            {
               eq('archetypeId', qarchetypeId) // Arquetipo de composition
            }
            
            if (qFromDate)
               ge('startTime', qFromDate) // greater or equal
            
            if (qToDate)
               le('startTime', qToDate) // lower or equal
         }
         
      }
      
      // TODO: sacar las agrupaciones a operaciones externas para no hacer if ...
      
      
      // 1. Agrupacion por owner (fila) una columna por cada path
      //    - Para display tabular
      //
      if (group == 'composition')
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
         
         archetypeIds.eachWithIndex { archId, i ->
            
            // Lookup del tipo de objeto en la path para saber los nombres de los atributos
            // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
            dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
            
            resHeaders[paths[i]] = [:]
            resHeaders[paths[i]]['type'] = dataidx.rmTypeName
            resHeaders[paths[i]]['name'] = dataidx.name
            
            switch (dataidx.rmTypeName)
            {
               case 'DV_QUANTITY':
                  resHeaders[paths[i]]['attrs'] = ['magnitude', 'units']
               break
               case 'DV_CODED_TEXT':
                  resHeaders[paths[i]]['attrs'] = ['value']
               break
               case 'DV_DATE_TIME':
                  resHeaders[paths[i]]['attrs'] = ['code', 'value']
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
            resHeaders.each { path, colData -> // colData = [type:'XX', attrs:['cc','vv']]
               
               //println "header: " + path + " " + colData
               //resGrouped[compoId]['cols']['type'] = idxtype
               
               col = [type: colData['type'], path: path] // pongo la path para debug
               
               // dvi para la columna actual
               dvi = dvis.find{it.path == path && it.owner.id == compoId}
               
               if (dvi)
               {
                  // Datos de cada path seleccionada dentro de la composition
                  switch (colData['type'])
                  {
                     case 'DV_QUANTITY':
                        col['magnitude'] = dvi.magnitude
                        col['units'] = dvi.units
                     break
                     case 'DV_CODED_TEXT':
                        col['value'] = dvi.value
                     break
                     case 'DV_DATE_TIME':
                        col['code'] = dvi.code
                        col['value'] = dvi.value
                     break
                     default:
                        throw new Exception("type "+colData['type']+" not supported")
                  }
                  
                  resGrouped[compoId]['cols'] << col
               }
            }
         }
         
         if (!format || format == 'xml')
         {
            render(text:([resHeaders, resGrouped] as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
         }
         else if (format == 'json')
         {
            render(text:([resHeaders, resGrouped] as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
         }
         else
         {
            render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
         }
         
         return
      }
      
      
      // 2. Agrupacion por path (serie)
      //    - Para display en grafica
      //
      if (group == 'path')
      {
         // En este caso los headers son las filas
         def resHeaders = [:]
         def dataidx
         
         // Columnas de la tabla (series)
         def resGrouped = [:]
         
         
         // TODO: necesito la fecha de la composition para cada item de la serie,
         //       el mismo indice en distintas series corresponde la misma fecha
         //       la fecha identifica la fila, y cada serie es una columna.
         
         // FIXME: deberia ser archId+path para que sea absoluta
         //        seria mas facil si archId y path fueran un solo campo
         def cols = res.groupBy { it.path }
         
         
         // TODO: cada serie debe tener el nombre de la path (lookup de DataIndex)
         
         archetypeIds.eachWithIndex { archId, i ->
            
            // Lookup del tipo de objeto en la path para saber los nombres de los atributos
            // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
            dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
            
            resGrouped[paths[i]] = [:]
            resGrouped[paths[i]]['type'] = dataidx.rmTypeName // type va en cada columna
            resGrouped[paths[i]]['name'] = dataidx.name // name va en cada columna
            
            // FIXME: hay tipos de datos que no deben graficarse
            // TODO: entregar solo valores segun el tipo de dato, en lugar de devolver DataValueIndexes
            //resGrouped[paths[i]]['serie'] = cols[paths[i]]
            
            resGrouped[paths[i]]['serie'] = []
            
            cols[paths[i]].each { dvi ->
               
               // Datos de cada path seleccionada dentro de la composition
               switch (dataidx.rmTypeName)
               {
                  case 'DV_QUANTITY':
                     resGrouped[paths[i]]['serie'] << [magnitude: dvi.magnitude,
                                                       units:     dvi.units,
                                                       date:      dvi.owner.startTime]
                  break
                  case 'DV_CODED_TEXT':
                     resGrouped[paths[i]]['serie'] << [value:     dvi.value,
                                                       date:      dvi.owner.startTime]
                  break
                  case 'DV_DATE_TIME':
                     resGrouped[paths[i]]['serie'] << [code:      dvi.code,
                                                       value:     dvi.value,
                                                       date:      dvi.owner.startTime]
                  break
                  default:
                     throw new Exception("type "+dataidx.rmTypeName+" not supported")
               }
               
               // para cada fila quiero fecha y uid de la composition
            }
         }
         
         if (!format || format == 'xml')
         {
            render(text:(resGrouped as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
         }
         else if (format == 'json')
         {
            render(text:(resGrouped as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
         }
         else
         {
            render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
         }
         return
      }
      
      
      // Por defecto no agrupa (group = null)
      if (!format || format == 'xml')
      {
         render(text:(res as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         render(text:(res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
      }
      
   } // querydata
   

   
   /**
    * Busqueda de compositions por datos simples (DataValueIndex)
    * 
    * TODO: pasarle criterio de contexto ehrId, composition archId
    * TODO: pasar lista de archId, path, operand, value
    * 
    * @param qehrId filtro de nivel 1
    * @param qarchetypeId filtro de nivel 1
    * @param retrieveData null o false no devuelve datos, true devuelve datos
    * @param showUI true si muestra listado de resultados en UI, false por defecto devuelve XMLs
    * 
    * @return
    */
   /*
    * Se implemento en RestController.queryCompositions
    *
   //def queryByData(String archetypeId, String path, String operand, String value, boolean retrieveData)
   def queryByData(String qehrId, String qarchetypeId, String fromDate, String toDate, boolean retrieveData, boolean showUI)
   {
      println params
      
      // muestra gui
      if (!params.doit)
      {
         return
      }
      
      
      // Viene una lista de cada parametro
      // String archetypeId, String path, String operand, String value
      // El mismo indice en cada lista corresponde con un atributo del mismo criterio de busqueda
      
      // ya viene el nombre correcto
//      String op
//      switch (operand)
//      {
//         case '=': op = 'eq'
//         break
//         case '<': op = 'lt'
//         break
//         case '>': op = 'gt'
//         break
//         case '!=': op = 'neq'
//         break
//      }
      
      
      // Datos de criterios
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      List operands = params.list('operand')
      List values = params.list('value')
      
      DataIndex dataidx
      String idxtype

      
      // parse de dates
      Date qFromDate
      Date qToDate

      if (fromDate)
      {
         qFromDate = Date.parse(config.l10n.date_format, fromDate)
      }
      
      if (toDate)
      {
         qToDate = Date.parse(config.l10n.date_format, toDate)
      }
      
      
      // Armado de la query
      String q = "FROM CompositionIndex ci WHERE "
      
      // ===============================================================
      // Criteria nivel 1 ehrId
      if (qehrId) q += "ci.ehrId = '" + qehrId + "' AND "
      
      // Criteria nivel 1 archetypeId (solo de composition)
      if (qarchetypeId) q += "ci.archetypeId = '" + qarchetypeId +"' AND "
      
      // Criterio de rango de fechas para ci.startTime
      // Formatea las fechas al formato de la DB
      if (qFromDate) q += "ci.startTime >= '"+ formatterDateDB.format( qFromDate ) +"' AND " // higher or equal
      if (qToDate) q += "ci.startTime <= '"+ formatterDateDB.format( qToDate ) +"' AND " // lower or equal
      
      //
      // ===============================================================
      
      archetypeIds.eachWithIndex { archId, i ->
         
         // Lookup del tipo de objeto en la path para saber los nombres de los atributos
         // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
         dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
         idxtype = dataidx?.rmTypeName
         
         println "idxtype: $idxtype"
         
         // Subqueries sobre los DataValueIndex de los CompositionIndex
         q +=
         " EXISTS (" +
         "  SELECT dvi.id" +
         "  FROM DataValueIndex dvi" +
         "  WHERE dvi.owner.id = ci.id" + // Asegura de que todos los EXISTs se cumplen para el mismo CompositionIndex (los criterios se consideran AND, sin esta condicion es un OR y alcanza que se cumpla uno de los criterios que vienen en params)
         "        AND dvi.archetypeId = '"+ archId +"'" +
         "        AND dvi.path = '"+ paths[i] +"'"
         
         // Consulta sobre atributos del DataIndex dependiendo de su tipo
         switch (idxtype)
         {
            case 'DV_DATE_TIME':
               q += "        AND dvi.value"+ operands[i] + values[i] // TODO: verificar formato, transformar a SQL
            break
            case 'DV_QUANTITY':
               q += "        AND dvi.magnitude"+ operands[i] + new Float(values[i])
            break
            case 'DV_CODED_TEXT':
               q += "        AND dvi.code"+ operands[i] +"'"+ values[i]+"'"
            break
            default:
              throw new Exception("type $idxtype not supported")
         }
         q += ")"
         
         
         // Agrega ANDs para los EXISTs, menos el ultimo
         if (i+1 < archetypeIds.size()) q += " AND "
      }
      
      
      println q
      
      
//      EXISTS (
//        SELECT dvi.id
//        FROM DataIndex dvi
//        WHERE dvi.owner.id = ci.id
//              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//              AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value
//              AND dvi.magnitude>140.0
//      ) AND EXISTS (
//        SELECT dvi.id
//        FROM DataIndex dvi
//        WHERE dvi.owner.id = ci.id
//              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//              AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value
//              AND dvi.magnitude<130.0
//      ) AND EXISTS (
//        SELECT dvi.id
//        FROM DataIndex dvi
//        WHERE dvi.owner.id = ci.id
//              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//              AND dvi.path = /content/data[at0001]/origin
//              AND dvi.value>20080101
//      )
      
      
      
      // TODO: criterio por atributos del ci
      def cilist = CompositionIndex.findAll( q )

      
      // Muestra compositionIndex/list
      if (showUI)
      {
         // FIXME: hay que ver el tema del paginado
         render(view:'/compositionIndex/list',
                model:[compositionIndexInstanceList: cilist, compositionIndexInstanceTotal:cilist.size()])
         return
      }
      
      // Devuelve CompositionIndex, si quiere el contenido es buscar las
      // compositions que se apuntan por el index
      if (!retrieveData)
      {
         render(text:(cilist as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
      }
      else
      {
         // FIXME: hay que armar bien el XML: declaracion de xml solo al
         //        inicio y namespaces en el root.
         //
         //  REQUERIMIENTO:
         //  POR AHORA NO ES NECESARIO ARREGLARLO, listando los index y luego
         //  haciendo get por uid de la composition alcanza. Esto es mas para XRE
         //  para extraer datos con reglas sobre un conjunto de compositions en un
         //  solo XML.
         //
         // FIXME: no genera xml valido porque las compos se guardan con:
         // <?xml version="1.0" encoding="UTF-8"?>
         //
         String buff
         String out = '<?xml version="1.0" encoding="UTF-8"?><list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">\n'
         cilist.each { compoIndex ->
            
            // FIXME: verificar que esta en disco, sino esta hay un problema
            //        de sincronizacion entre la base y el FS, se debe omitir
            //        el resultado y hacer un log con prioridad alta para ver
            //        cual fue el error.
            
            // Tiene declaracion de xml
            // Tambien tiene namespace, eso deberia estar en el nodo root 
            //buff = new File("compositions\\"+compoIndex.uid+".xml").getText()
            buff = new File(config.composition_repo + compoIndex.uid +".xml").getText()
            
            buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
            buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
            buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
            
//            
//             Composition queda:
//               <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
//             
            
            out += buff + "\n"
         }
         out += '</list>'
         
         render(text: out, contentType:"text/xml", encoding:"UTF-8")
      }
      
   } // queryByData
   */
   
   /**
    * Se implemento completa en QueryController
    * 
    * @param name
    * @param qarchetypeId
    * @return
    */
   def saveQueryByData(String name, String qarchetypeId)
   {
      // Datos de criterios
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      List operands = params.list('operand')
      
      def query = new Query(name:name, qarchetypeId:qarchetypeId, type:'composition') // qarchetypeId puede ser vacio
      
      archetypeIds.eachWithIndex { archId, i ->
         
         query.addToWhere( new DataCriteria(archetypeId:archId, path:paths[i], operand:operands[i]) )
      }
      
      if (!query.save())
      {
         println "query errors: "+ query.errors
      }

      
      render( query as grails.converters.XML )
   }
   
   
   /**
    * Devuelve una lista de DataIndex.
    * 
    * Accion AJAX/JSON, se usa desde queryByData GUI.
    * 
    * Cuando el usuario selecciona el arquetipo, esta accion
    * le devuelve la informacion de los indices definidos para
    * ese arquetipo; path, nombre, tipo rm, ...
    * 
    * @param archetypeId
    * @return
    */
   def getIndexDefinitions(String archetypeId)
   {
      // TODO: checkear params
      
      def list = DataIndex.findAllByArchetypeId(archetypeId)
      
      // Devuelve solo datos necesarios (sin id de DataIndex, ...)
      def rlist = [] 
      
      for(di in list)
         rlist << [archetypeId: di.archetypeId, path: di.path, rmTypeName: di.rmTypeName, name: di.name]
      
      
      render(text:(rlist as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
   }
   
   
   def commitTest()
   {
      //def contrib = new File("test\\resources\\contribution.xml")
      def version1 = new File("test\\resources\\version1.xml")
      def version2 = new File("test\\resources\\version2.xml")
      def version3 = new File("test\\resources\\version3.xml")
      
      return [version1: version1.getText(),
              version2: version2.getText(),
              version3: version3.getText()]
   }
}