package com.cabolabs.ehrserver.ehr.clinical_documents.data

/**   
 * @author Pablo Pazos Gutierrez
 */
class DvCodedTextIndex extends DvTextIndex {

   String code  // defining_code.codeString
   
   // segun las specs en produccion esto se codifica como tname(version_id)
   // y version_id puede ser un string vacio
   String terminologyId // defining_code.terminology_id.value = name(version_id)
}
