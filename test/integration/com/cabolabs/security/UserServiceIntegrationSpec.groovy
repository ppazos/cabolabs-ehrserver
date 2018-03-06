package com.cabolabs.security

import grails.test.spock.IntegrationSpec
import com.cabolabs.ehrserver.account.*

class UserServiceIntegrationSpec extends IntegrationSpec {

   def userService

   private String orgUid     = '11111111-1111-1111-1111-111111111178'
   private static String PS  = System.getProperty("file.separator")

   /**
    * Be careful: integrations tests run the bootstrap!
    */

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


      def user = new User(username: 'testuser', password: 'testuser', email: 'user@domain.com', organizations: [org]).save(failOnError:true, flush: true)

      def role = new Role(authority: 'ROLE_XYZ').save(failOnError: true, flush: true)

      UserRole.create( user, role, org, true )

      def user_without_roles = new User(username: 'norole', password: 'norole', email: 'norole@domain.com', organizations: [org]).save(failOnError:true, flush: true)
   }


   def cleanup()
   {
      // deletes the created instances
      /*
      def user = User.findByUsername("testuser")
      def role = Role.findByAuthority('ROLE_XYZ')
      def org = Organization.findByNumber("556677")

      UserRole.remove(user, role, org)
      user.delete(flush: true)
      role.delete(flush: true)

      User.findByUsername("norole").delete(flush: true)

      org.delete(flush: true)
      */

      Account.list()*.delete() // should delete the orgs

      UserRole.list()*.delete()
      User.list()*.delete()
      Role.list()*.delete()
   }

   void "test getByUsername existing user"()
   {
      when:
         def user = userService.getByUsername('testuser')

      then:
         assert user != null
         assert user.username == 'testuser'
   }

   void "test getByUsername non existing user"()
   {
      when:
         def user = userService.getByUsername('userxxx')

      then:
         assert user == null
   }

   void "test getByUsername null username"()
   {
      when:
         def user = userService.getByUsername(null)

      then:
         assert user == null
   }

   void "test getUserAuthorities for existing user with roles"()
   {
      when:
         def user = userService.getByUsername('testuser')
         def authorities = userService.getUserAuthorities(user, Organization.findByNumber("556677"))

      then:
         assert authorities != null
         assert authorities.size() == 1
         assert authorities[0].authority == "ROLE_XYZ"
   }

   void "test getUserAuthorities user with no roles"()
   {
      when:
         def user = userService.getByUsername('norole')
         def authorities = userService.getUserAuthorities(user, Organization.findByNumber("556677"))

      then:
         assert authorities != null
         assert authorities.size() == 0
   }
}
