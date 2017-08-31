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

package com.cabolabs.archetype

import com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexItem
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexShare
import grails.util.Holders

import groovy.util.slurpersupport.*
import grails.util.Holders
import java.util.logging.Logger
import com.cabolabs.ehrserver.data.DataValues
import com.cabolabs.security.Organization

/*
 * TODO: refactor to service.
 */
class OperationalTemplateIndexer {
   
   def config = Holders.config.app
   
   def log = Logger.getLogger('com.cabolabs.archetype.OperationalTemplateIndexer')
   
   def template
   def templateIndex // OperationalTemplateIndex correspondent to the template we are indexing
   def indexes = []
   
   // RM attributes that are not in the OPT but also need to be indexed for querying.
   // This is like a schema, but is not including the attrs that are on OPTs.
   def rm_attributes_not_in_opt = [
     'COMPOSITION': [
       'context': 'EVENT_CONTEXT' // if no other_context is specified the event context is not on the OPT, we need to check if it is or not to avoid double indexing.
     ],
     'EVENT_CONTEXT': [
       'setting': 'DV_CODED_TEXT',
       'location': 'String'
     ],
     'ACTION': [
       'time': 'DV_DATE_TIME',
       'instruction_details': 'INSTRUCTION_DETAILS'
     ],
     'INSTRUCTION_DETAILS': [
       'instruction_id': 'LOCATABLE_REF',
       'activity_id': 'String'
     ],
     'INSTRUCTION': [
       'narrative': 'DV_TEXT',
       'expiry_time': 'DV_DATE_TIME'
     ],
     'ACTIVITY': [
       'timing': 'DV_PARSABLE',
       'action_archetype_id': 'String'
     ],
     'HISTORY': [
       'origin': 'DV_DATE_TIME',
       'period': 'DV_DURATION',
       'duration': 'DV_DURATION'
     ],
     'EVENT': [ // to avoid issues with cliens using abstract types, considered point event
       'time': 'DV_DATE_TIME'
     ],
     'POINT_EVENT': [
       'time': 'DV_DATE_TIME'
     ],
     'INTERVAL_EVENT': [
       'time': 'DV_DATE_TIME',
       'width': 'DV_DURATION'
     ],
     'ELEMENT': [
       'null_flavour': 'DV_CODED_TEXT'
     ]
   ]
   
   // Some attributes should not be indexed since are not used for querying
   def avoid_indexing = [
     'COMPOSITION': [
       'category' // category is not needed for querying since the arcehtype id will determine if it is event or persistent
     ]
   ]
   
   /**
    * node and relPath are used to set the name for the AII created, helping on the Query Builder to
    * identify exactly the attribute referenced by the AII. Without that we have just ELEMENT.null_flavour
    * and we should have element_name.null_flavour.
    * That works when relPath is / that means the attr belongs to the node, when the attr belongs to an
    * attr that is only on rm_attributes_not_in_opt (not in OPT), like INSTRUCTION_DETAILS.instruction_id,
    * then the name for the AII is OK to be INSTRUCTION_DETAILS.instruction_id
    * For OBSERVATION, we should have data.origin and state.origin because both data and state are HISTORY,
    * and we will have two HISTORY.origin not knowing which one is data or state (this is 2 attr names together,
    * or the name+attr of the HISTORY if both have different names).
    */
   def createIndexesForRMAttributes(String type, String archetypeId, String path, GPathResult node, String relPath, GPathResult parent)
   {
      //println "createIndexesForRMAttributes: "+ type +" "+ path +" "+ relPath +", PARENT "+ parent.name() +", NODE "+ node.name()
   
      def attrs = rm_attributes_not_in_opt[type]
      
      def name
      if (relPath == '/')
      {
         def nodeId = node.node_id.text()
         if (!nodeId) nodeId = node.parent().parent().node_id.text()
         //println "nodeID "+ nodeId +" "+ node.name() +" "+ archetypeId +" "+ path +" "+ relPath
         name = getText(parent, nodeId)
      }
      else
      {
         name = type
      }
      
      attrs.each { _attr, _attr_type ->
      
         // Some nodes might or not be in the OPT, this checks if they are and
         // avoids processing, since those will be processed later causing double
         // indexing. That is the case of COMPOSITION.context that can be in the
         // OPT if other_context is specified, causing double routes /context/location
         if (parent.attributes.find{ it.rm_attribute_name.text() == _attr}) return
      
         // if type is simple, create index, if complex, follow until finding simple
         def isSimple = true
         try
         {
            DataValues.valueOfString(_attr_type)
         }
         catch (IllegalArgumentException ex)
         {
            isSimple = false
         }
         
         if (isSimple)
         {
            indexes << new ArchetypeIndexItem(
              archetypeId: archetypeId,
              path:        ( (path == '/') ? "/"+ _attr : path +"/"+ _attr ), 
              rmTypeName:  _attr_type,
              name:        [(getTemplateLanguage(this.template)): name +'.'+ _attr] // the name is just the parentclass.attr
            )
         }
         else
         {
            createIndexesForRMAttributes(_attr_type, archetypeId, ( (path == '/') ? "/"+ _attr : path +"/"+ _attr ), node, ( (relPath == '/') ? "/"+ _attr : relPath +"/"+ _attr ), parent)
         }
      }
   }
   
