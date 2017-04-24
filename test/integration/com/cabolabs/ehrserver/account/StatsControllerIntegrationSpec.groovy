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
   
   private createUsers()
   {
      def user = new User(
         username: 'testadmin', password: 'testadmin',
         email: 'testadmin@domain.com',
         organizations: [Organization.findByUid(orgUid)]).save(failOnError:true, flush: true)
      
      def role = Role.findByAuthority(Role.AD) // role created in bootstrap
      UserRole.create( user, role, true )
      
      
      user = new User(
         username: 'accman', password: 'accman',
         email: 'accman@domain.com',
         organizations: [Organization.findByUid(orgUid)]).save(failOnError:true, flush: true)
      
      role = Role.findByAuthority(Role.AM) // role created in bootstrap
      UserRole.create( user, role, true )
   }
   
   
   def setup()
   {
      createOrganization()
      createUsers()
   }

   def cleanup()
   {
      def user = User.findByUsername("testadmin")
      def role = Role.findByAuthority(Role.AD)
      
      UserRole.remove(user, role)
      user.delete(flush: true)
      
      user = User.findByUsername("accman")
      role = Role.findByAuthority(Role.AM)
      
      UserRole.remove(user, role)
      user.delete(flush: true)

      def org = Organization.findByUid(orgUid)
      org.delete(flush: true)
   }

   void "account stats"()
   {
      setup:
         // setup rest token user
         controller.request.securityStatelessMap = [username: 'testadmin', extradata:[organization:'123456']]
      
      when:
         def model = controller.userAccountStats(username)
         
      then:
         println model
         model != null
         controller.response.status == status
         
      where:
         username = 'accman'
         status = 200
         /* cant test other cases because the filter mock doesnt inject the springsecurityservice and the test fails because of that
         ''       | 400
         'orgman' | 400
         'accman' | 200
         */
   }
}
