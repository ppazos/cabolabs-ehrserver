package app

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import grails.plugin.springsecurity.SpringSecurityUtils

class AppController {

   def springSecurityService
   
   def index()
   {
      // shows main dashboard

      // http://stackoverflow.com/questions/6467167/how-to-get-current-user-role-with-spring-security-plugin
      //def roles = springSecurityService.getPrincipal().getAuthorities()
      //println springSecurityService.getPrincipal().getClass().getSimpleName() // String
      //println springSecurityService.getAuthentication() // UserPassOrgAuthToken
      //println springSecurityService.getAuthentication().getAuthorities() // [ROLE_ADMIN]
      //println springSecurityService.getCurrentUser() // error porque espera que springSecurityService.getPrincipal() sea Grails User
      
      // Count EHRs
      def count_ehrs
      def count_contributions
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         count_ehrs = Ehr.count()
         count_contributions = Contribution.count()
      }
      else
      {
         // auth token used to login
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         
         count_ehrs = Ehr.countByOrganizationUid(org.uid)
         count_contributions = Contribution.countByOrganizationUid(org.uid)
      }
      
      [count_ehrs:count_ehrs, count_contributions:count_contributions]
   }
}
