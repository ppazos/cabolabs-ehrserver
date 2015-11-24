package com.cabolabs.security

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.validation.ValidationException
import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.security.Organization
import com.cabolabs.security.Role
import com.cabolabs.security.UserRole

@Transactional(readOnly = false)
class UserController {

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
   
   def simpleCaptchaService
   def notificationService
   def springSecurityService

   def index(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      
      def list, count
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = User.list(params)
         count = User.count()
      }
      else
      {
         // auth token used to login
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         
         // no pagination

         // users with the current org.uid in their organizations list
         list = User.withCriteria {
            organizations {
               eq('uid', org.uid)
            }
         }
         
         count = list.size()
      }
      
      
      
      respond list, model:[userInstanceCount: count]
   }

   def show(User userInstance) {
      respond userInstance
   }
   
   def login()
   {
      // http://stackoverflow.com/questions/32621369/customize-login-in-grails-spring-security-plugin
   }

   /**
    * 
    * @return
    */
   def register()
   {
      //println params
      
      if (!params.register) // show view
      {
        render view: "register", model: [userInstance: new User(params)]
      }
      else
      {
        boolean captchaValid = simpleCaptchaService.validateCaptcha(params.captcha)
        
        def u = new User(
          username: params.username,
          password: params.password,
          email: params.email
        )
        def o
        
        User.withTransaction{ status ->
        
          try
          {
            u.save(failOnError: true, flush:true)
            
            UserRole.create( u, (Role.findByAuthority('ROLE_USER')), true )
            
            // TODO: create an invitation with token, waiting for account confirmation
            // 
            o = new Organization(name: params.organization.name)
            o.save(failOnError: true, flush:true)
            u.addToOrganizations(o).save(failOnError: true, flush:true)
            
            // TODO: UserRole ORG_* needs a reference to the org, since the user
            //      can be ORG_ADMIN in one org and ORG_STAFF in another org.
            UserRole.create( u, (Role.findByAuthority('ROLE_ORG_STAFF')), true )
            UserRole.create( u, (Role.findByAuthority('ROLE_ORG_MANAGER')), true ) // the user is creating the organization, it should be manager also
          }
          catch (ValidationException e)
          {
            println u.errors
            println o?.errors
            
            status.setRollbackOnly()
          }
          
          // FIXME: avoid saving stuff if the captcha is incorrect
          if (!captchaValid) status.setRollbackOnly()
          
        } // transaction
        
        // TODO: create a test of transactionality, were the user is saved but the org not, and check if the user is rolled back
        
        if (u.errors.hasErrors() || o?.errors.hasErrors() || !captchaValid)
        {
          flash.message = 'user.registerError.feedback'
          render view: "register", model: [userInstance: u, organizationInstance: o, captchaValid: captchaValid]
        }
        else
        {
          notificationService.sendUserRegisteredEmail(u.email, [o.name, o.number])
          render (view: "registerOk")
        }
      }
   }
   
   def create() {
      respond new User(params)
   }

   @Transactional
   def save(User userInstance)
   {
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      println "user.save organizations 1 "+ userInstance.organizations
      
      def sendNotification = false
      if (!userInstance.password)
      {
         userInstance.enabled = false
         userInstance.setPasswordToken()
         
         sendNotification = true
      }
      
      
      // Associate orgs
      def orgUids = params.list("organizationUid")
      def newOrgs = Organization.findAllByUidInList(orgUids)
      newOrgs.each { newOrg ->
         userInstance.addToOrganizations(newOrg)
      }
      
      
      if (!userInstance.save(flush:true))
      {
         respond userInstance.errors, view:'create'
         return
      }

      /*
      userInstance.validate() // it was validated and might have an error because enabled is true by default but might not have pass
      
      if (userInstance.hasErrors())
      {
         respond userInstance.errors, view:'create'
         return
      }

      userInstance.save flush:true
      */
      
      println "user.save organizations 2 "+ userInstance.organizations
      
      // TODO: UserRole ORG_* needs a reference to the org, since the user
      //      can be ORG_ADMIN in one org and ORG_STAFF in another org.
      UserRole.create( userInstance, (Role.findByAuthority('ROLE_ORG_STAFF')), true )

      
      if (sendNotification)
      {
         // token to create the URL for the email is in the userInstance
         notificationService.sendUserCreatedEmail(
            userInstance.email,
            [userInstance]
         )
      }
      
      
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
            redirect userInstance
         }
         '*' { respond userInstance, [status: CREATED] }
      }
   }

   def edit(User userInstance) {
      respond userInstance
   }

   @Transactional
   def update(User userInstance)
   {
      println "update "+ params
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      // Update organizations
      // Remove current

      def orgsToRemove = []
      orgsToRemove += userInstance.organizations
      orgsToRemove.each { org ->
         println "removeFromOrganizations " + org
         userInstance.removeFromOrganizations(org)
      }
      
      userInstance.organizations.clear()
      

      // Associate new
      def orgUids = params.list("organizationUid")
      def newOrgs = Organization.findAllByUidInList(orgUids)
      newOrgs.each { newOrg ->
         userInstance.addToOrganizations(newOrg)
      }
      
      if (!userInstance.save(flush:true))
      {
         respond userInstance.errors, view:'edit'
         return
      }
      

      
      // Role updating
      
      // Delete all current roles
      // FIXME: if the logged user is the same as the userInstance, and it has admin or org man roles, those cant be removed.
      def currentRoles = UserRole.findByUser(userInstance)
      currentRoles*.delete()
      
      // Add selected roles
      def roles = params.list('role')
      roles.each { authority ->
         
         UserRole.create( userInstance, (Role.findByAuthority(authority)), true )
      }

      // / Role updating

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'User.label', default: 'User'), userInstance.id])
            redirect userInstance
         }
         '*'{ respond userInstance, [status: OK] }
      }
   }

   @Transactional
   def delete(User userInstance) {

      if (userInstance == null)
      {
         notFound()
         return
      }

      userInstance.delete flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'User.label', default: 'User'), userInstance.id])
            redirect action:"index", method:"GET"
         }
         '*'{ render status: NO_CONTENT }
      }
   }
   
   def resetPassword(String token, String newPassword)
   {
      // GET: display reset view
      // POST: try to reset the pass
      
      assert token // token comes always and is required for reset
      
      if (request.post)
      {
         assert newPassword
         
         def user = User.findByResetPasswordToken(token)
         if (!user)
         {
            // TODO
         }
         
         assert user
         
         user.password = newPassword
         user.enabled = true
         user.save(flush:true)
         
         // TODO: I18N
         flash.message = "Password reseted!"
         
         redirect controller:'login'
         return
      }
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
