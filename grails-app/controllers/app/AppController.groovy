package app

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.versions.VersionFSRepoService

import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import grails.plugin.springsecurity.SpringSecurityUtils

class AppController {

   def springSecurityService
   def versionFSRepoService
   
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
      def version_repo_sizes = [:] // org => versio repo size
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         count_ehrs = Ehr.count()
         count_contributions = Contribution.count()
         
         def orgs = Organization.list()
         
         orgs.each { __org ->
            version_repo_sizes << [(__org): versionFSRepoService.getRepoSizeInBytes(__org.uid)]
         }
         
         // sort by usage, decreasing
         version_repo_sizes = version_repo_sizes.sort { -it.value }
      }
      else
      {
         count_ehrs = Ehr.countByOrganizationUid(org.uid)
         count_contributions = Contribution.countByOrganizationUid(org.uid)
         
         version_repo_sizes << [(org): versionFSRepoService.getRepoSizeInBytes(org.uid)]
      }
      
      [count_ehrs:count_ehrs, count_contributions:count_contributions, version_repo_sizes: version_repo_sizes]
   }
}
