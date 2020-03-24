/*
 * Copyright 2011-2020 CaboLabs Health Informatics
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

package com.cabolabs.ehrserver.parsers

import de.odysseus.staxon.json.JsonXMLConfig
import de.odysseus.staxon.json.JsonXMLConfigBuilder
import de.odysseus.staxon.json.JsonXMLOutputFactory

import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

import javax.xml.stream.XMLOutputFactory
import de.odysseus.staxon.json.JsonXMLInputFactory
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter

import groovy.json.JsonBuilder
import groovy.util.slurpersupport.GPathResult
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder

import groovy.util.slurpersupport.*

class JsonService {

   /***
    * Función encargada de convertir a json un string en xml
    * @param contenidoXml: Texto xml a pasar a Json
    * @rerturn texto enformato json que es devuelto por la función.
    ***/
   def xmlToJson(String contenidoXml)
   {
      log.info("Entra en función xmlToJson")
      InputStream input = new ByteArrayInputStream(contenidoXml.getBytes())
      ByteArrayOutputStream output = new ByteArrayOutputStream()
      JsonXMLConfig config = new JsonXMLConfigBuilder()
         .autoArray(true)
         .autoPrimitive(true)
         .prettyPrint(true)
         .build()

      try
      {
         /*
          * Create reader (XML).
          */
         XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input)
         /*
          * Create writer (JSON).
          */
         XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output)
         //writer.toString();
         /*
          * Copy events from reader to writer.
          */
         writer.add(reader)

         /*
          * Close reader/writer.
          */
         reader.close()
         writer.close()

         return output.toString()
      }
      finally
      {
         /*
          * As per StAX specification, XMLEventReader/Writer.close() doesn't close
          * the underlying stream.
          */
         // This executes even the return of the try is executed.
         output.close()
         input.close()
         log.info("Sale de función xmlToJson")
      }
   }

   // https://github.com/fandaqian/mogone-manager/blob/2184fd35c68285ddab4bcca271a6f5fe25d3e282/src/main/java/com/mogone/manager/util/StaxonUtils.java
   def json2xml(String json)
   {
      StringReader input = new StringReader(json)
      StringWriter output = new StringWriter()
      JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).repairingNamespaces(false).build()
      try
      {
         XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input)
         XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output)
         //writer = new PrettyXMLEventWriter(writer)
         writer.add(reader)
         reader.close()
         writer.close()
      }
      catch (Exception e)
      {
         e.printStackTrace()
         log.error("JsonService couldn't parse JSON 1 "+ e.getMessage())
      }
      finally
      {
         try
         {
            output.close()
            input.close()
         }
         catch (IOException e)
         {
            //e.printStackTrace()
            log.error("JsonService couldn't parse JSON 2")
         }
      }

      // TODO: remove using regex
      /*
      if (output.toString().length() >= 38) { //remove <?xml version="1.0" encoding="UTF-8"?>
            return output.toString().substring(39)
      }
      */
      return output.toString()
   }

   String xml2JsonV2(String xmlString, boolean prettyPrint = false)
   {
      // 2nd arg false makes namespaces appear as attributes on the root
      def xml = new XmlSlurper(false, false).parseText(xmlString)

      // test
      //def namespaceMap = xml.attributes().findAll { it.key.startsWith('xmlns') }
      //println namespaceMap

      // in/out param
      def jsonModel = [:]

      xml2JsonRecursive(jsonModel, xml)

      if (prettyPrint)
         return new JsonBuilder(jsonModel).toPrettyString()
      else
         return new JsonBuilder(jsonModel).toString()
   }

   def xml2JsonRecursive(Map jsonModel, GPathResult xml)
   {
      if (xml instanceof NodeChildren && xml.size() == 1)
      {
         xml = xml[0] // extracts NodeChild from NodeChildren when there is just one node
      }

      // Only NodeChildren will support the index operator
      if (xml instanceof NodeChild)
      {
         //println 'single name '+ xml.name()

         jsonModel[xml.name()] = [:]

         xml.attributes().each {
            jsonModel[xml.name()]['@'+it.key] = it.value
         }

         /*
         for:jsonContribution
         <root>
            <child>one</child>
            <child>two</child>
            <other>ooops</other>
         </root>

         childNodeNameRepeatMap = [child:2, other:1]
         */
         def childNodeNameRepeatMap = xml.children()*.name().groupBy{it}.collectEntries{ [(it.key): it.value.size()] }

         //println childNodeNameRepeatMap

         if (childNodeNameRepeatMap.size() == 0)
         {
            jsonModel[xml.name()]['$'] = xml.text()
         }

         childNodeNameRepeatMap.each { childName, repeat ->
            /*
            if (xml."${childName}".size() == 0)
               jsonModel[xml.name()][childName] = xml."${childName}".text() // TODO: allow date formatting and represent numbers without quotes (parse the string value).
            else
            */
            xml2JsonRecursive(jsonModel[xml.name()], xml."${childName}") // xml.childName can have more than one occurrence
         }
      }
      else
      {
         //println "multiple name "+ xml.name()
         jsonModel[xml.name()] = [] // multiple nodes

         // xml is multiple for two or more children with the same name
         xml.eachWithIndex { node, i ->

            if (!jsonModel[xml.name()][i]) jsonModel[xml.name()][i] = [:]

            node.attributes().each {
               jsonModel[xml.name()][i]['@'+it.key] = it.value
            }

            def childNodeNameRepeatMap = node.children()*.name().groupBy{it}.collectEntries{ [(it.key): it.value.size()] }
            //println childNodeNameRepeatMap

            /* this case, multiple child and single, generates a list: child: [1, 2]
            <parent>
             <child>1</child>
             <child>2</child>
            </parent>
            */
            if (childNodeNameRepeatMap.size() == 0)
            {
               //println "multiple text node"
               jsonModel[xml.name()][i]['$'] = node.text()
            }

            childNodeNameRepeatMap.eachWithIndex { childName, repeat, j ->

               //println "multiple complex node "+ childName
               xml2JsonRecursive(jsonModel[xml.name()][i], node."${childName}") // xml.childName can have more than one occurrence
            }
         }
      }

      /*
      xml.children().each {
         println it.name() +'/'+ it.size() +'/'+ it.children().size()
         //println it.name() +'/'+ it.getClass() +' '+ it.children().size()
         if (it.children().size() == 0)
            jsonModel[xml.name()][it.name()] = it.text() // TODO: allow date formatting and represent numbers without quotes (parse the string value).
         else
            xml2JsonRecursive(jsonModel[xml.name()], it)
      }
      */
   }

   String json2XmlV2(String jsonString, boolean prettyPrint = false)
   {
      def jsonSlurper = new JsonSlurper()
      def json = jsonSlurper.parseText(jsonString)

      def writer = new StringWriter()
      def xmlb

      if (prettyPrint)
         xmlb = new MarkupBuilder(writer)
      else
         xmlb = new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))


      xmlb.setDoubleQuotes(true)
      // println json.getClass() // LAzyMap

      json2XmlRecursive(json, xmlb)

      return writer.toString()
   }

   def json2XmlRecursive(Map json, MarkupBuilder xmlb)
   {
      def textNode

      json.keySet().each { name ->

         textNode = null

         // lists in xml should generate many xml elements with the same name
         // a : [ @id:1, @id:2 ] => <a id=1 /><a id=2/>
         if (json[name] instanceof List)
         {
            json[name].each { item ->

               textNode = null

               def attributes = item.collectEntries { it.key.startsWith('@') ? [(it.key - '@'): it.value] : [:] }
               def children = item.findAll{ !it.key.startsWith('@') && it.key != '$' }

               if (!children) // text node
               {
                  textNode = item['$']
                  xmlb."${name}"(attributes, textNode)
               }
               else
               {
                  xmlb."${name}"(attributes) {
                     json2XmlRecursive(children,  xmlb)
                  }
               }
            }
         }
         else
         {
            def attributes = json[name].collectEntries { it.key.startsWith('@') ? [(it.key - '@'): it.value] : [:] }
            def children = json[name].findAll{ !it.key.startsWith('@') && it.key != '$' }

            if (!children) // text node
            {
               textNode = json[name]['$']
               xmlb."${name}"(attributes, textNode)
            }
            else
            {
               xmlb."${name}"(attributes) {
                  json2XmlRecursive(children,  xmlb)
               }
            }
         }
      }
   }
}
