package parsers

//import com.thoughtworks.xstream.XStream
import ehr.Ehr
import common.change_control.Contribution
import common.change_control.Version
import common.generic.AuditDetails
import common.generic.DoctorProxy
import groovy.util.slurpersupport.GPathResult
//import support.identification.CompositionRef
import ehr.clinical_documents.CompositionIndex
import org.codehaus.groovy.grails.commons.ApplicationHolder

class XmlService {

   // Para acceder a las opciones de localizacion
   def config = ApplicationHolder.application.config.app
   
   
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
   def parseVersions(Ehr ehr, List<String> versionsXML,
      String auditSystemId, Date auditTimeCommitted, String auditCommitter,
      List dataOut)
   {
      List<Contribution> ret = []
      
      // Uso una lista para no reutilizar la misma variable que sobreescribe
      // las versions anteriors y me deja varias copias de la misma composition
      // en dataOut (quedan todos los punteros a la ultima que se procesa)
      def parsedVersion
      def commitAudit
      def data
      def version
      def compoIndex
      def startTime
      def contributionId
      def contribution
      versionsXML.eachWithIndex { versionXML, i ->
      
         // Sin esto pone tag0 como namespace en todas las tags!!!
         parsedVersion = new XmlSlurper(true, false).parseText(versionXML)
         
         // Parse AuditDetails from Version.commit_audit
         commitAudit = new AuditDetails(
            systemId:      parsedVersion.commit_audit.system_id.text(),
            
            /* 
             * version.commit_audit.time_committed is overriden by the server
             * to be comlpiant with the specs:
             * 
             * The time_committed attribute in both the Contribution and Version audits
             * should reflect the time of committal to an EHR server, i.e. the time of
             * availability to other users in the same system. It should therefore be
             * computed on the server in implementations where the data are created
             * in a separate client context.
             */
            timeCommitted: auditTimeCommitted, //Date.parse(config.l10n.datetime_format, parsedVersion.commit_audit.time_committed.text()),
            changeType:    parsedVersion.commit_audit.change_type.value.text(),
            committer: new DoctorProxy(
               name: parsedVersion.commit_audit.committer.name.text()
               // TODO: id
            )
         )
         
         println "XMLSERVICE change_type="+ commitAudit.changeType
         
         
         // Genera un UID para la composition
         String compositionUID = java.util.UUID.randomUUID() as String
         
         
         // T0004
         // =====================================================================
         // Crea indice para la composition
         // =====================================================================
         
         // -----------------------
         // Obligatorios en el XML: lo garantiza xmlService.parseVersions
         // -----------------------
         //  - composition.category.value con valor 'event' o 'persistent'
         //    - si no esta o tiene otro valor, ERROR
         //  - composition.context.start_time.value
         //    - DEBE ESTAR SI category = 'event'
         //    - debe tener formato completo: 20070920T104614,0156+0930
         //  - composition.@archetype_node_id
         //    - obligatorio el atributo
         //  - composition.'@xsi:type' = 'COMPOSITION'
         // -----------------------
         if (parsedVersion.data.context.start_time.value)
         {
            // http://groovy.codehaus.org/groovy-jdk/java/util/Date.html#parse(java.lang.String, java.lang.String)
            // Sobre fraccion: http://en.wikipedia.org/wiki/ISO_8601
            // There is no limit on the number of decimal places for the decimal fraction. However, the number of
            // decimal places needs to be agreed to by the communicating parties.
            //
            // TODO: formato de fecha completa que sea configurable
            //       ademas la fraccion con . o , depende del locale!!!
            startTime = Date.parse(config.l10n.datetime_format, parsedVersion.data.context.start_time.value.text())
         }
         
         /*
          * <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.signos.v1">
          *   <name>
          *     <value>Signos vitales</value>
          *   </name>
          *   <archetype_details>
          *     <archetype_id>
          *       <value>openEHR-EHR-COMPOSITION.signos.v1</value>
          *     </archetype_id>
          *     <template_id>
          *       <value>Signos</value>
          *     </template_id>
          *     <rm_version>1.0.2</rm_version>
          *   </archetype_details>
          *   ...
          */
         compoIndex = new CompositionIndex(
            uid:         compositionUID,
            category:    parsedVersion.data.category.value.text(), // event o persistent
            startTime:   startTime, // puede ser vacio si category es persistent
            subjectId:   ehr.subject.value,
            ehrId:       ehr.ehrId,
            archetypeId: parsedVersion.data.@archetype_node_id.text(),
            templateId:  parsedVersion.data.archetype_details.template_id.value.text()
         )
         
         
         // ==============================================================================
         // version.contribution will come frmo the client
         // https://github.com/ppazos/cabolabs-ehrserver/issues/51
         //
         // 1. If version.contribution.id.value is empty => exception
         // 2.a. If there is a contribution with the same id, get that from the DB to set into the Version instance
         // 2.b. If not, create a new contribution and save it
         
         contributionId = parsedVersion.contribution.id.value.text()
         if (!contributionId)
         {
            throw new Exception('version.contribution.id.value should not be empty')
         }
         
         // FIXME: la contribution debe existir solo si la version que proceso esta dentro de ella
         //        asi como esta este codigo, se mando 2 commits distintos y con el mismo contribution.uid
         //        va a procesar los 2 commits como si fueran el mismo.
         
         if (Contribution.countByUid(contributionId) == 0)
         {
            contribution = new Contribution(
               uid: contributionId,
               ehr: ehr,
               audit: new AuditDetails(
                  systemId:      auditSystemId,
                  
                  /*
                   * The time_committed attribute in both the Contribution and Version audits
                   * should reflect the time of committal to an EHR server, i.e. the time of
                   * availability to other users in the same system. It should therefore be
                   * computed on the server in implementations where the data are created
                   * in a separate client context.
                   */
                  timeCommitted: auditTimeCommitted,
                  //,
                  // changeType solo se soporta 'creation' por ahora
                  //
                  // El committer de la contribution es el mismo committer de todas
                  // las versiones, cada version tiene su committer debe ser el mismo.
                  committer: new DoctorProxy(
                     name: auditCommitter
                     // TODO: 'value' con el id
                  )
               )
               // versions se setean abajo
            )
            
            
            // Agrega contribution al EHR
            // Ehr -> Contribution (ya salva)
            ehr.addToContributions( contribution )
            
            
            if (!contribution.save())
            {
               println "XmlService parse Versions"
               println "Contribution errors"
               contribution.errors.allErrors.each {
                  println it
               }
            }
         }
         else
         {
            contribution = Contribution.findByUid(contributionId)
         }
         
         
         // El uid se lo pone el servidor: object_id::creating_system_id::version_tree_id
         // - object_id se genera (porque el changeType es creation)
         // - creating_system_id se obtiene del cliente
         // - version_tree_id es 1 (porque el changeType es creation)
         //
         version = new Version(
            uid: (parsedVersion.uid.value.text()), // the 3 components come from the client.
            lifecycleState: parsedVersion.lifecycle_state.value.text(),
            commitAudit: commitAudit,
            contribution: contribution,
            data: compoIndex
         )
         
         
         // ================================================================
         // Necesito verificar por el versionado, sino me guarda 2 versions con isLatestVersion en true
         
         // TODO: documentar
         // Si hay una nueva VERSION, del cliente viene con el ID de la version que se esta actualizando
         // y el servidor actualiza el VERSION.uid con la version nueva en VersionTreeId.
         // El cliente solo setea el id de la primera version, cuando es creation.
         
         // Si ya hay una version, el tipo de cambio no puede ser creation (verificacion extra)
         if (Version.countByUid(version.uid) > 0)
         {
            assert version.commitAudit.changeType != "creation"
            
            def previousLastVersion = Version.findByUid(version.uid)
            previousLastVersion.isLastVersion = false
            
            // FIXME: si falla, rollback. Este servicio deberia ser transaccional
            if (!previousLastVersion.save()) println previousLastVersion.errors
            
            
            // +1 en el version tree id de version.uid
            version.addTrunkVersion()
            //if (!version.save()) println version.errors // Salva con la contribution
         }
         
         
         // contribution -> version
         contribution.addToVersions( version )
         

         // Modifica XML con uid asignado a la composition
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
         //ret[i] = version
         
         if (!ret.contains(contribution)) ret << contribution
      }
      
      
      // ========================================================
      // Save constribution with versions in cascade
      if (!contribution.save())
      {
         contribution.errors.allErrors.each {
            println it
         }
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