package sm

import common.generic.AuditDetails
import common.generic.PatientProxy
import common.change_control.Version
import support.identification.CompositionRef
import demographic.Person
import ehr.Ehr

class EhrService {
   
   // TODO: probar anotar todos los metodos con CXF
   //       http://grails.1312388.n4.nabble.com/SOAP-plugin-for-Grails-td4634408.html#a4634570
   // http://grails.org/plugin/axis2
   static expose = ['axis2'] //['cxf']

   Person createPatient(String firstName, String lastName, Date dob, String sex)
   {
      // TODO
      return new Person(role:'pat')
   }
   
   /*
   def createPatientProxy(Person patient)
   {
      return new PatientProxy(value:patient.uid)
   }
   */
   
   def createDoctor()
   {
      // TODO
      //return new Person()
   }
   
   Ehr createEHR(PatientProxy subject)
   {
      /*
      def ehr = new Ehr(
         ehrId: java.util.UUID.randomUUID() as String,
         subject: subject
      )
      
      return ehr
      */
   }
   
   /**
    * Los parametros son parseados del XML previamente.
    * 
    * @param ehrId
    * @param audit
    * @param versions
    * @return
    */
   void commitContribution(String ehrId, AuditDetails audit, Version[] versions)
   {
      // TODO
      // Si se guardan las versiones en disco como XMLs, tengo que ver
      // como modificar los XMLs con los valores que esta operacion debe
      // setear, como ids, veriones, etc.
   }
   
   Ehr getEHR(String patId)
   {
      // TODO
      /* Criteria:
      Ehr {
         subject {
            eq('value', patId)
         }
      }
      */
   }
   
   /**
    * Busca compositions por el criterio de los parametros.
    * Todos los parametros pueden ser null, en cuyo caso se devuelven
    * todas las compositions de todo el sistema (de cualquier EHR)
    * 
    * @link http://www.openehr.org/wiki/display/spec/Ocean+Informatics+EHR+Service+Interface
    * 
    * @param ehrId
    * @param fromDate
    * @param toDate
    * @param committerId
    * @param archetypeId
    * @return
    */
   CompositionRef[] getCompositions(String ehrId, Date fromDate, Date toDate, String committerId, String archetypeId)
   {
      // TODO
   }
}