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
import com.cabolabs.openehr.opt.model.primitive.CDuration


class DataCriteriaDV_DURATION extends DataCriteria {

   static String indexType = 'DvDurationIndex'

   // magnitude is not an attribute from the IM but is an EHRServer
   // shorthand to know how many seconds are expressed in the duration.value
   // Also is easier to search by a number and not by an ISO 8601 duration string
   List magnitudeValue

   // Comparison operands
   String magnitudeOperand

   boolean magnitudeNegation = false


   DataCriteriaDV_DURATION()
   {
      rmTypeName = 'DV_DURATION'
      alias = 'dduri'
   }

   static hasMany = [magnitudeValue: Integer]

   static constraints = {
   }

   /**
    * Metadata that defines the types of criteria supported to search
    * by conditions over DV_QUANTITY.
    * @return
    */
   static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
   {
      def optMan = OptManager.getInstance()
      def namespace = RequestContextHolder.currentRequestAttributes().session.organization.uid
      def constraint = optMan.getNode(archetypeId, path, namespace) // ObjectNode
      /*
      println constraint.type
      println constraint.attributes.find{ it.rmAttributeName == 'value'}
      println constraint.attributes.find{ it.rmAttributeName == 'value'}.children // PrimitiveObjectNode
      println constraint.attributes.find{ it.rmAttributeName == 'value'}.children.type // C_PRIMITIVE_OBJECT
      println constraint.attributes.find{ it.rmAttributeName == 'value'}.children[0].item //
      println constraint.attributes.find{ it.rmAttributeName == 'value'}.children[0].item.range //
      */

      def criteria_constraints = [min: null, max: null]
      if (constraint)
      {
         // PrimitiveObjectNode
         def c_duration_value = constraint.attributes.find{ it.rmAttributeName == 'value'}?.children?.getAt(0)
         if (c_duration_value)
         {
            assert c_duration_value.item instanceof CDuration
            if (c_duration_value.item.range)
            {
               if (!c_duration_value.item.range.lowerUnbounded)
                  criteria_constraints.min = c_duration_value.item.range.lower.seconds() // lower instanceof Duration

               if (!c_duration_value.item.range.upperUnbounded)
                  criteria_constraints.max = c_duration_value.item.range.upper.seconds()
            }
         }
      }

      return [
        [
          magnitude: [
            eq:  'value', // operands eq,lt,gt,... can be applied to attribute magnitude and the reference value is a single value
            lt:  'value',
            gt:  'value',
            neq: 'value',
            le:  'value',
            ge:  'value',
            between: 'range', // operand between can be applied to attribute magnitude and the reference value is a list of 2 values: min, max
            criteria_constraints: criteria_constraints
          ]
        ]
      ]
   }

   static List attributes()
   {
      return ['value', 'magnitude']
   }

   static List functions()
   {
      return []
   }

   boolean containsFunction()
   {
      return false
   }
}
