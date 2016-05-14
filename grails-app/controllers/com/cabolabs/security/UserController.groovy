package com.cabolabs.security

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.validation.ValidationException
import grails.plugin.springsecurity.SpringSecurityUtils

import com.cabolabs.security.Organization
import com.cabolabs.security.Role
import com.cabolabs.security.UserRole

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless
import grails.converters.*
import grails.util.Holders

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
   
   
   // endpoint
   @SecuredStateless
   def profile(String username)
   {
      // username and organization number used on the API login
      def _username = request.securityStatelessMap.username
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _user = User.findByUsername(_username)
      

      // user I want to access
      def u = User.findByUsername(username)
      if (!username || !u)
      {
         withFormat {
            xml {
               render(status: 404, contentType: "text/xml", text: '<result>User doesn\'t exists</result>', encoding:"UTF-8")
            }
            json {
               render(status: 404, contentType: "application/json", text: '{"result": "User doesn\'t exists"}', encoding:"UTF-8")
            }
         }
         return
      }
      
      
      def allowed = (
         _user.authoritiesContains('ROLE_ADMIN') || // admins access everything
         (
            ( u.organizations.count{ it.number == _orgnum } == 1 ) && // organization of the logged user match one of the organizations of the requested user
            (
               u.username == _username || // user want to access self profile
               ( // org managers can see users with lees power than them
                  _user.authoritiesContains('ROLE_ORG_MANAGER') && _user.higherAuthority.higherThan( u.higherAuthority )
               )
            )
         )
      )
      
      if (!allowed)
      {
         withFormat {
            xml {
               render(status: 401, contentType: "text/xml", text: '<result>Unauthorized to access user info</result>', encoding:"UTF-8")
            }
            json {
               render(status: 401, contentType: "application/json", text: '{"result": "Unauthorized to access user info"}', encoding:"UTF-8")
            }
         }
         return
      }

      
      // allowed
      
      def data = [
         username: u.username,
         email: u.email,
         organizations: u.organizations
      ]
      
      withFormat {
         xml {
            def result = data as XML
            render(text: result, contentType:"text/xml", encoding:"UTF-8")
         }
         json {

            def result = data as JSON
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
      }
   }
   

   def show(User userInstance)
   {
      def orgnumber = springSecurityService.authentication.organization
      
      // FIXME: instead of checking if the logged user is not and admin trying to show an admin, it should check
      //        if the logged user has less permisssions than the userInstance.
      
      // If the user is admin (can see all) or
      // the userInstance has the same org as the logged user and the userInstance is not an admin.
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") ||
          (
           userInstance.organizations.count { it.number == orgnumber } > 0 &&
           !userInstance.authoritiesContains("ROLE_ADMIN")
          )
         )
      {
          respond userInstance
          return
      }
      flash.message = "You don't have permissions to access the user"
      redirect action:'index'
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
          //password: params.password,
          email: params.email,
          enabled: false
        )
        def o
        
        
        // generates a passwrod reset token, used in the email notification
        u.setPasswordToken()
        
        
        User.withTransaction{ status ->
        
          try
          {
            // TODO: create an invitation with token, waiting for account confirmation
            // 
            o = new Organization(name: params.org_name)
            o.save(failOnError: true, flush:true)
            
            // needs an organization before saving
            u.addToOrganizations(o).save(failOnError: true, flush:true) // FIXME: this is saving the user and we save the user below
            
            u.save(failOnError: true, flush:true)
            
            // TODO: UserRole ORG_* needs a reference to the org, since the user
            //      can be ORG_ADMIN in one org and ORG_STAFF in another org.
            //UserRole.create( u, (Role.findByAuthority('ROLE_ORG_STAFF')), true )
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
          //notificationService.sendUserRegisteredEmail(u.email, [o.name, o.number])
          // token to create the URL for the email is in the userInstance
          notificationService.sendUserCreatedEmail( u.email, [u], true )
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
      
      if (!userInstance.password)
      {
         userInstance.enabled = false
         userInstance.setPasswordToken()
      }
      
      
      // Associate orgs
      def orgUids = params.list("organizationUid")
      def newOrgs = Organization.findAllByUidInList(orgUids)
      newOrgs.each { newOrg ->
         userInstance.addToOrganizations(newOrg)
      }
      
      if (!userInstance.save(flush:true))
      {
         render model: [userInstance: userInstance], view:'create'
         return
      }


      // TODO: UserRole ORG_* needs a reference to the org, since the user
      //      can be ORG_ADMIN in one org and ORG_STAFF in another org.
      //UserRole.create( userInstance, (Role.findByAuthority('ROLE_ORG_STAFF')), true )

      // Add selected roles
      def roles = params.list('role')
      roles.each { authority ->
         
         UserRole.create( userInstance, (Role.findByAuthority(authority)), true )
      }
      
      

      // token to create the URL for the email is in the userInstance
      notificationService.sendUserCreatedEmail( userInstance.email, [userInstance] )

      
      
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
            redirect userInstance
         }
         '*' { respond userInstance, [status: CREATED] }
      }
   }

   def edit(User userInstance)
   {
      def orgnumber = springSecurityService.authentication.organization
      
      // FIXME: instead of checking if the logged user is not and admin trying to show an admin, it should check
      //        if the logged user has less permisssions than the userInstance.
      
      // If the user is admin (can see all) or
      // the userInstance has the same org as the logged user and the userInstance is not an admin.
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") ||
          (
           userInstance.organizations.count { it.number == orgnumber } > 0 &&
           !userInstance.authoritiesContains("ROLE_ADMIN")
          )
         )
      {
          respond userInstance
          return
      }
      flash.message = "You don't have permissions to access the user"
      redirect action:'index'
   }

   @Transactional
   def update(User userInstance)
   {
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      // FIXME: move this check to a service
      // check that I can update the userInstance
      def orgnumber = springSecurityService.authentication.organization
      // If the user is admin (can see all) or
      // the userInstance has the same org as the logged user and the userInstance is not an admin.
      if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") &&
          !(
           userInstance.organizations.count { it.number == orgnumber } > 0 &&
           !userInstance.authoritiesContains("ROLE_ADMIN")
          )
         )
      {
         flash.message = "You don't have permissions to access the user"
         redirect action:'index'
         return
      }
      
      
      
      // Selected roles from edit view
      def roles = params.list('role')
      
      // if the user is editing his data and it is an admin, he can't remove the admin role
      def loggedUser = springSecurityService.currentUser
      if (loggedUser.id == userInstance.id && userInstance.authoritiesContains('ROLE_ADMIN'))
      {
         if (!roles.contains('ROLE_ADMIN'))
         {
            flash.message = "You can't remove your ADMIN role"
            respond userInstance, view:'edit'
            return
         }
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
   
   
   // token comes always and is required for reset
   def resetPassword(String token, String newPassword, String confirmNewPassword)
   {
      // GET: display reset view
      // POST: try to reset the pass
      
      if (!token)
      {
         flash.message = "Token no present and needed for reseting password, try reseting again"
         redirect controller:'login', action:'auth'
         return
      }
      
      def user = User.findByResetPasswordToken(token)
      if (!user)
      {
         flash.message = "Password reset was already done, if you don't remember your password, click on 'Forgot password?'"
         redirect controller:'login', action:'auth'
         return
      }
      
      if (request.post)
      {
         if (!newPassword || !confirmNewPassword)
         {
            flash.message = "Please enter your new password and confirm it"
            return
         }
         
         def min_length = Holders.config.app.security.min_password_length
         if (newPassword.size() < min_length)
         {
            flash.message = "The password length should be at least $min_length"
            return
         }
         
         if (newPassword != confirmNewPassword)
         {
            flash.message = "The password confirm is not equal to the password, please try again"
            return
         }
         
         
         user.password = newPassword
         user.enabled = true
         user.save(flush:true)

         flash.message = "Password was reset!"
         redirect controller:'login', action:'auth'
         return
      }
   }
   
   def forgotPassword(String email)
   {
      if (request.post)
      {
         def user = User.findByEmail(email)
         
         if (!user)
         {
            flash.message = "Can't find a user for that email"
            return
         }
         
         
         // generates a password reset token, used in the email notification
         user.setPasswordToken()
         user.enabled = false // if enabled, password token is cleaned beforeInsert
         user.save(flush:true)
         
         
         try
         {
            notificationService.sendForgotPasswordEmail( user.email, [user] )
         }
         catch (Exception e) // FIXME: should rollback the user update if the email fails or retry the email send
         {
            log.error e.message
            
            flash.message = "There was a problem sending the email notification, please try again."
            return
         }
         

         
         flash.message = "Password reset email was sent, please check the instructions on that email"
         redirect controller:'login', action:'auth'
         return
      }
      // display the forgotPassword view
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
