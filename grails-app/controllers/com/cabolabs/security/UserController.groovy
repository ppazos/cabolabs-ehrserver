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
import com.cabolabs.ehrserver.api.ApiResponsesService

@Transactional(readOnly = false)
class UserController {

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
   
   def apiResponsesService
   def simpleCaptchaService
   def notificationService
   def springSecurityService
   //def userService
   def config = Holders.config.app

   def set_org_for_tests(org)
   {
      println "set org for tests"
      session.organization = org
   }
   
   def login()
   {
      // http://stackoverflow.com/questions/32621369/customize-login-in-grails-spring-security-plugin
   }
   
   def index(int max, int offset, String sort, String order, String username, String organizationUid)
   {
      println "index"
      
      max = Math.min(max ?: config.list_max, 100)
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def list, count
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
         
         count = list.totalCount
      }
      else
      {
         // current user lists only users from the current org
         
         def loggedInUser = springSecurityService.currentUser
         
         def org = session.organization // with this in the criteria makes the unit test fail.
         def urs = UserRole.withCriteria {
            eq('organization', org)
            if (username)
            {
               user {
                  like('username', '%'+username+'%')
               }
            }
         }
         
         // cant paginate because the search is on user role.
         list = urs.user.unique()
         count = list.size()
      }
      
