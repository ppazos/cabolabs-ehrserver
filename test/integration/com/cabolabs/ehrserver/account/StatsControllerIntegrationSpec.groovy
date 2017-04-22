package com.cabolabs.ehrserver.account

import com.cabolabs.security.*
import grails.test.spock.IntegrationSpec

class StatsControllerIntegrationSpec extends IntegrationSpec {

   StatsController controller = new StatsController()
   
   private String orgUid = '11111111-1111-1111-1111-111111111178'
   
   private createOrganization()
   {
      def org = new Organization(uid: orgUid, name: 'CaboLabs', number: '123456')
      org.save(failOnError: true)
   }
   
   private createAdmin()
   {
      def user = new User(
         username: 'testadmin', password: 'testadmin',
         email: 'testadmin@domain.com',
         organizations: [Organization.findByUid(orgUid)]).save(failOnError:true, flush: true)
      
      def role = Role.findByAuthority(Role.AD) // created in bootstrap
      UserRole.create( user, role, true )
   }
   
   
   def setup()
   {
      createOrganization()
      createAdmin()
   }

   def cleanup()
   {
      def user = User.findByUsername("testadmin")
      def role = Role.findByAuthority(Role.AD)
      
      UserRole.remove(user, role)
      user.delete(flush: true)

      def org = Organization.findByUid(orgUid)
      org.delete(flush: true)
   }

   void "account stats"()
   {
      when:
         def model = controller.userAccountStats('testadmin')
         
      then:
         println model
         model != null
   }
}
