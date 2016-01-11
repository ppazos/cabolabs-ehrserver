package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria

class DataCriteriaDV_COUNT extends DataCriteria {

    List magnitudeValue

    // Comparison operands
    String magnitudeOperand

   
    DataCriteriaDV_COUNT()
    {
       rmTypeName = 'DV_COUNT'
       alias = 'dci'
    }
    
    static hasMany = [magnitudeValue: Long]
    
    static constraints = {
    }
    static mapping = {
       magnitudeOperand column: "dv_count_operand"
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
             magnitude: [
                eq:  'value', // operands eq,lt,gt,... can be applied to attribute magnitude and the reference value is a single value
                lt:  'value',
                gt:  'value',
                neq: 'value',
                le:  'value',
                ge:  'value',
                between: 'range' // operand between can be applied to attribute magnitude and the reference value is a list of 2 values: min, max
             ]
          ]
       ]
    }
    
    static List attributes()
    {
       return ['magnitude']
    }
}
