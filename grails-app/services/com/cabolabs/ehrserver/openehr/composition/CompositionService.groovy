package com.cabolabs.ehrserver.openehr.composition

import grails.transaction.Transactional
import grails.util.Holders

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import com.cabolabs.ehrserver.parsers.JsonService

@Transactional
class CompositionService {

   def config = Holders.config.app
   def jsonService
   
   def compositionAsXml(String uid)
   {
      if (!uid)
      {
         throw new Exception("uid is mandatory")
      }
      
      def compoIndex = CompositionIndex.findByUid(uid)
      
      if (!compoIndex)
      {
         throw new Exception("Composition doesn't exists")
      }
      
      def version = compoIndex.getParent()
      def versionFile = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml")
      
      if (!versionFile.exists())
      {
         throw new Exception("Composition document doesn't exists")
      }
      
      def xml = versionFile.getText()
      
      return xml
   }
   
   def compositionAsJson(String uid)
   {
      def xml = compositionAsXml(uid)
      
      def json = jsonService.xmlToJson(xml)
      
      return json
   }
   
   def compositionAsHtml(String uid)
   {
      def xml = compositionAsXml(uid)
      
      // Transform to HTML
      def xslt = new File(config.xslt).getText()
      
      // Create transformer
      def transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslt)))
      
      def html = new StringWriter()
      
      // Perform transformation
      transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(html))
      
      return html.toString()
   }
   
   /*
   private void toHtml(GPathResult n, MarkupBuilder builder, String classPath)
   {
      // TODO: clases que sean clase.atributo del RM
      // (ej. OBS.data, HIST.events), asi puedo definir estilos por
      // atributo del RM.
      //
      // TODO: class por tipo del RM.
      //
      // necesito consultar el arquetipo para poder hacerlo o puedo consultar los ArchetypeIndexItem (temporal)
      
      if (n.children().isEmpty())
      {
         builder.div( class:'single_value', n.text() )
      }
      else
      {
         builder.div( class:classPath ) { // TODO: class = rmTypeName
            n.children().each { sn ->
               toHtml(sn, builder, classPath +'_'+ sn.name())
            }
         }
      }
   }
   */
}
