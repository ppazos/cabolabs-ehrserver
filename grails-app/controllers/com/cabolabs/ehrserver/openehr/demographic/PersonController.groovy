package com.cabolabs.ehrserver.openehr.demographic

import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.demographic.Person

class PersonController {

   def springSecurityService
   
   static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

   def index()
   {
      redirect(action: "list", params: params)
   }

   def list(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      
      def list, count
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = Person.findAllByDeleted(false, params)
         count = Person.countByDeleted(false)
      }
      else
      {
         // auth token used to login
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         
         list = Person.findAllByDeletedAndOrganizationUid(false, org.uid, params)
         count = Person.countByDeletedAndOrganizationUid(false, org.uid)
      }
      
      [personInstanceList: list, personInstanceTotal: count]
   }

   def create()
   {
      [personInstance: new Person(params)]
   }

   def save()
   {
      def personInstance = new Person(params)
      log.info "A"
      if (!personInstance.save(flush: true)) {
         log.info "B"
         render(view: "create", model: [personInstance: personInstance])
         return
      }
      
      log.info "C"

      flash.message = message(code: 'default.created.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
      redirect(action: "show", id: personInstance.id)
   }

   def show(Long id, String uid)
   {
     def personInstance
     if (id) personInstance = Person.get(id)
     else personInstance = Person.findByUid(uid)
   
     if (!personInstance) {
       flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), id])
       redirect(action: "list")
       return
     }

     [personInstance: personInstance]
   }

   def edit(Long id)
   {
      def personInstance = Person.get(id)
      if (!personInstance)
      {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), id])
         redirect(action: "list")
         return
      }

      [personInstance: personInstance]
   }

   def update(Long id, Long version)
   {
      def personInstance = Person.get(id)
      if (!personInstance) {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), id])
         redirect(action: "list")
         return
      }

      if (version != null) {
         if (personInstance.version > version) {
            personInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                    [message(code: 'person.label', default: 'Person')] as Object[],
                    "Another user has updated this Person while you were editing")
            render(view: "edit", model: [personInstance: personInstance])
            return
         }
      }

      personInstance.properties = params

      if (!personInstance.save(flush: true)) {
         render(view: "edit", model: [personInstance: personInstance])
         return
      }

      flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
      redirect(action: "show", id: personInstance.id)
   }

   def delete(Long id)
   {
      def personInstance = Person.get(id)
      if (!personInstance) {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), id])
         redirect(action: "list")
         return
      }

      try {
         //personInstance.delete(flush: true)
         personInstance.deleted = true
         personInstance.save(flush:true)
         flash.message = message(code: 'default.deleted.message', args: [message(code: 'person.label', default: 'Person'), id])
         redirect(action: "list")
      }
      catch (DataIntegrityViolationException e) {
         flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'person.label', default: 'Person'), id])
         redirect(action: "show", id: id)
      }
   }
}