      render view: 'index', model: [userInstanceList: list, userInstanceCount: count]
   }
   
   
   // endpoint
   @SecuredStateless
   def profile(String username)
   {
      // username and organization number used on the API login
      def _username = request.securityStatelessMap.username
      def org_uid = request.securityStatelessMap.extradata.org_uid
      def org = Organization.findByUid(org_uid)
      def _user = User.findByUsername(_username)
      
      // user I want to access
      def u = User.findByUsername(username)
      if (!username || !u)
      {
         withFormat {
            xml {
               render(status: 404, contentType: "text/xml", text: apiResponsesService.feedback_xml("User doesn\'t exists", 'AR', 53445), encoding:"UTF-8")
            }
            json {
               render(status: 404, contentType: "application/json", text: apiResponsesService.feedback_json("User doesn\'t exists", 'AR', 53445), encoding:"UTF-8")
            }
         }
         return
      }
      
      def allowed
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         allowed = true
      }
      else
      {
         // if user is not admin, can only see the profile of users of the current org and lower role
         allowed = (
            ( u.organizations.count{ it.uid == org_uid } == 1 ) && // organization of the logged user match one of the organizations of the requested user
            (
               u.username == _username || // user want to access self profile
               ( // org managers can see users with lees power than them
                  (_user.authoritiesContains(Role.OM, org) || _user.authoritiesContains(Role.AM, org)) && 
                   _user.getHigherAuthority(org).higherThan( u.getHigherAuthority(org) )
               )
            )
         )
      }
      
      if (!allowed)
      {
         withFormat {
            xml {
               render(status: 401, contentType: "text/xml", text: apiResponsesService.feedback_xml("Unauthorized to access user info", 'AR', 53445), encoding:"UTF-8")
            }
            json {
               render(status: 401, contentType: "application/json", text: apiResponsesService.feedback_json("Unauthorized to access user info", 'AR', 53445), encoding:"UTF-8")
            }
         }
         return
      }

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
      
      def userRoles = UserRole.findAllByUser(userInstance)
      
      // Admins can access all users
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         respond userInstance, [model: [roles: rolesICanAssign(), organizations: organizationsICanAssign(), userRoles: userRoles]]
         return
      }
      else
      {
         def loggedInUser = springSecurityService.currentUser

         // this condition checks 1. I have a role on the same org as the userInstance, 2. I have a higher role on that org
         
         // cant see the details of a user with higher authority
         // be careful: for the same roles, higherThan returns true
         
         // I can only manage users in the current org, if the user doesnt have a role in the current org
         // this code will return an error since it will compare my higher role with null. That is OK since
         // it's an invalid flow, maybe an attack. For now we don't show a friendly message for attackers,
         // just the exception.
         if (!loggedInUser.getHigherAuthority(session.organization).higherThan( userInstance.getHigherAuthority(session.organization) ))
         {
            flash.message = message(code:"notEnoughPermissionsTo.show.user")
            redirect action:'index'
            return
         }

         respond userInstance, [model: [roles: rolesICanAssign(), organizations: organizationsICanAssign(), userRoles: userRoles]]
         return
      }
   }
   
   /**
    * Roles are per org.
    */
   private rolesICanAssign()
   {
      def rolesPerOrgICanAssing = [:] // org -> roles
      
      // Admins can assign all roles to all orgs
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         def organizations = Organization.list()
         def roles = Role.list()
         organizations.each { org ->
            rolesPerOrgICanAssing[org] = roles
         }
      }
      else
      {
         // roles the current user can assign
         def roles
         def loggedInUser = springSecurityService.currentUser
         def userRoles = UserRole.findAllByUser(loggedInUser) // orgs the user can access and his role on each
         
         userRoles.each { userRole ->
            roles = Role.list()
            def hrole = loggedInUser.getHigherAuthority(userRole.organization)
            if (hrole.authority != Role.AD)
            {
               roles.removeAll { it.authority == Role.AD }
               if (hrole.authority != Role.AM)
               {
                  roles.removeAll { it.authority == Role.AM }
                  if (hrole.authority == Role.US) // user cant assign any roles on that org
                  {
                     roles = []
                  }
               }
            }
            
            rolesPerOrgICanAssing[userRole.organization] = roles
         }
      }
      
      return rolesPerOrgICanAssing
   }
   
   private organizationsICanAssign()
   {
      def organizations
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         organizations = Organization.list()
      }
      else
      {
         // non admins can only manage users for the current org
         organizations = [session.organization] //springSecurityService.currentUser.organizations
      }
      
      return organizations
   }
   
   def create()
   {
      // roles the current user can assign
      def roles = rolesICanAssign()
      
      // organizations the current user can assign
      def organizations = organizationsICanAssign()
      
      respond new User(params), [model: [roles: roles, organizations: organizations]]
   }

   @Transactional
   def save(User userInstance)
   {
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      // User should have one org assigned, if not we lose tack of the user and can't be managed.
      def _organizationsICanAssign = organizationsICanAssign()
      def inRoles, valid = false
      
      _organizationsICanAssign.each { org ->

         // org uid -> authorities
         inRoles = params.list(org.uid)
         if (inRoles)
         {
            valid = true
            return true
         }
      }
      if (!valid)
      {
         flash.message = message(code:"user.update.oneRoleShouldBeSelected")
         render model: [userInstance: userInstance, roles: rolesICanAssign(), organizations: organizationsICanAssign()], view:'create'
         return
      }
      
      if (!userInstance.password)
      {
         userInstance.enabled = false
         userInstance.setPasswordToken()
      }
           
      // need to save before creating UserRole, so UserRole can be saved
      if (!userInstance.save(flush:true))
      {
         render model: [userInstance: userInstance, roles: rolesICanAssign(), organizations: organizationsICanAssign()], view:'create'
         return
      }
      
      def loggedInUser = springSecurityService.currentUser
      def orguids, org, roles
      def _rolesICanAssign = rolesICanAssign()

      _rolesICanAssign.each { orgRoles ->
         org = orgRoles.key
         roles = orgRoles.value
         
         // org uid -> authorities
         inRoles = params.list(org.uid)
         
         inRoles.each { assignRole ->
            if (roles.authority.contains(assignRole)) // I can assign this role on this org
            {
               UserRole.create(userInstance, Role.findByAuthority(assignRole), org, true)
            }
         }
      }
      
      // email notification
      // TODO: schedule emails
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
      if (userInstance == null)
      {
         notFound()
         return
      }
      
      // Admins can access all users
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         respond userInstance, [model: [roles: rolesICanAssign(), 
                                        organizations: organizationsICanAssign(), 
                                        userRoles: UserRole.findAllByUser(userInstance)]]
         return
      }
      else
      {
         def loggedInUser = springSecurityService.currentUser
         
         // this condition checks 1. I have a role on the same org as the userInstance, 2. I have a higher role on that org
         
         // cant see the details of a user with higher authority
         // be careful: for the same roles, higherThan returns true
         if (!loggedInUser.getHigherAuthority(session.organization).higherThan( userInstance.getHigherAuthority(session.organization) ))
         {
            flash.message = message(code:"notEnoughPermissionsTo.show.user")
            redirect action:'index'
            return
         }

         respond userInstance, [model: [roles: rolesICanAssign(), 
                                        organizations: organizationsICanAssign(), 
                                        userRoles: UserRole.findAllByUserAndOrganization(userInstance, session.organization)]]
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
      
      // User should have one org assigned, if not we lose tack of the user and can't be managed.
      def userRoles
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")) // admins manage user roles on any org
      {
         userRoles = UserRole.findAllByUser(userInstance)
      }
      else // non admins only manage roles for current org
      {
         userRoles = UserRole.findAllByUserAndOrganization(userInstance, session.organization)
      }
         
      def _organizationsICanAssign = organizationsICanAssign()
      def inRoles, valid = false
      
      _organizationsICanAssign.each { org ->

         // org uid -> authorities
         inRoles = params.list(org.uid)
         if (inRoles)
         {
            valid = true
            return true
         }
      }
      if (!valid)
      {
         flash.message = message(code:"user.update.oneRoleShouldBeSelected")
         render model: [userInstance: userInstance, 
                        roles: rolesICanAssign(), 
                        organizations: organizationsICanAssign(), 
                        userRoles: userRoles], view:'edit'
         return
      }

      def loggedInUser = springSecurityService.currentUser
      def orguids, org, roles, highestRole
      def _rolesICanAssign = rolesICanAssign()
      
      // Removes current roles, for the roles and orgs the current user can assign, if those roles were removed.
      userRoles.each { userRole ->

         // check if a role that the user already has is coming on params => do not delete only delete
         // the onse that the user has and are not coming on the params (and the current user can manage)
         inRoles = params.list(userRole.organization.uid) // this is also checking the org in params exists!
      
         // if user is self, can't remove his highest role on this org
         if (loggedInUser.id == userInstance.id)
         {
            // TODO: this leaves a consistent state but the user doesn't know what is happening, we need to show notifications.
            // flash.message = message(code: "user.update.cantRemoveHighestRole", args:[highestRole.authority])
            highestRole = userInstance.getHigherAuthority(userRole.organization)
            if (!inRoles.contains(highestRole.authority))
            {
               return // avoids executing the rest of the each
            }
         }
         
         // this condition checks 1. that I can assign roles on that org,
         // 2. I can assign that specific role on that org
         // 3. the role was removed from the user in the UI, if it wasn't removed, keep it
         if (_rolesICanAssign[userRole.organization].contains(userRole.role) && !inRoles.contains(userRole.role.authority))
         {
            UserRole.remove( userInstance, userRole.role, userRole.organization, true )
         }
      }
      
      _rolesICanAssign.each { orgRoles ->
         org = orgRoles.key
         roles = orgRoles.value
         
         // org uid -> authorities
         inRoles = params.list(org.uid)
         
         inRoles.each { assignRole ->
            // I can assign this role on this org
            if (roles.authority.contains(assignRole) && !UserRole.exists(userInstance.id, Role.findByAuthority(assignRole).id, org.id))
            {
               UserRole.create(userInstance, Role.findByAuthority(assignRole), org, true)
            }
         }
      }
      
      if (!userInstance.save(flush:true))
      {
         render model: [userInstance: userInstance, 
                        roles: rolesICanAssign(), 
                        organizations: organizationsICanAssign(), 
                        userRoles: userRoles], view:'edit'
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
               //u.addToOrganizations(o).save(failOnError: true, flush:true) // FIXME: this is saving the user and we save the user below
               
               u.save(failOnError: true, flush:true)
               
               // TODO: UserRole ORG_* needs a reference to the org, since the user
               //      can be ORG_ADMIN in one org and ORG_STAFF in another org.
               // the user is creating the organization, it should be manager also, because is the first, is account manager
               UserRole.create( u, (Role.findByAuthority(Role.AM)), o, true )
               
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
            println u.errors
            println o.errors
            flash.message = 'user.registerError.feedback'
            render view: "register", model: [userInstance: u, organizationInstance: o, captchaValid: captchaValid]
         }
         else
         {
            //notificationService.sendUserRegisteredEmail(u.email, [o.name, o.number])
            // token to create the URL for the email is in the userInstance
            notificationService.sendUserCreatedEmail( u.email, [u], true )
            redirect(action:'registerOk')
         }
      }
   }
   
   // just renders, needed because it shows the view in the right locale, without this it doesnt.
   def registerOk()
   {
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
