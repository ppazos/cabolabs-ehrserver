package com.cabolabs.security


import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

//http://grails-plugins.github.io/grails-spring-security-core/guide/single.html#springSecurityUtils
import grails.plugin.springsecurity.SpringSecurityUtils


@Transactional(readOnly = true)
class OrganizationController {

   def springSecurityService
   
   static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

   def index(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      
      def list, count
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = Organization.list(params)
         count = Organization.count()
      }
      else
      {
         def user = springSecurityService.loadCurrentUser()
         
         //println "organizations: "+ user.organizations.toString()
         
         // no pagination
         list = user.organizations
         count = list.size()
      }
      
      render view:'index', model:[organizationInstanceList:list, total:count]
   }

   def show(Organization organizationInstance)
   {
      respond organizationInstance
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
         //respond organizationInstance, view:'create'
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
      
      /*
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'organization.label', default: 'Organization'), organizationInstance.id])
            redirect organizationInstance
         }
         '*' { respond organizationInstance, [status: CREATED] }
      }
      */
   }

   def edit(Organization organizationInstance)
   {
      respond organizationInstance
   }

   @Transactional
   def update(Organization organizationInstance)
   {
      if (organizationInstance == null)
      {
         notFound()
         return
      }

      if (organizationInstance.hasErrors())
      {
         respond organizationInstance.errors, view:'edit'
         return
      }

      organizationInstance.save flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'Organization.label', default: 'Organization'), organizationInstance.id])
            redirect organizationInstance
         }
         '*'{ respond organizationInstance, [status: OK] }
      }
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
