package parsers

// grails test-app me dice que no encuentra la lib...
//import com.thoughtworks.xstream.XStream
import grails.test.mixin.*
import org.junit.*

import common.change_control.*
import common.generic.AuditDetails
import ehr.clinical_documents.CompositionIndex
import ehr.Ehr
import common.generic.PatientProxy

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(XmlService)
@Mock([XmlService, Ehr, PatientProxy, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails])
class XmlServiceTests { //  extends GroovyTestCase
   
   void testVersion()
   {
      def file = new File("test\\resources\\version.xml")
      def xml = file.getText()
      
      def ehr = new Ehr(
         subject: new PatientProxy(
            value: '1234-12341-1341'
         )
      )
      if (!ehr.save()) println ehr.errors
      
      List data = []
      def version = service.parseVersions(ehr, [xml],
          'systemID', new Date(), 'Mr. Committer', data)
      
	  /*
      XStream xstream = new XStream()
      xstream.omitField(Version.class, "errors")
      //xstream.omitField(CompositionRef.class, "errors") // T0004
      xstream.omitField(CompositionIndex.class, "errors")
      xstream.omitField(DoctorProxy.class, "errors")
      xstream.omitField(AuditDetails.class, "errors")
      
      println xstream.toXML(version)
      */
	  
      //println xstream.toXML(data) data[0] es un GPathResult parseado con XmlSlurper
      
      /*
      def stringWriter = new StringWriter()
      new XmlNodePrinter(new PrintWriter(stringWriter)).print(data[0])
      println stringWriter.toString()
      */
      
      println groovy.xml.XmlUtil.serialize( data[0] )
   }
}