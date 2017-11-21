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
import groovy.time.TimeCategory

class DataCriteriaDV_DATE_TIME extends DataCriteria {

   List valueValue
   String valueOperand

   // Support for functions
   List age_in_yearsValue
   String age_in_yearsOperand
   
   List age_in_monthsValue
   String age_in_monthsOperand
   
   boolean valueNegation = false
   boolean age_in_yearsNegation = false
   boolean age_in_monthsNegation = false
   
   DataCriteriaDV_DATE_TIME()
   {
      rmTypeName = 'DV_DATE_TIME'
      alias = 'ddti'
   }
    
   static hasMany = [valueValue: Date, age_in_yearsValue: Integer, age_in_monthsValue: Integer]
    
   static constraints = {
      valueOperand nullable: true
      age_in_yearsOperand nullable: true
      age_in_monthsOperand nullable: true
   }
    
   static mapping = {
      valueValue column: "dv_datetime_value"
      age_in_yearsValue column: "dv_datetime_age_in_years"
      age_in_monthsValue column: "dv_datetime_age_in_months"
   }
    
   /**
    * Metadata that defines the types of criteria supported to search
    * by conditions over DV_QUANTITY.
    * @return
    */
   static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
   {
      return [
         [
            value: [
               eq:  'value', // operands eq,lt,gt,... can be applied to attribute magnitude and the reference value is a single value
               lt:  'value',
               gt:  'value',
               neq: 'value',
               le:  'value',
               ge:  'value',
               between: 'range' // operand between can be applied to attribute magnitude and the reference value is a list of 2 values: min, max
            ]
         ],
         [
            age_in_years: [
               eq:  'value',
               lt:  'value',
               gt:  'value',
               neq: 'value',
               le:  'value',
               ge:  'value',
               between: 'range'
            ]
         ],
         [
            age_in_months: [
               eq:  'value',
               lt:  'value',
               gt:  'value',
               neq: 'value',
               le:  'value',
               ge:  'value',
               between: 'range'
            ]
         ]
      ]
   }
    
   static List attributes()
   {
      return ['value']
   }
   
   static List functions()
   {
      return ['age_in_years', 'age_in_months']
   }
   
   boolean containsFunction()
   {
      return age_in_yearsOperand != null || age_in_monthsOperand != null
   }
   
   String evaluateFunction(String function)
   {
      def time_attr, value, operand, negation
      if (function == 'age_in_years')
      {
         time_attr = 'years'
         
         value = age_in_yearsValue
         operand = age_in_yearsOperand
         negation = age_in_yearsNegation
      }
      else if (function == 'age_in_months')
      {
         time_attr = 'months'
         
         value = age_in_monthsValue
         operand = age_in_monthsOperand
         negation = age_in_monthsNegation
      }
      else
      {
         throw new Exception("function $function not supported")
      }
      
      
      //def criteria_spec = specs[this.spec]
      //def criteriaValueType = criteria_spec[function][operand]
      def criteriaValueType = ((operand == 'between') ? 'range' : 'value')

      // age_in_years criteriaValueType is value or range
      if (criteriaValueType == 'value')
      {
         // function logic, calculates the limit age of the date with the value in years to compare with the attr 'value' in the query
         def now = new Date()
         def criteria_value
         use(TimeCategory) {
            criteria_value = now - value[0]."$time_attr"
         }
         
         return (negation ? 'NOT ' : '') + criteria_value.asSQLValue(operand) +' '+ sqlOperand(operand) +' '+ this.alias +'.value '
      }
      else if (criteriaValueType == 'range')
      {
         value.sort()
         
         def now = new Date()
         def criteria_value_low, criteria_value_high
         use(TimeCategory) {
            criteria_value_low  = now - value[0]."$time_attr"
            criteria_value_high = now - value[1]."$time_attr" // high is really the lower value since value[1] is greater but is -
         }
         
         return this.alias +'.value '+ (negation ? 'NOT ' : '') +'BETWEEN '+ criteria_value_high.asSQLValue(operand) +' AND '+ criteria_value_low.asSQLValue(operand)
      }
   }
}
