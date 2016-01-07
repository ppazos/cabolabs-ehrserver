package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.openehr.opt.manager.OptManager

class DataCriteriaDV_QUANTITY extends DataCriteria {

    List magnitudeValue
    String unitsValue
   
    // Comparison operands
    String magnitudeOperand
    String unitsOperand
   
    DataCriteriaDV_QUANTITY()
    {
       rmTypeName = 'DV_QUANTITY'
       alias = 'dqi'
    }
    
    static hasMany = [magnitudeValue: Double]
    
    static constraints = {
    }
    static mapping = {
       unitsValue column: "dv_qty_units"
    }
    
    /**
     * Metadata that defines the types of criteria supported to search
     * by conditions over DV_QUANTITY.
     * @return
     */
    static List criteriaSpec(String archetypeId, String path)
    {
       def optMan = OptManager.getInstance()
       def arch = optMan.getReferencedArchetype(archetypeId)
       
       def units = [:]
       def u
       arch.getNode(path).xmlNode.list.each {
          u = it.units.text() 
          units[u] = u // mm[Hg] -> mm[Hg] // keep it as map to keep the same structure as the DV_CODED_TEXT 
       }
       
       def spec = [
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
             units: [
                eq: 'value'
             ]
          ]
       ]
       
       if (units.size() > 0) spec[0].units.units = units
       
       return spec
    }
    
    static List attributes()
    {
       return ['magnitude', 'units']
    }
    
    String toString()
    {
       return this.getClass().getSimpleName() +": "+ this.magnitudeOperand +" "+ this.magnitudeValue.toString() +" "+ this.unitsOperand +" "+ this.unitsValue
    }
}
