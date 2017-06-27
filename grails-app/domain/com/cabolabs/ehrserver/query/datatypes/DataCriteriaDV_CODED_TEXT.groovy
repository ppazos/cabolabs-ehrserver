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
   static List criteriaSpec(String archetypeId, String path)
   {
      def optMan = OptManager.getInstance()
      
      /*
      println "path "+ path
      println "arch 1 "+ arch
      println "arch "+ arch.archetypeId
      //println "arch nodes "+ arch.nodes
      println "node "+ arch.getNode(path).xmlNode.rm_type_name.text()
      
      println "text nodes "+ arch.getNode(path).nodes
      
      println "node codes "+ arch.getNode(path + '/defining_code').xmlNode.code_list.text()
      */
      
/* can be many for the one archetypeId
      println "-------------"
      println "criteriaSpec "+ optMan.getReferencedArchetypes(archetypeId)
      println "-------------"
*/
      
      // List of valid codes for the code criteria
      // The path received points to the DV_CODED_TEXT, the codes are in the child CODE_PRHASE
      def codes = [:]
      def code
      def lang = RequestContextHolder.currentRequestAttributes().session.lang
      // if the coded text doesn't have a constraint, xmlNode is null
      // https://github.com/ppazos/cabolabs-ehrserver/issues/528
      optMan.getNode(archetypeId, path + '/defining_code')?.xmlNode?.code_list.each {
        
        code = it.text()
        codes[code] = optMan.getText(archetypeId, code, lang) // at00XX -> name
      }
      
      def spec = [
        [
          code: [
            eq: 'value',   // operand eq can be applied to attribute code and the reference value is a single value
            in_list: 'list' // operand in_list can be applied to attribute code and the reference value is a list of values
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
      
      if (codes.size() > 0)
      {
        spec[0].code.codes = codes
        spec[0].terminologyId.codes = ['local': 'local'] // if the terms are defined in the archetype, the terminology is local
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
      
      return spec
   }
   
   static List attributes()
   {
      return ['value', 'code', 'terminologyId']
   }
   
   String toString()
   {
      return this.getClass().getSimpleName() +": "+ this.codeOperand +" "+ this.codeValue.toString() +" "+ this.terminologyIdOperand +" "+ this.terminologyIdValue
   }
}
