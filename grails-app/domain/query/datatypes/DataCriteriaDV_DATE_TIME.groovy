package query.datatypes

import query.DataCriteria

class DataCriteriaDV_DATE_TIME extends DataCriteria {

    List valueValue

    // Comparison operands
    String valueOperand

   
    DataCriteriaDV_DATE_TIME()
    {
       rmTypeName = 'DV_DATE_TIME'
       alias = 'ddti'
    }
    
    static hasMany = [valueValue: Date]
    
    static constraints = {
    }
    
    /**
     * Metadata that defines the types of criteria supported to search
     * by conditions over DV_QUANTITY.
     * @return
     */
    static List criteriaSpec()
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
