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

package com.cabolabs.ehrserver.parsers

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.ChangeType
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import groovy.util.slurpersupport.GPathResult
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.exceptions.CommitCantCreateNewVersionException
import com.cabolabs.ehrserver.exceptions.CommitContributionReferenceException
import com.cabolabs.ehrserver.exceptions.CommitNotSupportedChangeTypeException
import com.cabolabs.ehrserver.exceptions.CommitRequiredValueNotPresentException
import com.cabolabs.ehrserver.exceptions.CommitWrongChangeTypeException
import com.cabolabs.ehrserver.exceptions.VersionRepoNotAccessibleException
import com.cabolabs.ehrserver.exceptions.XmlValidationException
import grails.util.Holders
import java.nio.file.AccessDeniedException
import java.nio.file.FileAlreadyExistsException
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.util.DateParser
import com.cabolabs.ehrserver.versions.VersionFSRepoService

// https://stackoverflow.com/questions/21138173/grails-saving-multiple-object-rollback-all-object-if-one-fails-to-save
import org.springframework.transaction.interceptor.TransactionAspectSupport

//import grails.transaction.Transactional

//@Transactional
class XmlService {

   static transactional = true
   
   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   def validationErrors = [:] // xsd validatios errros for the committed versions
   def xmlValidationService
   def versionFSRepoService
   
   def CHANGE_TYPE_CREATION = 249
   def CHANGE_TYPE_AMENDMENT = 250
   def CHANGE_TYPE_MODIFICATION = 251
   def CHANGE_TYPE_DELETED = 523
   
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

      checkVersions(versions)

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
      storeVersionXMLs(ehr, versions, contribution)
      
      // Save the contribution with all the versions
      //  throws grails.validation.ValidationException that contains the errors
      //contribution.save(flush:true, failOnError:true) // saved through the ehr
      
      // TEST: this might save the contrib and there is no need of saving the contrib later
      //ehr.addToContributions( contribution )

      return contribution
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
         
