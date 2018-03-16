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

//http://grails-plugins.github.io/grails-spring-security-core/guide/single.html#springSecurityUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import com.cabolabs.ehrserver.account.*
import com.cabolabs.ehrserver.openehr.ehr.Ehr

@Transactional(readOnly = true)
class OrganizationController {

   def springSecurityService
   def statelessTokenProvider
   def configurationService
   def organizationService

   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def config = Holders.config.app

   def index(int offset, String sort, String order, String name, String number)
   {
      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'

      def list
      def c = Organization.createCriteria()

      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            if (name)
            {
               like('name', '%'+name+'%')
            }
            if (number)
            {
               like('number', '%'+number+'%')
            }
         }
      }
      else
      {
         def user = springSecurityService.loadCurrentUser()
         def orgs = user.organizations

         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            'in'('uid', orgs.uid)
            if (name)
            {
               like('name', '%'+name+'%')
            }
            if (number)
            {
               like('number', '%'+number+'%')
            }
         }
      }

      [organizationInstanceList: list, total: list.totalCount]
   }

   // organizationInstance comes from the security filter on params
   def show()
   {
      def plan_max_tokens
      def user = springSecurityService.loadCurrentUser()
      def account = user.account
      def plan_assoc = Plan.associatedNow(account) // can be null in dev env, on this case, no constraints apply to org creation
      if (plan_assoc)
      {
         plan_max_tokens = plan_assoc.plan.max_api_tokens_per_organization
      }

      [organizationInstance: params.organizationInstance, ehr_count: Ehr.countByOrganizationUid(params.organizationInstance.uid), plan_max_tokens: plan_max_tokens]
   }

   def create()
   {
      def accounts = []
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         accounts = Account.list()
      }
      else
      {
         // This limit is checked for non-admins because with admins we don't know which account will be selected, so the max orgs is not available
         // Checks organization creation plan limits
         def user = springSecurityService.loadCurrentUser()
         def account = user.account
         def plan_assoc = Plan.associatedNow(account) // can be null in dev env, on this case, no constraints apply to org creation
         if (plan_assoc)
         {
            def plan_max_orgs = plan_assoc.plan.max_organizations
            def account_org_count = Organization.countByAccount(account) // faster than user.account.organizations.size() that requires more queries

            if (account_org_count == plan_max_orgs)
            {
               flash.message = message(code:"organization.create.cantCreateNewOrg.maxOrgsLimit")
               redirect action:'index'
               return
            }
         }
      }

      [organizationInstance: new Organization(params), accounts: accounts]
   }


   /**
    * name: name of the organization
    * assign: if true, assign the logged in admin to the created org
    * account_id: account to be associated with the org
    */
   @Transactional
   def save(String name, Boolean assign, Long account_id)
   {
      // https://github.com/ppazos/cabolabs-ehrserver/issues/847
      def user = springSecurityService.loadCurrentUser()
      def account
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         account = Account.get(account_id)
      }
      else
      {
         account = user.account
      }

      if (!account)
      {
         def accounts = []
         if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
         {
            accounts = Account.list()
         }

         flash.message = message(code: 'organization.save.noAccount')
         render view:'create', model:[organizationInstance: organizationInstance, accounts: accounts]
         return
      }

      // Checks organization creation plan limits
      def plan_assoc = Plan.associatedNow(account) // can be null in dev env, on this case, no constraints apply to org creation
      if (plan_assoc)
      {
         def plan_max_orgs = plan_assoc.plan.max_organizations
         def account_org_count = Organization.countByAccount(account) // faster than user.account.organizations.size() that requires more queries

         if (account_org_count == plan_max_orgs)
         {
            flash.message = message(code:"organization.create.cantCreateNewOrg.maxOrgsLimit")
            redirect action:'index'
            return
         }
      }

      def organizationInstance = organizationService.create(account, name)

      def accman_associated = false // prevents associating the accman twice if the current user is accman
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         // assign org to admin only if admin choose to
         if (assign)
         {
            // Assign org to logged user
            UserRole.create( user, (Role.findByAuthority('ROLE_ADMIN')), organizationInstance, true )
         }
      }
      else
      {
         // Assign org to logged user
         // uses the higher role on the current org to assign on the new org
         def higher_role_in_current_org = user.getHigherAuthority(session.organization)

         UserRole.create( user, higher_role_in_current_org, organizationInstance, true )

         if (higher_role_in_current_org.authority == Role.AM) accman_associated = true
      }

      // create accman userrole for the account contact
      if (!accman_associated)
      {
         UserRole.create( account.contact, (Role.findByAuthority(Role.AM)), organizationInstance, true )
      }


      flash.message = message(code: 'default.created.message', args: [message(code: 'organization.label', default: 'Organization'), organizationInstance.id])
      redirect action:'show', params:[uid:organizationInstance.uid]
   }


   def edit()
   {
      [organizationInstance: params.organizationInstance]
   }


   @Transactional
   def update(String uid, Long version)
   {
      def organizationInstance = Organization.findByUid(uid)
      organizationInstance.properties = params
      organizationInstance.validate()

      if (organizationInstance.hasErrors())
      {
         respond organizationInstance.errors, view:'edit'
         return
      }

      organizationInstance.save flush:true

      redirect action:'show', params:[uid:uid]
   }

   @Transactional
   def delete(Organization organizationInstance)
   {
      if (organizationInstance == null)
      {
         notFound()
         return
      }

      organizationInstance.delete flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'Organization.label', default: 'Organization'), organizationInstance.id])
            redirect action:"index", method:"GET"
         }
         '*'{ render status: NO_CONTENT }
      }
   }

   @Transactional
   def generateApiKey(String uid, String systemId)
   {
      if (!uid)
      {
         flash.message = message(code: 'default.error.uidParamIsRequired')
         redirect action: 'index'
         return
      }

      def org = Organization.findByUid(uid)
      if (!org)
      {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'organization.label', default: 'Organization'), uid])
         redirect action: 'index'
         return
      }

      // Checks api key creation plan limits
      def user = springSecurityService.loadCurrentUser()
      def account = user.account
      def plan_assoc = Plan.associatedNow(account) // can be null in dev env, on this case, no constraints apply to org creation
      if (plan_assoc)
      {
         def plan_max_tokens = plan_assoc.plan.max_api_tokens_per_organization
         def apikey_count = ApiKey.countByOrganization(org)

         if (apikey_count == plan_max_tokens)
         {
            flash.message = message(code:"organization.create.cantCreateNewApiKey.maxTokensLimit")
            redirect action:'show', params:[uid: uid]
            return
         }
      }

      if (params.doit)
      {
         if (!systemId)
         {
            flash.message = message(code: 'default.error.systemIsRequired')
            return
         }

         def virtualUser = new User(username: 'apikey'+String.random(50),
                                    password: String.uuid(),
                                    email: String.random(50) + '@apikey.com',
                                    isVirtual: true,
                                    enabled: true,
                                    organizations: [org])

         virtualUser.save(failOnError: true)

         UserRole.create(virtualUser, Role.findByAuthority(Role.US), org, true)

         def key = new ApiKey(organization: org,
                              user: virtualUser,
                              systemId: systemId,
                              token: statelessTokenProvider.generateToken(virtualUser.username, null, [organization: org.number, org_uid: org.uid]))

         key.save(failOnError: true)

         redirect action:'show', params:[uid:org.uid]
      }
   }

   @Transactional
   def deleteApiKey(ApiKey key)
   {
      if (!key)
      {
         println "empty key to delete..."
      }

      // Need to delete the key first because the key has a not null constraint to the user
      def keyUser = key.user

      UserRole.remove(keyUser, Role.findByAuthority(Role.US), key.organization, true)

      key.delete()
      keyUser.delete()

      redirect action: 'show', params: [uid: key.organization.uid]
   }

   protected void notFound()
   {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'organization.label', default: 'Organization'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
