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

class DataCriteriaDV_ORDINAL extends DataCriteria {

    // Comparison values
    List valueValue // int
    List symbol_codeValue // coded text
    String symbol_terminology_idValue // coded text
    String symbol_valueValue // text
    
    // Comparison operands
    String valueOperand
    String symbol_valueOperand
    String symbol_codeOperand
    String symbol_terminology_idOperand
    
    boolean valueNegation = false
    boolean symbol_valueNegation = false
    boolean symbol_codeNegation = false
    boolean symbol_terminology_idNegation = false
    
    DataCriteriaDV_ORDINAL()
    {
       rmTypeName = 'DV_ORDINAL'
       alias = 'dvol'
    }
   
    static hasMany = [valueValue: Integer, symbol_codeValue: String]
    
    static constraints = {
       valueOperand(nullable:true)
       symbol_valueOperand(nullable:true)
       symbol_codeOperand(nullable:true)
       symbol_terminology_idOperand(nullable:true)
       symbol_terminology_idValue(nullable:true)
       symbol_valueValue(nullable:true)
    }
    static mapping = {
       symbol_terminology_idValue column: "dv_ordinal_terminology_id"
       symbol_valueValue column: "dv_ordinal_value"
    }
    
    /**
     * Metadata that defines the types of criteria supported to search 
     * by conditions over DV_CODED_TEXT.
     * @return
     */
    static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
    {
       return [
          [ // for the ordinal number
             value: [
                eq:  'value', // operands eq,lt,gt,... can be applied to attribute magnitude and the reference value is a single value
                lt:  'value',
                gt:  'value',
                neq: 'value',
                le:  'value',
                ge:  'value',
                between: 'range'
             ]
          ],
          [ // like dv text
             symbol_value: [
                contains:  'value', // ilike %value%
                eq:  'value'
             ]
          ],
          [ // like coded text
             symbol_code: [
                eq: 'value',    // operand eq can be applied to attribute code and the reference value is a single value
                in_list: 'list' // operand in_list can be applied to attribute code and the reference value is a list of values
             ],
             symbol_terminology_id: [
                eq: 'value',
                contains: 'value'
             ]
          ]
       ]
    }
    
    static List attributes()
    {
       return ['value', 'symbol_value', 'symbol_code', 'symbol_terminology_id']
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
