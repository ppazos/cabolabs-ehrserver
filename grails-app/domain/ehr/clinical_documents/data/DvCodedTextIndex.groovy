package ehr.clinical_documents.data

/**
 * TODO: esta clase podria heredar de DvTextIndex y las
 *       busquedas por value incuirian DvText y DvCodedText
 *       
 * @author Pablo Pazos Gutierrez
 */
class DvCodedTextIndex extends DataValueIndex {

   // data
   String value // heredado de DvText
   String code  // defining_code.codeString
   
   // segun las specs en produccion esto se codifica como tname(version_id)
   // y version_id puede ser un string vacio
   String terminologyId // defining_code.terminology_id.value = name(version_id)
}