   // elems?
   // [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/context/setting', rmTypeName:'DV_CODED_TEXT']
   //def dataIndexes = []
   
   // GPathResuilt corresponding to the reference to a root archetyped element
   // It helps to get the name for the ELEMENT nodes to create indexes
   //def currentRoot
   
   /**
    * @param template parsed Opt to be indexed
    * @param org organization to share the Opt with
    * @return
    */
   def createOptIndex(GPathResult template, Organization org)
   {
      def templateId = template.template_id.value.text()
      def concept = template.concept.text()
      def uid = template.uid.value.text()
      
      def archetypeId = template.definition.archetype_id.value.text()
      def archetypeConcept = template.definition.term_definitions.find { it.@code.text() == 'at0000' }.items.find { it.@id == 'text' }.text()
      
      def templateIndex = new OperationalTemplateIndex(
         templateId: templateId,
         concept: concept,
         language: getTemplateLanguage(template),
         uid: uid,
         archetypeId: archetypeId,
         archetypeConcept: archetypeConcept
      )
      if (!templateIndex.save(flush:true)) println templateIndex.errors // TODO: log errors and throw except
      
      
      // Create share
      // FIXME: use resourceService to do the share, but we need to refactor this class to a service...
      def share = new OperationalTemplateIndexShare(opt: templateIndex, organization: org)
      share.save(failOnError:true, flush:true)

      return templateIndex
   }
   
   /**
    * Who uses the system can place initial OPTs generated by tools, that do not
    * use UUIDs for file naming, so OPT files names can be anything. This method
    * takes the base OPTs from the base_opts folder, moves them to the opt_repo
    * with the correct fileUid as name, and executes indexAll to load the indexes
    * for those files.
    * 
    * @return
    */
   def setupBaseOpts()
   {
      def opts_path = config.opt_repo
      def base_path = config.opt_repo.withTrailSeparator() + 'base_opts'
      def base_repo = new File( base_path )
      
      if (!base_repo.exists()) throw new Exception("No existe "+ base_path)
      if (!base_repo.canRead()) throw new Exception("No se puede leer "+ base_path)
      if (!base_repo.isDirectory()) throw new Exception("No es un directorio "+ base_path)
      
      // Only copy the base opts if the opt repo is empty, to avoid copying again every time the app starts.
      if (new File(opts_path).listFiles().count { it.name ==~ /.*\.opt/ } == 0)
      {
         def dest
         base_repo.eachFileMatch groovy.io.FileType.FILES, ~/.*\.opt/, { file ->
            
            dest = new File(opts_path + System.getProperty("file.separator") + String.uuid() + '.opt')
            java.nio.file.Files.copy(file.toPath(), dest.toPath())
         }
      }
   }
   
   /**
    * Helps on updating/overwriting an existing template by deleting the previous indexes.
    * @param opt
    * @return
    */
   def deleteOptReferences(OperationalTemplateIndex opt)
   {
      //println "opt: "+ opt.templateId
     
      def archNodes = opt.referencedArchetypeNodes.collect() // avoid ConcurrentModificationException

      archNodes.each { archNode ->
      
         opt.removeFromReferencedArchetypeNodes(archNode)
      }
      
      opt.save(flush:true)
      opt.delete(flush:true)
      
      archNodes.each { archNode ->
      
         archNode.refresh()
         
         if (archNode.parentOpts.size() > 0) // other opts reference the same archNode, do not delete
         {
            //println " 1- archNode not deleted, it has more than one parent template "
         }
         else
         {
            //println " 2- arch parent opt "+ archNode.parentOpts.templateId

            // delete the arch node that is only referenced from this opt
            // it will be regenerated by the indexAll, if the node was not
            // removed from the opt.
            archNode.delete(flush:true)
         }
      }
   }
   
