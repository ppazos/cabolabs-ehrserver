package com.cabolabs.ehrserver.query

import com.cabolabs.ehrserver.query.datatypes.*
import com.cabolabs.ehrserver.ehr.clinical_documents.IndexDefinition
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex

import org.springframework.dao.DataIntegrityViolationException

import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import grails.util.Holders
import com.cabolabs.ehrserver.ehr.clinical_documents.data.*
import grails.converters.*

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.ehr.clinical_documents.*

class QueryController {

    static allowedMethods = [save: "POST", update: "POST", delete: "DELETE"]
    
    def springSecurityService
    
    // Para acceder a las opciones de localizacion
    def config = Holders.config.app
    

    def index()
    {
        redirect(action: "list", params: params)
    }

    def list(Integer max)
    {
        params.max = Math.min(max ?: 10, 100)
        [queryInstanceList: Query.list(params), queryInstanceTotal: Query.count()]
    }

    def create()
    {
        [queryInstance: new Query(params),
         dataIndexes: IndexDefinition.list(), // to create filters or projections
         templateIndexes: OperationalTemplateIndex.list()]
    }
    
    def edit (Long id)
    {
       if (!id || !Query.exists(id))
       {
          flash.message = "Query doesn't exists"
          redirect(action: "list", params: params)
          return
       }
       
       render (
          view: 'create',
          model: [
             queryInstance: Query.get(id),
             dataIndexes: IndexDefinition.list(), // to create filters or projections
             templateIndexes: OperationalTemplateIndex.list(),
             mode: 'edit'
          ]
       )
    }
    

    /** FIXME: move this to a test
     * Diagnostic tests for some HQL queries that didn't seems to work well.
     *
    def hql() {
       
       println "dvi count: "+ DataValueIndex.count()
       
       def erhId = '4657fae4-e361-4a52-b4fe-58367235c808'
       
       def archetypeId = 'openEHR-EHR-OBSERVATION.blood_pressure.v1'
       def archetypePath = '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'
       
       
//        $/
//        SELECT dvi.id
//        FROM DataValueIndex dvi
//        WHERE dvi.owner.id = ci.id
//          AND dvi.archetypeId = '${dataCriteria.archetypeId}' 
//          AND dvi.archetypePath = '${dataCriteria.path}'
//        /$
        
       
       def subq
       
//       subq = $/
//          SELECT dvi.id
//          FROM DataValueIndex dvi
//          WHERE
//            dvi.archetypeId = '${archetypeId}' AND
//            dvi.archetypePath = '${archetypePath}'
//       /$
//       println "SUBQ DVI: "+ subq
//       println DataValueIndex.executeQuery(subq)
//       
//       
//       // JOINs dvi and dqi to compare the field value.
//       // THIS DOESNT WORK I NEED TO JOIN THE SUBCLASS EXPLICITLY!!!
//       subq = $/
//          SELECT dvi.id
//          FROM DataValueIndex dvi
//          WHERE
//            dvi.archetypeId = '${archetypeId}' AND
//            dvi.archetypePath = '${archetypePath}' AND
//            dvi.magnitude > 10.0
//       /$
//       println "SUBQ DVI: "+ subq
//       println DataValueIndex.executeQuery(subq)
//       
//       
//       
//       
//       // JOINs dvi and dqi to compare the field value.
//       subq = $/
//          SELECT dvi.id 
//          FROM DataValueIndex dvi, DvQuantityIndex dqi 
//          WHERE 
//            dvi.archetypeId = '${archetypeId}' AND
//            dvi.archetypePath = '${archetypePath}' AND
//            dvi.id = dqi.id AND
//            dqi.magnitude > 10.0
//       /$
//       println "SUBQ DVI: "+ subq
//       println DataValueIndex.executeQuery(subq)
//       
       
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
    */
    
    
    /**
     * Test query, se ejecuta desde create, para probar la query.
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
       if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
       {
          params['ehrs'] = Ehr.list()
       }
       else
       {
          // auth token used to login
          def auth = springSecurityService.authentication
          def org = Organization.findByNumber(auth.organization)
          
          params['ehrs'] = Ehr.findAllByOrganizationUid(org.uid)
       }
       
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
       
       return params
    }


    /**
     * 
     * @param name
     * @param qarchetypeId
     * @param type composition | datavalue
     * @param format xml | json formato por defecto
     * @param group none | composition | path agrupamiento por defecto
     * @return
     */
    def save(String name, String type, String format, String group)
    {
       println ">> save: " + params
       
       //println request.JSON // org.codehaus.groovy.grails.web.json.JSONObject
       //println request.JSON.query.getClass()
       
       println "-----------------------------------------"
       def query = Query.newInstance(request.JSON.query)
       if (!query.save(flush:true)) println query.errors.allErrors
       println "-----------------------------------------"
       
       JSON.use('deep')
       render query as JSON
    }
    
    
    def update()
    {
       println '>> update '+ params
       
       def json = request.JSON.query
       def query = Query.get(json.id) // the id comes in the json object
       query.updateInstance(json)
       if (!query.save(flush:true)) println query.errors.allErrors
       //def query = createOrUpdateQuery(id)
       
       render query as JSON
    }
    

