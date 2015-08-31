package query.datatypes

import query.DataCriteria
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
       
       def units = []
       arch.getNode(path).xmlNode.list.each {
          units << it.units.text() // mm[Hg]
       }
       
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
             units: [
                eq: 'value',
                units: units
             ]
          ]
       ]
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