   def indexAll(Organization org)
   {
      def path = config.opt_repo
      def repo = new File( path )
      
      if (!repo.exists()) throw new Exception("No existe "+ path)
      if (!repo.canRead()) throw new Exception("No se puede leer "+ path)
      if (!repo.isDirectory()) throw new Exception("No es un directorio "+ path)
      
      // The first indexAll should also share because there are no OPT, there are no shares
      def shareWithOrg = false // we create the shares here
      
      //println "opt count "+ OperationalTemplateIndex.count()
      //println "shareWithOrg "+ shareWithOrg
      
      // remove indexes associated with the org, the create new ones
      def shares = OperationalTemplateIndexShare.findAllByOrganization(org)
      def opt, archNodes, other_shares
      def shares_to_delete = []
      //def share_with_orgs = [org] // share/reshare OPTs with these orgs
      
      def reshares = [:] // templateId > [org]

      // the first time OPTs are indexed, it wont access this loop because there are no shares,
      // so the OPT will be shared with the default org.
      shares.each { share ->
      
         opt = share.opt
         
         //share.opt = null
         other_shares = OperationalTemplateIndexShare.findAllByOpt(opt) // includes current share
         
         other_shares.each { reshare ->
            if (reshares[reshare.opt.templateId] == null) reshares[reshare.opt.templateId] = []
            reshares[reshare.opt.templateId] << reshare.organization
         }
         
         //share_with_orgs = other_shares.organization
         
         other_shares*.delete(flush:true)
         
         deleteOptReferences(opt)
         
         //share.delete(flush:true)
         //println "opt count "+ OperationalTemplateIndex.count()
      }

      // TODO: the new template should be shared with the same orgs...
      def share
      repo.eachFileMatch groovy.io.FileType.FILES, ~/.*\.opt/, { file ->
         
         // Load only if the name is an UUID, it is the OperationalTemplateIndex.fileUid
         try
         {
            UUID uuid = UUID.fromString( file.name - '.opt' )
            opt = index(file, org, shareWithOrg)
            
            /*
            share_with_orgs.each { share_with_org ->
            
               share = new OperationalTemplateIndexShare(opt: opt, organization: share_with_org)
               share.save(failOnError:true, flush:true)
            }
            */
            
         }
         catch (IllegalArgumentException exception)
         {
             println "File ${file.name} not indexed, the file name should be an UUID. Please put your initial OPT in the base_opts folder."
         }
      }
      
      // initial state, for bootstrap
      // just share the base OPTs wit hthe sample orgs
      if (!shares)
      {
         def opts = OperationalTemplateIndex.list()
         def orgs = Organization.list()
         opts.each { _opt ->
            if (reshares[_opt.templateId] == null) reshares[_opt.templateId] = []
            reshares[_opt.templateId] = orgs
         }
      }
      
      reshares.each { templateId, organizations ->
         opt = OperationalTemplateIndex.findByTemplateId(templateId)
         organizations.each { _org ->
            share = new OperationalTemplateIndexShare(opt: opt, organization: _org)
            share.save(failOnError:true, flush:true)
         }
      }
   }
   
   private boolean templateAlreadyExistsForOrg(GPathResult template, Organization org)
   {
      // from OperationalTemplateController.upload
      // check existing by OPT uid or templateId, shared with an org of the current user
      def opt_uid = template.uid.value.text()
      def opt_template_id = template.template_id.value.text()
      
      def c = OperationalTemplateIndexShare.createCriteria()
      def shares = c.list {
         // exists an OPT with uid or template id?
         opt {
            or {
               eq('uid', opt_uid)
               eq('templateId', opt_template_id)
            }
         }
         eq('organization', org)
      }
      
      // 1. there is one share, with the session org => overwrite if specified
      // 2. there is one share, with another org of the current user => can't overwrite, should upload the OPT and overwrite while logged with that org
      // 3. there are many shares => can't overwrite, should remove the shares first
      
      if (shares.size() != 0)
      {
         return true
      }
      
      return false
   }
   
   private String getTemplateLanguage(GPathResult template)
   {
      template.language.terminology_id.value.text() +"::"+ template.language.code_string.text()
   }
   
