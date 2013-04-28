package common.change_control

import common.generic.AuditDetails
import ehr.Ehr
import javax.xml.bind.annotation.*

/**
 * La contribution queda pendiente/incompleta hasta que no se envien todas las versiones referenciadas.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
class Contribution {

<<<<<<< HEAD
   Ehr ehr // puntero para atras al ehr que contiene esta contribution
=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
   AuditDetails audit
   
   String uid = (java.util.UUID.randomUUID() as String)
   
   // Emula CONTRIBUTION.versions(Set<OBJECT_REF>) usando relaciones directas a
   // las versiones contenidas en la Contribution en lugar de OBJECT_REFs
   List versions
   static hasMany = [versions:Version]
   
   static constraints = {
   }
   
   static mapping = {
      audit column:'contrib_audit' // En algunos dbms audit o audit_id son reservados
      audit cascade: 'save-update'
      versions cascade: 'save-update'
   }
   
   static belongsTo = [Ehr]
}