package query.datatypes

import query.DataCriteria

class DataCriteriaDV_TEXT extends DataCriteria {

    List valueValues

    // Comparison operands
    String valueOperand

   
    DataCriteriaDV_TEXT()
    {
       rmTypeName = 'DV_TEXT'
    }
    
    static hasMany = [valueValues: String]
    
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
                contains:  'value', // ilike %value%
                eq:  'value'
             ]
          ]
       ]
    }
    
    static List attributes()
    {
       return ['value']
    }
}
