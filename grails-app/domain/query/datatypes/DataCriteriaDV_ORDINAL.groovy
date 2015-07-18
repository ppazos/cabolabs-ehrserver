package query.datatypes

import query.DataCriteria

class DataCriteriaDV_ORDINAL extends DataCriteria {

    // Comparison values
    List valueValues // int
    List symbol_codeValues // coded text
    String symbol_terminology_idValue // coded text
    String symbol_valueValues // text
    
    // Comparison operands
    String valueOperand
    String symbol_valueOperand
    String symbol_codeOperand
    String symbol_terminology_idOperand
    
    DataCriteriaDV_ORDINAL()
    {
       rmTypeName = 'DV_ORDINAL'
    }
   
    static hasMany = [valueValues: int, symbol_codeValues: String]
    
    static constraints = {
    }
    
    /**
     * Metadata that defines the types of criteria supported to search 
     * by conditions over DV_CODED_TEXT.
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
             symbol_terminology_id: [eq: 'value']
          ]
       ]
    }
    
    static List attributes()
    {
       return ['value', 'symbol_value', 'symbol_code', 'symbol_terminology_id']
    }
}
