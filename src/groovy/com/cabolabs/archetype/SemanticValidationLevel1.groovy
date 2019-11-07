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

import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexItem
import com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem
import groovy.util.slurpersupport.GPathResult
import java.util.logging.Logger
import grails.util.Holders
//import com.cabolabs.ehrserver.data.DataValues
import com.cabolabs.openehr.opt.manager.OptManager
import org.springframework.web.context.request.RequestContextHolder
import com.cabolabs.openehr.opt.model.OperationalTemplate

/*
 * This class verifies that COMPOSITION instances parsed to GPathResult in the commit
 * comply with the referenced template, only at the structural level (doesn't validate data).
 * This means it checks for each node in the COMPOSITION, if it's defined inside the template,
 * in other words, the COMPOSITION should include nodes defined by the referenced template and
 * only those nodes, any extra nodes not defined by the template mean there is a semantic violation.
 *
 * Even if some nodes are not in the OPT, but are part of the RM and might come in the COMPO,
 * AIIs and TIIs are created for those nodes, so all notes explicit or implicit for all the
 * COMPOs that comply with an OPT, should have a correspondent AII/TII in the database.
 *
 * A level 2 validator would check occurrence and cardinality constraints.
 * A level 3 validator would check the data constraints.
 */
class SemanticValidationLevel1 {

   def log = Logger.getLogger('com.cabolabs.archetype.SemanticValidationLevel1')
   String namespace
   Map errors = [:] // compo index => error list

   // pruned attrs
   // name is attr from LOCATABLE
   // archetype_details, ..., composer are attrs from COMPOSITION
   // subject, encoding are attrs of ENTRY
   // TODO: it might be shorter to write wich attributes to process
   // COMPOSITION.context should be processed only if it's on the OPT,
   // when context.other_context is not constrained, the OPT won't have a /context node
   // but the COMPO will have context for all the event COMPOS, that shouldn't be reported
   // as an error.
   def avoid_procesing_attrs = [
     COMPOSITION  : ['name', 'archetype_details', 'uid', 'language', 'territory', 'category', 'composer'],
     EVENT_CONTEXT: ['start_time', 'end_time', 'setting', 'location'],
     SECTION      : ['name', 'archetype_details', 'uid'],
     OBSERVATION  : ['name', 'archetype_details', 'uid', 'language', 'encoding', 'subject'],
     EVALUATION   : ['name', 'archetype_details', 'uid', 'language', 'encoding', 'subject'],
     INSTRUCTION  : ['name', 'archetype_details', 'uid', 'language', 'encoding', 'subject', 'narrative', 'expiry_time'],
     ACTIVITY     : ['name', 'archetype_details', 'uid', 'timing', 'action_archetype_id'],
     ACTION       : ['name', 'archetype_details', 'uid', 'language', 'encoding', 'subject', 'time'],
     INSTRUCTION_DETAILS: ['instruction_id', 'activity_id'],
     ISM_TRANSITION     : ['current_state', 'transition', 'careflow_step'],
     ADMIN_ENTRY   : ['name', 'archetype_details', 'uid', 'language', 'encoding', 'subject'],
     HISTORY       : ['name', 'archetype_details', 'uid', 'origin', 'period', 'duration'],
     EVENT         : ['name', 'archetype_details', 'uid', 'time'], // treated as POINT_EVENT
     POINT_EVENT   : ['name', 'archetype_details', 'uid', 'time'],
     INTERVAL_EVENT: ['name', 'archetype_details', 'uid', 'time', 'width', 'math_function'],
     ITEM_SINGLE   : ['name', 'archetype_details', 'uid'],
     ITEM_LIST     : ['name', 'archetype_details', 'uid'],
     ITEM_TABLE    : ['name', 'archetype_details', 'uid'],
     ITEM_TREE     : ['name', 'archetype_details', 'uid'],
     CLUSTER       : ['name', 'archetype_details', 'uid'],
     ELEMENT       : ['name', 'value', 'archetype_details', 'uid', 'null_flavour']
   ]

   /* classes that are not LOCATABLE, so don't have node_id in the path */
   def pathables = [
     'EVENT_CONTEXT',
     'ISM_TRANSITION',
     'INSTRUCTION_DETAILS'
   ]


