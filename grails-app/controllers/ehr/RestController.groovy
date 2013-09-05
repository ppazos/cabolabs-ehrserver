package ehr

import grails.converters.*
import java.text.SimpleDateFormat
import demographic.Person
import query.Query
import query.DataGet
import query.DataCriteria
import ehr.clinical_documents.DataIndex
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.data.DataValueIndex

import org.codehaus.groovy.grails.commons.ApplicationHolder

class RestController {

   /**
    * Auxiliar para consultas por datos (ej. queryCompositions)
    */
   static Map operandMap = [
     'eq': '=',
     'lt': '<',
     'gt': '>',
     'neq': '<>' // http://stackoverflow.com/questions/723195/should-i-use-or-for-not-equal-in-tsql
   ]
   
   // TODO: un index con la lista de servicios y parametros de cada uno (para testing)
   
   //def formatter = new SimpleDateFormat("yyyyMMdd'T'hhmmss.SSSSZ")
   //def formatterDate = new SimpleDateFormat("yyyyMMdd")
   def formatter = new SimpleDateFormat( ApplicationHolder.application.config.app.l10n.datetime_format )
   def formatterDate = new SimpleDateFormat( ApplicationHolder.application.config.app.l10n.date_format )
   
   def ehrList(String format, int max, int offset)
   {
      // TODO: fromDate, toDate
      
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      // Lista ehrs
      def _ehrs = Ehr.list(max: max, offset: offset, readOnly: true)
      
      
      /*
      println params
      
      withFormat { 
         xml { println "xml" } 
         json { println "json" }
         html { println "html" }
         text { println "text" }         
      }
      */
      
      
      // ===========================================================================
      // 3. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         /*
         <result>
          <ehrs>
            <ehr>
              <ehrId>33b94e05-3da5-4291-872e-07b3a4664837</ehrId>
              <dateCreated>20121105T113730.0890-0200</dateCreated>
              <subjectUid>bf529d1c-b74a-4c4f-b6dd-c44c44cd9a3f</subjectUid>
              <systemId>ISIS_EHR_SERVER</systemId>
            </ehr>
            <ehr>
              <ehrId>d06e3256-d65e-436e-95da-5c9bffd05dbd</ehrId>
              <dateCreated>20121105T113732.0171-0200</dateCreated>
              <subjectUid>43a399c9-a5e0-4b51-9422-99c3991ea941</subjectUid>
              <systemId>ISIS_EHR_SERVER</systemId>
            </ehr>
          </ehrs>
          <pagination>...</pagination>
          </result>
          */
         //render(text: ehrs as XML, contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            'result' {
               'ehrs' {
                  _ehrs.each { _ehr ->
                     'ehr'{
                        ehrId(_ehr.ehrId)
                        dateCreated( this.formatter.format( _ehr.dateCreated ) ) // TODO: format
                        subjectUid(_ehr.subject.value)
                        systemId(_ehr.systemId)
                     }
                  }
               }
               pagination {
                  delegate.max(max)
                  delegate.offset(offset)
                  nextOffset(offset+max) // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
                  prevOffset( ((offset-max < 0) ? 0 : offset-max) )
               }
            }
         }
      }
      else if (format == "json")
      {
         /*
         {
          "ehrs": [
            {
              "ehrId": "33b94e05-3da5-4291-872e-07b3a4664837",
              "dateCreated": "20121105T113730.0890-0200",
              "subjectUid": "bf529d1c-b74a-4c4f-b6dd-c44c44cd9a3f",
              "systemId": "ISIS_EHR_SERVER"
            },
            {
              "ehrId": "d06e3256-d65e-436e-95da-5c9bffd05dbd",
              "dateCreated": "20121105T113732.0171-0200",
              "subjectUid": "43a399c9-a5e0-4b51-9422-99c3991ea941",
              "systemId": "ISIS_EHR_SERVER"
            }
          ],
          "pagination": {...}
        }
        */
         def data = [
           ehrs: [],
           pagination: [
               'max': max,
               'offset': offset,
               nextOffset: offset+max, // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
               prevOffset: ((offset-max < 0) ? 0 : offset-max )
            ]
         ]
         
         _ehrs.each { _ehr ->
            data.ehrs << [
               ehrId: _ehr.ehrId,
               dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
               subjectUid: _ehr.subject.value,
               systemId: _ehr.systemId
            ]
         }
         
         //render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
         render data as JSON
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrList
   
   
   def ehrForSubject(String subjectUid, String format)
   {
      // ===========================================================================
      // 1. Paciente existe?
      //
      def _subject = Person.findByUidAndRole(subjectUid, 'pat')
      if (!_subject)
      {
         render(status: 500, text:"<result><code>error</code><message>No existe el paciente $subjectUid</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 2. Paciente tiene EHR?
      //
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         subject {
            eq ('value', subjectUid)
         }
      }
      if (!_ehr)
      {
         render(status: 500, text:"<result><code>error</code><message>EHR no encontrado para el paciente $subjectUid, se debe crear un EHR para el paciente</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 3. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehr'{
               ehrId(_ehr.ehrId)
               dateCreated( this.formatter.format( _ehr.dateCreated ) )
               delegate.subjectUid(_ehr.subject.value) // delegate para que no haya conflicto con la variable con el mismo nombre
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            ehrId: _ehr.ehrId,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrForSubject
   
   
   def ehrGet(String ehrUid, String format)
   {
      // 1. EHR existe?
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         eq ('ehrId', ehrUid)
      }
      if (!_ehr)
      {
         render(status: 500, text:"<result><code>error</code><message>EHR no encontrado para el ehrUid $ehrUid</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehr'{
               ehrId(_ehr.ehrId)
               dateCreated( this.formatter.format( _ehr.dateCreated ) ) // TODO: format
               subjectUid(_ehr.subject.value)
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            ehrId: _ehr.ehrId,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrGet
   
   
   
   def patientList(String format, int max, int offset)
   {
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      
      // ===========================================================================
      // 1. Lista personas con rol paciente
      //
      def subjects = Person.findAllByRole('pat', [max: max, offset: offset, readOnly: true])
      
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'result' {
               'patients' {
                  subjects.each { person ->
                     'patient'{
                        uid(person.uid)
                        firstName(person.firstName)
                        lastName(person.lastName)
                        dob(this.formatterDate.format( person.dob ) )
                        sex(person.sex)
                        idCode(person.idCode)
                        idType(person.idType)
                     }
                  }
               }
               pagination {
                  delegate.max(max)
                  delegate.offset(offset)
                  nextOffset(offset+max) // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
                  prevOffset( ((offset-max < 0) ? 0 : offset-max) )
               }
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            patients: [],
            pagination: [
               'max': max,
               'offset': offset,
               nextOffset: offset+max, // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
               prevOffset: ((offset-max < 0) ? 0 : offset-max )
            ]
         ]
         subjects.each { person ->
            data.patients << [
               uid: person.uid,
               firstName: person.firstName,
               lastName: person.lastName,
               dob: this.formatterDate.format( person.dob ),
               sex: person.sex,
               idCode: person.idCode,
               idType: person.idType
            ]
         }
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // patientList
   
   
   /*
    * Servicios sobre consultas.
    */
   def queryList(String format, int max, int offset)
   {
      println params
      
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      // Lista ehrs
      def _queries = Query.list(max: max, offset: offset, readOnly: true)
      
      // Si format es cualquier otra cosa, tira XML por defecto (no se porque)
      /*
      withFormat {
      
         xml { render 'xml' }
         json { render 'json' }
      }
      */
      
      withFormat {
      
         xml {
            render(contentType:"text/xml", encoding:"UTF-8") {
               'result' {
                  'queries' {
                     _queries.each { query ->
                        delegate.query {
                           name(query.name) // FIXME: debe tener uid
                           type(query.type)
                           delegate.format(query.format)
                           qarchetypeId(query.qarchetypeId)
                           group(query.group)
                           
                           delegate.select {
                             query.select.each { _dataGet ->
                                get {
                                  archetypeId(_dataGet.archetypeId)
                                  path(_dataGet.path)
                                }
                             }
                           }
                        }
                     }
                  }
                  pagination {
                     delegate.max(max)
                     delegate.offset(offset)
                     nextOffset(offset+max) // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
                     prevOffset( ((offset-max < 0) ? 0 : offset-max) )
                  }
               }
            }
         }
         json {
           render 'json'
         }
      }
   }
   
   def queryShow()
   {
      // TODO: query as JSON or XML
   }
   
   // FIXME: this should receive queryUID, params should be only params of the query
   //        like dates or output format, the criteria is defined the Query.   
   def queryData(String qehrId, String qarchetypeId, String fromDate, String toDate, String format, String group)
   {
      println "queryData"
      println params
      
      // En una consulta EQL archetypeId+path seria el SELECT
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      
      // parse de dates
      Date qFromDate
      Date qToDate

      if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
      if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)
      
      // Query data
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
            
            if (qFromDate) ge('startTime', qFromDate) // greater or equal
            if (qToDate) le('startTime', qToDate) // lower or equal
         }
      }
      
      println res
      println "group $group"
      
      // Group
      if (group == 'composition')
      {
         res = queryDataGroupComposition(res, archetypeIds, paths)
      }
      else if (group == 'path')
      {
         res = queryDataGroupPath(res, archetypeIds, paths)
      }

      // Format
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
      return
   }
   
   /**
    * Usada por queryData para agrupar por composition
    */
   private queryDataGroupComposition(res, archetypeIds, paths)
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
            case ['DV_QUANTITY', 'DvQuantity']:
               resHeaders[paths[i]]['attrs'] = ['magnitude', 'units']
            break
            case ['DV_CODED_TEXT', 'DvCodedText']:
               resHeaders[paths[i]]['attrs'] = ['value']
            break
            case ['DV_DATE_TIME', 'DvDateTime']:
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
                  case ['DV_QUANTITY', 'DvQuantity']:
                     col['magnitude'] = dvi.magnitude
                     col['units'] = dvi.units
                  break
                  case ['DV_CODED_TEXT', 'DvCodedText']:
                     col['value'] = dvi.value
                  break
                  case ['DV_DATE_TIME', 'DvDateTime']:
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
      
      return [resHeaders, resGrouped]
   } // queryDataGroupComposition
   
   /**
    * Usada por queryData para agrupar por path
    */
   private queryDataGroupPath(res, archetypeIds, paths)
   {
      // En este caso los headers son las filas
      //def resHeaders = [:]
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
               case ['DV_QUANTITY', 'DvQuantity']: // FIXME: this is a bug on adl parser it uses Java types instead of RM ones
                  resGrouped[paths[i]]['serie'] << [magnitude: dvi.magnitude,
                                                    units:     dvi.units,
                                                    date:      dvi.owner.startTime]
               break
               case ['DV_CODED_TEXT', 'DvCodedText']:
                  resGrouped[paths[i]]['serie'] << [value:     dvi.value,
                                                    date:      dvi.owner.startTime]
               break
               case ['DV_DATE_TIME', 'DvDateTime']:
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
      
      return resGrouped
   }
   
   /**
    * Previo QueryController.testQueryByData
    * Solo soporta XML.
    * @return
    */
   def queryCompositions(String qehrId, String qarchetypeId, String fromDate, String toDate, boolean retrieveData, boolean showUI)
   {
       println "queryCompositions"
       println params
       
       
       // Viene una lista de cada parametro
       // String archetypeId, String path, String operand, String value
       // El mismo indice en cada lista corresponde con un atributo del mismo criterio de busqueda
       
       // Datos de criterios
       List archetypeIds = params.list('archetypeId')
       List paths = params.list('path')
       //List operands = params.list('operand')
       List values = params.list('value')
       
       // Con nombres eq, lt, ...
       // Hay que transformarlo a =, <, ...
       // No vienen los operadores directamente porque rompen en HTML, ej. <, >
       List operands = params.list('operand')
       operands = operands.collect {
          operandMap[it] // 'gt' => '>'
       }
       
       DataIndex dataidx
       String idxtype
 
       
       // parse de dates
       Date qFromDate
       Date qToDate
 
       if (fromDate)
          qFromDate = Date.parse(config.l10n.date_format, fromDate)
       
       if (toDate)
          qToDate = Date.parse(config.l10n.date_format, toDate)
       
       
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
       
       /**
        * FIXME: issue #6
        * si en el create se verifican las condiciones para que a aqui no
        * llegue una path a un tipo que no corresponde, el error de tipo
        * no sucederia nunca, asi no hay que tirar except aca.
        */

       archetypeIds.eachWithIndex { archId, i ->
          
          // Lookup del tipo de objeto en la path para saber los nombres de los atributos
          // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
          dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
          idxtype = dataidx?.rmTypeName
          
          
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
             case ['DV_DATE_TIME', 'DvDateTime']: // ADL Parser bug: uses Java class names instead of RM Type Names...
                q += "        AND dvi.value"+ operands[i] + values[i] // TODO: verificar formato, transformar a SQL
             break
             case ['DV_QUANTITY', 'DvQuantity']: // ADL Parser bug: uses Java class names instead of RM Type Names...
                q += "        AND dvi.magnitude"+ operands[i] + new Float(values[i])
             break
             case ['DV_CODED_TEXT', 'DvCodedText']: // ADL Parser bug: uses Java class names instead of RM Type Names...
                q += "        AND dvi.code"+ operands[i] +"'"+ values[i]+"'"
             break
             // TODO: are more types
             default:
               throw new Exception("type $idxtype not supported")
          }
          q += ")"
          
          
          // Agrega ANDs para los EXISTs, menos el ultimo
          if (i+1 < archetypeIds.size()) q += " AND "
       }
       
       println q
       
       /*
       EXISTS (
         SELECT dvi.id
         FROM DataIndex dvi
         WHERE dvi.owner.id = ci.id
               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value
               AND dvi.magnitude>140.0
       ) AND EXISTS (
         SELECT dvi.id
         FROM DataIndex dvi
         WHERE dvi.owner.id = ci.id
               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value
               AND dvi.magnitude<130.0
       ) AND EXISTS (
         SELECT dvi.id
         FROM DataIndex dvi
         WHERE dvi.owner.id = ci.id
               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
               AND dvi.path = /content/data[at0001]/origin
               AND dvi.value>20080101
       )
       */
       
       
       // TODO: criterio por atributos del ci
       def cilist = CompositionIndex.findAll( q )
 
       println "Resultados (CompositionIndex): " + cilist
       
       
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
             
             /**
              * Composition queda:
              *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
              */
             
             out += buff + "\n"
          }
          out += '</list>'
          
          render(text: out, contentType:"text/xml", encoding:"UTF-8")
       }
   }
}