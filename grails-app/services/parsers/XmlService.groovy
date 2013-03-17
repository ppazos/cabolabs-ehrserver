package parsers

import com.thoughtworks.xstream.XStream
import common.change_control.Contribution
import common.change_control.Version
import common.generic.AuditDetails
import common.generic.DoctorProxy
import groovy.util.slurpersupport.GPathResult
import support.identification.CompositionRef

class XmlService {

   /*
   <version>
     <!-- OBJECT_REF -->
     <contribution>
       <id>
         <value></value>
       </id>
       <namespace></namespace>
       <type></type>
     </contribution>
     
     <!-- AUDIT_DETAILS -->
     <commit_audit>
       <system_id></system_id>
       
       <!-- DV_DATE_TIME -->
       <time_committed>
         <value></value>
       </time_committed>
       
       <!-- DV_CODED_TEXT -->
       <change_type>
         <value>creation</value>
         <defining_code>
           <terminology_id>
             <value>openehr</value>
           </terminology_id>
           <code_string>249</code_string>
         </defining_code>
       </change_type>
       
       <!-- PARTY_IDENTIFIED -->
       <committer>
         <name></name>
       </committer>
     </commit_audit>
     
     <!-- OBJECT_VERSION_ID -->
     <uid>
       <value>object_id::creating_system_id::version_tree_id</value>
     </uid>
     
     <!-- COMPOSITION -->
     <data>
     ...
     </data>
     
     <!-- DV_CODED_TEXT -->
     <lifecycle_state>
       <value>completed</value>
       <defining_code>
         <terminology_id>
           <value>openehr</value>
         </terminology_id>
         <code_string>532</code_string>
       </defining_code>
     </lifecycle_state>
   </version>
   */
   def parseVersions(List<String> versionsXML, List dataOut)
   {
      //new File('debug_xml.log') << versionsXML.toString()
      
      List<Version> ret = []
      
      // Uso una lista para no reutilizar la misma variable que sobreescribe
      // las versions anteriors y me deja varias copias de la misma composition
      // en dataOut (quedan todos los punteros a la ultima que se procesa)
      def parsedVersion
      def commitAudit
      def data
      def version
      versionsXML.eachWithIndex { versionXML, i ->
      
         // Sin esto pone tag0 como namespace en todas las tags!!!
         parsedVersion = new XmlSlurper(true, false).parseText(versionXML)
         
         commitAudit = new AuditDetails(
            systemId:      'ISIS_EHR_SERVER', //parsedVersion.commit_audit.system_id.text(),
            timeCommitted: new Date(),
            changeType:    parsedVersion.commit_audit.change_type.value.text(),
            committer: new DoctorProxy(
               name: parsedVersion.commit_audit.committer.name.text()
            )
         )
         
         
         String compositionUID = java.util.UUID.randomUUID() as String
         
         // A la composition se le asigna un UID en el EHR Server
         // TODO: cambiar el XML de la composition para ponerle este valor en
         //         <data xsi:type="COMPOSITION"><uid> que es atributo de LOCATABLE
         // Actualizar el XML: http://groovy.codehaus.org/Updating+XML+with+XmlSlurper
         data = new CompositionRef(
            value: compositionUID
         )
         
         
         // El uid se lo pone el servidor: object_id::creating_system_id::version_tree_id
         // - object_id se genera (porque el changeType es creation)
         // - creating_system_id se obtiene de committer.name
         // - version_tree_id es 1 (porque el changeType es creation)
         //
         version = new Version(
            //uid: parsedVersion.uid.value.text(), // object_id::creating_system_id::version_tree_id
            uid: (java.util.UUID.randomUUID() as String) +'::'+ parsedVersion.commit_audit.committer.name.text() +'::1',
            lifecycleState: parsedVersion.lifecycle_state.value.text(),
            commitAudit: commitAudit,
            
            // La contribution se setea afuera por quien crea la Contribution
            //  - la ref a contribution NO se comitea,
            //    se crea en el servidor junto a la Contribution.
            
            data: data
         )
         
         // Modifica XML con uid asignado
         // Supongo que la COMPOSITION NO tiene un UID
         parsedVersion.data.appendNode {
            uid {
               // Sin poner el id explicitamente desde un string asignaba
               // el mismo uid a todas las compositions.
               value(compositionUID)
            }
         }
         
         
         // Aca no lo puedo leer, dice que es vacio !???
         //println "xmlService.parseVersions: compo.uid="+ parsedVersion.data.uid.value.text()
         //println "xmlService.parseVersion: compo.uid="+ data.value
         
         // Agrega namespaces al nuevo root
         // Para que no de excepciones al parsear el XML de la composition
         parsedVersion.data.@xmlns = 'http://schemas.openehr.org/v1'
         parsedVersion.data.'@xmlns:xsi' = 'http://www.w3.org/2001/XMLSchema-instance'
         
         // Parametro de salida
         //compositionOut = parsedVersion.data
         dataOut[i] = parsedVersion.data // Sin parsedVersions como lista en el siguiente loop sobreescribe todos los nodos que ya se pusieron en la lista (queda el puntero pero no el objeto)
         
         // test
         //println "Compo con nuevo UID:"
         //println new groovy.xml.StreamingMarkupBuilder().bind{ out << parsedVersion.data}
         
         // Agrega la version parseada para retornar
         ret[i] = version
      }
      
      return ret
   }
   
   /*
    * las compositions se guardan tal cual como XML en disco, no es necesario parsearlas
    * 
   <!-- COMPOSITION -->
   <data xsi:type="COMPOSITION" archetype_node_id="archetype_id|node_id">
   
     <!-- atributos de LOCATABLE --------- -->
     
     <!-- DV_TEXT -->
     <name>
     <!-- UID_BASED_ID -->
     <uid>
     
     <!-- ARCHETYPED > LOCATABLE.archetype_details -->
     <archetype_details>
       
       <!-- ARCHETYPE_ID -->
       <archetype_id>
         <value>arch_id</value>
       </archetype_id>
       
       <!-- String -->
       <rm_version>1.0.2</rm_version>
     </archetype_details>
     
     <!-- /atributos de LOCATABLE --------- -->
     
     <!-- CODE_PHRASE -->
     <language>
     <!-- CODE_PHRASE -->
     <territory>
     <!-- DV_CODED_TEXT -->
     <category>
     <!-- PARTY_IDENTIFIED -->
     <composer>
     <!-- EVENT_CONTEXT -->
     <context>
       <start_time>
         <value></value>
       </start_time>
     </context>
     
     <!-- SECTION / ENTRY, pueden ser varias -->
     <content xsi:type="INSTRUCTION" archetype_node_id="no va si es un arquetipo plano">
       <!-- atributos de LOCATABLE --------- -->
     </content>
     <content xsi:type="EVALUATION" archetype_node_id="no va si es un arquetipo plano">
       <!-- atributos de LOCATABLE --------- -->
     </content>
   </data>
   */
}