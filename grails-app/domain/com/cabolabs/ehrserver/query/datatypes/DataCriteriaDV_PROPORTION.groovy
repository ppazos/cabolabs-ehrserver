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
    static List criteriaSpec(String archetypeId, String path)
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
}
