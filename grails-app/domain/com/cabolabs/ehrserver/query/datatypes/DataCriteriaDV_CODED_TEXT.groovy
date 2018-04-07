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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem
import org.springframework.web.context.request.RequestContextHolder

class DataCriteriaDV_CODED_TEXT extends DataCriteria {

   // Comparison values
   List codeValue
   String terminologyIdValue
   String valueValue

   // Comparison operands
   String codeOperand
   String terminologyIdOperand
   String valueOperand

   boolean codeNegation = false
   boolean terminologyIdNegation = false
   boolean valueNegation = false

   DataCriteriaDV_CODED_TEXT()
   {
      rmTypeName = 'DV_CODED_TEXT'
      alias = 'dcti'
   }

   static hasMany = [codeValue: String]

   static constraints = {
      codeOperand(nullable:true)
      terminologyIdOperand(nullable:true)
      valueOperand(nullable:true)
      terminologyIdValue(nullable:true)
      valueValue(nullable:true)
   }
   static mapping = {
      valueValue column: "dv_codedtext_value"
      terminologyIdValue column: "dv_codedtext_terminology_id"
   }

   /**
    * Metadata that defines the types of criteria supported to search
    * by conditions over DV_CODED_TEXT.
    * @return
    */
   static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
   {
      def spec = [
        [
          code: [
            eq: 'value',     // operand eq can be applied to attribute code and the reference value is a single value
            in_list: 'list', // operand in_list can be applied to attribute code and the reference value is a list of values

            // TODO: there is a dependence between code and terminologyId constraints, if in_snomed_exp is selected,
            //       I want codes to terminologyId to be set to this list, or I can send it always and avoid processing it on the ui.
            // International Edition 20170131
            // International Edition 20160131
            // International Edition 20160731
            // Spanish Edition 20160430
            // Spanish Edition 20160430 + SNS 20160430
            // Spanish Edition 20161031
            // Spanish Edition 20161031 + SNS 20161031
            in_snomed_exp: 'snomed_exp'
          ],
          terminologyId: [
            eq: 'value',
            contains: 'value'
          ]
        ],
        [
          value: [contains: 'value']
        ]
      ]

      if (returnCodes)
      {
         def optMan = OptManager.getInstance()

         // List of valid codes for the code criteria
         // The path received points to the DV_CODED_TEXT, the codes are in the child CODE_PRHASE
         def codes = [:]

         def lang = RequestContextHolder.currentRequestAttributes().session.lang
         def namespace = RequestContextHolder.currentRequestAttributes().session.organization.uid

         // 1. codeList can be empty if the archetype doesn't have a constraint.
         // 2. for DV_TEXT we generate DV_CODED_TEXT index to support inheritance,
         // but if the OPT doesn't have a DV_CODED_TEXT, the path will return a null node.
         // https://github.com/ppazos/cabolabs-ehrserver/issues/528

         def constraint = optMan.getNode(archetypeId, path + '/defining_code', namespace)
         if (constraint && constraint.type == 'C_CODE_PHRASE') // C_CODE_PHRASE is the only type that has codeList, the constraint can be also COSTRAINT_REF or or C_CODE_REFERENCE.
         {
            constraint.codeList.each { code ->

              codes[code] = optMan.getText(archetypeId, code, lang, namespace) // at00XX -> name
            }
         }



         // if it starts with underscore, do not process on the ui
         /* currently we dont need the versions on the query builder since versions are used to get the name/rubric, and queries use just the conceptid
         spec[0].terminologyId._snomed = ['SNOMED-CT(International Edition 20170131)'         : 'SNOMED-CT(International Edition 20170131)',
                                          'SNOMED-CT(International Edition 20160131)'         : 'SNOMED-CT(International Edition 20160131)',
                                          'SNOMED-CT(International Edition 20160731)'         : 'SNOMED-CT(International Edition 20160731)',
                                          'SNOMED-CT(Spanish Edition 20160430)'               : 'SNOMED-CT(Spanish Edition 20160430)',
                                          'SNOMED-CT(Spanish Edition 20160430 + SNS 20160430)': 'SNOMED-CT(Spanish Edition 20160430 + SNS 20160430)',
                                          'SNOMED-CT(Spanish Edition 20161031)'               : 'SNOMED-CT(Spanish Edition 20161031)',
                                          'SNOMED-CT(Spanish Edition 20161031 + SNS 20161031)': 'SNOMED-CT(Spanish Edition 20161031 + SNS 20161031)']
         */
         spec[0].terminologyId._snomed = ['SNOMED-CT': 'SNOMED-CT'] // this needs to be set to the terminology when in_snomed_exp operator is selected.

         if (codes.size() > 0)
         {
           spec[0].code.codes = codes
           spec[0].terminologyId.codes = ['local': 'local'] // if the terms are defined in the archetype, the terminology is local
         }
         else if (path.endsWith('/null_flavour')) // show valid null flavour codes
         {
            // TODO: support getting this from i18n openehr terminology
            /*
            <group name="null flavours">
             <concept id="271" rubric="no information"/>
             <concept id="253" rubric="unknown"/>
             <concept id="272" rubric="masked"/>
             <concept id="273" rubric="not applicable"/>
            </group>
            */
            spec[0].code.codes = [
               253: 'unknown',
               271: 'no information',
               272: 'masked',
               273: 'not applicable'
            ]
            spec[0].terminologyId.codes = ['openehr': 'openehr'] // if the terms are defined in the archetype, the terminology is local
         }
         else if (path == '/context/setting') // TODO: check archetype is for COMPOSITION
         {
            // Provide setting codes from openEHR terminology
            // TODO: this should be provided by configuration and be per organization since the openEHR terminology is open for this.
            // TODO: i18n
            spec[0].code.codes = [
               225: 'home',
               227: 'emergency care',
               228: 'primary medical care',
               229: 'primary nursing care',
               230: 'primary allied health care',
               231: 'midwifery care',
               232: 'secondary medical care',
               233: 'secondary nursing care',
               234: 'secondary allied health care',
               235: 'complementary health care',
               236: 'dental care',
               237: 'nursing home care',
               238: 'other care'
            ]
            spec[0].terminologyId.codes = ['openehr': 'openehr']
         }
         else
         {
           // https://github.com/ppazos/cabolabs-ehrserver/issues/154
           def idef = ArchetypeIndexItem.findByArchetypeIdAndPath(archetypeId, path)
           if (idef && idef.terminologyRef)
           {
             // terminology:WHO?subset=ATC&amp;language=en-GB
             // WHO
             def terminology = idef.terminologyRef.split('\\?')[0].split(':')[1]

             spec[0].terminologyId.codes = [(terminology): terminology]
           }
         }
      }

      return spec
   }

   static List attributes()
   {
      return ['value', 'code', 'terminologyId']
   }

   static List functions()
   {
      return []
   }

   String toString()
   {
      return this.getClass().getSimpleName() +": "+ this.codeOperand +" "+ this.codeValue.toString() +" "+ this.terminologyIdOperand +" "+ this.terminologyIdValue
   }

   boolean containsFunction()
   {
      return false
   }
}