   def index(GPathResult template, String file_uid, Organization org, boolean shareWithOrg)
   {
      // TODO: refactor, this is 99% createOptIndex()
      this.template = template
      
      // Create opt index
      def templateId = this.template.template_id.value.text()
      def concept = this.template.concept.text()
      def language = getTemplateLanguage(template)
      def uid = this.template.uid.value.text()
      
      def archetypeId = this.template.definition.archetype_id.value.text()
      def archetypeConcept = this.template.definition.term_definitions.find { it.@code.text() == 'at0000' }.items.find { it.@id == 'text' }.text()
      
      // if it is not coming from an alraedy indexed OPT
      if (!this.templateIndex)
      {
         this.templateIndex = new OperationalTemplateIndex(
            templateId: templateId,
            concept: concept,
            language: language,
            uid: uid,
            archetypeId: archetypeId,
            archetypeConcept: archetypeConcept
         )
         
         if (file_uid) this.templateIndex.fileUid = file_uid
         
         // TODO: log errors and throw except
         if (!this.templateIndex.save(flush:true)) println this.templateIndex.errors
         
         if (shareWithOrg)
         {
            // Create share
            // FIXME: use resourceService to do the share, but we need to refactor this class to a service...
            def share = new OperationalTemplateIndexShare(opt: this.templateIndex, organization: org)
            share.save(failOnError:true, flush:true)
         }
      }
      
      indexObject(this.template.definition, '/', '/', this.template.definition, false)
      
      
      // AII for COMPOSITION
      this.indexes << new ArchetypeIndexItem(
         archetypeId: archetypeId,
         path: '/',
         rmTypeName: 'COMPOSITION',
         name: [(getTemplateLanguage(this.template)): archetypeConcept]
      )
      
      //println this.paths // test
      this.indexes.each { di ->
         
         if (di.instanceOf(ArchetypeIndexItem))
         {
            def existingIndex = ArchetypeIndexItem.findByArchetypeIdAndPathAndRmTypeName(di.archetypeId, di.path, di.rmTypeName)
            
            // If the archetype index item already exists, process the name to see if it is on a different lang 
            if (existingIndex)
            {
               // If the template is in a language not yet indexed in the name of the existingIndex, 
               // add the name of the existingIndex in the language of the template.
               // The issue is that templates are defined for one language, and archetype paths can be
               // referenced from many templates, using different languages, so the name associated
               // with each path needs to be recorded in each language.
               def opt_lang = getTemplateLanguage(this.template)
               def lang_found = existingIndex.name.find { it.key == opt_lang }
               if (!lang_found)
               {
                  //println "adding a new language to the archetype index: "+ di.archetypeId + di.path +": "+ opt_lang +" "+ di.name[opt_lang]
                  existingIndex.name[opt_lang] = di.name[opt_lang] // copies the name from the new index to the existing one
               }

               
               // check if the template already has the arch node index, if not, add it to the template.
               if (!templateIndex.referencedArchetypeNodes.find {it.archetypeId == di.archetypeId && it.path == di.path && it.rmTypeName == di.rmTypeName})
               {
                  //println "reference existing "+ templateIndex.templateId +" > "+ di.archetypeId + di.path
                  templateIndex.addToReferencedArchetypeNodes(existingIndex) // adds the existing one, do not create a new one
               }
            }
            else
            {
               templateIndex.addToReferencedArchetypeNodes(di)
            }
         }
         else
         {
            templateIndex.addToTemplateNodes(di)
         }
      }
      this.indexes = []
   }
   
   def index(File templateFile, Organization org, boolean shareWithOrg)
   {
      if (!templateFile.exists())  throw new Exception("No existe "+ templateFile.getAbsolutePath())
      if (!templateFile.canRead())  throw new Exception("No se puede leer "+ templateFile.getAbsolutePath())
      if (!templateFile.isFile())  throw new Exception("No es un archivo "+ templateFile.getAbsolutePath())
      
      // The names of the OPT files from the OPT repo, should be their fileUid
      def file_uid = templateFile.name - '.opt'
      def template = new XmlSlurper().parseText( templateFile.getText() ) // GPathResult
      
      // index only if the opt doesnt exist, this avoids to load 2 opts with the same concept or uid from indexAll
      if (!templateAlreadyExistsForOrg(template, org))
      {
         index(template, file_uid, org, shareWithOrg)
      }
      else
      {
         log.info('File '+ templateFile.name +' was not loaded because other template with the same concept or UID is already loaded')
      }
      
      
      def opt = this.templateIndex
      
      // reset for the next index, used from indexAll
      this.template = null
      this.templateIndex = null
      
      return opt
   }
   
