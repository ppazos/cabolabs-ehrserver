package com.cabolabs.security

import grails.test.mixin.*
import spock.lang.*
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.SpringSecurityService

@TestFor(UserController)
@Mock([User, Role, UserRole])
class UserControllerSpec extends Specification {

   // this is executed before al the tests, allows metaprogramming.
   def setupSpec()
   {
      // without this actions that check permissions fail
      SpringSecurityUtils.metaClass.static.ifAllGranted = { String role ->
         return true
      }
   }
   
   def setup()
   {
      println "setup"
      
      // mock logged in user
      // http://stackoverflow.com/questions/11925705/mock-grails-spring-security-logged-in-user
      def organization = new Organization(name: 'Hospital de Clinicas', number: '1234')
      organization.save(failOnError:true, flush: true)

      def loggedInUser = new User(username:"admin", password:"admin", email:"e@m.com") //, organizations:[organization])
      loggedInUser.addToOrganizations(organization)
      loggedInUser.save(failOnError:true, flush: true)
      
      def role = new Role(authority: 'ROLE_ADMIN')
      role.save(failOnError:true, flush: true)
      
      UserRole.create( loggedInUser, role, true )
      
      controller.springSecurityService = [
        encodePassword: 'admin',
        reauthenticate: { String u -> true},
        loggedIn: true,
        principal: loggedInUser,
        currentUser: loggedInUser
      ]
      
      controller.userService = new UserService()
   }
   
   def cleanup()
   {
      println "cleanup"
      
      def user = User.findByUsername("admin")
      def role = Role.findByAuthority('ROLE_ADMIN')
      
      UserRole.remove(user, role)
      user.delete(flush: true)
      role.delete(flush: true)
      
      Organization.findByNumber("1234").delete(flush: true)
      
      controller.springSecurityService = []
   }
   
   
   def populateValidParams(params)
   {
        assert params != null
        // TODO: Populate valid properties like...
        params["username"] = 'testuset' // should not be admin, that user is created to be the logged in user.
        params["password"] = 'testuset'
        params["email"] = 'testuset@m.com'
        params["organizationUid"] = '1234'
   }

   void "Test metaprogramming"()
   {
      when:
          def ret = SpringSecurityUtils.ifAllGranted("pepe")
          
      then:
         ret == true
   }
   
   void "Test the index action returns the correct model"()
   {
        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            model.userInstanceList.size() == 1 // the logged in user is returned
            model.userInstanceCount == 1
            model.userInstanceList[0].username == "admin"
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
            def user = new User()
            user.validate()
            controller.save(user)

      then:"The create view is rendered again with the correct model"
        
            view == "/user/create"
            model.userInstance != null // returns a default instance to fill fields with default values


      when:"The save action is executed with a valid instance"
            response.reset()
            controller.request.method = 'POST'
            request.contentType = FORM_CONTENT_TYPE
            populateValidParams(params)
            user = new User(params)
            
            println "valid "+ user.validate() +" "+ user.errors

            controller.save(user)

      then:"A redirect is issued to the show action"
      println response.redirectedUrl
      println " view "+ view
      println " model "+ model
      // view /user/create           // CHECK: is not redirecting to show!
      // model [userInstance:admin]
      
            controller.flash.message != null
            User.count() == 1
   }

   void "Test that the show action returns the correct model"()
   {
      /*
       setup:
          SpringSecurityService.metaClass.getAuthentication { ->
             return [
                username: 'user',
                password: 'pass',
                organization: '1234'
             ]
          }
          //controller.springSecurityService =  new SpringSecurityService()
       */
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def user = new User(params)
            controller.show(user)

        then:"A model is populated containing the domain instance"
            model.userInstance == user
   }

   void "Test that the edit action returns the correct model"()
   {
      /*
       given:
          def svcMock = mockFor(SpringSecurityService)
          //svcMock.authentication
          svcMock.demand.getAuthentication {
             return [
                username: 'user',
                password: 'pass',
                organization: '1234'
             ]
          }
          controller.springSecurityService = svcMock.createMock()
        */
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def user = new User(params)
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
            println response.redirectedUrl
            //response.redirectedUrl == '/user/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            controller.request.method = 'PUT'
            def user = new User()
            user.validate()
            
            // params needed for update
            controller.params.organizationUid = '1234'
            controller.params.role = ['ROLE_USER']
            
            
            // a valid user should have 1 role
            def role = new Role(authority: 'ROLE_USER')
            role.save(failOnError:true, flush: true)
            UserRole.create( user, role, true )
            
            
            controller.update(user)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.userInstance == user

            
        when:"A valid domain instance is passed to the update action"
            response.reset()
            controller.request.method = 'PUT'
            populateValidParams(params)
            user = new User(params).save(flush: true)
            
            
            controller.update(user)

        then:"A redirect is issues to the show action"
        println response.redirectedUrl
            //response.redirectedUrl == "/user/show/$user.id"
            flash.message != null
   }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            controller.request.method = 'DELETE'
            request.contentType = FORM_CONTENT_TYPE
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/user/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def user = new User(params).save(flush: true)

        then:"It exists"
            User.count() == 2 // counts also the logged in user

        when:"The domain instance is passed to the delete action"
            controller.delete(user)

        then:"The instance is deleted"
            User.count() == 1 // leaves the logged in user
            response.redirectedUrl == '/user/index'
            flash.message != null
    }
}
