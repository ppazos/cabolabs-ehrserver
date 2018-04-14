/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.query

import com.cabolabs.ehrserver.query.datatypes.*
import com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.data.DataValues

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

import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy

class QueryController {

   static allowedMethods = [save: "POST", update: "POST", delete: "DELETE"]

   def springSecurityService
   def resourceService
   def configurationService
   def querySnomedService

   // Para acceder a las opciones de localizacion
   def config = Holders.config.app


   def index()
   {
      redirect(action: "list", params: params)
   }

   def list(int offset, String sort, String order, String name, boolean isDeleted)
   {
      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      if (isDeleted == null) isDeleted = false

      def list
      def org = session.organization
      def shares = QueryShare.findAllByOrganization(org)
      def c = Query.createCriteria()

      // Same for admins and other users since private queries should not be accessed even by admins
      list = c.list (max: max, offset: offset, sort: sort, order: order) {
         if (name)
         {
            like('name', '%'+name+'%')
         }
         if (shares)
         {
            or {
               eq('isPublic', true)
               'in'('id', shares.query.id)
            }
         }
         else
         {
            eq('isPublic', true)
         }

         eq('isDeleted', isDeleted)
      }

      [queryInstanceList: list.groupBy{it.queryGroup}, queryInstanceTotal: list.totalCount]
   }


   def create()
   {
      /*
      templateIndexes: used for the filter by document type
      dataIndexes is not being used
      */
      [
       queryInstance: new Query(params),
       templateIndexes: OperationalTemplateIndex.findAllByOrganizationUidAndLanguage(session.organization.uid, session.lang), // queries cna be created for any version of the OPT
       queryGroups: QueryGroup.findAllByOrganizationUid(session.organization.uid)
      ]
   }

   /**
    * @param name
    * @param qarchetypeId
    * @param type composition | datavalue
    * @param format xml | json formato por defecto
    * @param group none | composition | path agrupamiento por defecto
    * @return
    */
   def save(String name, String type, String format, String group)
   {
      //println request.JSON // org.codehaus.groovy.grails.web.json.JSONObject
      //println request.JSON.query.getClass()
      request.JSON.query.organizationUid = session.organization.uid
      def query = Query.newInstance(request.JSON.query)


      // https://github.com/ppazos/cabolabs-ehrserver/issues/340
      def user = springSecurityService.getCurrentUser()
      query.author = user
      query.cacheHQLWhere()

      if (query.hasErrors())
      {
         flash.message = e.message

         render (
           view: 'create',
           model: [
             queryInstance: query,
             templateIndexes: OperationalTemplateIndex.findAllByOrganizationUidAndLanguage(session.organization.uid, session.lang), // queries can be created for any version of the OPT
             queryGroups: QueryGroup.findAllByOrganizationUid(session.organization.uid),
             mode: 'edit'
           ]
         )

         return
      }

      // TODO: errors in json to be displayed
      if (!query.save(flush:true))
      {
         println query.errors.allErrors
      }

      // private queries should be shared with the current org
      if (!query.isPublic)
      {
         resourceService.shareQuery(query, session.organization)
      }

      render query as JSON
   }

