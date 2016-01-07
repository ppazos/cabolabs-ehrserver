package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria

class DataCriteriaDV_DURATION extends DataCriteria {

    // magnitude is not an attribute from the IM but is an EHRServer
    // shorthand to know how many seconds are expressed in the duration.value
    // Also is easier to search by a number and not by an ISO 8601 duration string
    List magnitudeValue

    // Comparison operands
    String magnitudeOperand

   
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
       return ['value', 'magnitude']
    }
}
