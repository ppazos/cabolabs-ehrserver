package support.identification

import javax.xml.bind.annotation.*
import ehr.Ehr
import common.change_control.Version

/**
 * FIXME: creo que se podria usar directamente la CONTRIBUTION sin necesidad de la REF
 * 
 * Emula OBJECT_REF a una CONTRIBUTION para ser utilizada desde EHR.
 *
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
class ContributionRef {
   
   // Referencia valida localmente
   String namespace = "local"
   
   // Apunta a una CONTRIBUTION
   String type = "CONTRIBUTION"
   
   // Identificador de la CONTRIBUTION, asignado por el EHR Server cuando se hace commit
   // Es un UUID (java.util.UUID.randomUUID() as String)
   String value
   
   
   static constraints = {
   }
   
   // ContributionRef se debe guardar en cascada cuando se guarda el Ehr o cuando se guarda una Version
   static belongsTo = [Ehr, Version]
}