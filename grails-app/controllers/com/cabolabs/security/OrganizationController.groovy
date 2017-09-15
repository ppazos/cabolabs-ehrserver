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
import com.cabolabs.ehrserver.account.ApiKey

@Transactional(readOnly = true)
class OrganizationController {

   def springSecurityService
   def statelessTokenProvider
   def configurationService
   
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
      [organizationInstance: params.organizationInstance]
   }

   def create()
   {
      respond new Organization(params)
   }

   @Transactional
   def save(Organization organizationInstance)
   {
      if (organizationInstance == null)
      {
         notFound()
         return
      }
      log.info "antes de has errors"
      if (organizationInstance.hasErrors())
      {
         log.info "has errors"
         render view:'create', model:[organizationInstance:organizationInstance]
         return
      }

      log.info "luego de has errors"
      organizationInstance.save flush:true
      
      def user = springSecurityService.loadCurrentUser()
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         // assign org to admin only if admin choose to
         if (params.assign)
         {
            // Assign org to logged user
            UserRole.create( user, (Role.findByAuthority('ROLE_ADMIN')), organizationInstance, true )
            user.save(flush:true)
         }
      }
      else
      {
         // Assign org to logged user
         // uses the higher role on the current org to assign on the new org
         UserRole.create( user, user.getHigherAuthority(session.organization), organizationInstance, true )
         user.save(flush:true)
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

      if (params.doit)
      {
         if (!systemId)
         {
            flash.message = message(code: 'default.error.systemIsRequired')
            return
         }
         
         def org = Organization.findByUid(uid)
         if (!org)
         {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'organization.label', default: 'Organization'), uid])
            redirect action: 'index'
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
         println "null"
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