    /**
     * This action shows the query UI on the server.
     * The query itself is executed agains the REST API: rest/query(queryUID)
     * @param uid
     * @return
     */
    def execute(String uid)
    {
       if (!uid)
       {
          flash.message = 'query.execute.error.queryUidMandatory'
          redirect(action:'list')
          return
       }
       
       def query = Query.findByUid(uid)
       if (!query)
       {
          flash.message = 'query.execute.error.queryDoesntExists'
          flash.args = [uid]
          redirect(action:'list')
          return
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
    
    
    /**
     * Devuelve una lista de IndexDefinition.
     *
     * Accion AJAX/JSON, se usa desde queryByData GUI.
     *
     * se usa en query create cuando el usuario selecciona el arquetipo
     * esta accion le devuelve los indices definidos para ese arquetipo
     * con: path, nombre, tipo rm, ...
     *
     * @param archetypeId
     * @return
     */
    def getIndexDefinitions(String archetypeId, boolean datatypesOnly)
    {
       // TODO: checkear params
       
       // TODO: define supported DVs in a singleton
       def datatypes = ['DV_QUANTITY', 'DV_CODED_TEXT', 'DV_TEXT', 'DV_DATE_TIME',
           'DV_BOOLEAN', 'DV_COUNT', 'DV_PROPORTION', 'DV_ORDINAL', 'DV_DURATION']
       
       // FIXME: we are creating each IndexDefinition for each archetype/path but for each template too.
       //        If 2 templates have the same arch/path, two IndexDefinitions will be created,
       //        then is we get the IndexDefinitions for an archetype we can get duplicated records.
       //        The code below (hack) avoids returning duplicated archetype/path, BUT WE NEED TO CREATE
       //        INDEXES DIFFERENTLY, like having the OPT data in a different record and the archetype/path
       //        in IndexDefinition, and a N-N relationship between OPTs and the referenced arch/path.
       //        Current fix is for https://github.com/ppazos/cabolabs-ehrserver/issues/102
       
       //def list = IndexDefinition.findAllByArchetypeId(archetypeId)
       def list = IndexDefinition.withCriteria {
          resultTransformer(org.hibernate.criterion.CriteriaSpecification.ALIAS_TO_ENTITY_MAP) // Get a map with attr names instead of a list with values
          projections {
            groupProperty('archetypeId', 'archetypeId')
            groupProperty('archetypePath', 'archetypePath')
            property('rmTypeName', 'rmTypeName')
            property('name', 'name')
          }
          eq 'archetypeId', archetypeId
          
          if (datatypesOnly)
          {
             'in'('rmTypeName', datatypes)
          }
       }
       
       render(text:(list as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
    }
    
    /**
     * Get criteria spec to create condition for composition queries.
     * @param datatype
     * @return
     */
    def getCriteriaSpec(String archetypeId, String path, String datatype)
    {
       // TODO: simplificar a metodo dinamico + try catch por si pide cualquier cosa.
       def res = []
       switch (datatype) {
          case 'DV_QUANTITY':
            res = DataCriteriaDV_QUANTITY.criteriaSpec(archetypeId, path)
          break
          case 'DV_CODED_TEXT':
            res = DataCriteriaDV_CODED_TEXT.criteriaSpec(archetypeId, path)
          break
          case 'DV_TEXT':
            res = DataCriteriaDV_TEXT.criteriaSpec(archetypeId, path)
          break
          case 'DV_DATE_TIME':
            res = DataCriteriaDV_DATE_TIME.criteriaSpec(archetypeId, path)
          break
          case 'DV_BOOLEAN':
            res = DataCriteriaDV_BOOLEAN.criteriaSpec(archetypeId, path)
          break
          case 'DV_COUNT':
            res = DataCriteriaDV_COUNT.criteriaSpec(archetypeId, path)
          break
          case 'DV_PROPORTION':
            res = DataCriteriaDV_PROPORTION.criteriaSpec(archetypeId, path)
          break
          case 'DV_ORDINAL':
            res = DataCriteriaDV_ORDINAL.criteriaSpec(archetypeId, path)
          break
          case 'DV_DURATION':
            res = DataCriteriaDV_DURATION.criteriaSpec(archetypeId, path)
          break
       }
       
       render(text:(res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
    }
    
    def export(Long id)
    {
       def q = Query.get(id)
       def criteriaMap, _value
       
       // TODO: this code should be reused in RestConrtoller.queryList
       withFormat {
          xml {
             render(contentType: "text/xml") {
                query {
                   uid(q.uid)
                   name(q.name)
                   format(q.format)
                   type(q.type)
                   
                   if (q.type == 'composition')
                   {
                      criteriaLogic(q.criteriaLogic)
                      templateId(q.templateId)
                      
                      for (criteria in q.where)
                      {
                         delegate.criteria {
                            archetypeId(criteria.archetypeId)
                            path(criteria.path)
                            //operand(criteria.operand)
                            //value(criteria.value)
                            
                            criteriaMap = criteria.getCriteriaMap() // [attr: [operand: value]] value can be a list
                            
                            conditions {
                               criteriaMap.each { attr, cond ->
                                  
                                  _value = cond.find{true}.value // can be a list
                                  
                                  "$attr" {
                                     operand(cond.find{true}.key) // first entry of the operand: value map
                                     
                                     if (_value instanceof List)
                                     {
                                        list {
                                           _value.each { val ->
                                              item(val)
                                           }
                                        }
                                     }
                                     else
                                     {
                                        value(_value)
                                     }
                                  }
                               }
                            }
                         }
                      }
                   }
                   else
                   {
                      group(q.group) // Group is only for datavalue
                      
                      for (proj in q.select)
                      {
                         projection {
                            archetypeId(proj.archetypeId)
                            path(proj.path)
                         }
                      }
                   }
                }
             }
          }
          json {
             render(contentType: "application/json") {
                delegate.query = {
                   uid    = q.uid
                   name   = q.name
                   format = q.format
                   type   = q.type
                   
                   if (q.type == 'composition')
                   {
                      criteriaLogic = q.criteriaLogic
                      templateId    = q.templateId
                      criteria      = q.where.collect { [archetypeId: it.archetypeId, path: it.path, conditions: it.getCriteriaMap()] }
                   }
                   else
                   {
                      group         = q.group // Group is only for datavalue
                      projections   = q.select.collect { [archetypeId: it.archetypeId, path: it.path] }
                   }
                } // query
             }
          }
          html {
             return "format not supported"
          }
       }
    }
}
