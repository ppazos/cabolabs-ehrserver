package query.datatypes

import query.DataCriteria

class DataCriteriaDV_TEXT extends DataCriteria {

    String valueValue

    // Comparison operands
    String valueOperand

   
    DataCriteriaDV_TEXT()
    {
       rmTypeName = 'DV_TEXT'
       alias = 'dti'
    }
    
    //static hasMany = [valueValues: String] // FIXME: this should be one value since no spec requires a in_list or range.
    
    static constraints = {
    }
    static mapping = {
       valueValue column: "dv_text_value"
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
