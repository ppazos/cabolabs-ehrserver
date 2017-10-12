package com.cabolabs.ehrserver.query

import grails.transaction.Transactional

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.POST

@Transactional
class QuerySnomedService {

   def getCodesFromExpression(String snomedExpr)
   {
      def res = []
      
      def http = new HTTPBuilder('http://veratechnas1.synology.me:6699')
      
      http.request( POST ) {
         uri.path = '/SnomedQuery/ws/JSONQuery'
         uri.query = [cache: 'true']
         send URLENC, [query: snomedExpr]
         headers.Accept = 'application/json'
       
         response.success = { resp, json ->
            println "POST Success: ${resp.statusLine}" // POST Success: HTTP/1.1 200 OK
            //println resp.statusLine.statusCode // 200
            //println json.getClass() // class net.sf.json.JSONArray
           
            json.each { item ->
               //println item.idconcept +' '+ item.concept
               
               res << item.idconcept
            }
         }
         
         response.failure = { resp, reader ->
            println 'request failed'
            println resp
            println resp.statusLine
            println resp.status
            println reader.text
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
