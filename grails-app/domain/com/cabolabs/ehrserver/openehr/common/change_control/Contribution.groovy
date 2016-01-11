package com.cabolabs.ehrserver.openehr.common.change_control

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import java.text.SimpleDateFormat
import com.cabolabs.ehrserver.openehr.ehr.Ehr

/**
 * La contribution queda pendiente/incompleta hasta que no se envien todas las versiones referenciadas.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
class Contribution {

   Ehr ehr // puntero para atras al ehr que contiene esta contribution
   AuditDetails audit
   
   // uid is set by the client:
   // https://github.com/ppazos/cabolabs-ehrserver/issues/51
   String uid //= (java.util.UUID.randomUUID() as String)
   
   // Emula CONTRIBUTION.versions(Set<OBJECT_REF>) usando relaciones directas a
   // las versiones contenidas en la Contribution en lugar de OBJECT_REFs
   List versions = []
   static hasMany = [versions:Version]
   
   // Internal to simplify querying and grouping
   int dateGroup      // yyyymmdd to group by day
   int yearMonthGroup // yyyymm to group by month
   int yearGroup      // yyyy to group by year
   
   
   // multitenancy
   // copy of the EHR.organizationUid
   String organizationUid
   
   
   static constraints = {
      uid(nullable: false)
   }
   
   static mapping = {
      audit column:'contrib_audit' // En algunos dbms audit o audit_id son reservados
      audit cascade: 'save-update'
      versions cascade: 'save-update'
   }
   
   static belongsTo = [Ehr]
   
   
   def beforeInsert()
   {
      def d = this.audit.timeCommitted
      this.dateGroup      = Integer.parseInt( new SimpleDateFormat("yyyyMMdd").format(d) )
      this.yearMonthGroup = Integer.parseInt( new SimpleDateFormat("yyyyMM").format(d) )
      this.yearGroup      = Integer.parseInt( new SimpleDateFormat("yyyy").format(d) )
   }
   
   
   @Override
   public boolean equals(Object other)
   {
      if (!(other instanceof Contribution)) return false

      return this.uid.equals(other.uid)
   }
   
   @Override
   public int hashCode()
   {
      // http://stackoverflow.com/questions/113511/hash-code-implementation
      return this.uid.hashCode()
   }
}
