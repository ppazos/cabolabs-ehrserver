package com.cabolabs.security

/*
 * This test is complete and updated. 2017-04-16
 *
 * The UserControllerIntegrationSpec test were added to this unit test
 * because of some issues with integration tests and @Transactional make
 * impossible to implement as integration.
 *
 * This is a possible solution, should be tried:
 * http://stackoverflow.com/questions/39831355/grails-spock-unit-test-requires-to-mock-transaction-manager
 *
 * This should be used  to test the UserController.
 */

import java.util.List;

import grails.test.mixin.*
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import spock.lang.*
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.SpringSecurityService
import com.cabolabs.ehrserver.account.*

@TestFor(UserController)
@Mock([User, Role, UserRole, Organization])
class UserControllerSpec extends Specification {

   static String organization_number = '123456'

   // this is executed before all the tests, allows metaprogramming.
   def setupSpec()
   {
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
      def organization = new Organization(name: 'Hospital de Clinicas', number: organization_number)
      account.addToOrganizations(organization)
      account.save(failOnError: true) // saves the organization

      // 4. Account setup: create ACCMAN role
      def accmanRole = new Role(authority: Role.AM).save(failOnError: true, flush: true)

      // 5. Account setup: create user role association
      UserRole.create( accman, accmanRole, organization, true )



      // mock logged in user
      // http://stackoverflow.com/questions/11925705/mock-grails-spring-security-logged-in-user

      def loggedInUser = new User(username:"orgman", password:"orgman", email:"e@m.com")
      loggedInUser.save(failOnError:true, flush: true)

      def orgmanRole = new Role(authority: Role.OM)
      orgmanRole.save(failOnError:true, flush: true)

      UserRole.create( loggedInUser, orgmanRole, organization, true )


      def userRole = new Role(authority: Role.US)
      userRole.save(failOnError:true, flush: true)



      controller.springSecurityService = [
        encodePassword: 'orgman',
        reauthenticate: { String u -> true},
        loggedIn: true,
        principal: loggedInUser,
        currentUser: loggedInUser,
        authentication: [username:'orgman', organization:organization_number]
      ]

      controller.notificationService = [
         sendUserRegisteredOrCreatedEmail: { mail, params -> println "Email sent ${mail}" }
      ]

      // mock configurationService and ConfigurationItem
      controller.configurationService = [
         getValue: { key -> if (key == 'ehrserver.console.lists.max_items' ) return 20 }
      ]

      controller.set_org_for_tests(organization)

/*
      controller.userService = [
         saveAndNotify : { User userInstance, params ->

            userInstance.save(failOnError:true) // need to save the user, if not the save test fails
         },
         updateOrganizations : { User user, List newOrgUids -> return }
      ]
*/

      /* this doesnt work, the mock above does
      controller.userService = new UserService()

      // mocking injection
      controller.userService.springSecurityService = controller.springSecurityService
      */

      // without this actions that check permissions fail
      SpringSecurityUtils.metaClass.static.ifAllGranted = { String _role ->
         return controller.springSecurityService.principal.authoritiesContains(_role, organization)
      }
   }

   def cleanup()
   {
      /*
      def user = User.findByUsername("orgman")
      def role = Role.findByAuthority(Role.OM)
      def userrole = Role.findByAuthority(Role.US)

      def org = Organization.findByNumber("1234")

      UserRole.remove(user, role, org)
      user.delete(flush: true)
      role.delete(flush: true)
      userrole.delete(flush: true)

      org.delete(flush: true)
      */

      Account.list()*.delete() // should delete the orgs

      UserRole.list()*.delete()
      User.list()*.delete()
      Role.list()*.delete()

      controller.springSecurityService = []
   }

   /*
    * A valid user has
    * - an id (it is saved)
    * - has a role
    * - is associated with one organization
    */
   def generateValidUser(boolean nodata = false)
   {
      def params = new GrailsParameterMap(request) //[:] // GrailsParameterMap allows to use methods like list(name) used in the code
      if (!nodata)
      {
         populateValidParams(params)
      }
      def user = new User(params)

      // should have an organization
      def org = Organization.findByNumber(organization_number)

      // sould have an id
      user.save(flush: true)

      // should have 1 role
      def role = Role.findByAuthority(Role.US)

      UserRole.create( user, role, org, true )

      return user
   }


