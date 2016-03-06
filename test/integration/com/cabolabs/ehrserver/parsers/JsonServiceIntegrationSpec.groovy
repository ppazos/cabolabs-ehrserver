package com.cabolabs.ehrserver.parsers

import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper

class JsonServiceIntegrationSpec extends IntegrationSpec {

   def jsonService
   
   private static String PS = System.getProperty("file.separator")
   
   def setup()
   {
   }

   def cleanup()
   {
   }

   void "XML version to JSON"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1.xml'
         def xml = new File(path).text
      
      when:
         def jsonString = jsonService.xmlToJson(xml)
         def jsonSlurper = new JsonSlurper()
         def json = jsonSlurper.parseText(jsonString)
      
      then:
         assert jsonString != null
         assert json.version."@xsi:type" == "ORIGINAL_VERSION"
   }
}
