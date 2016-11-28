package com.cabolabs.ehrserver.ehr.clinical_documents

import com.cabolabs.security.Organization

class OperationalTemplateIndexShare {

   OperationalTemplateIndex opt
   Organization organization
   
   static constraints = {
      opt nullable: false
      organization: nullable: false
   }
}