   def populateValidParams(params)
   {
        assert params != null
        params["username"] = 'testuser' // should not be admin, that user is created to be the logged in user.
        params["password"] = 'testuser'
        params["email"] = 'testuser@m.com'
   }

   void "Test metaprogramming"()
   {
      when:
          def ret = SpringSecurityUtils.ifAllGranted("pepe")

      then:
         ret == false
   }

   void "Test the index action returns the correct model"()
   {
        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            model.userInstanceList.size() == 2 // users created on the setup are returned
            model.userInstanceCount == 2
            model.userInstanceList[0].user.username == "testaccman"
            model.userInstanceList[1].user.username == "orgman"
   }

   void "Test the create action returns the correct model"()
   {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.userInstance!= null
   }

   void "Test the save action correctly persists an instance"()
   {
      when:"The save action is executed with an invalid instance"
            controller.request.method = 'POST'
            request.contentType = FORM_CONTENT_TYPE

            def user = generateValidUser(true) // generates user, role, org etc without user data
            user.validate() // invalid, user doesnt have data

            controller.save(user)

      then:"The create view is rendered again with the correct model"
            view == "/user/create"
            model.userInstance != null // returns a default instance to fill fields with default values


      when:"The save action is executed with a valid instance but missing params"
            response.reset()

            populateValidParams(params)
            user = new User(params)

            // no organizationUid and role params

            controller.save(user)

      then:"Back to user/create to add the missing params"
            view == '/user/create'
            model.userInstance.username == user.username
            controller.flash.message == 'user.update.oneRoleShouldBeSelected'
            User.count() == 2 // users created from setup


      when:"The save action is executed with a valid instance"
            response.reset()

            populateValidParams(params)
            user = new User(params)

            // params needed for save
            controller.params[ Organization.findByNumber(organization_number).uid ] = ['ROLE_USER']

            controller.save(user)

      then:"A redirect is issued to the show action"
            controller.flash.message == 'default.created.message'
            response.redirectedUrl == "/user/show/$user.id"
            model.userInstance.username == user.username
            User.count() == 3
   }


   void "Test that the show action returns the correct model"()
   {
//
//       setup:
//          SpringSecurityService.metaClass.getAuthentication { ->
//             return [
//                username: 'user',
//                password: 'pass',
//                organization: '1234'
//             ]
//          }
//          //controller.springSecurityService =  new SpringSecurityService()
//
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            def user = generateValidUser()
            controller.show(user)

        then:"A model is populated containing the domain instance"
            model.userInstance == user
   }


   void "Test that the edit action returns the correct model"()
   {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            def user = generateValidUser()
            controller.edit(user)

        then:"A model is populated containing the domain instance"
            model.userInstance == user
   }


   void "Test the update action performs an update on a valid domain instance"()
   {
      when:"Update is called for a domain instance that doesn't exist"
            controller.request.method = 'PUT'
            request.contentType = FORM_CONTENT_TYPE
            controller.update(null)

      then:"A 404 error is returned"
            response.redirectedUrl == '/user/index'
            flash.message != null


      when:"An invalid domain instance is passed to the update action"
            response.reset()

            def user = generateValidUser(true) // generates user, role, org etc without user data
            user.validate() // invalid, doesnt have data

            // params needed for update
            controller.params['no existing org'] = [] // no roles

            controller.update(user)

      then:"The edit view is rendered again with the invalid instance"
            view == '/user/edit'
            model.userInstance == user


      when:"A valid domain instance but with an org uid that doesnt belongs to the logged in user"
            response.reset()

            user = generateValidUser()

            // params needed for update
            controller.params[Organization.findByNumber(organization_number).uid] = [] // no roles

            controller.update(user)

      then:"A redirect is issues to the show action"
            controller.flash.message == 'user.update.oneRoleShouldBeSelected'
            view == "/user/edit"
            model.userInstance == user


      when:"A valid domain instance is passed to the update action"
            response.reset()

            // uses the same user created on the previous case, to avoid unique username violation
            //user = generateValidUser()

            // params needed for update
            controller.params[Organization.findByNumber(organization_number).uid] = ['ROLE_USER']

            controller.update(user)

       then:"A redirect is issues to the show action"
            controller.flash.message == 'default.updated.message'
            response.redirectedUrl == "/user/show/$user.id"
   }

