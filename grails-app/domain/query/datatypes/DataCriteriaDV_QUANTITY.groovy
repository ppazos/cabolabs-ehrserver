package query.datatypes

import query.DataCriteria

class DataCriteriaDV_QUANTITY extends DataCriteria {

    List magnitudeValues
    String unitsValue
   
    // Comparison operands
    String magnitudeOperand
    String unitsOperand
   
    DataCriteriaDV_QUANTITY()
    {
       rmTypeName = 'DV_QUANTITY'
    }
    
    static hasMany = [magnitudeValues: String]
    
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
             magnitude: [
                eq:  'value', // operands eq,lt,gt,... can be applied to attribute magnitude and the reference value is a single value
                lt:  'value',
                gt:  'value',
                neq: 'value',
                le:  'value',
                ge:  'value',
                between: 'range' // operand between can be applied to attribute magnitude and the reference value is a list of 2 values: min, max
             ], 
             units: [eq: 'value']
          ]
       ]
    }
    
    static List attributes()
    {
       return ['magnitude', 'units']
    }
    
    String toString()
    {
       return this.getClass().getSimpleName() +": "+ this.magnitudeOperand +" "+ this.magnitudeValues.toString() +" "+ this.unitsOperand +" "+ this.unitsValue
    }
}
