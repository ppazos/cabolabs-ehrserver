/*
 * Copyright 2011-2017 CaboLabs Health Informatics
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
         //e.printStackTrace()
         log.error("JsonService couldn't parse JSON 1")
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
}
