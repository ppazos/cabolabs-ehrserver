package query

import ehr.clinical_documents.DataIndex
import org.springframework.dao.DataIntegrityViolationException
import ehr.clinical_documents.CompositionIndex
import org.codehaus.groovy.grails.commons.ApplicationHolder

class QueryController {

    static allowedMethods = [save: "POST", update: "POST"] //, delete: "POST"]
    
    // Para acceder a las opciones de localizacion
    def config = ApplicationHolder.application.config.app
    

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [queryInstanceList: Query.list(params), queryInstanceTotal: Query.count()]
    }

    def create() {
        [queryInstance: new Query(params)]
    }
    

    
    /**
     * Test query, se ejecuta desde create
     * 
     * @param type composition | datavalue
     * 
     * @return
     */
    def test(String type)
    {
       println "test"
       println params


       // ==================================================================
       // asegura que archetypeId, path, value y operand son siempre listas,
       // el gsp espera listas.
       //
       params['archetypeId'] = params.list('archetypeId')
       params['path'] =params.list('path')
       
       if (type == 'composition')
       {
          params['operand'] = params.list('operand')
          params['value'] = params.list('value')
       }
       
       
       
       /*
        * [
        *  sarchetypeId:openEHR-EHR-COMPOSITION.encounter.v1, 
        *  archetypeId: [ 
        *   openEHR-EHR-COMPOSITION.encounter.v1, 
        *   openEHR-EHR-COMPOSITION.encounter.v1
        *  ], 
        *  svalue:, 
        *  _action_test:Test, 
        *  soperand:<, 
        *  spath: /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value, 
        *  name:ewe, 
        *  value: [
        *   123, 
        *  ], 
        *  path: [ 
        *    /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value, 
        *    /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value], 
        *    operand:[>, <], 
        *    type:composition, 
        *    showUI:false, 
        *    action:save, controller:query
        *  ]
        * 
        * [
        *  sarchetypeId:openEHR-EHR-COMPOSITION.encounter.v1, 
        *  archetypeId: [
        *   openEHR-EHR-COMPOSITION.encounter.v1, 
        *   openEHR-EHR-COMPOSITION.encounter.v1, 
        *   openEHR-EHR-COMPOSITION.encounter.v1
        *  ], 
        *  svalue:20000101, 
        *  _action_test:Test, 
        *  soperand:=, 
        *  spath:/content/data[at0001]/events[at0006]/time, 
        *  name:ewe, 
        *  value: [
        *   234, , 20000101  << prueba para mandar 2 valores sin uno en el medio, OK!
        *  ], 
        *  path: [
        *   /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value, 
        *   /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value, 
        *   /content/data[at0001]/events[at0006]/time
        *  ], 
        *  operand:[=, >, =], 
        *  type:composition, 
        *  showUI:false, 
        *  action:save, controller:query]
        */
       
       //render "ok"
       
       return params
    }
    
    /**
     * Copiada de test/queryByData
     * Se llama desde la gui test.gsp para obtener compositions por datos.
     * 
     * @param qehrId
     * @param qarchetypeId
     * @param fromDate
     * @param toDate
     * @param retrieveData
     * @param showUI
     * @return
     */
    /*
     * Implementacion definitiva en RestController.queryCompositions
     * 
    def testQueryByData(String qehrId, String qarchetypeId, String fromDate, String toDate, boolean retrieveData, boolean showUI)
    {
       println "testQueryByData"
       println params
       
       
       // Viene una lista de cada parametro
       // String archetypeId, String path, String operand, String value
       // El mismo indice en cada lista corresponde con un atributo del mismo criterio de busqueda
       
//     ya viene el nombre correcto
//       String op
//       switch (operand)
//       {
//          case '=': op = 'eq'
//          break
//          case '<': op = 'lt'
//          break
//          case '>': op = 'gt'
//          break
//          case '!=': op = 'neq'
//          break
//       }
       
       
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
          qFromDate = Date.parse(config.l10n.date_format, fromDate)
       
       if (toDate)
          qToDate = Date.parse(config.l10n.date_format, toDate)
       
       
       println "prev query"
       
       
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
       
       println "post query"
       
       println q
       
       
//       EXISTS (
//         SELECT dvi.id
//         FROM DataIndex dvi
//         WHERE dvi.owner.id = ci.id
//               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value
//               AND dvi.magnitude>140.0
//       ) AND EXISTS (
//         SELECT dvi.id
//         FROM DataIndex dvi
//         WHERE dvi.owner.id = ci.id
//               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value
//               AND dvi.magnitude<130.0
//       ) AND EXISTS (
//         SELECT dvi.id
//         FROM DataIndex dvi
//         WHERE dvi.owner.id = ci.id
//               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//               AND dvi.path = /content/data[at0001]/origin
//               AND dvi.value>20080101
//       )
       
       
       
       // TODO: criterio por atributos del ci
       def cilist = CompositionIndex.findAll( q )
 
       println "Resultados (CompositionIndex): " + cilist
       
       println "prev mostrar resultados"
       
       
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
//              Composition queda:
//                <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
//              
             
             out += buff + "\n"
          }
          out += '</list>'
          
          render(text: out, contentType:"text/xml", encoding:"UTF-8")
       }
       
    } // testQueryByData
    */

    /**
     * 
     * @param name
     * @param qarchetypeId
     * @param type composition | datavalue
     * @param format xml | json formato por defecto
     * @param group '' | composition | path agrupamiento por defecto
     * @return
     */
    def save(String name, String qarchetypeId, String type, String format, String group)
    {
       def query = new Query(name:name, qarchetypeId:qarchetypeId, type:type, format:format, group:group) // qarchetypeId puede ser vacio
       
       List archetypeIds = params.list('archetypeId')
       List paths = params.list('path')
       
       if (type == 'composition')
       {
          List operands = params.list('operand')
          List values = params.list('value') // Pueden haber values vacios
          
          // Crea criterio
          archetypeIds.eachWithIndex { archId, i ->
             
             query.addToWhere(
                new DataCriteria(archetypeId:archId, path:paths[i], operand:operands[i], value:values[i])
             )
          }
       }
       else if (type == 'datavalue')
       {
          // Crea seleccion
          archetypeIds.eachWithIndex { archId, i ->
             
             query.addToSelect(
                new DataGet(archetypeId:archId, path:paths[i])
             )
          }
       }
       else
       {
          // Caso no permitido
       }
       
       if (!query.save())
       {
          println "query errors: "+ query.errors
       }
       
       /*
        def queryInstance = new Query(params)
        if (!queryInstance.save(flush: true)) {
            render(view: "create", model: [queryInstance: queryInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'query.label', default: 'Query'), queryInstance.id])
        redirect(action: "show", id: queryInstance.id)
       */
       
       redirect(action:'show', id:query.id)
    }

    
    def execute(String uid)
    {
       def query = Query.findByUid(uid)
       if (!query)
       {
          flash.message = 'No existe la query con uid = $uid'
          redirect(action:'list')
       }
       
       return [query: query, type: query.type]
    }
    
    
    def show(Long id) {
        def queryInstance = Query.get(id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), id])
            redirect(action: "list")
            return
        }

        [queryInstance: queryInstance]
    }

    def edit(Long id) {
        def queryInstance = Query.get(id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), id])
            redirect(action: "list")
            return
        }

        [queryInstance: queryInstance]
    }

    def update(Long id, Long version) {
        def queryInstance = Query.get(id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (queryInstance.version > version) {
                queryInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'query.label', default: 'Query')] as Object[],
                          "Another user has updated this Query while you were editing")
                render(view: "edit", model: [queryInstance: queryInstance])
                return
            }
        }

        queryInstance.properties = params

        if (!queryInstance.save(flush: true)) {
            render(view: "edit", model: [queryInstance: queryInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'query.label', default: 'Query'), queryInstance.id])
        redirect(action: "show", id: queryInstance.id)
    }

    def delete(Long id) {
        def queryInstance = Query.get(id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), id])
            redirect(action: "list")
            return
        }

        try {
            queryInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'query.label', default: 'Query'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'query.label', default: 'Query'), id])
            redirect(action: "show", id: id)
        }
    }
}
