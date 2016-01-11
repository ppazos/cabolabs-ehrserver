package com.cabolabs.ehrserver.ehr.clinical_documents.data

/**   
 * @author Pablo Pazos Gutierrez
 */
class DvOrdinalIndex extends DataValueIndex {

   int value
   
   // Attributes from symbol: dv_coded_text
   String symbol_value
   String symbol_code  // defining_code.codeString
   
   // segun las specs en produccion esto se codifica como tname(version_id)
   // y version_id puede ser un string vacio
   String symbol_terminology_id // defining_code.terminology_id.value = name(version_id)
}
