package parsers

// grails test-app me dice que no encuentra la lib...
import com.thoughtworks.xstream.XStream
import grails.test.mixin.*
import org.junit.*

import common.change_control.Contribution
import common.change_control.Version
import common.generic.DoctorProxy
import common.generic.AuditDetails
import support.identification.VersionRef
import support.identification.ContributionRef
import support.identification.CompositionRef

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(XmlService)
class XmlServiceTests { //  extends GroovyTestCase

   //def xmlService
   
   void testContribution()
   {
      //fail "Implement me"
      def file = new File("test\\resources\\contribution.xml")
      def xml = file.getText()
      
      //def contribution = xmlService.parseConstribution()
      def contribution = service.parseContribution(xml)
      
      XStream xstream = new XStream()
      xstream.omitField(Contribution.class, "errors")
      xstream.omitField(DoctorProxy.class, "errors")
      xstream.omitField(AuditDetails.class, "errors")
      xstream.omitField(VersionRef.class, "errors")
      //xstream.omitField(DoctorProxy.class, "errors")
      
      String txt = xstream.toXML(contribution)
      
      println txt
   }
   
   void testVersion()
   {
      def file = new File("test\\resources\\version.xml")
      def xml = file.getText()
      
      List data = []
      def version = service.parseVersion(xml, data)
      
      XStream xstream = new XStream()
      xstream.omitField(Version.class, "errors")
      xstream.omitField(CompositionRef.class, "errors")
      xstream.omitField(DoctorProxy.class, "errors")
      xstream.omitField(AuditDetails.class, "errors")
      xstream.omitField(ContributionRef.class, "errors")
      
      println xstream.toXML(version)
      
      //println xstream.toXML(data) data[0] es un GPathResult parseado con XmlSlurper
      
      /*
      def stringWriter = new StringWriter()
      new XmlNodePrinter(new PrintWriter(stringWriter)).print(data[0])
      println stringWriter.toString()
      */
      
      println groovy.xml.XmlUtil.serialize( data[0] )
   }
}