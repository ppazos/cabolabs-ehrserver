package parsers

//import com.thoughtworks.xstream.XStream
import ehr.Ehr
import common.change_control.Contribution
import common.change_control.Version
import common.generic.AuditDetails
import common.generic.DoctorProxy
import common.generic.PatientProxy
import groovy.util.slurpersupport.GPathResult
import ehr.clinical_documents.CompositionIndex
import grails.util.Holders

class XmlService {

   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   def validationErrors = [:] // xsd validatios errros for the committed versions
   
   /**
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
      this.validationErrors = validateVersions(versionsXML) // the key is the index of the errored version
      
      // There are errors, can't return the contributions
      // The caller should get the errors and process them
      if (this.validationErrors.size() > 0)
      {
         return null
      }
      
      
      List<Contribution> ret = [] // FIXME: una contribution por commit
      
      println ":: versionsXML: "+ versionsXML.size()
      
      
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
      def slurper = new XmlSlurper(true, false)
      versionsXML.eachWithIndex { versionXML, i ->
      
      println "************ EACH WITH INDEX ***************** "+ i
      
         // Sin esto pone tag0 como namespace en todas las tags!!!
         parsedVersion = slurper.parseText(versionXML)

         
         // Parse AuditDetails from Version.commit_audit
         commitAudit = parseVersionCommitAudit(parsedVersion, auditTimeCommitted)
         
         
         println "XMLSERVICE change_type="+ commitAudit.changeType
         
         
         compoIndex = parseCompositionIndex(parsedVersion, ehr)
         
         // The contribution is set from the 1st version because is the same
         // for all the versions committed together
         if (!contribution)
         {
            contribution = parseCurrentContribution(
               parsedVersion, ehr,
               auditSystemId, auditTimeCommitted, auditCommitter
            )
            
            assert contribution != null
            
            println "PRE contribution.save"
            
            if (!contribution.save())
            {
               println "XmlService parse Versions"
               println "Contribution errors"
               contribution.errors.allErrors.each {
                  println it
               }
            }
            else println "Guarda contrib"
            
            println "POST contribution.save"
            
            // FIXME: it seems the addtoContributions is executed twice for the same contribution!!!
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
         
         
         // Test to see if the code above also adds the version to currentContribution.versions
         //assert contribution.versions[i].uid == version.uid
         
         
         // ================================================================
         // Necesito verificar por el versionado, sino me guarda 2 versions con isLatestVersion en true
         
         // TODO: documentar
         // Si hay una nueva VERSION, del cliente viene con el ID de la version que se esta actualizando
         // y el servidor actualiza el VERSION.uid con la version nueva en VersionTreeId.
         // El cliente solo setea el id de la primera version, cuando es creation.
         
         // Si ya hay una version, el tipo de cambio no puede ser creation (verificacion extra)
         if (Version.countByUid(version.uid) == 1)
         {
            assert version.commitAudit.changeType != "creation"
            
            def previousLastVersion = Version.findByUid(version.uid)
            previousLastVersion.data.lastVersion = false // lastVersion pasa a estar solo en CompoIndex por https://github.com/ppazos/cabolabs-ehrserver/issues/66
            
            println "PRE previousVersion.save"
            println (previousLastVersion as grails.converters.XML)
            
            // FIXME: si falla, rollback. Este servicio deberia ser transaccional
            // This is adding (I dont know why) the version to the contribution.versions list
            if (!previousLastVersion.save()) println previousLastVersion.errors.allErrors
            
            println "POST previousVersion.save"
            println (previousLastVersion as grails.converters.XML)
            
            
            // +1 en el version tree id de version.uid
            version.addTrunkVersion()
            // version se salva luego con la contribution
            
            
            // ================================================================
            // FIXME: this is not updating the version uid
            
            
            // The new version.uid was updated in memory and saved into the DB,
            // for checkout purposes we need also to update it in the XML version
            // received, because the version uid received is for the previous version
            // but the saved version is the one that will be checked out, so should
            // have the next version uid.
            
            // This searches for the version id in the XML string and changes
            // it with the new version uid. This will be saved to a file by the controller.
            parsedVersion.uid.value = version.uid // Aca ya agrega la version a contribution.versions!!!! en modification
            
            // ================================================================
         }
         else // creation (no previous version exists)
         {
            // There is was problem: saving the previousLastVersion added the version to the contribution.versions (weird),
            // so the contribution.addToVersions when there was a previous version, added the version twice to the
            // contribution.versions list. This else is to avoid that duplicated addition to contribution.versions.
             
            println "***** VERSIONS PREV " +contribution.versions
            contribution.addToVersions( version )
            println "***** VERSIONS AFTER " +contribution.versions
         }

         
         assert contribution.versions.size() == i+1
         
         
         // Aca no lo puedo leer, dice que es vacio !???
         //println "xmlService.parseVersions: compo.uid="+ parsedVersion.data.uid.value.text()
         //println "xmlService.parseVersion: compo.uid="+ data.value
         
         
         // Parametro de salida
         dataOut[i] = parsedVersion
         
         // test
         //println "Compo con nuevo UID:"
         //println new groovy.xml.StreamingMarkupBuilder().bind{ out << parsedVersion.data}
         
         // FIXME: deberia procesar 1 contribution por vez
         //if (!ret.contains(currentContribution)) ret << currentContribution
      
      } // each versionXML
      
      ret << contribution
      
      /*
      // ========================================================
      // Save constribution with versions in cascade
      if (!contribution.save())
      {
         contribution.errors.allErrors.each {
            println it
         }
      }
      */
      
