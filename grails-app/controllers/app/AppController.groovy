package app

import grails.plugin.springsecurity.annotation.Secured

class AppController {

   @Secured(value = ['ROLE_ADMIN', 'ROLE_ORG_MANAGER', 'ROLE_CLINICAL_MANAGER'])
   def index()
   {
      // shows main dashboard
   }
}
