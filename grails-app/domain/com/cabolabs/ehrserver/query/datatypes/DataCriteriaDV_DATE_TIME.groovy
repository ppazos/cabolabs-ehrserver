package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria

class DataCriteriaDV_DATE_TIME extends DataCriteria {

    List valueValue

    // Comparison operands
    String valueOperand

   
    DataCriteriaDV_DATE_TIME()
    {
       rmTypeName = 'DV_DATE_TIME'
       alias = 'ddti'
    }
    
    // test
    def beforeInsert() {
       println "beforeInsert: "+ this.valueValue
   }
    
    static hasMany = [valueValue: Date]
    
    static constraints = {
    }
    
    static mapping = {
       valueValue column: "dv_datetime_value"
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
             value: [
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
       return ['value']
    }
}
