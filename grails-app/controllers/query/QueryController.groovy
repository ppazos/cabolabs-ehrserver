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
        [queryInstance: new Query(params),
         dataIndexes: ehr.clinical_documents.DataIndex.list(), // to create filters or projections
         templateIndexes: ehr.clinical_documents.OperationalTemplateIndex.list()]
    }
    

    
    /**
     * Test query, se ejecuta desde create, solo para ver si se pasan bien los params.
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
       
       if (!query.save())
       {
          println "================================="
          println "query errors: "+ query.errors
          query.errors.allErrors.each { println it }
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
