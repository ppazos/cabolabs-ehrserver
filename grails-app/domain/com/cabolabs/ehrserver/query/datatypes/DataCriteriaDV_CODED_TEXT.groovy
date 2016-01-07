package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.ehrserver.ehr.clinical_documents.IndexDefinition

class DataCriteriaDV_CODED_TEXT extends DataCriteria {

    // Comparison values
    List codeValue
    String terminologyIdValue
    String valueValue
    
    // Comparison operands
    String codeOperand
    String terminologyIdOperand
    String valueOperand
    
    DataCriteriaDV_CODED_TEXT()
    {
       rmTypeName = 'DV_CODED_TEXT'
       alias = 'dcti'
    }
   
    static hasMany = [codeValue: String]
    
    static constraints = {
       codeOperand(nullable:true)
       terminologyIdOperand(nullable:true)
       valueOperand(nullable:true)
       terminologyIdValue(nullable:true)
       valueValue(nullable:true)
    }
    static mapping = {
       valueValue column: "dv_codedtext_value"
       terminologyIdValue column: "dv_codedtext_terminology_id"
    }
    
    /**
     * Metadata that defines the types of criteria supported to search 
     * by conditions over DV_CODED_TEXT.
     * @return
     */
    static List criteriaSpec(String archetypeId, String path)
    {
       def optMan = OptManager.getInstance()
       def arch = optMan.getReferencedArchetype(archetypeId)
       
       /*
       println "path "+ path
       println "arch 1 "+ arch
       println "arch "+ arch.archetypeId
       //println "arch nodes "+ arch.nodes
       println "node "+ arch.getNode(path).xmlNode.rm_type_name.text()
       
       println "text nodes "+ arch.getNode(path).nodes
       
       println "node codes "+ arch.getNode(path + '/defining_code').xmlNode.code_list.text()
       */
       
       // List of valid codes for the code criteria
       // The path received points to the DV_CODED_TEXT, the codes are in the child CODE_PRHASE
       def codes = [:]
       def code
       arch.getNode(path + '/defining_code').xmlNode.code_list.each {
          
          code = it.text()
          codes[code] = arch.getText(code) // at00XX -> name
       }
       
       def spec = [
          [
             code: [
                eq: 'value',    // operand eq can be applied to attribute code and the reference value is a single value
                in_list: 'list' // operand in_list can be applied to attribute code and the reference value is a list of values
             ],
             terminologyId: [
                eq: 'value',
                contains: 'value'
             ]
          ],
          [
             value: [contains: 'value']
          ]
       ]
       
       if (codes.size() > 0)
       {
          spec[0].code.codes = codes
          spec[0].terminologyId.codes = ['local': 'local'] // if the terms are defined in the archetype, the terminology is local
       }
       else
       {
          // https://github.com/ppazos/cabolabs-ehrserver/issues/154
          def idef = IndexDefinition.findByArchetypeIdAndArchetypePath(archetypeId, path)
          if (idef && idef.terminologyRef)
          {
             // terminology:WHO?subset=ATC&amp;language=en-GB
             // WHO
             def terminology = idef.terminologyRef.split('\\?')[0].split(':')[1]
             
             spec[0].terminologyId.codes = [(terminology): terminology]
          }
       }
        
       return spec
    }
    
    static List attributes()
    {
       return ['value', 'code', 'terminologyId']
    }
    
    String toString()
    {
       return this.getClass().getSimpleName() +": "+ this.codeOperand +" "+ this.codeValue.toString() +" "+ this.terminologyIdOperand +" "+ this.terminologyIdValue
    }
}
