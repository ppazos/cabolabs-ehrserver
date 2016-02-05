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
      
      def user = new User(username: 'user', password: 'user', email: 'user@domain.com', organizations: [org]).save(failOnError:true, flush: true)
      
      def role = new Role(authority: 'ROLE_XYZ').save(failOnError: true, flush: true)
      
      UserRole.create( user, role, true )
      
      def user_without_roles = new User(username: 'norole', password: 'norole', email: 'norole@domain.com', organizations: [org]).save(failOnError:true, flush: true)
   }
      

   def cleanup()
   {
   }

   void "test getByUsername existing user"()
   {
      when:
         def user = userService.getByUsername('user')
      
      then:
         assert user != null
         assert user.username == 'user'
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
         def user = userService.getByUsername('user')
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
