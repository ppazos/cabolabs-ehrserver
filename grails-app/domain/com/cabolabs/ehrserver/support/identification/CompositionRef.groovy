package com.cabolabs.ehrserver.support.identification

import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import com.cabolabs.ehrserver.openehr.common.change_control.Version


/**
 * Emula OBJECT_REF a una COMPOSITION para ser utilizada desde EHR.
 *  
 * @author Pablo Pazos Gutierrez <pablo@openehr.org.es>
 *
 */
//@XmlAccessorType(XmlAccessType.FIELD)
class CompositionRef {

   // Referencia valida localmente
   String namespace = "local"
   
   // Apunta a una COMPOSITION
   String type = "COMPOSITION"
   
   // Identificador confiable de la COMPOSITION, asignado por el EHR Server cuando se hace commit
   // Es un UUID (java.util.UUID.randomUUID() as String)
   String value
   
   static constraints = {
   }
   
   // Cuando se salva la version se debe salvar la CompositionRef
   static belongsTo = [Version]
}
