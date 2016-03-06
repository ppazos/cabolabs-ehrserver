package com.cabolabs.ehrserver.parsers

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import groovy.util.slurpersupport.GPathResult
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import grails.util.Holders
import java.nio.file.AccessDeniedException
import java.nio.file.FileAlreadyExistsException
import javax.xml.bind.ValidationException
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.util.DateParser

class XmlService {

   static transactional = true
   
   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   def validationErrors = [:] // xsd validatios errros for the committed versions
   def xmlValidationService
   
   
   def processCommit(Ehr ehr, GPathResult versions, String auditSystemId, Date auditTimeCommitted, String auditCommitter)
   {
      // Validate versions
      // Throw javax.xml.bind.ValidationException if there are a validation error
      //  - errors will be saved into validationErrors
      validateVersions(versions)
      
      // Check contribution id
      //  If there is more than 1 version in the commit
      //   - check all version have the same contribution id, error if not
      checkContributions(versions)
      
      // Parse contribution once, since it is the same for all versions
      //  - create the contrib and associated it with the ehr
      def contribution = parseCurrentContribution(versions.version[0], ehr, auditSystemId, auditTimeCommitted, auditCommitter)
      
      // For each version committed
      //  Parse compo index
      //  Check if a version exists with the uid in the version XML (it can be 0 or 1, 1 is the case of modification/amendment)
      //  If there is a previous version
      //   If the change type of the current version is creation, error
      //   Else
      //    - create version and associate the compo index
      //    - change the last version status on previous version
      //    - update the trunk version id on the previous version
      //  Associate the version with the contribution
      def domainVersions = parseVersions(ehr, versions, auditTimeCommitted, contribution)
      
      // just checking :)
      assert contribution.versions != null
      assert contribution.versions.size() > 0
      
      
      // VersionedComposition creation by processing the change type
      // For each version in the contribution
      //  If change type = creation
      //   - create new versioned compo for the version
      //   - add versioned compo to ehr
      //  If change type = amendment or modification
      //   - check if a versioned compo already exists, error if not
      //   - 
      //  TODO: support more types
      manageVersionedCompositions(domainVersions, ehr)
      

      // If contribution and versions can be saved ok
      //  - check if file exists, error if exists
      //  - save version XML files on file system
      storeVersionXMLs(versions)
      
      
      // Save the contribution with all the versions
      //  throws grails.validation.ValidationException that contains the errors
      //contribution.save(flush:true, failOnError:true) // saved through the ehr
      

      // TEST: this might save the contrib and there is no need of saving the contrib later
      ehr.addToContributions( contribution )
   }
   
   
   /**
    * Validates all versions in the commit, against the version XSD.
    * Throws a ValidationException if errors are found.
    * @param versions
    * @return
    */
   def validateVersions(GPathResult versions)
   {
      // This will have the ns declared in the versions element,
      // like: ['xmlns':'http://schemas.openehr.org/v1', 'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance']
      // Those will be copied to each individual version to validate them individually.
      def namespaceMap = versions.attributes().findAll { it.key.startsWith('xmlns') }
      
      def errors = [:] // The index is the index of the version, the value is the list of errors for each version
      
      versions.version.eachWithIndex { versionXML, i ->
         
         if (!xmlValidationService.validateVersion(versionXML, namespaceMap))
         {
            errors[i] = xmlValidationService.getErrors() // Important to keep the correspondence between version index and error reporting.
         }
      }
      
      this.validationErrors = errors
      
      if (this.validationErrors.size() > 0) throw new ValidationException('There are errors in the XML versions')
   }
   
   
   /**
    * Throws an IllegalArgumentException if there are two versions with different contribution id
    * @param versions
    * @return
    */
   def checkContributions(GPathResult versions)
   {
      if (versions.version.size() == 1) return // nothing to check
      
      // All contribution ids are the same?
      def firstContributionId
      def loopContributionId
      versions.version.each { versionXML ->
         
         loopContributionId = versionXML.contribution.id.value.text()
         if (!loopContributionId)
         {
            throw new IllegalArgumentException('version.contribution.id.value should not be empty')
         }
         
         // Set the first contribution uid, then compare the first with the rest,
         // one is different, throw an exception.
         if (!firstContributionId) firstContributionId = loopContributionId
         else
         {
            if (firstContributionId != loopContributionId)
            {
               throw new IllegalArgumentException("two versions in the same commit reference different contributions ${firstContributionId} and ${loopContributionId}")
            }
         }
      }
   }
   
   
   /**
    * This method manages the versions, creating new versioned compositions
    * or adding the new version to an existing versioned compo.
    * @param parsedVersions
    * @return
    */
   def manageVersionedCompositions(List parsedVersions, Ehr ehr)
   {
      parsedVersions.eachWithIndex { version, i ->
         
//         println "version "+ version.uid
//         println "version commitAudit "+ version.commitAudit
//         println "change type "+ version.commitAudit.changeType
         
         def versionedComposition
         switch (version.commitAudit.changeType)
         {
            case 'creation':
            
               versionedComposition = new VersionedComposition(
                  uid: version.objectId,
                  ehr: ehr,
                  isPersistent: (version.data.category == 'persistent'))
               
               // If errors, throws grails.validation.ValidationException with the errors
               versionedComposition.save(flush:true, failOnError:true)
               
            break
            case ['amendment', 'modification']:
               
               versionedComposition = VersionedComposition.findByUid(version.objectId)

               // VersionedObject should exist for change type modification or amendment
               if (!versionedComposition)
               {
                  throw new IllegalArgumentException("A change type ${version.commitAudit.changeType} was received, but there are no previous versions with id ${version.objectId}")
               }
               
               // Nothing needs to be done if the versioned compo exists, since version and versioned compo
               // are linked by the objectId. We can do further validations, like the template is the same for
               // the previous latest version and the new version. Just to assure data consistency. (TODO)
               
               // XmlService hace previousLastVersion.isLastVersion = false
               // asi la nueva version es la unica con isLastVersion == true
               
               
               // ======================================================
               // Improvement: DataValueIndexes for old versions can be deleted because queries are executed over the latest version.
               // ======================================================
               
               // No crea el VersionedComposition porque ya deberia estar
               
               assert ehr.containsVersionedComposition(version.objectId) : "El EHR ya deberia contener el versioned object con uid "+ version.objectId +" porque el tipo de cambio es "+version.commitAudit.changeType
               
            break
            default:
               throw new IllegalArgumentException("Change type ${version.commitAudit.changeType} not supported yet")

         } // switch changeType
      }
   }
   
