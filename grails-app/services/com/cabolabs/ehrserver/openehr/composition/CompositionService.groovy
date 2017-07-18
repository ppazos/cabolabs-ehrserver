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

package com.cabolabs.ehrserver.openehr.composition

import grails.transaction.Transactional
import grails.util.Holders

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import com.cabolabs.ehrserver.parsers.JsonService
import com.cabolabs.ehrserver.versions.VersionFSRepoService

@Transactional
class CompositionService {

   def config = Holders.config.app
   def jsonService
   def versionFSRepoService
   
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
      
      // Throws FileNotFoundException
      def versionFile = versionFSRepoService.getExistingVersionFile(compoIndex.organizationUid, version)
      
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
      def xslt = new File(config.xslt)
      def xslt_content
      if (!xslt.exists()) // try to load from resources
      {
         xslt_content = Holders.grailsApplication.parentContext.getResource(config.xslt).inputStream.text
      }
      else
      {
         xslt_content = xslt.text
      }
      
      // Create transformer
      def transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslt_content)))
      
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
