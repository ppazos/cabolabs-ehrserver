package com.cabolabs.security

import grails.test.spock.IntegrationSpec

class UserServiceIntegrationSpec extends IntegrationSpec {

   def userService
   
   private static String PS = System.getProperty("file.separator")
   
   /**
    * Be careful: integrations tests run the bootstrap!
    */
   
   def setup()
   {
      //println Role.count() // 5
      
      def org = new Organization(name: 'Test Org', number: '556677').save(failOnError:true, flush: true)
      
      def user = new User(username: 'testuser', password: 'testuser', email: 'user@domain.com', organizations: [org]).save(failOnError:true, flush: true)
      
      def role = new Role(authority: 'ROLE_XYZ').save(failOnError: true, flush: true)
      
      UserRole.create( user, role, true )
      
      def user_without_roles = new User(username: 'norole', password: 'norole', email: 'norole@domain.com', organizations: [org]).save(failOnError:true, flush: true)
   }
      

   def cleanup()
   {
      // deletes the created instances
      def user = User.findByUsername("testuser")
      def role = Role.findByAuthority('ROLE_XYZ')
      def org = Organization.findByNumber("556677")
      
      UserRole.remove(user, role, org)
      user.delete(flush: true)
      role.delete(flush: true)
      
      User.findByUsername("norole").delete(flush: true)
      
      org.delete(flush: true)
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
   
   void "test getUserAuthorities admin user"()
   {
      when:
         def user = userService.getByUsername('testuser')
         def authorities = userService.getUserAuthorities(user)
         println authorities
      
      then:
         assert authorities != null
         assert authorities.size() == 1
   }
   
   void "test getUserAuthorities user with no roles"()
   {
      when:
         def user = userService.getByUsername('norole')
         def authorities = userService.getUserAuthorities(user)
         println authorities
      
      then:
         assert authorities != null
         assert authorities.size() == 0
   }
}
