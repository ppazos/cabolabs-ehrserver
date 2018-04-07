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
import org.springframework.web.context.request.RequestContextHolder

class DataCriteriaDV_QUANTITY extends DataCriteria {

   List magnitudeValue
   String unitsValue

   // Comparison operands
   String magnitudeOperand
   String unitsOperand

   boolean magnitudeNegation = false
   boolean unitsNegation = false

   DataCriteriaDV_QUANTITY()
   {
      rmTypeName = 'DV_QUANTITY'
      alias = 'dqi'
   }

   static hasMany = [magnitudeValue: Double]

   static constraints = {
   }
   static mapping = {
      unitsValue column: "dv_qty_units"
   }

   /**
    * Metadata that defines the types of criteria supported to search
    * by conditions over DV_QUANTITY.
    * @return
    */
   static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
   {
      def spec = [
        [
          magnitude: [
            eq:  'value', // operands eq,lt,gt,... can be applied to attribute magnitude and the reference value is a single value
            lt:  'value',
            gt:  'value',
            neq: 'value',
            le:  'value',
            ge:  'value',
            between: 'range' // operand between can be applied to attribute magnitude and the reference value is a list of 2 values: min, max
          ],
          units: [
            eq: 'value'
          ]
        ]
      ]

      if (returnCodes)
      {
         //println archetypeId +" "+ path
         def optMan = OptManager.getInstance()
         def units = [:]
         def namespace = RequestContextHolder.currentRequestAttributes().session.organization.uid

         optMan.getNode(archetypeId, path, namespace).list.each { c_qty_item ->

            // keep it as map to keep the same structure as the DV_CODED_TEXT
            units[c_qty_item.units] = c_qty_item.units // mm[Hg] -> mm[Hg]
         }

         if (units.size() > 0) spec[0].units.units = units
      }

      return spec
   }

   static List attributes()
   {
      return ['magnitude', 'units']
   }

   static List functions()
   {
      return []
   }

   String toString()
   {
      return this.getClass().getSimpleName() +": "+ this.magnitudeOperand +" "+ this.magnitudeValue.toString() +" "+ this.unitsOperand +" "+ this.unitsValue
   }

   boolean containsFunction()
   {
      return false
   }
}
