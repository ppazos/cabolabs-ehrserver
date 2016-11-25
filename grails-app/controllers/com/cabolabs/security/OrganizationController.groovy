package com.cabolabs.security


import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

//http://grails-plugins.github.io/grails-spring-security-core/guide/single.html#springSecurityUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders

@Transactional(readOnly = true)
class OrganizationController {

   def springSecurityService
   
   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def config = Holders.config.app
   
   def index(int max, int offset, String sort, String order, String name, String number)
   {
      max = Math.min(max ?: config.list_max, 100)
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
      
      // Assign org to logged user
      def user = springSecurityService.loadCurrentUser()
      user.addToOrganizations(organizationInstance)
      user.save(flush:true)

      
      flash.message = message(code: 'default.created.message', args: [message(code: 'organization.label', default: 'Organization'), organizationInstance.id])
      redirect action:'show', id:organizationInstance.id
   }

   def edit()
   {
      [organizationInstance: params.organizationInstance]
   }

   @Transactional
   def update(String uid, Long version)
   {
      println "update "+ params +" uid: "+ uid
      
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
