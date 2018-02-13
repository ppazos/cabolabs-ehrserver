package com.cabolabs.ehrserver.conf

import grails.test.mixin.TestFor
import spock.lang.Specification
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ConfigurationItem)
class ConfigurationItemSpec extends Specification {

   def setup() {
   }

   def cleanup() {
   }

   void "test validation OK"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'abc', type:'string', blank:false, description:'my config item')
      then:
         conf.validate()
   }
   
   void "test validation OK number"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'123.5', type:'number', blank:false, description:'my config item')
      then:
         conf.validate()
   }
   
   void "test validation OK URL"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'https://www.cabolabs.com', type:'url', blank:false, description:'my config item')
      then:
         conf.validate()
   }
   
   void "test validation blank value error"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'', type:'string', blank:false, description:'my config item')
      then:
         !conf.validate()
   }
   
   void "test validation null value error"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'', type:'string', blank:false, description:'my config item')
      then:
         !conf.validate()
   }
   
   void "test validation wrong type error"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'abc', type:'date', blank:false, description:'my config item')
      then:
         !conf.validate()
   }
   
   void "test validation value type mismatch boolean"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'123', type:'boolean', blank:false, description:'my config item')
      then:
          conf.validate()
   }
   
   void "test validation value type mismatch number"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'abc', type:'number', blank:false, description:'my config item')
      then:
         !conf.validate()
   }
   
   void "test validation value type mismatch UUID"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'abc', type:'uuid', blank:false, description:'my config item')
      then:
         !conf.validate()
   }
   
   void "test validation value type mismatch OID"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'abc', type:'oid', blank:false, description:'my config item')
      then:
         !conf.validate()
   }
   
   void "test validation value type mismatch URL"()
   {
      when:
         def conf = new ConfigurationItem(key:'a.b.c', value:'abc', type:'url', blank:false, description:'my config item')
      then:
         !conf.validate()
   }
}