   /*
    * templateFileName es el nombre del archivo sin la extension.
    */
   def index(String fullTemplateFileName)
   {
      def path = fullTemplateFileName
      def tfile = new File( path )
      // file exists is checked inside
      return index(tfile)
   }
   
   def indexAttribute(GPathResult node, String parentPath, String archetypePath, GPathResult parent, boolean indexingDatavalue)
   {
      if (!node) throw new Exception("Nodo vacio")

      def nextPath
      def nextArchPath
      node.children.each {
         
         //println "child "+ it.name()
         if (parentPath == '/') nextPath = parentPath + node.rm_attribute_name.text()
         else nextPath = parentPath +'/'+ node.rm_attribute_name.text()
         
         if (archetypePath == '/') nextArchPath = archetypePath + node.rm_attribute_name.text()
         else nextArchPath = archetypePath +'/'+ node.rm_attribute_name.text()
         
         indexObject(it, nextPath, nextArchPath, parent, indexingDatavalue)
      }
   }
   
   
   def getText(node, nodeId)
   {
      def term = node.term_definitions.find { it.@code.text() == nodeId } // <term_definitions code="at0000">
      def text = term.items.find { it.@id.text() == "text" }.text() // <items id="text">Tobacco Use Summary</items>
      return text
   }
   
