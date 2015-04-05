package query

import ehr.clinical_documents.IndexDefinition
import org.springframework.dao.DataIntegrityViolationException
import ehr.clinical_documents.CompositionIndex
import grails.util.Holders
import ehr.clinical_documents.data.*
import grails.converters.*

class QueryController {

    static allowedMethods = [save: "POST", update: "POST"] //, delete: "POST"]
    
    // Para acceder a las opciones de localizacion
    def config = Holders.config.app
    

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [queryInstanceList: Query.list(params), queryInstanceTotal: Query.count()]
    }

    def create() {
        [queryInstance: new Query(params),
         dataIndexes: ehr.clinical_documents.IndexDefinition.list(), // to create filters or projections
         templateIndexes: ehr.clinical_documents.OperationalTemplateIndex.list()]
    }
    

    /*
     * Diagnostic tests for some HQL queries that didn't seems to work well.
     */
    def hql() {
       
       println "dvi count: "+ DataValueIndex.count()
       
       def erhId = '4657fae4-e361-4a52-b4fe-58367235c808'
       
       def archetypeId = 'openEHR-EHR-OBSERVATION.blood_pressure.v1'
       def archetypePath = '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'
       
       /*
        * $/
          SELECT dvi.id
          FROM DataValueIndex dvi
          WHERE dvi.owner.id = ci.id
            AND dvi.archetypeId = '${dataCriteria.archetypeId}' 
            AND dvi.archetypePath = '${dataCriteria.path}'
          /$
        */
       
       def subq
       
       /*
       subq = $/
          SELECT dvi.id
          FROM DataValueIndex dvi
          WHERE
            dvi.archetypeId = '${archetypeId}' AND
            dvi.archetypePath = '${archetypePath}'
       /$
       println "SUBQ DVI: "+ subq
       println DataValueIndex.executeQuery(subq)
       
       
       // JOINs dvi and dqi to compare the field value.
       // THIS DOESNT WORK I NEED TO JOIN THE SUBCLASS EXPLICITLY!!!
       subq = $/
          SELECT dvi.id
          FROM DataValueIndex dvi
          WHERE
            dvi.archetypeId = '${archetypeId}' AND
            dvi.archetypePath = '${archetypePath}' AND
            dvi.magnitude > 10.0
       /$
       println "SUBQ DVI: "+ subq
       println DataValueIndex.executeQuery(subq)
       
       
       
       
       // JOINs dvi and dqi to compare the field value.
       subq = $/
          SELECT dvi.id 
          FROM DataValueIndex dvi, DvQuantityIndex dqi 
          WHERE 
            dvi.archetypeId = '${archetypeId}' AND
            dvi.archetypePath = '${archetypePath}' AND
            dvi.id = dqi.id AND
            dqi.magnitude > 10.0
       /$
       println "SUBQ DVI: "+ subq
       println DataValueIndex.executeQuery(subq)
       */
       
       subq = $/
          SELECT dvi.id FROM DataValueIndex dvi ,DvQuantityIndex dqi
          WHERE dvi.archetypeId = 'openEHR-EHR-OBSERVATION.blood_pressure.v1' AND
                dvi.archetypePath = '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value' AND
                dqi.id = dvi.id AND
                dqi.magnitude > 10.0
       /$
       
       println "SUBQ DVI: "+ subq
       println DataValueIndex.executeQuery(subq)
       
       def query = $/
       FROM CompositionIndex ci
       WHERE EXISTS ( 
             SELECT dvi.id
             FROM DataValueIndex dvi, DvQuantityIndex dqi
             WHERE dvi.owner = ci AND
                   dvi.archetypeId = 'openEHR-EHR-OBSERVATION.blood_pressure.v1' AND 
                   dvi.archetypePath = '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value' AND
                   dvi.id = dqi.id AND
                   dqi.magnitude > 33 )/$
       
       println "QUERY "+ query
       println CompositionIndex.executeQuery( query )
       
       render "done"
    }
    
    
    /**
     * Test query, se ejecuta desde create, solo para ver si se pasan bien los params.
     * 
     * @param type composition | datavalue
     * @param name nombre de la query a probar, puede no estar creada
     * @param archetypeId lista de archetype ids para absolutizar las paths
     * @param path lista de paths para cada archetype id
     * @param operand
     * @param value
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
       params['archetypePath'] = params.list('archetypePath')
       
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
     * 
     * @param name
     * @param qarchetypeId
     * @param type composition | datavalue
     * @param format xml | json formato por defecto
     * @param group '' | composition | path agrupamiento por defecto
     * @return
     */
    def save(String name, String type, String format, String group)
    {
       println params
       
       def query = new Query(name:name, type:type, format:format, group:group) // qarchetypeId puede ser vacio
       
       List archetypeIds = params.list('archetypeId')
       List paths = params.list('archetypePath')
       
       
       // FIXME: switch
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
       
       if (!query.save(flush:true))
       {
          println "================================="
          println "query errors: "+ query.errors
          query.errors.allErrors.each { println it }
       }
       
       //redirect(action:'show', id:query.id)
       render query as JSON
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

    /** TODO https://github.com/ppazos/cabolabs-ehrserver/issues/71
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
    */

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
