package com.cabolabs.ehrserver.parsers

// grails test-app me dice que no encuentra la lib...
//import com.thoughtworks.xstream.XStream
import grails.test.mixin.*
import org.junit.*
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.parsers.XmlService;

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(XmlService)
@Mock([XmlService, Ehr, PatientProxy, Contribution, VersionedComposition, Version, CompositionIndex, AuditDetails])
class XmlServiceTests { //  extends GroovyTestCase
   
   static String PS = System.getProperty("file.separator")
   
   void testVersion()
   {
      service.xmlValidationService = new com.cabolabs.ehrserver.parsers.XmlValidationService()
      
      def file = new File("test"+ PS +"resources"+ PS +"commit"+ PS +"test_commit_1.xml")
      def xml = file.getText()
      def slurper = new XmlSlurper(false, false)
      def _parsedVersions = slurper.parseText(xml)
      
      def ehr = new Ehr(
         subject: new PatientProxy(
            value: '1234-12341-1341'
         ),
         organizationUid: '1234-1234-1234'
      )
      if (!ehr.save()) println ehr.errors
      
      // shouldn't fail
      service.processCommit(ehr, _parsedVersions, 'systemID', new Date(), 'Mr. Committer')
      
      
      def contribution = Contribution.get(1)

      assert contribution != null
      assert contribution.versions != null
      assert contribution.versions.size() == 1

      
	  /*
      XStream xstream = new XStream()
      xstream.omitField(Version.class, "errors")
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
      
      //println groovy.xml.XmlUtil.serialize( data[0] )
   }
}
