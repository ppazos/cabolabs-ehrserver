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

   def "xml2JsonV2 test 1"()
   {
      when:
         def xml = '''<root>
            <child>one</child>
            <child>two</child>
            <other>ooops</other>
            <complex>
              <single>
                <s1>1</s1>
              </single>
              <single>
                <s1>2</s1>
              </single>
            </complex>
         </root>'''

         def json = jsonService.xml2JsonV2(xml, true)

         println json

      then:
         assert json != null
   }

   def "json2XmlV2 test 1"()
   {
      when:
         def json = '''{
               "root":{
                  "child":[
                     {"$":"one"},
                     {"$":"two"}
                  ],
                  "other":{
                     "$":"ooops"
                  },
                  "complex":{
                     "single":[
                        {
                           "s1":{
                              "$":"1"
                           }
                        },
                        {
                           "s1":{
                              "$":"2"
                           }
                        }
                     ]
                  }
               }
            }'''

         def xml = jsonService.json2XmlV2(json, true)

         println xml

      then:
         assert xml != null
   }

   def "xml2JsonV2 test 2"()
   {
      when:
         def xml = '''<root>
                       <single>
                         <s1>1</s1>
                       </single>
                       <single>
                         <s1>2</s1>
                       </single>
                     </root>'''

         def json = jsonService.xml2JsonV2(xml, true)

         println json

      then:
         assert json != null
   }

   def "json2XmlV2 test 2"()
   {
      when:
         def json = '''{
                  "root":{
                     "single":[
                        {
                           "s1":{
                              "$":"1"
                           }
                        },
                        {
                           "s1":{
                              "$":"2"
                           }
                        }
                     ]
                  }
               }'''

         def xml = jsonService.json2XmlV2(json, true)

         println xml

      then:
         assert xml != null
   }

   def "xml2JsonV2 test 3"()
   {
      when:
         def xml = '''<root>
           <single id="1">
             <s1>1</s1>
           </single>
           <single id="2">
             <s1>2</s1>
           </single>
         </root>'''

         def json = jsonService.xml2JsonV2(xml, true)

         println json

      then:
         assert json != null
   }

   def "json2XmlV2 test 3"()
   {
      when:
         def json = '''{
                  "root":{
                     "single":[
                        {
                           "@id":"1",
                           "s1":{
                              "$":"1"
                           }
                        },
                        {
                           "@id":"2",
                           "s1":{
                              "$":"2"
                           }
                        }
                     ]
                  }
               }'''

         def xml = jsonService.json2XmlV2(json, true)

         println xml

      then:
         assert xml != null
   }

   def "xml2JsonV2 test 4"()
   {
      when:
         def xml = '''<root>
           <single id="1">
             <s1 id="3">1</s1>
           </single>
           <single id="2">
             <s1 id="4">2</s1>
           </single>
         </root>'''

         def json = jsonService.xml2JsonV2(xml, true)

         println json

      then:
         assert json != null
   }

   def "json2XmlV2 test 4"()
   {
      when:
         def json = '''{
            "root":{
               "single":[
                  {
                     "@id":"1",
                     "s1":{
                        "@id":"3","$":"1"
                     }
                  },
                  {
                     "@id":"2",
                     "s1":{
                        "@id":"4",
                        "$":"2"
                     }
                  }
               ]
            }
         }'''

         def xml = jsonService.json2XmlV2(json, true)

         println xml

      then:
         assert xml != null
   }
}
