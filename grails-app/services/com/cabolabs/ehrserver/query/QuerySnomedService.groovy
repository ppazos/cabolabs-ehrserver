package com.cabolabs.ehrserver.query

import grails.transaction.Transactional

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.POST
import com.cabolabs.ehrserver.exceptions.QuerySnomedServiceException
import com.cabolabs.ehrserver.conf.ConfigurationItem

@Transactional
class QuerySnomedService {

   def base = 'https://snquery.veratech.es'

   // always throws exceptions on failing cases!
   def getCodesFromExpression(String snomedExpr)
   {
      def res = []
      def error = false
      def status
      def http = new HTTPBuilder(this.base)

      def api_key = ConfigurationItem.findByKey('ehrserver.query.snquery.auth_token')?.value ?: System.getenv('EHRSERVER_SNQUERY_KEY')

      try
      {
         http.request( POST ) {

            uri.path = '/ws/JSONQuery'
            uri.query = [cache: 'true']
            send URLENC, [query: snomedExpr]
            headers.Accept = 'application/json'
            headers.Authorization = 'Bearer '+ api_key

            response.success = { resp, json ->
               
               log.info "POST Success: ${resp.statusLine}" // POST Success: HTTP/1.1 200 OK
               //println resp.statusLine.statusCode // 200
               //println json.getClass() // class net.sf.json.JSONArray

               json.each { item ->

                  log.debug item.idconcept +' '+ item.concept

                  res << item.idconcept
               }
            }

            // FIXME: log correctly
            // FIXME: throw exception based on status like 429 Too Many Requests, etc.
            response.failure = { resp, reader ->

               log.error 'request failed'
               //println resp groovyx.net.http.HttpResponseDecorator
               log.error resp.statusLine.toString() // HTTP/1.1 429 Too Many Requests
               log.error resp.status.toString() // 429
               log.error reader.text.toString()

               status = resp.status

               error = true
            }
         }
      }
      catch (Exception e)
      {
         throw new QuerySnomedServiceException('querySnomedService.error.connectionError', e)
      }

      if (error)
      {
         if (status == 429)
         {
            throw new QuerySnomedServiceException('querySnomedService.error.tooManyRequests')
         }
         else
         {
            throw new QuerySnomedServiceException('querySnomedService.error.unknownError')
         }
      }

      return res
   }


   def validateExpression(String snomedExpr)
   {
      def valid = true

      def http = new HTTPBuilder(this.base)

      def api_key = ConfigurationItem.findByKey('ehrserver.query.snquery.auth_token')?.value ?: System.getenv('EHRSERVER_SNQUERY_KEY')

      http.request( POST ) {

         uri.path = '/ws/validate'
         uri.query = [cache: 'true']
         send URLENC, [query: snomedExpr]
         headers.Accept = 'application/json'
         headers.Authorization = 'Bearer '+ api_key

         response.success = { resp, json ->

            log.info "POST Success: ${resp.statusLine}" // POST Success: HTTP/1.1 200 OK

            valid = json[0] // [true] / [false]
         }

         response.failure = { resp, reader ->
            
            log.error 'request failed'
            //println resp groovyx.net.http.HttpResponseDecorator
            log.error resp.statusLine.toString() // HTTP/1.1 429 Too Many Requests
            log.error resp.status.toString() // 429
            log.error reader.text.toString()

            valid = false
         }
      }

      return valid
   }
}
