package com.cabolabs.ehrserver.openehr.common.change_control

// FIXME: move to another package like helpers or commit

import com.cabolabs.ehrserver.openehr.ehr.Ehr

/**
 * Clase auxiliar para mantener los commits pendientes de ser finalizados.
 * Esta clase no es parte del modelo de openEHR es parte de la implementacion del EHR Server.
 * Se elimina cuando se finaliza el commit o se hace rollback.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
class Commit {

   String ehrUid
   String contributionId
   
   static transients = ['ehr', 'contribution']
   
   def getEhr()
   {
      return Ehr.findByUid(this.ehrUid)
   }
   
   def getContribution()
   {
      return Contribution.findByUid(this.contributionId)
   }
}