         if (versionXML.data.'@xsi:type'.text() != 'COMPOSITION')
         {
            errors[i] = ['version.data.xsi:type should be "COMPOSITION"']
         }
         else if (!xmlValidationService.validateVersion(versionXML, namespaceMap)) // XSD validation
         {
            errors[i] = xmlValidationService.getErrors() // Important to keep the correspondence between version index and error reporting.
         }
      }
      
      this.validationErrors = errors
      
      if (this.validationErrors.size() > 0) throw new XmlValidationException('There are errors in the XML versions')
      
      
      // if there are many compos, and some have uid, the uid should be unique between the compos
      if (versions.version.size() > 1)
      {
         def cuids = versions.version.data.uid.value.collect { it.text() }
         
         // false do not modifies the collection
         if (cuids.unique(false).size() < cuids.size())
         {
            throw new CommitCantCreateNewVersionException("Some composition uids are present and are not unique}")
         }

         // If a compo.uid exists in the XML, it should bet exist on the database, avoid weird cases of reusing UIDs for testing.
         cuids.each { cuid ->
            if (CompositionIndex.countByUid(cuid) > 0)
            {
               throw new CommitCantCreateNewVersionException("The composition uid "+ cuid +" already exists in the database, be sure your system is generating random UIDs and not reusing them")
            }
         }
      }
   }
   
   
   /**
    * Throws an IllegalArgumentException if there are two versions with different contribution id
    * @param versions
    * @return
    */
   def checkContributions(GPathResult versions)
   {
      //println "check contributions"
      //println versions.version.collect { it.contribution.id.value.text() }

      // All contribution ids are the same?
      def firstContributionId
      def loopContributionId
      
      if (versions.version.size() > 1)
      {
         // all versions reference the same contribution id
         versions.version.each { versionXML ->
            
            loopContributionId = versionXML.contribution.id.value.text()
            if (!loopContributionId)
            {
               throw new CommitRequiredValueNotPresentException('version.contribution.id.value should not be empty')
            }
            
            // Set the first contribution uid, then compare the first with the rest,
            // one is different, throw an exception.
            if (!firstContributionId) firstContributionId = loopContributionId
            else
            {
               if (firstContributionId != loopContributionId)
               {
                  throw new CommitContributionReferenceException("two versions in the same commit reference different contributions ${firstContributionId} and ${loopContributionId}")
               }
            }
         }
      }
      else
      {
         firstContributionId = versions.version[0].contribution.id.value.text()
      }
      
      // there are no previous contributions with the same id
      if (Contribution.countByUid(firstContributionId) != 0)
      {
         throw new CommitContributionReferenceException("the committed contribution id already exists ${firstContributionId}, maybe your previous commit used the same id?")
      }
   }
   
   /*
    * version.uid should not exist if change type is creation
    */
   def checkVersions(GPathResult versions)
   {
      def errors = [:]
      def uid
      versions.version.eachWithIndex { parsedVersion, i ->
         uid = parsedVersion.uid.value.text()
         if (
            Version.countByUid(uid) == 1 &&
            parsedVersion.commit_audit.change_type.defining_code.code_string.text() == '249'
            )
         {
            // TODO: i18n
            errors[i] = ["A version with UID ${uid} already exists, but the change type is 'creation'. If you want to create a new version, the changeType should be 'amendment' or 'modification'. If not, might committed the same version twice by error."]
         }
      }
      
      this.validationErrors = errors
      
      if (this.validationErrors.size() > 0) throw new CommitWrongChangeTypeException('There are duplicated version uids with change type creation')
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
            case ChangeType.CREATION:
            
               versionedComposition = new VersionedComposition(
                  uid: version.objectId,
                  ehr: ehr,
                  isPersistent: (version.data.category == 'persistent'))
               
               // If errors, throws grails.validation.ValidationException with the errors
               //versionedComposition.save(flush:true, failOnError:true)
               if (!versionedComposition.save())
               {
                  println "errors "+ versionedComposition.errors
                  println versionedComposition.errors.allErrors
               }
               
            break
            case [ChangeType.AMENDMENT, ChangeType.MODIFICATION]:
               
               versionedComposition = VersionedComposition.findByUid(version.objectId)

               // VersionedObject should exist for change type modification or amendment
               if (!versionedComposition)
               {
                  throw new CommitWrongChangeTypeException("A change type ${version.commitAudit.changeType} was received, but there are no previous versions with id ${version.objectId}")
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
               
               //assert ehr.containsVersionedComposition(version.objectId) : "El EHR ya deberia contener el versioned object con uid "+ version.objectId +" porque el tipo de cambio es "+version.commitAudit.changeType
               
            break
            case ChangeType.DELETED:
            
               // check if version.lifecycleState is "523" (deleted)
               
               if (version.lifecycleState != '523')
               {
                  throw new CommitWrongChangeTypeException("Change type is deleted but the version lifecycle state is ${version.lifecycleState} and 523 (deleted is expected)")
               }
               
               // should be processed as a new version
               
               versionedComposition = VersionedComposition.findByUid(version.objectId)

               // VersionedObject should exist for change type modification or amendment
               if (!versionedComposition)
               {
                  throw new CommitWrongChangeTypeException("A change type ${version.commitAudit.changeType} was received, but there are no previous versions with id ${version.objectId}")
               }
            
            break
            default:
               throw new CommitNotSupportedChangeTypeException("Change type ${version.commitAudit.changeType} not supported yet")

         } // switch changeType
      }
   }
   
   /**
    * Stores XML documents committed, as files.
    * @param versions
    * @return
    */
   def storeVersionXMLs(Ehr ehr, GPathResult versions, Contribution contribution)
   {
      // CHECK: the compo in version.data doesn't have the injected 
      // compo.uid that parsedCompositions[i] does have.
      /*
       * XmlUtil.serialize generate these warnings but work ok:
       * | Error Warning:  org.apache.xerces.parsers.SAXParser: Feature 'http://javax.xml.XMLConstants/feature/secure-processing' is not recognized.
         | Error Warning:  org.apache.xerces.parsers.SAXParser: Property 'http://javax.xml.XMLConstants/property/accessExternalDTD' is not recognized.
         | Error Warning:  org.apache.xerces.parsers.SAXParser: Property 'http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit' is not recognized.
       */
      
      def file, path, version
      versions.version.each { versionXML ->
         
         try
         {
            version = contribution.versions.find { it.uid == versionXML.uid.text()}
            file = versionFSRepoService.getNonExistingVersionFile( ehr.organizationUid, version )
         }
         catch (VersionRepoNotAccessibleException e)
         {
            throw new RuntimeException(e.message, e)
         }
         catch (FileAlreadyExistsException e)
         {
            throw new RuntimeException("Unable to save composition from commit, file ${path} already exists. Maybe you committed the same version twice?", e)
         }

         // FIXME: check if the XML has the namespace declarations of the root node from the commit
         file << groovy.xml.XmlUtil.serialize( versionXML )
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
          <value>complete</value>
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
      def commitAudit, compoIndex, version, existingPersistentCompo
      def dataOut = []
      
      def parsedVersion
      for (int i = 0; i <versionsXML.version.size(); i++)
      {
         parsedVersion = versionsXML.version[i]
      
         // Parse AuditDetails from Version.commit_audit
         commitAudit = parseVersionCommitAudit(parsedVersion, auditTimeCommitted)
         compoIndex = parseCompositionIndex(parsedVersion, ehr, auditTimeCommitted)

         // El uid se lo pone el servidor: object_id::creating_system_id::version_tree_id
         // - object_id se genera (porque el changeType es creation)
         // - creating_system_id se obtiene del cliente
         // - version_tree_id es 1 (porque el changeType es creation)
         //
         version = new Version(
            uid: (parsedVersion.uid.value.text()), // the 3 components come from the client.
            lifecycleState: parsedVersion.lifecycle_state.defining_code.code_string.text(),
            commitAudit: commitAudit,
            data: compoIndex
         )
         
         // Verify correct parameters for persistent commit
         // If the version is a persistent composition, and for the EHR there is already a version,
         // the change type should be "modification", if not, CommitWrongChangeTypeException
         // If everything is OK, the version UID should be checked: it should be the same as the compo index for the ehrid and archetype checked above.
         
         if (compoIndex.category == "persistent")
         {
            existingPersistentCompo = CompositionIndex.findByCategoryAndArchetypeIdAndEhrUid(compoIndex.category, compoIndex.archetypeId, compoIndex.ehrUid)
            
            if (version.commitAudit.changeType == ChangeType.CREATION && existingPersistentCompo)
            {
               throw new CommitWrongChangeTypeException("A persistent composition for ${compoIndex.archetypeId} already exists in the EHR ${compoIndex.ehrUid}, so the change type should not be 'creation', it should be 'modification'.")
            }
            
            if (version.commitAudit.changeType != ChangeType.CREATION && !existingPersistentCompo)
            {
               throw new CommitWrongChangeTypeException("A persistent composition for ${compoIndex.archetypeId} does not exists in the EHR ${compoIndex.ehrUid}, the change type should be 'creation' and it is ${version.commitAudit.changeTyp}")
            }
            
            // Persistent compo versioning process
            // If is modification/amendment and exists a persistent compo for the archid and ehruid
            if ([ChangeType.MODIFICATION, ChangeType.AMENDMENT].contains(version.commitAudit.changeType) &&
                existingPersistentCompo)
            {
               // check if the version has the right objectid, if should exist the VersionedCompo for that object id
               def versionedComposition = VersionedComposition.findByUid(version.objectId)
               if (!versionedComposition)
               {
                  throw new RuntimeException("version.uid..value.objectId ${version.objectId} doesn't correspond to an existing versioned object. Please use the checkout service to get the right uid and be able to commit a new version for a persistent composition")
               }
               
               def lastVersion = versionedComposition.latestVersion
               if (lastVersion.uid != version.uid)
               {
                  throw new CommitCantCreateNewVersionException("A change type ${version.commitAudit.changeType} was received for a version that is not the latest, please use the checkout service to get the latest version uid")
               }
               
               def previousLastVersion = lastVersion
               previousLastVersion.data.lastVersion = false
               
               if (!previousLastVersion.save()) println previousLastVersion.errors.allErrors

               // +1 version tree id of version.uid
               version.addTrunkVersion()

               // Update the XML with the new version uid.
               parsedVersion.uid.value = version.uid
            }
         }
         else // event
         {
            // ================================================================
            // Necesito verificar por el versionado, sino me guarda 2 versions con isLatestVersion en true
            
            // TODO: documentar
            // Si hay una nueva VERSION, del cliente viene con el ID de la version que se esta actualizando
            // y el servidor actualiza el VERSION.uid con la version nueva en VersionTreeId.
            // El cliente solo setea el id de la primera version, cuando es creation.
            
            // Si ya hay una version, el tipo de cambio no puede ser creation (verificacion extra)
            if (Version.countByUid(version.uid) == 1)
            {
               /* this is checked on checkVersions, here we know change type is NOT creation.
               if (version.commitAudit.changeType == ChangeType.CREATION)
               {
                  //IllegalArgumentException
                  throw new CommitWrongChangeTypeException("A version with UID ${version.uid} already exists, but the change type is 'creation'. If you want to create a new version, the changeType should be 'amendment' or 'modification'. If not, might committed the same version twice by error.")
               }
               */
               
               // change type is not creation

               // Verifies that the commit is a new version of the latestVersion
               // Avoid committing an amendment for a version that is not the latest.
               // This keeps consistency for the linear versioning we support.
               def versionedComposition = VersionedComposition.findByUid(version.objectId)
               def lastVersion = versionedComposition.latestVersion
               if (lastVersion.uid != version.uid)
               {
                  throw new CommitCantCreateNewVersionException("A change type ${version.commitAudit.changeType} was received for a version that is not the latest, please checkout the latest version ${lastVersion.uid}")
               }
               
               // Commit is for the last version of the compo
               def previousLastVersion = lastVersion
               previousLastVersion.data.lastVersion = false
               

               // FIXME: si falla, rollback. Este servicio deberia ser transaccional
               // This is adding (I dont know why) the version to the contribution.versions list
               if (!previousLastVersion.save()) println previousLastVersion.errors.allErrors

               //println "POST previousVersion.save"
               //println (previousLastVersion as grails.converters.XML)
               
               // +1 version tree id of version.uid
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
         }

         dataOut[i] = version
         contribution.addToVersions(version)
         
      } // each versionXML
      
      return dataOut
   }
   
   
   private AuditDetails parseVersionCommitAudit(GPathResult version, Date auditTimeCommitted)
   {
      def change_type_code = version.commit_audit.change_type.defining_code.code_string.text()
      
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
         changeType:    ChangeType.fromValue(change_type_code as short),
         committer: new DoctorProxy(
            name: version.commit_audit.committer.name.text()
            // TODO: id
         )
      )
   }
   
   private CompositionIndex parseCompositionIndex(GPathResult version, Ehr ehr, Date auditTimeCommitted)
   {
      // context data for event compositions
      Date startTime, endTime
      String location
      
      String category
      
      /* <category>
            <value>evento</value>
            <defining_code>
              <terminology_id>
                <value>openehr</value>
              </terminology_id>
              <code_string>433</code_string>
            </defining_code>
          </category>
       */
      // Correct assignation of category without depending on the name (locale dependant!)
      def category_code = version.data.category.defining_code.code_string.text()
      if (category_code == "431") category = 'persistent'
      else if (category_code == "433") category = 'event'
      else
      {
         println "Incorrect category code '${category_code}' for COMPOSITION, should be 431 or 433"
         throw new RuntimeException("Incorrect category code '${category_code}' for COMPOSITION, should be 431 or 433")
      }
      
      
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

      if (category == 'event')
      {
         // start time is mandatory for event, that is checked by the XSD on a previous step.

         // http://groovy.codehaus.org/groovy-jdk/java/util/Date.html#parse(java.lang.String, java.lang.String)
         // Sobre fraccion: http://en.wikipedia.org/wiki/ISO_8601
         // There is no limit on the number of decimal places for the decimal fraction. However, the number of
         // decimal places needs to be agreed to by the communicating parties.
         //
         // TODO: formato de fecha completa que sea configurable
         //       ademas la fraccion con . o , depende del locale!!!
         startTime = DateParser.tryParse(version.data.context.start_time.value.text())
         
         // end time is optional in the IM
         if (!version.data.context.end_time.isEmpty())
            endTime = DateParser.tryParse(version.data.context.end_time.value.text())
         
         // location is optional
         location = version.data.context.location.text() // can be empty
      }
      
      // Check if the committed compo has an uid, if not, the server assigns one
      def compoUid
      if (version.data.uid.size() == 0)
      {
         compoUid = (java.util.UUID.randomUUID() as String)
         
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
      
      /* // TODO: when we support device created data, type should be taken from the XML, and the class might not be DoctorProxy but Actor.
       <composer xsi:type="PARTY_IDENTIFIED">
         <external_ref>
           <id xsi:type="HIER_OBJECT_ID">
             <value>84e1cbc5-87a8-4253-acb2-56ba35b6ef93</value>
           </id>
           <namespace>DEMOGRAPHIC</namespace>
           <type>PERSON</type>
         </external_ref>
         <name>Dr. Yamamoto</name>
       </composer>      
      */
      def composer = new DoctorProxy(
         value: version.data.composer.external_ref.id.value.text(),
         name: version.data.composer.name.text()
      )
      
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
      
      
      // ----------------------------------------------------------------------
      // Canonical transformation of the composition to a string to hash
      
      // need to add namespaces to avoid errors while serializing
      def namespaceMap = [ 'xmlns': 'http://schemas.openehr.org/v1', 'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance']
      def compo = version.data[0]
      namespaceMap.each { ns, val ->
         compo."@$ns" = val
      }
      
      def compositionString = groovy.xml.XmlUtil.serialize( compo )
      
      // Removes the added namespaces to avoid saving them on store version
      namespaceMap.each { ns, val ->
         compo.attributes().remove(ns)
      }

      def byteSize = compositionString.size()
      def hash = compositionString.md5()
      // ----------------------------------------------------------------------
      
      
      def compoIndex = new CompositionIndex(
         uid:           compoUid,  // UID for compos is assigned by the server
         category:      category,  // event / persistent
         startTime:     startTime, // mandatory for event, null for persistent
         endTime:       endTime,
         timeCommitted: auditTimeCommitted,
         location:      location,  // optional for event, null for persistent
         subjectId:     ehr.subject.value,
         ehrUid:        ehr.uid,
         organizationUid: ehr.organizationUid,
         archetypeId:   version.data.@archetype_node_id.text(),
         templateId:    version.data.archetype_details.template_id.value.text(),
         composer:      composer,
         byteSize:      byteSize,
         hash:          hash
      )
	  
	  if (!compoIndex.validate())
	  {
        println "Errors with compoIndex "+ compoIndex.errors
        throw new RuntimeException("Errors with compoIndex on parseCompositionIndex "+ compoIndex.errors)
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
         throw new CommitRequiredValueNotPresentException('version.contribution.id.value should not be empty')
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
            // changeType is only for the version audit, not for the contribution audit
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
