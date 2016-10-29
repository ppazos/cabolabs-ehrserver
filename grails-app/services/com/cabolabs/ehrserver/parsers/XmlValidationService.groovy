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
         //Holders.applicationContext
         //Holders.grailsApplication.parentContext.getResource("classpath:$filePath")
         
         // getResource returns a ServletContextResource
         def xsdInputStream = Holders.grailsApplication.parentContext.getResource(xsdPath).inputStream
         //def xsdInputStream = this.getClass().getResourceAsStream(System.getProperty("file.separator") + xsdPath)
         
         // reserouce on xsd\Version.xsd = null
         println "reserouce on "+ xsdPath +" = "+ xsdInputStream
         
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