   void "Test avoid removing higher role from self"()
   {
      setup:
         /*
         def accmanuser = new User(username:"accman", password:"accman", email:"accman@m.com")
         accmanuser.save(failOnError:true, flush: true)

         def accman = new Role(authority: Role.AM)
         accman.save(failOnError:true, flush: true)

         def org = Organization.get(1)

         UserRole.create( accmanuser, accman, org, true )
         */
         def accmanuser = UserRole.findByRole( Role.findByAuthority(Role.AM) ).user
         def org = accmanuser.organizations[0]

         // logged user
         controller.springSecurityService = [
            encodePassword: 'testaccman',
            reauthenticate: { String u -> true},
            loggedIn: true,
            principal: accmanuser,
            currentUser: accmanuser,
            authentication: [username:'testaccman', organization:organization_number]
         ]

      when:
         controller.request.method = 'PUT'
         request.contentType = FORM_CONTENT_TYPE

         // params needed for update
         controller.params[org.uid] = [Role.US] // should add this but not remove AM

         controller.update(accmanuser)

      then:
         controller.response.status == 302
         controller.flash.message == 'default.updated.message'
         //response.redirectedUrl == "/user/show/$user.id"
         accmanuser.getAuthorities(org).size() == 2 // adds user but maintains accman

      cleanup:
         println "clean"
         //UserRole.remove(accmanuser, accman, org)
         //accmanuser.delete(flush: true)
         //accman.delete(flush: true)
   }


   void "Test that the delete action deletes an instance if it exists"()
   {
      when:"The delete action is called for a null instance"
            controller.request.method = 'DELETE'
            request.contentType = FORM_CONTENT_TYPE
            controller.delete(null)

      then:"A 404 is returned"
            response.redirectedUrl == '/user/index'
            flash.message != null


      when:"A domain instance is created"
            response.reset()
            def user = generateValidUser() // creates and saves user

      then:"It exists"
            User.count() == 3 // counts new user and the 2 created on the setup


      when:"The domain instance is passed to the delete action"
            controller.delete(user)

      then:"The instance is deleted"
            User.count() == 2 // leaves the users created in the setup
            response.redirectedUrl == '/user/index'
            flash.message != null
   }


   void "test reset password with no token"()
   {
      when:
        controller.request.method = "GET"
        controller.resetPassword()

      then:
        controller.response.redirectedUrl == '/login/auth'
        controller.flash.message == 'user.resetPassword.noToken'
   }

   void "test reset password with invalid token"()
   {
      when:
        controller.params.token = 'invalid token'
        controller.resetPassword()

      then:
        controller.response.redirectedUrl == '/login/auth'
        controller.flash.message == "user.resetPassword.alreadyResetOrExpired"
   }

   void "test reset password with valid token, post to reset, no new password"()
   {
      setup:
        def user = generateValidUser()
        //def user = User.findByUsername('testuser')
        user.setPasswordToken()
        //user.enabled = false // if enabled, password token is cleaned beforeInsert
        user.save(flush:true)

      when:
        controller.params.token = user.resetPasswordToken
        controller.request.method = "POST"
        def res = controller.resetPassword()

      then:
        //println res // null
        //println controller
        //println controller.modelAndView // null, can't check the returned view
        controller.flash.message == "user.resetPassword.passwordConfirmationNeeded"
   }

   void "test reset password with valid token, post to reset, new password, without confirm"()
   {
      setup:
        def user = generateValidUser()
        user.setPasswordToken()
        //user.enabled = false // if enabled, password token is cleaned beforeInsert
        user.save(flush:true)

      when:
        controller.params.token = user.resetPasswordToken
        controller.params.newPassword = 'newPassword'
        controller.request.method = "POST"
        def res = controller.resetPassword()
        user.refresh()

      then:
        // TODO: check the value of the new password stored
        controller.flash.message == "user.resetPassword.passwordConfirmationNeeded"
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
        def user = generateValidUser()
        user.setPasswordToken()
        //user.enabled = false // if enabled, password token is cleaned beforeInsert
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
        controller.flash.message == "user.resetPassword.passwordResetOK"
   }
}
