package ehr

class EhrTagLib {

   def hasEhr = { attrs, body ->
      
      if (!attrs.patientUID) throw new Exception("patientUID es obligatorio")
      
      //println patientUID
      
      def c = Ehr.createCriteria()
      
      def ehr = c.get {
         subject {
            eq ('value', attrs.patientUID)
         }
      }
      
      //println ehr
      
      if (ehr) out << body()
   }
   
   def dontHasEhr = { attrs, body ->

      if (!attrs.patientUID) throw new Exception("patientUID es obligatorio")
      
      //println attrs.patientUID
      
      def c = Ehr.createCriteria()
      
      def ehr = c.get {
         subject {
            eq ('value', attrs.patientUID)
         }
      }
      
      //println ehr
      
      if (!ehr) out << body()
   }
}