   private String versionFileName(GPathResult version)
   {
      return config.version_repo + version.uid.text().replaceAll('::', '_') +'.xml'
   }
   
   /**
    * Stores XML documents committed, as files.
    * @param versions
    * @return
    */
   def storeVersionXMLs(GPathResult versions)
   {
      // CHECK: the compo in version.data doesn't have the injected 
      // compo.uid that parsedCompositions[i] does have.
      /*
       * XmlUtil.serialize generate these warnings but work ok:
       * | Error Warning:  org.apache.xerces.parsers.SAXParser: Feature 'http://javax.xml.XMLConstants/feature/secure-processing' is not recognized.
         | Error Warning:  org.apache.xerces.parsers.SAXParser: Property 'http://javax.xml.XMLConstants/property/accessExternalDTD' is not recognized.
         | Error Warning:  org.apache.xerces.parsers.SAXParser: Property 'http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit' is not recognized.
       */
      
      // FIXME: this check should be done on setup
      if (!new File(config.version_repo).canWrite()) throw new AccessDeniedException("Unable to write file ${config.version_repo}")
      
      
      def file, path
      versions.version.each { version ->
         
         path = versionFileName(version)
         file = new File( path )
         
         //if (file.exists()) throw new FileAlreadyExistsException("Unable to save composition from commit, file ${path} already exists")
         // Need to throw unchecked exception to make the service rollback
         // Ref: http://www.jellyfishtechnologies.com/services-grails-transactional-behaviour/
         if (file.exists()) throw new RuntimeException("Unable to save composition from commit, file ${path} already exists")
         
         // FIXME: check if the XML has the namespace declarations of the root node from the commit
         file << groovy.xml.XmlUtil.serialize( version )
      }
   }
   