   // attrs that should be avoided, but children should be processed
   // COMPO.context doesn't have an AII, but it's children do
   def continue_with_children_attrs = ['context']

   // for the lists, the type is the type of the elements in the list, since the XML have all
   // the items with the same name and type as the list items, there is no LIST type there.
   // _ask_node_ is used where there is an abstract type with many subclasses, and the XML
   // should have the concrete class on xsi:type.
   def rm_schema = [
     'COMPOSITION': [
       'context'      : 'EVENT_CONTEXT',
       'content'      : '_ask_node_',
       'language'     : 'CODE_PHRASE',
       'territory'    : 'CODE_PHRASE',
       'category'     : 'DV_CODED_TEXT',
       'name'         : 'DV_TEXT', // can be coded text
       'uid'          : 'HIER_OBJECT_ID',
       'archetype_details': 'ARCHETYPED',
       'composer'     : '_ask_node_' // PARTY_IDENTIFIED, PARTY_SELF, PARTY_RELATED
     ],
     'EVENT_CONTEXT': [
       'start_time'   : 'DV_DATE_TIME',
       'end_time'     : 'DV_DATE_TIME',
       'setting'      : 'DV_CODED_TEXT',
       'location'     : 'String',
       'other_context': '_ask_node_',
       'health_care_facility': '_ask_node_'
       // participations
     ],
     'SECTION': [
       'items'               : '_ask_node_', //'_list_', // LIST<CONTENT_ITEM> = LIST<_ask_node_>
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'OBSERVATION': [
       'data'                : 'HISTORY',
       'state'               : 'HISTORY',
       'protocol'            : '_ask_node_',
       'language'            : 'CODE_PHRASE',
       'encoding'            : 'CODE_PHRASE',
       'subject'             : '_ask_node_',
       // provider
       // other_participations
       // workflow_id
       // guideline_id
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'EVALUATION': [
       'data'                : '_ask_node_',
       'protocol'            : '_ask_node_',
       'language'            : 'CODE_PHRASE',
       'encoding'            : 'CODE_PHRASE',
       'subject'             : '_ask_node_',
       // provider
       // other_participations
       // workflow_id
       // guideline_id
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'INSTRUCTION': [
       'narrative'           : 'DV_TEXT',
       'expiry_time'         : 'DV_DATE_TIME',
       'activities'          : 'ACTIVITY', //'_list_', // LIST<ACTIVITY>
       // wf_definition
       'protocol'            : '_ask_node_',
       'language'            : 'CODE_PHRASE',
       'encoding'            : 'CODE_PHRASE',
       'subject'             : '_ask_node_',
       // provider
       // other_participations
       // workflow_id
       // guideline_id
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'ACTIVITY': [
       'description'         : '_ask_node_',
       'timing'              : 'DV_PARSABLE',
       'action_archetype_id' : 'String',
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'ACTION': [
       'time'                : 'DV_DATE_TIME',
       'description'         : '_ask_node_',
       'ism_transition'      : 'ISM_TRANSITION',
       'instruction_details' : 'INSTRUCTION_DETAILS',
       'protocol'            : '_ask_node_',
       'language'            : 'CODE_PHRASE',
       'encoding'            : 'CODE_PHRASE',
       'subject'             : '_ask_node_',
       // provider
       // other_participations
       // workflow_id
       // guideline_id
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'INSTRUCTION_DETAILS': [
       'instruction_id'      : 'LOCATABLE_REF',
       'activity_id'         : 'String',
       'wf_details'          : '_ask_node_'
     ],
     'ISM_TRANSITION': [
       'current_state'       : 'DV_CODED_TEXT',
       'transition'          : 'DV_CODED_TEXT',
       'careflow_step'       : 'DV_CODED_TEXT'
     ],
     'ADMIN_ENTRY': [
       'data'                : '_ask_node_',
       'language'            : 'CODE_PHRASE',
       'encoding'            : 'CODE_PHRASE',
       'subject'             : '_ask_node_',
       // provider
       // other_participations
       // workflow_id
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'HISTORY': [
       'origin'              : 'DV_DATE_TIME',
       'period'              : 'DV_DURATION',
       'duration'            : 'DV_DURATION',
       'events'              : '_ask_node_', // '_list_' // LIST<EVENT> = LIST<_ask_node_>,
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'EVENT': [ // to avoid issues with cliens using abstract types, is considered as point event
       'time'                : 'DV_DATE_TIME',
       'data'                : '_ask_node_',
       'state'               : '_ask_node_',
       'name'                : 'DV_TEXT', // can be coded text
       'uid'                 : 'HIER_OBJECT_ID',
       'archetype_details'   : 'ARCHETYPED'
     ],
     'POINT_EVENT': [
       'time'               : 'DV_DATE_TIME',
       'data'                : '_ask_node_',
       'state'              : '_ask_node_',
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ],
     'INTERVAL_EVENT': [
       'time'               : 'DV_DATE_TIME',
       'data'                : '_ask_node_',
       'width'              : 'DV_DURATION',
       'math_function'      : 'DV_CODED_TEXT',
       // sample_count
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ],
     'ITEM_SINGLE': [
       'item'               : 'ELEMENT',
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ],
     'ITEM_LIST': [
       'items'              : 'ELEMENT', //'_list_', // LIST<ELEMENT>
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ],
     'ITEM_TABLE': [
       'rows'               : 'CLUSTER', //'_list_', // LIST<CLUSTER>
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ],
     'ITEM_TREE': [
       'items'              : '_ask_node_', //'_list_', // LIST<ITEM> = LIST<_ask_node_>
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ],
     'CLUSTER': [
       'items'              : '_ask_node_', //'_list_', // LIST<ITEM> = LIST<_ask_node_>
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ],
     'ELEMENT': [
       'value'              : '_ask_node_',
       'null_flavour'       : 'DV_CODED_TEXT',
       'name'               : 'DV_TEXT', // can be coded text
       'uid'                : 'HIER_OBJECT_ID',
       'archetype_details'  : 'ARCHETYPED'
     ]
   ]

   SemanticValidationLevel1(String org_uid)
   {
      this.namespace = org_uid
   }

   /**
    * Validate each version.data in the parsed versions.
    */
   def validateVersions(GPathResult versions)
   {
      // TODO: add index to associate errors with each COMPO
      versions.version.eachWithIndex { version, i ->
         // TODO: can paralelize if versions > 1
         validateComposition(version.data, i)
      }
   }

   /**
    * Traverses the compo tree, similar to DataIndexerService, but without creating any data,
    * just checking paths against the referenced OPT. If a COMPOSITION node is not on the
    * OPT, the traverse for that branch stops since the whole subtree won't exist in the OPT.
    */
   def validateComposition(GPathResult compo, int compoIndex)
   {
      def templateId = compo.archetype_details.template_id.value.text()
      def archetypeId = compo.archetype_details.archetype_id.value.text()
      log.info (templateId+' '+archetypeId)

      def optMan = OptManager.getInstance(Holders.config.app.opt_repo.withTrailSeparator())
      //def namespace = RequestContextHolder.currentRequestAttributes().request.securityStatelessMap.extradata.org_uid
      def opt = optMan.getOpt(templateId, this.namespace)

      // test
      opt.nodes.sort{ it.key }.each { p, o -> println p }

      traverseCompoNodes(compo, templateId, '/', archetypeId, '/', 'COMPOSITION', opt, compoIndex)
   }

   /**
    * paths are for the parent, current node paths are calculated internally
    * templatePath is the data path in the template, constructed from a data instance, is not the template path from the OPT!
    */
   def traverseCompoNodes(GPathResult node, String templateId, String templatePath,
                          String archetypeId, String archetypePath, String nodeRMType,
                          OperationalTemplate opt, int compoIndex)
   {
      // paths to current node are templatePath and archetypePath
      // path for children are calculated using getChildPathsAndRootArchetype

      //println 'templatePath '+ templatePath

      // TODO: data value attributesshould be avoided since those won't have AIIs
      // ISSUE: AIIs doesnt have intermediate structures, HISTORY, ITEM_TREE, etc. need to ask on
      // the OPT form OPTMAN! and this will be quicker since is memory instead of db queries

      /*
      // namespace is in securityStatelessMap because this is executed from the RestController.commit
      //def namespace = RequestContextHolder.currentRequestAttributes().session.organization.uid
      def namespace = RequestContextHolder.currentRequestAttributes().request.securityStatelessMap.extradata.org_uid
      //println optMan.referencedArchetypes[namespace]
      //println optMan.referencedArchetypes[namespace][archetypeId][0].nodes.sort{ it.key }.collect{ it.key +": "+ it.value.rmTypeName +" > "+ archetypeId }
      println archetypeId + archetypePath +' ns:'+ namespace
      println optMan.getNode(archetypeId, archetypePath, namespace)?.rmTypeName
      println "----------"
      */
      //println 'opt.getNode '+ opt.getNode(templatePath)


      // FIXME: here getNode is given a template data path, not a template path defined in the OPT,
      //        and checks against OPT defined paths, not template data paths, we need to calculate
      //        template data paths on the OPT to make the correct check.


      // check the node is defined by the OPT
      if (!continue_with_children_attrs.contains(node.name()) &&
          !opt.existsNodeByTemplateDataPath(templatePath)) // in memory verification!
      {
         if (!errors[compoIndex]) errors[compoIndex] = []
         errors[compoIndex] << 'Found a node ('+ templatePath +') that is not defined in the template "'+ templateId +'"' // TODO: i18n
      }
      else // continiue traverse
      {
         if (nodeRMType == '_ask_node_') nodeRMType = node.'@xsi:type'.text()

         //println 'nodeRMType '+ nodeRMType
         if (!nodeRMType)
         {
            throw new Exception('missing node on rm_schema '+ node.name() +' '+ node.name.text() +' '+ templatePath)
         }

         // TODO: we might need to avoid some attributes
         def paths
         node.children().each{

            paths = getChildPathsAndRootArchetype(it, templatePath, archetypePath, archetypeId)

            //println paths.rootArchetype + paths.archetypePath

            // nodes that are DV are avoided because their attributes don't have AIIs
            if (!avoid_procesing_attrs[nodeRMType].contains(it.name()))
            {
               // rm_attributes[nodeRMType][it.name()] => childRMType
               traverseCompoNodes(it, templateId, paths.templatePath,
                                  paths.rootArchetype, paths.archetypePath,
                                  rm_schema[nodeRMType][it.name()],
                                  opt, compoIndex)
            }
         }
      }
   }

   /**
    * Function from DataIndexerService, TODO: refactor.
    * These paths are data paths since are calculated from composition instances.
    */
   private Map getChildPathsAndRootArchetype(node, templatePath, archetypePath, archetypeId)
   {
      // Path del template para el indice (absoluta)
      String outTemplatePath
      String outArchetypePath

      // La path del root va sin nombre, ej. sin esto el root que es / seria /data
      if (templatePath == '')
      {
        outTemplatePath = '/'
        outArchetypePath = '/'
      }
      else if (!node.'@archetype_node_id'.isEmpty()) // Si tiene archetype_node_id
      {
         // Para que los hijos de la raiz no empiecen con //
         if (templatePath == '/') templatePath = ''
         if (archetypePath == '/') archetypePath = ''

         // Si es un nodo atNNNN
         if (node.'@archetype_node_id'.text().startsWith('at'))
         {
            outTemplatePath = templatePath + '/' + node.name() + '[' + node.'@archetype_node_id'.text() + ']'
            outArchetypePath = archetypePath + '/' + node.name() + '[' + node.'@archetype_node_id'.text() + ']'
         }
         else // Si es un archetypeId
         {
            outTemplatePath = templatePath + '/' + node.name() + '[archetype_id='+ node.'@archetype_node_id'.text() +']'
            outArchetypePath = '/' // This node is an archetype root because it has an archetypeId
            archetypeId = node.'@archetype_node_id'.text()
         }
      }
      else // No tiene archetype_node_id
      {
         // Para que los hijos de la raiz no empiecen con //
         if (templatePath == '/') templatePath = ''
         if (archetypePath == '/') archetypePath = ''

         outTemplatePath = templatePath + '/' + node.name()
         outArchetypePath = archetypePath + '/' + node.name()
      }

      return [templatePath: outTemplatePath, archetypePath: outArchetypePath, rootArchetype: archetypeId]
   }
}
