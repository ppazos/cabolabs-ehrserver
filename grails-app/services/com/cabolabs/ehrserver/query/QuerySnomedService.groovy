package com.cabolabs.ehrserver.query

import grails.transaction.Transactional

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.POST
import com.cabolabs.ehrserver.exceptions.QuerySnomedServiceException

@Transactional
class QuerySnomedService {

   def api_key = '942645e3-a305-4d0d-9de1-145d65d8b2c4'
   //def api_key = '22222222-2222-2222-2222-222222222222'

   // always throws exceptions on failing cases!
   def getCodesFromExpression(String snomedExpr)
   {
      def res = []
      def error = false
      def status
      def http = new HTTPBuilder('http://veratechnas1.synology.me:6699')

      try
      {
         http.request( POST ) {
            uri.path = '/SnomedQuery/ws/JSONQuery'
            uri.query = [cache: 'true']
            send URLENC, [query: snomedExpr]
            headers.Accept = 'application/json'
            headers.Authorization = 'Bearer '+ api_key // FIXME: get from config

            response.success = { resp, json ->
               println "POST Success: ${resp.statusLine}" // POST Success: HTTP/1.1 200 OK
               //println resp.statusLine.statusCode // 200
               //println json.getClass() // class net.sf.json.JSONArray

               json.each { item ->
                  //println item.idconcept +' '+ item.concept

                  res << item.idconcept
               }
            }

            // FIXME: log correctly
            // FIXME: throw exception based on status like 429 Too Many Requests, etc.
            response.failure = { resp, reader ->
               println 'request failed'
               println resp
               println resp.statusLine
               println resp.status
               println reader.text

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

      def http = new HTTPBuilder('http://veratechnas1.synology.me:6699')

      http.request( POST ) {
         uri.path = '/SnomedQuery/ws/validate'
         uri.query = [cache: 'true']
         send URLENC, [query: snomedExpr]
         headers.Accept = 'application/json'
         headers.Authorization = 'Bearer '+ api_key // FIXME: get from config

         response.success = { resp, json ->
            println "POST Success: ${resp.statusLine}" // POST Success: HTTP/1.1 200 OK
            valid = json[0] // [true] / [false]
         }

         response.failure = { resp, reader ->
            println 'request failed'
            println resp
            println resp.statusLine
            println resp.status
            println reader.text

            valid = false
         }
      }

      return valid
   }
}
