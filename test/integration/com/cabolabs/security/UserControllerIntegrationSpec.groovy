package com.cabolabs.security

import grails.test.spock.IntegrationSpec
import grails.test.mixin.TestFor

@TestFor(UserController)
class UserControllerIntegrationSpec extends IntegrationSpec {
   
   // services used in the controller
   def springSecurityService
   def simpleCaptchaService
   def notificationService
   
   def setup()
   {
      def org = new Organization(name: 'Test Org', number: '556677').save(failOnError:true, flush: true)
      
      def user = new User(username: 'testuser', password: 'testuser', email: 'user@domain.com', organizations: [org]).save(failOnError:true, flush: true)
      def role = new Role(authority: 'ROLE_XYZ').save(failOnError: true, flush: true)
      
      UserRole.create( user, role, true )
   }

   def cleanup()
   {
      // deletes the created instances
      def user = User.findByUsername("testuser")
      def role = Role.findByAuthority('ROLE_XYZ')
      
      UserRole.remove(user, role)
      user.delete(flush: true)
      role.delete(flush: true)
      
      Organization.findByNumber("556677").delete(flush: true)
   }

      
   void "test reset password with no token"()
   {
      when:
        controller.resetPassword()
      
      then:
        controller.response.redirectedUrl == '/login/auth'
        controller.flash.message == 'Token no present and needed for reseting password, try reseting again'
   }
   
   void "test reset password with invalid token"()
   {
      when:
        controller.params.token = 'invalid token'
        controller.resetPassword()
      
      then:
        controller.response.redirectedUrl == '/login/auth'
        controller.flash.message == "Password reset was already done, if you don't remember your password, click on 'Forgot password?'"
   }
   
   void "test reset password with valid token, post to reset, no new password"()
   {
      setup:
        def user = User.findByUsername('testuser')
        user.setPasswordToken()
        user.enabled = false // if enabled, password token is cleaned beforeInsert
        user.save(flush:true)
        
      when:
        controller.params.token = user.resetPasswordToken
        controller.request.method = "POST"
        def res = controller.resetPassword()
      
      then:
        //println res // null
        //println controller
        //println controller.modelAndView // null, can't check the returned view
        controller.flash.message == "Please enter your new password and confirm it"
   }
   
   void "test reset password with valid token, post to reset, new password, without confirm"()
   {
      setup:
        def user = User.findByUsername('testuser')
        user.setPasswordToken()
        user.enabled = false // if enabled, password token is cleaned beforeInsert
        user.save(flush:true)
        
      when:
        controller.params.token = user.resetPasswordToken
        controller.params.newPassword = 'newPassword'
        controller.request.method = "POST"
        def res = controller.resetPassword()
        user.refresh()
      
      then:
        // TODO: check the value of the new password stored
        controller.flash.message == "Please enter your new password and confirm it"
        /*
        user.enabled == true
        user.resetPasswordToken == null
        controller.response.redirectedUrl == '/login/auth'
        controller.flash.message == "Password was reset!"
        */
   }
   
   void "test reset password with valid token, post to reset, new password, with confirm"()
   {
      setup:
        def user = User.findByUsername('testuser')
        user.setPasswordToken()
        user.enabled = false // if enabled, password token is cleaned beforeInsert
        user.save(flush:true)
        
      when:
        controller.params.token = user.resetPasswordToken
        controller.params.newPassword = 'newPassword'
        controller.params.confirmNewPassword = 'newPassword'
        controller.request.method = "POST"
        def res = controller.resetPassword()
        user.refresh()
      
      then:
        // TODO: check the value of the new password stored
        user.enabled == true
        user.resetPasswordToken == null
        controller.response.redirectedUrl == '/login/auth'
        controller.flash.message == "Password was reset!"
   }
}
