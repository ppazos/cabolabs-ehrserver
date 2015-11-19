package app

//import grails.plugin.springsecurity.annotation.Secured

class AppController {

   def springSecurityService
   
   //@Secured(value = ['ROLE_ADMIN', 'ROLE_ORG_MANAGER', 'ROLE_CLINICAL_MANAGER'])
   def index()
   {
      // shows main dashboard

      // http://stackoverflow.com/questions/6467167/how-to-get-current-user-role-with-spring-security-plugin
      //def roles = springSecurityService.getPrincipal().getAuthorities()
      //println springSecurityService.getPrincipal().getClass().getSimpleName() // String
      //println springSecurityService.getAuthentication() // UserPassOrgAuthToken
      //println springSecurityService.getAuthentication().getAuthorities() // [ROLE_ADMIN]
      //println springSecurityService.getCurrentUser() // error porque espera que springSecurityService.getPrincipal() sea Grails User
   }
}
