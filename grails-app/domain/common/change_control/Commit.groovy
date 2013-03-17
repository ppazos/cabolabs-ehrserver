package common.change_control

import ehr.Ehr

/**
 * Clase auxiliar para mantener los commits pendientes de ser finalizados.
 * Esta clase no es parte del modelo de openEHR es parte de la implementacion del EHR Server.
 * Se elimina cuando se finaliza el commit o se hace rollback.
 * 
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
class Commit {

   String ehrId
   String contributionId
   
   static transients = ['ehr', 'contribution']
   
   def getEhr()
   {
      return Ehr.findByEhrId(this.ehrId)
   }
   
   def getContribution()
   {
      return Contribution.findByUid(this.contributionId)
   }
}