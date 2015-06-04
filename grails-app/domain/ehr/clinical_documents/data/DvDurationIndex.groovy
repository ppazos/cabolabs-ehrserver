package ehr.clinical_documents.data

/** 
 * @author Pablo Pazos Gutierrez
 */
class DvDurationIndex extends DataValueIndex {

   String value
   Double magnitude // calculated, duration in seconds
   
   static constraints =  {
      magnitude(nullable:true) // JUST FOR TESING
   }
}
