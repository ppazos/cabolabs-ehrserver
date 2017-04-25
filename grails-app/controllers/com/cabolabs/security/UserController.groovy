/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.cabolabs.ehrserver.account.*

@Transactional(readOnly = false)
class UserController {

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
   
   def simpleCaptchaService
   def notificationService
   def springSecurityService
   def userService
   def config = Holders.config.app

   def login()
   {
      // http://stackoverflow.com/questions/32621369/customize-login-in-grails-spring-security-plugin
   }
   
   def index(int max, int offset, String sort, String order, String username, String organizationUid)
   {
      max = Math.min(max ?: config.list_max, 100)
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def list
      def c = User.createCriteria()
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            eq('isVirtual', false)
            if (username)
            {
               like('username', '%'+username+'%')
            }
            if (organizationUid)
            {
               organizations {
                  eq ('uid', organizationUid) // TODO: check if org exists
               }
            }
         }
      }
      else
      {
         def loggedInUser = springSecurityService.currentUser
         
         def userHasAccessToOrg
         if (organizationUid)
         {
            userHasAccessToOrg = loggedInUser.organizations.uid.contains( organizationUid )
            if (!userHasAccessToOrg)
            {
               // Just show the message and return the default list
               flash.mesage = "The current user doesn't have access to the organization with UID ${organizationUid}, or the organization doesn't exists"
            }
         }
         
         // auth token used to login
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            eq('isVirtual', false)
            organizations {
               if (organizationUid && userHasAccessToOrg)
               {
                  eq ('uid', organizationUid)
               }
               else
               {
                  eq ('uid', org.uid)
               }
            }
            if (username)
            {
               like('username', '%'+username+'%')
            }
         }
      }
      
      render view: 'index', model: [userInstanceList: list, userInstanceCount: list.totalCount]
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
               render(status: 404, contentType: "text/xml", text: "<result>User doesn't exists</result>", encoding:"UTF-8")
            }
            json {
               render(status: 404, contentType: "application/json", text: '{"result": "User doesn\'t exists"}', encoding:"UTF-8")
            }
         }
         return
      }
      
      
      def allowed = (
         _user.authoritiesContains(Role.AD) || // admins access everything
         (
            ( u.organizations.count{ it.number == _orgnum } == 1 ) && // organization of the logged user match one of the organizations of the requested user
            (
               u.username == _username || // user want to access self profile
               ( // org managers can see users with lees power than them
                  (_user.authoritiesContains(Role.OM) || _user.authoritiesContains(Role.AM)) && _user.higherAuthority.higherThan( u.higherAuthority )
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
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      def loggedInUser = springSecurityService.currentUser
      
      // cant see the details of a user with higher authority
      // be careful: for the same roles, higherThan returns true
      if (!loggedInUser.higherAuthority.higherThan( userInstance.higherAuthority ))
      {
         flash.message = message(code:"notEnoughPermissionsTo.show.user")
         redirect action:'index'
         return
      }
      
      // Admins can access all users
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         respond userInstance
         return
      }
      else
      {
         // If not admin, the user should be on an organization of the loggerInUser
         if (loggedInUser.organizations.disjoint( userInstance.organizations ))
         {
            flash.message = message(code:"notEnoughPermissionsTo.show.user")
            redirect action:'index'
            return
         }

         respond userInstance
         return
      }
   }
   
   
   def create()
   {
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
      
      def loggedInUser = springSecurityService.currentUser

      def orgs = params.list("organizationUid")
      def roles = params.list('role')
      
      // Organizations cant be empty
      if (orgs.size() == 0)
      {
         flash.message = message(code:"user.update.oneOrganizationShouldBeSelected")
         render model: [userInstance: userInstance], view:'create'
         return
      }
      
      // All the organizations assigned to the new user should be accessible by the current user
      def notAllowedOrg = orgs.find { !loggedInUser.organizations.uid.contains(it) }
      if (notAllowedOrg)
      {
         flash.message = message(code:"cantAssingOrganization.save.user", args:[notAllowedOrg])
         render model: [userInstance: userInstance], view:'create'
         return
      }
      
      // All the roles assigned to the new user should be lower o equal to the highest role of the current user
      def highestRole = loggedInUser.higherAuthority
      def notAllowedRole = roles.find { !highestRole.higherThan( new Role(it) ) }
      if (notAllowedRole)
      {
         flash.message = message(code:"cantAssingRole.save.user", args:[notAllowedRole])
         render model: [userInstance: userInstance], view:'create'
         return
      }
      
      // roles cant be empty
      if (roles.size() == 0)
      {
         flash.message = message(code:"user.update.oneRoleShouldBeSelected")
         render model: [userInstance: userInstance], view:'create'
         return
      }
      
      
      // Associate orgs
      def newOrgs = Organization.findAllByUidInList(orgs)
      newOrgs.each { newOrg ->
         userInstance.addToOrganizations(newOrg)
      }
      
      try
      {
         userService.saveAndNotify(userInstance, params)
      }
      catch (Exception e)
      {
         render model: [userInstance: userInstance], view:'create'
         return
      }
      
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
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      // TODO: refactor - same code as show
      def loggedInUser = springSecurityService.currentUser
      
      // cant see the details of a user with higher authority
      // be careful: for the same roles, higherThan returns true
      if (!loggedInUser.higherAuthority.higherThan( userInstance.higherAuthority ))
      {
         flash.message = message(code:"notEnoughPermissionsTo.show.user")
         redirect action:'index'
         return
      }
      
      // Admins can access all users
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         respond userInstance
         return
      }
      else
      {
         // If not admin, the user should be on an organization of the loggerInUser
         if (loggedInUser.organizations.disjoint( userInstance.organizations ))
         {
            flash.message = message(code:"notEnoughPermissionsTo.show.user")
            redirect action:'index'
            return
         }

         respond userInstance
         return
      }
   }

   @Transactional
   def update(User userInstance)
   {
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      def loggedInUser = springSecurityService.currentUser
      
      // cant see the details of a user with higher authority
      // be careful: for the same roles, higherThan returns true
      if (!loggedInUser.higherAuthority.higherThan( userInstance.higherAuthority ))
      {
         flash.message = message(code:"notEnoughPermissionsTo.show.user")
         redirect action:'index'
         return
      }
      
      // Admins can access all users
      if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         // If not admin, the user should be on an organization of the loggerInUser
         if (loggedInUser.organizations.disjoint( userInstance.organizations ))
         {
            flash.message = message(code:"notEnoughPermissionsTo.show.user")
            redirect action:'index'
            return
         }
      }
      
      // update allowed
      
      def orgs = params.list("organizationUid")
      def roles = params.list('role')
            
      // Organizations cant be empty
      if (orgs.size() == 0)
      {
         flash.message = message(code:"user.update.oneOrganizationShouldBeSelected")
         render model: [userInstance: userInstance], view:'edit'
         return
      }
      
      // All the organizations assigned to the new user should be accessible by the current user
      def notAllowedOrg = orgs.find { !loggedInUser.organizations.uid.contains(it) }
      if (notAllowedOrg)
      {
         flash.message = message(code:"cantAssingOrganization.save.user", args:[notAllowedOrg])
         render model: [userInstance: userInstance], view:'edit'
         return
      }
      
      // Update roles.
      // All the roles assigned to the new user should be lower o equal to the highest role of the current user
      def highestRole = loggedInUser.higherAuthority
      def notAllowedRole = roles.find { !highestRole.higherThan( new Role(it) ) }
      if (notAllowedRole)
      {
         flash.message = message(code:"cantAssingRole.save.user", args:[notAllowedRole])
         render model: [userInstance: userInstance], view:'edit'
         return
      }
      
      // roles cant be empty
      if (roles.size() == 0)
      {
         flash.message = message(code:"user.update.oneRoleShouldBeSelected")
         render model: [userInstance: userInstance], view:'edit'
         return
      }
      
      // if the user is editing his data and can't remove the highest role
      // e.g. admins cant remove their own admin role
      if (loggedInUser.id == userInstance.id)
      {
         if (!roles.contains(highestRole.authority))
         {
            flash.message = message(code: "user.update.cantRemoveHighestRole", args:[highestRole.authority])
            render model: [userInstance: userInstance], view:'edit'
            return
         }
      }
      
      
      // Update orgs.
      userService.updateOrganizations(userInstance, orgs)
      
      
      // Role updating
      
      // Delete all current roles
      def currentRoles = UserRole.findAllByUser(userInstance)
      currentRoles*.delete(flush: true)
      
      // Add selected roles
      
      roles.each { authority ->
         
         UserRole.create( userInstance, (Role.findByAuthority(authority)), true )
      }

      // / Role updating
      
      
      if (!userInstance.save(flush:true))
      {
         render model: [userInstance: userInstance], view:'edit'
         return
      }
      
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'User.label', default: 'User'), userInstance.id])
            redirect userInstance
         }
         '*'{ respond userInstance, [status: OK] }
      }
   }

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
               // the user is creating the organization, it should be manager also, because is the first, is account manager
               UserRole.create( u, (Role.findByAuthority(Role.AM)), true )
               
               // associate the basic plan to the new org
               def p1 = Plan.get(1)
               p1.associate( o )
            }
            catch (ValidationException e)
            {
               println u.errors
               println o?.errors
               
               status.setRollbackOnly()
            }
          
            // FIXME: avoid saving stuff if the captcha is incorrect
            // WHY not checking this before the instances are created?
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
   
   
   // same as forgotPassword but it can be triggered by an admin / org manager
   // from the user/show to let users reset their password even if they forgot
   // the email used to register.
   def resetPasswordRequest(String email)
   {
      def user = User.findByEmail(email)
      
      if (!user)
      {
         flash.message = "Can't find a user for that email"
         redirect(action:'show', id:params.id)
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
         redirect(action:'show', id:params.id)
         return
      }
      
      
      flash.message = "Password reset email was sent, please check the instructions on that email"
      redirect(action:'show', id:params.id)
      return
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
