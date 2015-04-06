import javax.xml.parsers.SAXParserFactory
import javax.xml.validation.SchemaFactory
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.ErrorHandler
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.parsers.SAXParser
import org.xml.sax.XMLReader
import org.xml.sax.InputSource
import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.Validator

class VersionValidator {

   def errors = []
   
   public boolean validate(String xml)
   {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      Schema schema = schemaFactory.newSchema(
        [ new StreamSource("C:\\Documents and Settings\\pab\\My Documents\\GitHub\\cabolabs-ehrserver\\xsd\\Version.xsd") ] as Source[]
      )

      // Validate with validator
      Validator validator = schema.newValidator()
      ErrorHandler errorHandler = new SimpleErrorHandler(xml)
      validator.setErrorHandler(errorHandler)
      validator.validate(new StreamSource(new StringReader(xml)))
      
      this.errors = errorHandler.getErrors()
      
      return !errorHandler.hasErrors() // If validate() (==!hasErrors) is false, then the user can .getErrors()
   }
   
   public List<String> getErrors()
   {
      return this.errors
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
         this.exceptions.eachWithIndex { e, i ->
            
            ret << "ERROR "+ i +") "+ e.getMessage() +"\nline #: "+ e.getLineNumber() +"\n>>> "+
                   this.xml_lines[e.getLineNumber()-1].trim() // line of the problem in the XML
            //        (e.getColumnNumber()-1).times{ print " " } // marks the column
            //        println "^"
         }
         return ret
      }
   }
}

def path = 'C:\\Documents and Settings\\pab\\My Documents\\GitHub\\cabolabs-emrapp\\committed\\composition_3918_0_err.xml'
def xml = new File(path).text
def validator = new VersionValidator()
if (!validator.validate(xml))
{
   validator.getErrors().each { println it }
}
else
{
   println "iuju!!!"
}
