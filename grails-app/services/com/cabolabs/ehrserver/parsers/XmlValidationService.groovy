
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

import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import javax.xml.XMLConstants

import org.xml.sax.ErrorHandler
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException

import grails.util.Holders
import groovy.util.slurpersupport.GPathResult
import grails.util.Holders

class XmlValidationService {

   def errors = []
   
   
   public boolean validateOPT(String xml)
   {
      return this.validate(xml, Holders.config.app.opt_xsd)
   }
   
   public boolean validateVersion(GPathResult xml, Map namespaces)
   {
      //xml.'@xmlns' = 'http://schemas.openehr.org/v1'
      //xml.'@xmlns:xsi' = 'http://www.w3.org/2001/XMLSchema-instance'
      namespaces.each { ns, val ->
         xml."@$ns" = val
      }
      def xmlStr = groovy.xml.XmlUtil.serialize( xml )
      
      return this.validate(xmlStr, Holders.config.app.version_xsd)
   }
   
   public boolean validateVersion(String xml)
   {
      return this.validate(xml, Holders.config.app.version_xsd)
   }
   
   
   public List<String> getErrors()
   {
      return this.errors
   }
   
   
   private boolean validate(String xml, String xsdPath)
   {
      this.errors = [] // Reset the errors for reuse
      
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      Schema schema
      
      def xsd = new File(xsdPath)
      if (!xsd.exists()) // try to load from resources
      {
         // getResource returns a ServletContextResource
         def xsdInputStream = Holders.grailsApplication.parentContext.getResource(xsdPath).inputStream

         // resource on xsd\Version.xsd = null
         println "resource on "+ xsdPath +" = "+ xsdInputStream
         
         schema = schemaFactory.newSchema( [ new StreamSource( xsdInputStream ) ] as Source[] )
      }
      else
         schema = schemaFactory.newSchema( [ new StreamSource( xsdPath ) ] as Source[] )
      
      // Validate with validator
      Validator validator = schema.newValidator()
      ErrorHandler errorHandler = new SimpleErrorHandler(xml)
      validator.setErrorHandler(errorHandler)
      
      try
      {
         validator.validate(new StreamSource(new StringReader(xml)))
      }
      catch (org.xml.sax.SAXParseException e) // XML not valid
      {
         errorHandler.exceptions << e
      }
      
      this.errors = errorHandler.getErrors()
      
      return !errorHandler.hasErrors() // If validates is false, then the user can .getErrors()
   }
   
   
   private class SimpleErrorHandler implements ErrorHandler {
     
      def exceptions = []
      def xml_lines
      
      public SimpleErrorHandler(String xml)
      {
         this.xml_lines = xml.readLines()
      }
   
      public void warning(SAXParseException e) throws SAXException
      {
         this.exceptions << e
      }
   
      public void error(SAXParseException e) throws SAXException
      {
         this.exceptions << e
      }
   
      // if a fatal error occurs, then this stop validating
      public void fatalError(SAXParseException e) throws SAXException
      {
         this.exceptions << e
      }
      
      public boolean hasErrors()
      {
         return this.exceptions.size() > 0
      }
      
      public List<String> getErrors()
      {
         def ret = []
         this.exceptions.each { e ->
            
            ret << "ERROR "+ e.getMessage() +"\nline #: "+ e.getLineNumber() +"\n>>> "+
                   this.xml_lines[e.getLineNumber()-1].trim() // line of the problem in the XML
            //        (e.getColumnNumber()-1).times{ print " " } // marks the column
            //        println "^"
         }
         return ret
      }
   }
}