      return ret
   }
   
   
   private Map validateVersions(List<String> versionsXML)
   {
      def errors = [:] // The index is the index of the version, the value is the list of errors for each version
      def validator = new VersionValidator()
      versionsXML.eachWithIndex { versionXML, i ->
         if (!validator.validate(versionXML))
         {
            errors[i] = validator.getErrors() // Important to keep the correspondence between version index and error reporting.
         }
      }
      return errors
   }
   
   
   private AuditDetails parseVersionCommitAudit(GPathResult version, Date auditTimeCommitted)
   {
      // Parse AuditDetails from Version.commit_audit
      return new AuditDetails(
         systemId:      version.commit_audit.system_id.text(),
         
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
         changeType:    version.commit_audit.change_type.value.text(),
         committer: new DoctorProxy(
            name: version.commit_audit.committer.name.text()
            // TODO: id
         )
      )
   }
   
   private CompositionIndex parseCompositionIndex(GPathResult version, Ehr ehr)
   {
      Date startTime
      
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
      //    - debe tener formato completo: 20070920T104614,156+0930
      //  - composition.@archetype_node_id
      //    - obligatorio el atributo
      //  - composition.'@xsi:type' = 'COMPOSITION'
      // -----------------------
      if (version.data.context.start_time.value)
      {
         // http://groovy.codehaus.org/groovy-jdk/java/util/Date.html#parse(java.lang.String, java.lang.String)
         // Sobre fraccion: http://en.wikipedia.org/wiki/ISO_8601
         // There is no limit on the number of decimal places for the decimal fraction. However, the number of
         // decimal places needs to be agreed to by the communicating parties.
         //
         // TODO: formato de fecha completa que sea configurable
         //       ademas la fraccion con . o , depende del locale!!!
         startTime = Date.parse(config.l10n.datetime_format, version.data.context.start_time.value.text())
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
      def compoIndex = new CompositionIndex(
         uid:         (java.util.UUID.randomUUID() as String), // UID for compos is assigned by the server
         category:    version.data.category.value.text(), // event o persistent
         startTime:   startTime, // puede ser vacio si category es persistent
         subjectId:   ehr.subject.value,
         ehrId:       ehr.ehrId,
         archetypeId: version.data.@archetype_node_id.text(),
         templateId:  version.data.archetype_details.template_id.value.text()
      )
      
      
      // Modifica XML con uid asignado a la composition
      // Supongo que la COMPOSITION NO tiene un UID
      version.data.appendNode {
         uid {
            // Sin poner el id explicitamente desde un string asignaba
            // el mismo uid a todas las compositions.
            value(compoIndex.uid)
         }
      }
      
      return compoIndex
   }
   
   private Contribution parseCurrentContribution(GPathResult version, Ehr ehr,
                                         String auditSystemId, Date auditTimeCommitted, String auditCommitter)
   {
      // This instance of XmlService process one contribution at a time
      // But each version on the version list has a reference to the same contribution,
      // and the contribution will have a list of all the versions committed.
      def currentContribution
      
      
      // FIXME: deberia reorrer todas las version y verificar que todas
      //        tienen una referencia a la misma contribution
      
      // ==============================================================================
      // version.contribution will come from the client
      // https://github.com/ppazos/cabolabs-ehrserver/issues/51
      //
      // 1. If version.contribution.id.value is empty => exception
      // 2.a. If there is a contribution with the same id, get that from the DB to set into the Version instance
      // 2.b. If not, create a new contribution and save it
      
      def contributionId = version.contribution.id.value.text()
      if (!contributionId)
      {
         throw new Exception('version.contribution.id.value should not be empty')
      }
      
      // FIXME: la contribution debe existir solo si la version que proceso esta dentro de ella
      //        asi como esta este codigo, si mando 2 commits distintos y con el mismo contribution.uid
      //        va a procesar los 2 commits como si fueran el mismo.
      
      // TODO: verify there is no contribution with the same uid in the db
      
      currentContribution = new Contribution(
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
      
      // TEST: this might save the contrib and there is no need of saving the contrib later
      ehr.addToContributions( currentContribution )
      
      return currentContribution
   }
   
   
   
   /**
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