   /*
    * Procesa nodos objeto de la definicion del template.
    * node es un elemento C_OBJECT ej. <children xsi:type="C_COMPLEX_OBJECT"> con rm_type_name, node_id, ...
    */
   // https://github.com/ppazos/cabolabs-ehrserver/issues/471
   // indexingDatavalue flag to mark the index of a datavalue through the process, and avoid adding nodeIds on case of true.
   def indexObject (GPathResult node, String parentPath, String archetypePath, GPathResult parent, boolean indexingDatavalue)
   {
      if (!node) throw new Exception("Nodo vacio")
      
      if (node.rm_type_name.text().startsWith('DV_INTERVAL'))
      {
         println "DV_INTERVAL indexes not yet supported"
         return
      }
      
      /*
      println "indexObject "+ this.template.template_id.value.text() +": "+
                              parent.archetype_id.value.text() +" "+ archetypePath
      */
      
      // this is not indexing RM attributes that are not in the OPT, like ACTION.time or ACTION.instruction_details
      
      // just to validate the type against the supported types
      def validIndexType = true
      try
      {
         DataValues.valueOfString(node.rm_type_name.text())
         indexingDatavalue = true
         //println "indexingDatavalue "+ node.rm_type_name.text() +" "+ archetypePath
      }
      catch (IllegalArgumentException ex)
      {
         validIndexType = false
      }
      
      
      // Avoid slots
      if (node.'@xsi:type'.text() == "ARCHETYPE_SLOT")
      {
         log.info('ARCHETYPE_SLOT found, further indexing avoided for template '+ this.template.template_id.value.text())
         return
      }
      
      //println "indexObject: "+ node.name() // children con xsi:type como attr y rm_type_name como hijo 0
      
      def path = parentPath
      
      //println path
      
      // Archetype Roots will have Term Definitions inside from where the name of the indexes should be taken.
      // Each Archetype Root Terminology is independent from the other Archetype Roots.
      //println "Node type: "+ node.'@xsi:type'
      if (node.'@xsi:type'.text() == "C_ARCHETYPE_ROOT")
      {
         // Helps to get the name for the indexed ELEMENTs
         parent = node
         
         path += '[archetype_id='+ node.archetype_id.value +']' // slot in the path instead of node_id
         archetypePath = '/' // archetype root found
         
         // test
         //println "index root "+ node.name() +" nodeid "+ node.node_id.text()
         
         // Adding indexes for root nodes, needed for querying with concept names instead of archetype ids
         def archIndexIndex = new ArchetypeIndexItem(
            archetypeId: parent.archetype_id.value.text(),
            path: archetypePath,
            rmTypeName: node.rm_type_name.text(),
            name: [(getTemplateLanguage(this.template)): getText(node, node.node_id.text())]
         )
         
         def optIndexItem = new OperationalTemplateIndexItem(
            templateId: this.template.template_id.text(),
            path: path,
            rmTypeName: node.rm_type_name.text(),
            name: getText(node, node.node_id.text())
         )
         
         indexes << archIndexIndex
         indexes << optIndexItem
      }
      else
      {
         if (path != '/' && !node.node_id.isEmpty() && node.node_id.text() != '')
         {
            path += '['+ node.node_id.text() + ']'
            
            // Avoid adding nodeIds for datavalue attributes https://github.com/ppazos/cabolabs-ehrserver/issues/471
            if (!indexingDatavalue)
            {
               archetypePath += '['+ node.node_id.text() + ']'
               //println "!indexingDatavalue, add nodeId to path "+ archetypePath
            }
         }
      }
      
      //println "parent "+ parent.name()
      //println "current rm_type_name "+ node.rm_type_name.text()
      
      /*
       * If current node should be indexed based on its rm_type_name
       */
      if ( !node.rm_type_name.isEmpty() && validIndexType )
      {
         // test
         //this.paths << path
         println " > index: "+ path +' '+ node.rm_type_name.text()

         // --------------------------------------------------------
         // Find node name
         def nodeId = node.node_id.text()
         def term
         def description
         def addParentAttrName = false // https://github.com/ppazos/cabolabs-ehrserver/issues/103
         
         // For datatypes there is no nodeId, we should get the nodeId of the parent ELEMENT,
         // but some archetype editors add nodeIds to datatypes, for now we avoid to set nodeId for datatype paths.
//         if (!nodeId)
//         {
            // .parent es attributes 'value', .parent.parent es children 'ELEMENT'
            nodeId = node.parent().parent().node_id.text()
            addParentAttrName = true
//         }
         
         /* reference structure:
          * <term_definitions code="at0000">
              <items id="text">Tobacco Use Summary</items>
              <items id="description">Summary or persisting information about tobacco use or consumption.</items>
            </term_definitions>
          */
//         term = this.currentRoot.term_definitions.find { it.@code.text() == nodeId } // <term_definitions code="at0000">
         
         /*
         term = parent.term_definitions.find { it.@code.text() == nodeId } // <term_definitions code="at0000">
         description = term.items.find { it.@id.text() == "text" }.text() // <items id="text">Tobacco Use Summary</items>
         */
         
         description = getText(parent, nodeId)
         
         if (addParentAttrName)
         {
            def parentAttrName = node.parent().rm_attribute_name.text()
            
            // Avoids to add .value to at the ELEMENT.value indexes, for those we want to show just the parent ELEMENT name
            if (parentAttrName != 'value')
            {
               description += '.'+ parentAttrName // node.parent() is a C_ATTRIBUTE
            }
         }
         
         // --------------------------------------------------------
         // Find type of ELEMENT.value
         
         /* reference structure:
          * <attributes xsi:type="C_SINGLE_ATTRIBUTE">
              <rm_attribute_name>value</rm_attribute_name>
              ...                                    
              <children xsi:type="C_COMPLEX_OBJECT">
                <rm_type_name>DV_BOOLEAN</rm_type_name>
          */
         //def valueNode = node.attributes.find { it.rm_attribute_name.text() == "value" }
         //def type = valueNode.children[0].rm_type_name.text() // DV_BOOLEAN
         
         def type = node.rm_type_name.text() // DV_BOOLEAN
         def archIndexIndex = new ArchetypeIndexItem(
           archetypeId: parent.archetype_id.value.text(),
           path: archetypePath, // +"/value", 
           rmTypeName: type,
           name: [(getTemplateLanguage(this.template)): description]
         )
         
         def optIndexItem = new OperationalTemplateIndexItem(
            templateId: this.template.template_id.text(),
            path: path,
            rmTypeName: type,
            name: description
         )
         
         // https://github.com/ppazos/cabolabs-ehrserver/issues/137
         if (type == 'DV_CODED_TEXT')
         {
            def def_code_node = node.attributes.find{ it.rm_attribute_name.text() == 'defining_code' }
            def uri = def_code_node.children.referenceSetUri.text()
            if (uri) archIndexIndex.terminologyRef = uri
         }
         
         indexes << archIndexIndex
         indexes << optIndexItem
      }
      
      
      // Index attrs not in the OPT
      if ( !node.rm_type_name.isEmpty() )
      {
         createIndexesForRMAttributes(node.rm_type_name.text(), parent.archetype_id.value.text(), archetypePath, node, '/', parent)
      }
      
      
      // continue processing
      node.attributes.each {
         println node.rm_type_name.text() +" attr "+ it.rm_attribute_name.text() +" path "+ path +" apath "+ archetypePath
         
         if (avoid_indexing[node.rm_type_name.text()]?.contains(it.rm_attribute_name.text())) return
         
         indexAttribute(it, path, archetypePath, parent, indexingDatavalue) // No pone su nodeID porque es root
      }
      
      //indexingDatavalue = false // reset flag after processing children attrs of datavalue
      // ===========================================================================
   }
}
