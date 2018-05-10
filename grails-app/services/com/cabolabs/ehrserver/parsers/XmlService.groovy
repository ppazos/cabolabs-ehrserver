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

   def CATEGORY_EVENT = "433"
   def CATEGORY_PERSISTENT = "431"

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
      def parsedVersionsMap = parseVersions(ehr, versions, auditTimeCommitted, auditSystemId, contribution)

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
      manageVersionedCompositions(contribution.versions, ehr)

      // If contribution and versions can be saved ok
      //  - check if file exists, error if exists
      //  - save version XML files on file system
      storeVersionXMLs(ehr, parsedVersionsMap, contribution)

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

         // event compose require context.start_time to be not empty
         if (versionXML.data.category.defining_code.code_string.text() == CATEGORY_EVENT)
         {
            if (versionXML.data.context.size() == 0 ||
                versionXML.data.context.start_time.size() == 0 ||
                versionXML.data.context.start_time.value.size() == 0)
            {
               errors[i] = ['version.data.context.start_time.value should be present when version.data.category.defining_code.code_string is 433']
            }
         }
      }

      this.validationErrors = errors

      /*
      println errors

      if (errors.size() > 0)
      {
         println groovy.xml.XmlUtil.serialize( versions )
      }
      */

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
    * because https://github.com/ppazos/cabolabs-ehrserver/issues/742 version.uid will be assigned by server
    * versioning will be done using the version.preceding_version_uid.value
    */
   def checkVersions(GPathResult versions)
   {
      def errors = [:]
      def change_type, object_id, preceding_version_uid, version
      versions.version.eachWithIndex { parsedVersion, i ->

         change_type = parsedVersion.commit_audit.change_type.defining_code.code_string.text()
         preceding_version_uid = parsedVersion.preceding_version_uid.value.text() // 0..1, uuid::system_id::version_tree_id

         if (change_type == '249') // 249 == creation
         {
            if (preceding_version_uid)
            {
               errors[i] = ["A contribution has change_type 'creation' and includes a preceding_version_uid ${preceding_version_uid}. The use of preceding_version_uid is only for non 'creation' commits."]
            }
         }
         else // change_type != creaton
         {
            if (!preceding_version_uid)
            {
               errors[i] = ["A contribution has change_type different from 'creation' and doesn't includes version.preceding_version_uid. Modifications or amendments require preceding_version_uid to be present."]
            }
            else
            {
               /*
               // object_id from preceding version should exist
               object_id = preceding_version_uid.split("::")[0] // uuid
               if (VersionedComposition.countByUid(object_id) == 0)
               {
                  errors[i] = ["The preceding_version_uid ${preceding_version_uid} references and object_id ${object_id} that doesn't exists."]
               }
               */

               // Version should exist with the preceding_version_uid
               // This includes the case of the VersionedComposition existence for the object_id part
               // of the preceding_version_uid, so we don't need the check commented above
               if (Version.countByUid(preceding_version_uid) == 0)
               {
                  errors[i] = ["The preceding_version_uid ${preceding_version_uid} references a version that doesn't exists."]
               }
               else // if there are versions, the new version should version the latest version
               {
                  // version that should be versioned, might not be the latest version for the versioned compo
                  version = Version.findByUid(preceding_version_uid)

                  // should version the latest version
                  if (!version.data.lastVersion)
                  {
                     errors[i] = ["A change was committed for the version ${preceding_version_uid} that is not the latest version of the composition, please use the checkout service to get the latest version uid"]
                  }
               }
            }
         }
      }

      this.validationErrors = errors

      if (this.validationErrors.size() > 0) throw new CommitWrongChangeTypeException('Please check the detailed errors.')
   }


   /**
    * This method manages the versions, creating new versioned compositions
    * or adding the new version to an existing versioned compo.
    * @param parsedVersions
    * @return
    */
   def manageVersionedCompositions(List domainVersions, Ehr ehr)
   {
      domainVersions.each { version ->

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
   def storeVersionXMLs(Ehr ehr, Map versions, Contribution contribution)
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
      versions.each { version_uid, versionXML ->

         try
         {
            //println "XML version.uid "+ versionXML.uid.value // null because XMLSlurper doesn't evalaute the XML after adding the uid to the XML

            version = contribution.versions.find { it.uid == version_uid}
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
   def parseVersions(Ehr ehr, GPathResult versionsXML, Date auditTimeCommitted, String auditSystemId, Contribution contribution)
   {
      // Because checkVersions was executed before we know all versions have
      // - change_type creation and no preceding_version_uid, or,
      // - change_type != creation and preceding_version_uid, and,
      // - object_id in preceding_version_uid exists

      def commitAudit, compoIndex, version, existingPersistentCompo,
          version_uid, preceding_version_uid, preceding_version_uid_parts,
          object_id, version_tree_id, lastVersion, previousLastVersion

      // version_uid => versionXML (GPathResult)
      def dataOut = [:]

      def parsedVersion
      for (int i = 0; i <versionsXML.version.size(); i++)
      {
         parsedVersion = versionsXML.version[i]

         // Parse AuditDetails from Version.commit_audit
         commitAudit = parseVersionCommitAudit(parsedVersion, auditTimeCommitted)
         compoIndex = parseCompositionIndex(parsedVersion, ehr, auditTimeCommitted)

         // version.uid is assigned by the server
         // For new version
         // version_uid = String.uuid() +'::'+ auditSystemId +'::1'
         // For modifications
         // version_uid = preceding_version_uid.split("::")[0] +'::'+ auditSystemId +'::'+ addTrunkVersion(preceding_version_uid)
         if (commitAudit.changeType == ChangeType.CREATION)
         {
            version_uid = String.uuid() +'::'+ auditSystemId +'::1'
         }
         else
         {
            preceding_version_uid = parsedVersion.preceding_version_uid.value.text()
            preceding_version_uid_parts = preceding_version_uid.split("::")
            object_id = preceding_version_uid_parts[0] // uuid
            version_tree_id = preceding_version_uid_parts[2] // 1, 2, 3, etc. major version only!

            // this is doing what version.addTrunkVersion did before, addTrunkVersion is no longer needed
            version_uid = object_id +'::'+ auditSystemId +'::'+ (new Integer(version_tree_id) + 1).toString()
         }


         // El uid se lo pone el servidor: object_id::creating_system_id::version_tree_id
         // - object_id se genera (porque el changeType es creation)
         // - creating_system_id se obtiene del cliente
         // - version_tree_id es 1 (porque el changeType es creation)
         //
         version = new Version(
            uid: version_uid,
            lifecycleState: parsedVersion.lifecycle_state.defining_code.code_string.text(),
            commitAudit: commitAudit,
            data: compoIndex
         )

         if (preceding_version_uid) version.precedingVersionUid = preceding_version_uid

         // Verify correct parameters for persistent commit
         // If the version is a persistent composition, and for the EHR there is already a version,
         // the change type should be "modification", if not, CommitWrongChangeTypeException
         // If everything is OK, the version UID should be checked: it should be the same as the compo index for the ehrid and archetype checked above.

         if (compoIndex.category == "persistent")
         {
            existingPersistentCompo = CompositionIndex.findByCategoryAndArchetypeIdAndEhrUid(compoIndex.category, compoIndex.archetypeId, compoIndex.ehrUid)

            if (commitAudit.changeType == ChangeType.CREATION && existingPersistentCompo)
            {
               throw new CommitWrongChangeTypeException("A persistent composition for ${compoIndex.archetypeId} already exists in the EHR ${compoIndex.ehrUid}, so the change type should not be 'creation', it should be 'modification'.")
            }

            if (commitAudit.changeType != ChangeType.CREATION && !existingPersistentCompo)
            {
               throw new CommitWrongChangeTypeException("A persistent composition for ${compoIndex.archetypeId} does not exists in the EHR ${compoIndex.ehrUid}, the change type should be 'creation' and it is ${commitAudit.changeType}")
            }

            // Persistent compo versioning process
            // If is modification/amendment and exists a persistent compo for the archid and ehruid
            if ([ChangeType.MODIFICATION, ChangeType.AMENDMENT].contains(commitAudit.changeType) && existingPersistentCompo)
            {
               // checkVersions checked the preceding_version_uid is the uid of the last version of the  versioned compo
               // and also checked for these change types, the preceding_version_uid should be present
               lastVersion = Version.findByUid(preceding_version_uid)

               previousLastVersion = lastVersion
               previousLastVersion.data.lastVersion = false

               if (!previousLastVersion.save()) println previousLastVersion.errors.allErrors

            }
         }
         else // event
         {
            // checkVersions already verified the change type and the preceding_version_uid
            // - checked the preceding_version_uid is the uid of the last version of the  versioned compo
            // - and also checked for these change types, the preceding_version_uid should be present

            // create new version for event compo
            if (commitAudit.changeType != ChangeType.CREATION)
            {
               // same code as versioning a persistent compo, TODO: refactor
               lastVersion = Version.findByUid(preceding_version_uid)

               previousLastVersion = lastVersion
               previousLastVersion.data.lastVersion = false

               if (!previousLastVersion.save()) println previousLastVersion.errors.allErrors
            }
         }

         // Server sets the XML with the new version uid.

         // delete uid if present from the client
         parsedVersion.uid.replaceNode {}

         // append sibling after commit_audit adds version.uid
         parsedVersion.commit_audit.replaceNode { commit_audit ->
            mkp.yield(commit_audit)
            uid {
               value(version.uid)
            }
         }

         dataOut[version.uid] = parsedVersion
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
      String category
      String category_code = version.data.category.defining_code.code_string.text()

      // extract string value that will be persisted in the compo index
      switch (category_code)
      {
         case CATEGORY_PERSISTENT:
            category = 'persistent'
         break
         case CATEGORY_EVENT:
            category = 'event'
         break
         default:
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

      if (category_code == CATEGORY_EVENT)
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
