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

package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria

class DataCriteriaDV_PARSABLE extends DataCriteria {

   String valueValue
   String valueOperand

   List formalismValue
   String formalismOperand

   boolean valueNegation = false
   boolean formalismNegation = false


   DataCriteriaDV_PARSABLE()
   {
      rmTypeName = 'DV_PARSABLE'
      alias = 'dpab'
   }

   static hasMany = [formalismValue: String]

   static constraints = {
   }

   static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
   {
      println path

      def spec = [
        [
          value: [
            contains: 'value' // ilike %value%
          ],
          formalism: [
            eq: 'value',
            in_list: 'list'
          ]
        ],
        [
          formalism: [
            eq: 'value',
            in_list: 'list'
          ]
        ]
      ]

      // TODO: return formalisms
      if (returnCodes)
      {
         spec[0].formalism.codes = [
            'text/xml':         'text/xml',
            'text/rtf':         'text/rtf',
            'text/plain':       'text/plain',
            'text/html':        'text/html',
            'application/json': 'application/json',
            'ISO8601':          'ISO8601',
            'HL7_GTS':          'HL7_GTS'
         ]
         spec[1].formalism.codes = spec[0].formalism.codes
      }

      return spec
   }

   static List attributes()
   {
      return ['value', 'formalism']
   }

   static List functions()
   {
      return []
   }

   String toString()
   {
      return this.getClass().getSimpleName() +": "+ this.valueOperand +" "+ this.valueValue +" "+ this.formalismOperand +" "+ this.formalismValue.toString()
   }

   boolean containsFunction()
   {
      return false
   }
}
