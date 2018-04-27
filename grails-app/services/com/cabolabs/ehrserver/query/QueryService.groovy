package com.cabolabs.ehrserver.query

import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.json.JSONObject
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.util.DateParser
import com.cabolabs.ehrserver.exceptions.VersionRepoNotAccessibleException

@Transactional
class QueryService {

   def grailsApplication
   def versionFSRepoService

   /**
    * json contains the query and query parameters that are used from the
    * query builder when testing the query and might be used by a service.
    */
   def executedNotStoredCompositionQuery(JSONObject json, String orgUid)
   {
      def result = [:]
      result['result'] = []

      String qehrId        = json.qehrId
      String fromDate      = json.fromDate
      String toDate        = json.toDate
      String qarchetypeId  = json.qarchetypeId

      int max              = json.max ?: 20
      int offset           = json.offset ?: 0

      String composerUid   = json.composerUid
      String composerName  = json.composerName

      def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')

      String organizationUid
      if (qehrId)
      {
         def ehr = Ehr.findByUid(qehrId)
         if (!ehr)
         {
            result['error'] = [message: g.message(code:'rest.error.ehr_doesnt_exists', args:[qehrId]),
                               code: '403',
                               status: 404]
            return result
         }

         organizationUid = ehr.organizationUid
      }
      else
      {
         // use the orguid of the org used to login
         organizationUid = orgUid
      }


      // parse de dates
      Date qFromDate
      Date qToDate

       // verify parsability and return errors, see: https://github.com/ppazos/cabolabs-ehrserver/wiki/API-error-codes-and-messages
      if (fromDate)
      {
         qFromDate = DateParser.tryParse(fromDate)
         if (!qFromDate)
         {
            result['error'] = [message: g.message(code:'rest.error.invalid_format', args:['fromDate', fromDate]),
                               code: '479',
                               status: 400]
            return result
         }
      }

      if (toDate)
      {
         qToDate = DateParser.tryParse(toDate)
         if (!qToDate)
         {
            result['error'] = [message: g.message(code:'rest.error.invalid_format', args:['toDate', toDate]),
                               code: '480',
                               status: 400]
            return result
         }
      }

      if (qFromDate && qToDate && qFromDate > qToDate)
      {
         result['error'] = [message: g.message(code:'rest.error.from_bigger_than_to', args:[fromDate, toDate]),
                            code: '481',
                            status: 400]
         return result
      }

      def json_query = json.query
      json_query.organizationUid = organizationUid
      def query = Query.newInstance(json_query)

      def cilist = query.executeComposition(qehrId, qFromDate, qToDate, organizationUid, max, offset, composerUid, composerName)

      result['result'] = cilist

      return result
   }

   /**
    * result can be list or map, map is when qehrid is null and the results are grouped by EHR.
    * qehrid can be null.
    */
   String retrieveDataFromCompositionQueryResult(Object result, String qehrId, String orgUid)
   {
      // TODO: use string builder append instead of +
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
      def version
      String buff
      String out = '<?xml version="1.0" encoding="UTF-8"?><list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">\n'
      def vf
      if (!qehrId) // group by ehrUid
      {
         result.each { ehrUid, compoIndexes ->

             out += '<ehr uid="'+ ehrUid +'">'

             // idem else, TODO refactor
             compoIndexes.each { compoIndex ->

                // FIXME: verificar que esta en disco, sino esta hay un problema
                //        de sincronizacion entre la base y el FS, se debe omitir
                //        el resultado y hacer un log con prioridad alta para ver
                //        cual fue el error.

                // adds the version, not just the composition
                version = compoIndex.getParent()

                try
                {
                   vf = versionFSRepoService.getExistingVersionFile(orgUid, version)
                   buff = vf.getText()
                }
                catch (VersionRepoNotAccessibleException e)
                {
                   log.warning e.message
                   return // continue with next compoIndex
                }
                catch (FileNotFoundException e)
                {
                   log.warning e.message
                   return // continue with next compoIndex
                }

                buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
                buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
                buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')

                /**
                 * Composition queda:
                 *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
                 */

                out += buff + "\n"
             }
             out += '</ehr>'
         }
      }
      else
      {
         result.each { compoIndex ->

            // FIXME: verificar que esta en disco, sino esta hay un problema
            //        de sincronizacion entre la base y el FS, se debe omitir
            //        el resultado y hacer un log con prioridad alta para ver
            //        cual fue el error.

            // adds the version, not just the composition
            version = compoIndex.getParent()

            try
            {
               vf = versionFSRepoService.getExistingVersionFile(orgUid, version)
               buff = vf.getText()
            }
            catch (VersionRepoNotAccessibleException e)
            {
               log.warning e.message
               return // continue with next compoIndex
            }
            catch (FileNotFoundException e)
            {
               log.warning e.message
               return // continue with next compoIndex
            }

            buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
            buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
            buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')

            /**
             * Composition queda:
             *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
             */

            out += buff + "\n"
         }
      }
      out += '</list>'

      return out
   }
}
