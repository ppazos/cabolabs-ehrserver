package com.cabolabs.ehrserver.parsers

import grails.test.spock.IntegrationSpec

class XmlValidationServiceIntegrationSpec extends IntegrationSpec {

   def xmlValidationService
   
   private static String PS = System.getProperty("file.separator")
   
   def setup()
   {
   }

   def cleanup()
   {
   }

   void "validate OPT"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'opts'+PS+'Encuentro.opt'
         def opt = new File(path).text
      
      when:
         def valid = xmlValidationService.validateOPT(opt)
      
      then:
         assert valid
   }
   
   void "validate invalid OPT"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'opts'+PS+'Encuentro_invalid.opt'
         def opt = new File(path).text
      
      when:
         def valid = xmlValidationService.validateOPT(opt)
      
      then:
         assert !valid
         assert xmlValidationService.getErrors().size() > 0
   }
   
   void "validate version"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1.xml'
         def xml = new File(path).text
         def slurper = new XmlSlurper(false, false)
         def parsedVersion = slurper.parseText(xml)
         def namespaces = [:]
      
      when:
         def valid = xmlValidationService.validateVersion(parsedVersion, namespaces)
      
      then:
         assert valid
   }
   
   void "validate invalid version"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1_invalid.xml'
         def xml = new File(path).text
         def slurper = new XmlSlurper(false, false)
         def parsedVersion = slurper.parseText(xml)
         def namespaces = [:]
      
      when:
         def valid = xmlValidationService.validateVersion(parsedVersion, namespaces)
      
      then:
         assert !valid
         assert xmlValidationService.getErrors().size() > 0
   }
   
   void "validate string version"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1.xml'
         def xml = new File(path).text
      
      when:
         def valid = xmlValidationService.validateVersion(xml)
      
      then:
         assert valid
   }
   
   void "validate invalid string version"()
   {
      setup:
         def path = 'test'+PS+'resources'+PS+'versions'+PS+'91cf9ded-e926-4848-aa3f-3257c1d89554_EMR_APP_1_invalid.xml'
         def xml = new File(path).text
      
      when:
         def valid = xmlValidationService.validateVersion(xml)
      
      then:
         assert !valid
         assert xmlValidationService.getErrors().size() > 0
   }
}
