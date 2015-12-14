package kernel

import common.generic.DoctorProxy
import common.generic.PatientProxy
import ehr.Ehr

/**
 * Operaciones basicas para usarse en el resto del sistemas.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 */
class ApiService {

   def ehrExists(String ehrUid)
   {
      /*
      def before = System.currentTimeMillis()
      
      def c = Ehr.createCriteria()
      def count = c.get {
         projections {
            count "id"
         }
         eq('uid', ehrUid)
      }
      
      def after = System.currentTimeMillis()
      println "Sorting took ${after-before} ms"
      
      println count
      
      
      before = System.currentTimeMillis()
      
      count = Ehr.countByUid(ehrUid)
      
      after = System.currentTimeMillis()
      println "Sorting took ${after-before} ms"
      
      println count
      */
      
      // TODO: verificar que el ehrId es unico para que sea == 1
      return Ehr.countByUid(ehrUid) == 1
   }
   
   def contributionExists(String contributionUid)
   {
      // TODO
   }
   
   def patientExists(PatientProxy subject)
   {
      // TODO
   }
   
   def patientExists(String personUID)
   {
      // TODO: debe tener el id y el rol
   }
   
   def doctorExists(DoctorProxy committer)
   {
      // TODO
   }
   
   def doctorExists(String personUID)
   {
      // TODO: debe tener el id y el rol
   }
}
