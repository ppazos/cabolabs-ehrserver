package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria

class DataCriteriaDV_IDENTIFIER extends DataCriteria {

    String identifierValue  // needed to change the DV_IDENTIFIER.id attr name to identifier because it is used by grails for the identity.
    String typeValue
    String issuerValue
    String assignerValue
    

    // Comparison operands
    String identifierOperand
    String typeOperand
    String issuerOperand
    String assignerOperand
   
    DataCriteriaDV_IDENTIFIER()
    {
       rmTypeName = 'DV_IDENTIFIER'
       alias = 'dvidi'
    }
    
    //static hasMany = [valueValues: String] // FIXME: this should be one value since no spec requires a in_list or range.
    
    static constraints = {
       issuerValue nullable: true
       assignerValue nullable: true
       issuerOperand nullable: true
       assignerOperand nullable: true
    }
    static mapping = {
       //valueValue column: "dv_text_value"
    }
    
    /**
     * Metadata that defines the types of criteria supported to search
     * by conditions over DV_QUANTITY.
     * @return
     */
    static List criteriaSpec(String archetypeId, String path)
    {
       return [
          [ // full criteria
             identifier: [
                contains:  'value', // ilike %value%
                eq:  'value'
             ],
             type: [
                contains:  'value', // ilike %value%
                eq:  'value'
             ],
             issuer: [
                contains:  'value', // ilike %value%
                eq:  'value'
             ],
             assigner: [
                contains:  'value', // ilike %value%
                eq:  'value'
             ]
          ],
          [ // id + type criteria
             identifier: [
                contains:  'value', // ilike %value%
                eq:  'value'
             ],
             type: [
                contains:  'value', // ilike %value%
                eq:  'value'
             ]
          ]
       ]
    }
    
    static List attributes()
    {
       return ['identifier', 'type', 'issuer', 'assigner']
    }
}