   /**
   <versions>
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
      <version>
      ...
      </version>
    </version>
   */
   def parseVersions(Ehr ehr, GPathResult versionsXML, Date auditTimeCommitted, Contribution contribution)
   {
      def commitAudit, compoIndex, version
      def dataOut = []
      
      versionsXML.version.eachWithIndex { parsedVersion, i ->
      
         //println "parseVersion "+ parsedVersion.uid.value.text()
         
         // Parse AuditDetails from Version.commit_audit
         commitAudit = parseVersionCommitAudit(parsedVersion, auditTimeCommitted)
         //println "commitAudit: "+ commitAudit
         
         compoIndex = parseCompositionIndex(parsedVersion, ehr)
         

         // El uid se lo pone el servidor: object_id::creating_system_id::version_tree_id
         // - object_id se genera (porque el changeType es creation)
         // - creating_system_id se obtiene del cliente
         // - version_tree_id es 1 (porque el changeType es creation)
         //
         version = new Version(
            uid: (parsedVersion.uid.value.text()), // the 3 components come from the client.
            lifecycleState: parsedVersion.lifecycle_state.value.text(),
            commitAudit: commitAudit,
            data: compoIndex
         )
         
         
         // ================================================================
         // Necesito verificar por el versionado, sino me guarda 2 versions con isLatestVersion en true
         
         // TODO: documentar
         // Si hay una nueva VERSION, del cliente viene con el ID de la version que se esta actualizando
         // y el servidor actualiza el VERSION.uid con la version nueva en VersionTreeId.
         // El cliente solo setea el id de la primera version, cuando es creation.
         
         // Si ya hay una version, el tipo de cambio no puede ser creation (verificacion extra)
         if (Version.countByUid(version.uid) == 1)
         {
            if (version.commitAudit.changeType == "creation")
            {
               //IllegalArgumentException
               throw new RuntimeException("A version with UID ${version.uid} already exists, but the change type is 'creation', it should be 'amendment' or 'modification'")
            }
            
            // change type is not creation

            
            // Verifies that the commit is a new version of the latestVersion
            // Avoid committing an amendment for a version that is not the latest.
            // This keeps consistency for the linear versioning we support.
            def versionedComposition = VersionedComposition.findByUid(version.objectId)
            def lastVersion = versionedComposition.latestVersion
            if (lastVersion.uid != version.uid)
            {
               throw new IllegalArgumentException("A change type ${version.commitAudit.changeType} was received for a version that is not the latest, please checkout the latest version ${lastVersion.uid}")
            }
            
            
            // Commit is for the last version of the compo
            
            def previousLastVersion = lastVersion //Version.findByUid(version.uid)
            previousLastVersion.data.lastVersion = false // lastVersion pasa a estar solo en CompoIndex por https://github.com/ppazos/cabolabs-ehrserver/issues/66
            

            // FIXME: si falla, rollback. Este servicio deberia ser transaccional
            // This is adding (I dont know why) the version to the contribution.versions list
            if (!previousLastVersion.save()) println previousLastVersion.errors.allErrors

            //println "POST previousVersion.save"
            //println (previousLastVersion as grails.converters.XML)
            
            
            // +1 en el version tree id de version.uid
            version.addTrunkVersion()
            // version se salva luego con la contribution
            

            // ================================================================
            // Update the XML with the new version uid.
            //
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

         dataOut[i] = version
         contribution.addToVersions(version)
         
      } // each versionXML
      
      return dataOut
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
         //startTime = Date.parse(config.l10n.datetime_format, version.data.context.start_time.value.text())
         startTime = DateParser.tryParse(version.data.context.start_time.value.text())
      }
      
      // Check if the committed compo has an uid, if not, the server assigns one
      def compoUid = (java.util.UUID.randomUUID() as String)
      if (version.data.uid.size() == 0)
      {
         // Add the compo uid to the XML
         // Supongo que la COMPOSITION NO tiene un UID
         // With + groovy adds the new node after the name node to be compliant with the XSD
         // http://stackoverflow.com/questions/5022353/groovy-xmlslurper-and-inserting-child-nodes
         version.data.name + {
            uid('xsi:type': 'HIER_OBJECT_ID') {
               // Sin poner el id explicitamente desde un string asignaba
               // el mismo uid a todas las compositions.
               value(compoUid)
            }
         }
      }
      else
      {
         compoUid = version.data.uid.value.text() // takes the existing compo uid
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
         uid:         compoUid, // UID for compos is assigned by the server
         category:    version.data.category.value.text(), // event o persistent
         startTime:   startTime, // puede ser vacio si category es persistent
         subjectId:   ehr.subject.value,
         ehrUid:      ehr.uid,
         organizationUid: ehr.organizationUid,
         archetypeId: version.data.@archetype_node_id.text(),
         templateId:  version.data.archetype_details.template_id.value.text()
      )
      
      return compoIndex
   }
   
   private Contribution parseCurrentContribution(GPathResult version, Ehr ehr,
                                         String auditSystemId, Date auditTimeCommitted, String auditCommitter)
   {
      // This instance of XmlService process one contribution at a time
      // But each version on the version list has a reference to the same contribution,
      // and the contribution will have a list of all the versions committed.
      def currentContribution
      
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
         organizationUid: ehr.organizationUid,
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