   def edit ()
   {
      def queryInstance = params.query // set on filter

      if (!queryInstance)
      {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.uid])
         redirect(action: "list")
         return
      }

      render (
        view: 'create',
        model: [
          queryInstance: queryInstance,
          templateIndexes: OperationalTemplateIndex.findAllByOrganizationUidAndLanguage(session.organization.uid, session.lang), // queries can be created for any version of the OPT
          queryGroups: QueryGroup.findAllByOrganizationUid(session.organization.uid),
          mode: 'edit'
        ]
      )
   }

   def update()
   {
      //def json = request.JSON.query
      //def query = Query.get(json.id) // the id comes in the json object

      def json = params.json
      def query = params.query
      query.updateInstance(json)
      query.cacheHQLWhere()

      // TODO: error as json
      if (!query.save(flush:true)) println query.errors.allErrors


      // public queries dont have shares
      if (query.isPublic) resourceService.cleanSharesQuery(query)
      else // private queries should be shared with the current org
      {
         resourceService.shareQuery(query, session.organization)
      }

      render query as JSON
   }

   /** FIXME: move this to a test
    * Diagnostic tests for some HQL queries that didn't seems to work well.
    *
   def hql() {

      println "dvi count: "+ DataValueIndex.count()

      def erhId = '4657fae4-e361-4a52-b4fe-58367235c808'

      def archetypeId = 'openEHR-EHR-OBSERVATION.blood_pressure.v1'
      def archetypePath = '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'


//      $/
//      SELECT dvi.id
//      FROM DataValueIndex dvi
//      WHERE dvi.owner.id = ci.id
//        AND dvi.archetypeId = '${dataCriteria.archetypeId}'
//        AND dvi.archetypePath = '${dataCriteria.path}'
//      /$


      def subq

//      subq = $/
//        SELECT dvi.id
//        FROM DataValueIndex dvi
//        WHERE
//         dvi.archetypeId = '${archetypeId}' AND
//         dvi.archetypePath = '${archetypePath}'
//      /$
//      println "SUBQ DVI: "+ subq
//      println DataValueIndex.executeQuery(subq)
//
//
//      // JOINs dvi and dqi to compare the field value.
//      // THIS DOESNT WORK I NEED TO JOIN THE SUBCLASS EXPLICITLY!!!
//      subq = $/
//        SELECT dvi.id
//        FROM DataValueIndex dvi
//        WHERE
//         dvi.archetypeId = '${archetypeId}' AND
//         dvi.archetypePath = '${archetypePath}' AND
//         dvi.magnitude > 10.0
//      /$
//      println "SUBQ DVI: "+ subq
//      println DataValueIndex.executeQuery(subq)
//
//
//
//
//      // JOINs dvi and dqi to compare the field value.
//      subq = $/
//        SELECT dvi.id
//        FROM DataValueIndex dvi, DvQuantityIndex dqi
//        WHERE
//         dvi.archetypeId = '${archetypeId}' AND
//         dvi.archetypePath = '${archetypePath}' AND
//         dvi.id = dqi.id AND
//         dqi.magnitude > 10.0
//      /$
//      println "SUBQ DVI: "+ subq
//      println DataValueIndex.executeQuery(subq)
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
        params['ehrs'] = Ehr.findAllByOrganizationUid(session.organization.uid)
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

      // TODO: make with criteria to get just the values and unique ones
      params['composerUids'] = DoctorProxy.createCriteria().list {
         projections {
            distinct("value")
         }
         isNotNull("value")
      }

      return params
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

   def show(String uid)
   {
      if (!uid)
      {
         flash.message = 'query.execute.error.queryUidMandatory'
         redirect(action:'list')
         return
      }

      def queryInstance = Query.findByUid(uid)

      if (!queryInstance)
      {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), uid])
         redirect(action: "list")
         return
      }

      [queryInstance: queryInstance]
   }


   def delete(String uid)
   {
      if (!uid)
      {
         flash.message = 'query.execute.error.queryUidMandatory'
         redirect(action:'list')
         return
      }

      def queryInstance = Query.findByUid(uid)

      if (!queryInstance)
      {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), uid])
         redirect(action: "list")
         return
      }

      try
      {
         //queryInstance.delete(flush: true)
         queryInstance.isDeleted = true
         queryInstance.save(flush: true)
         flash.message = message(code: 'default.deleted.message', args: [message(code: 'query.label', default: 'Query'), uid])
         redirect(action: "list")
      }
      catch (DataIntegrityViolationException e)
      {
         flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'query.label', default: 'Query'), uid])
         redirect(action: "show", params: [uid: uid])
      }
   }


   def getArchetypesInTemplate(String template_id)
   {
      def list = ArchetypeIndexItem.withCriteria {

         parentOpts {
            and {
               eq('templateId', template_id)
               eq('organizationUid', session.organization.uid)
            }
         }

         eq('path', '/')

         order("archetypeId", "asc")
      }

      render(text:(list as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
   }

   /**
    * Devuelve una lista de ArchetypeIndexItem.
    *
    * se usa en query create cuando el usuario selecciona el arquetipo
    * esta accion le devuelve los indices definidos para ese arquetipo
    * con: path, nombre, tipo rm, ...
    */
   def getArchetypePaths(String template_id, String archetypeId, boolean datatypesOnly)
   {
      def datatypes = DataValues.valuesStringList()

      def list = ArchetypeIndexItem.withCriteria {
         eq 'archetypeId', archetypeId

         if (datatypesOnly)
         {
           'in'('rmTypeName', datatypes)
         }

         parentOpts {
            and {
               eq('templateId', template_id)
               eq('organizationUid', session.organization.uid)
            }
         }

         order("path", "asc")
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
        case 'DV_DATE':
         res = DataCriteriaDV_DATE.criteriaSpec(archetypeId, path)
        break
        case 'DV_IDENTIFIER':
         res = DataCriteriaDV_IDENTIFIER.criteriaSpec(archetypeId, path)
        break
        case 'DV_MULTIMEDIA':
         res = DataCriteriaDV_MULTIMEDIA.criteriaSpec(archetypeId, path)
        break
        case 'DV_PARSABLE':
         res = DataCriteriaDV_PARSABLE.criteriaSpec(archetypeId, path)
        break
        case 'String':
         res = DataCriteriaString.criteriaSpec(archetypeId, path)
        break
      }

      render(text:(res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
   }

   /**
    * AJAX call from query builder to validate snomed expressions used as criteria values.
    */
   def validateSnomedExpression(String snomedExpr)
   {
      def valid = querySnomedService.validateExpression(snomedExpr)

      render(text:([is_valid: valid, snomed_expression: snomedExpr] as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
   }

   /**
    * Shows the query on it's JSON or XML form.
    */
   def export(String uid)
   {
      if (!uid)
      {
         flash.message = 'query.execute.error.queryUidMandatory'
         redirect(action:'list')
         return
      }

      def q = Query.findByUid(uid)
      def criteriaMap, _value

      // TODO: this code should be reused in RestConrtoller.queryList
      withFormat {
         xml {
            render(text: q.getXML(), contentType: "text/xml")
         }
         json {
            render(text: q.getJSON(), contentType: "application/json")
         }
         html {
            return "format not supported"
         }
      }
   }

   // TOOD: move to query group controller

   // query group list
   def groups()
   {
      def groups = QueryGroup.findAllByOrganizationUid(session.organization.uid)

      render view: '/queryGroup/index', model: [groups: groups]
   }

   // query group create
   def createGroup()
   {
      if (!params.doit)
      {
         render view: '/queryGroup/create'
         return
      }

      def qg = new QueryGroup(params)
      qg.organizationUid = session.organization.uid

      if (!qg.save()) println qg.errors

      redirect (action: "groups")
   }

   def showGroup(String uid)
   {
      def qg = QueryGroup.findByUid(uid)

      render view: '/queryGroup/show', model: [queryGroupInstance: qg]
   }

   def editGroup(String uid)
   {
      def qg = QueryGroup.findByUid(uid)

      if (!params.doit)
      {
         render view: '/queryGroup/edit', model: [queryGroupInstance: qg]
         return
      }

      qg.name = params.name // the only field that can change is the name

      if (!qg.save()) println qg.errors

      redirect (action: "showGroup", params: [uid: uid])
   }

   def executeCountGroup(String uid)
   {
      def qg = QueryGroup.findByUid(uid)

      def res = qg.executeCount(session.organization.uid)
      render (res as JSON)
   }
}
