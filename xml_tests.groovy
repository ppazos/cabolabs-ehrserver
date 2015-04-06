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

def benchmark = { closure ->  
  start = System.currentTimeMillis()  
  closure.call()  
  now = System.currentTimeMillis()  
  now - start  
}  

public class SimpleErrorHandler implements ErrorHandler {

   def xml_lines
   public SimpleErrorHandler(String xml) {
      this.xml_lines = xml.readLines()
   }

    public void warning(SAXParseException e) throws SAXException {
        println "WARNING: "+ e.getMessage() +" "+ e.getLineNumber() +" "+ this.xml_lines[e.getLineNumber()-1]  //e.getSystemId()
    }

    public void error(SAXParseException e) throws SAXException {
        println "ERROR: "+ e.getMessage() +" "+ e.getLineNumber()
        println this.xml_lines[e.getLineNumber()-1] // line of the problem in the XML
        (e.getColumnNumber()-1).times{ print " " } // marks the column
        println "^"
    }

    public void fatalError(SAXParseException e) throws SAXException {
        println "FATAL ERROR: "+ e.getMessage() +" "+ e.getLineNumber() +" " this.xml_lines[e.getLineNumber()-1]
    }
}

def duration = benchmark {

    def path = 'C:\\Documents and Settings\\pab\\My Documents\\GitHub\\cabolabs-emrapp\\committed\\composition_3918_0_err.xml'
    def xml = new File(path).text
    
    //println xml
    
    // XSD Validation: 2 ways, parsing or with validator. The result is the same, less code with validator.
    
    // Validate with parse 1
    SAXParserFactory factory = SAXParserFactory.newInstance()
    factory.setValidating(false)
    factory.setNamespaceAware(true)
    
    // Common code
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    Schema schema = schemaFactory.newSchema(
        [ new StreamSource("C:\\Documents and Settings\\pab\\My Documents\\GitHub\\cabolabs-ehrserver\\xsd\\Version.xsd") ] as Source[]
      )
      
    // VAlidate with parse 2
    factory.setSchema( schema )
    SAXParser parser = factory.newSAXParser()
    XMLReader reader = parser.getXMLReader()
    reader.setErrorHandler(new SimpleErrorHandler(xml))
    reader.parse(new InputSource(new StringReader(xml)))
    
    // Validate with validator
//    Validator validator = schema.newValidator()
//    validator.setErrorHandler(new SimpleErrorHandler(xml))
//    validator.validate(new StreamSource(new StringReader(xml)))
}

println "execution took ${duration} ms"  