package com.cabolabs.ehrserver.query

import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.json.JSONObject
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.util.DateParser

@Transactional
class QueryService {

   def grailsApplication

   /**
    * json contains the query and query parameters that are used from the
    * query builder when testing the query and might be used by a service.
    */
   def executedNotStoredCompositionQuery(JSONObject json, String orgUid)
   {
      def result = [:]
      result['result'] = []
   
      // http://mrhaki.blogspot.com/2009/11/groovy-goodness-convert-string-to.html
      boolean retrieveData = json.retrieveData ? json.retrieveData.toBoolean() : false
      boolean showUI       = json.showUI ? json.showUI.toBoolean() : false
      String qehrId        = json.qehrId
      String fromDate      = json.fromDate
      String toDate        = json.toDate
      String qarchetypeId  = json.qarchetypeId
      String format        = json.format
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
}
