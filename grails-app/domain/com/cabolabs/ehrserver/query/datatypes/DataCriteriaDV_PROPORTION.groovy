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
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvProportionIndex

class DataCriteriaDV_PROPORTION extends DataCriteria {

    List numeratorValue
    List denominatorValue
    Integer typeValue

    // Comparison operands
    String numeratorOperand
    String denominatorOperand
    String typeOperand

    boolean numeratorNegation = false
    boolean denominatorNegation = false
    boolean typeNegation = false
   
    DataCriteriaDV_PROPORTION()
    {
       rmTypeName = 'DV_PROPORTION'
       alias = 'dpi'
    }
    
    static hasMany = [numeratorValue: Double, denominatorValue: Double]
    
    static constraints = {
    }
    static mapping = {
       typeValue column: "dv_proportion_type"
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
             numerator: [
                eq:  'value',
                lt:  'value',
                gt:  'value',
                neq: 'value',
                le:  'value',
                ge:  'value',
                between: 'range'
             ],
             denominator: [
                eq:  'value',
                lt:  'value',
                gt:  'value',
                neq: 'value',
                le:  'value',
                ge:  'value',
                between: 'range'
             ],
             type: [
                // for this attribute values are known, for attributes of coded text
                // or ordinal we can lookup the OPT to see if a value list constraint
                // is defined, and grab the values from there.
                eq_one: DvProportionIndex.constraints.type.inList
             ]
          ]
       ]
    }
    
    static List attributes()
    {
       return ['numerator', 'denominator', 'type']
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
