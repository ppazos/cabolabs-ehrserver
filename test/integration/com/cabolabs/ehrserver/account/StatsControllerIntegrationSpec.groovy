package com.cabolabs.ehrserver.account

import com.cabolabs.security.*
import grails.test.spock.IntegrationSpec
import com.cabolabs.ehrserver.account.*

class StatsControllerIntegrationSpec extends IntegrationSpec {

   StatsController controller = new StatsController()

   private String orgUid = '11111111-1111-1111-1111-111111111178'


   private createAdmin()
   {
      def adminRole = new Role(authority: Role.AD).save(failOnError: true, flush: true)

      def user = new User(
         username: 'testadmin', password: 'testadmin',
         email: 'testadmin@domain.com').save(failOnError:true, flush: true)

      UserRole.create( user, adminRole, Organization.findByUid(orgUid), true )
   }


   def setup()
   {
      // 1. Account setup: create account manager user
      def accman = new User(
         username: 'testaccman',
         password: 'testaccman',
         email: 'testaccman@domain.com',
      ).save(failOnError:true, flush: true)

      // 2. Account setup: create account
      def account = new Account(contact: accman, companyName:'Test company')

      // 3. Account setup: create organization
      def org = new Organization(uid: orgUid, name: 'CaboLabs', number: '123456')
      account.addToOrganizations(org)
      account.save(failOnError: true) // saves the org

      // 4. Account setup: create ACCMAN role
      def accmanRole = new Role(authority: Role.AM).save(failOnError: true, flush: true)

      // 5. Account setup: create user role association
      UserRole.create( accman, accmanRole, org, true )


      createAdmin()
   }

   def cleanup()
   {
   /*
      def org = Organization.findByUid(orgUid)
      def user = User.findByUsername("testadmin")
      def role = Role.findByAuthority(Role.AD)

      UserRole.remove(user, role, org)
      user.delete(flush: true)

      user = User.findByUsername("accman")
      role = Role.findByAuthority(Role.AM)

      UserRole.remove(user, role, org)
      user.delete(flush: true)

      org.delete(flush: true)
      */

      Account.list()*.delete() // should delete the orgs

      UserRole.list()*.delete()
      User.list()*.delete()
      Role.list()*.delete()
   }

   void "account stats"()
   {
      setup:
         // setup rest token user
         controller.request.securityStatelessMap = [username: 'testaccman', extradata:[organization:'123456', org_uid:orgUid]]

      when:
         def model = controller.userAccountStats(username)

      then:
         //println controller.response.text
         controller.response.json.organizations.keySet()[0] == orgUid
         controller.response.status == status

      where:
         username = 'testaccman'
         status = 200
         /* cant test other cases because the filter mock doesnt inject the springsecurityservice and the test fails because of that
         ''       | 400
         'orgman' | 400
         'accman' | 200
         */
   }
}
