package com.cabolabs.ehrserver.parsers

import de.odysseus.staxon.json.JsonXMLConfig
import de.odysseus.staxon.json.JsonXMLConfigBuilder
import de.odysseus.staxon.json.JsonXMLOutputFactory

import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

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
}
