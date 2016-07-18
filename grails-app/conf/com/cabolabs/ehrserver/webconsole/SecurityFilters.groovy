package com.cabolabs.ehrserver.webconsole

import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.security.User
import com.cabolabs.ehrserver.openehr.demographic.Person
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.ehr.Ehr

class SecurityFilters {
   
   def springSecurityService

   def filters = {
      all(controller:'*', action:'*') {
         before = {

         }
         after = { Map model ->

         }
         afterView = { Exception e ->

         }
      }
      
      /**
       * All the lists already filter by organization, 
       * this checks authorization for show/edit/save.
       * 
       **/
      
      person_save(controller:'person', action:'save') {
         before = {
            // params.organizationUid should be one of the orgs associated with the current user
            // 
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            //def org = Organization.findByNumber(auth.organization) // organization used to login
            
            if (!params.organizationUid || !orgs.uid.contains(params.organizationUid))
            {
               flash.message = "You don't have access to the specified organization"
               chain controller: 'person', action: 'create' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            return true
         }
      }
      
      person_update(controller:'person', action:'update') {
         before = {
            
            println "a person update filter params "+ params
            
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            
            return true
         }
      }
      
      person_delete(controller:'person', action:'delete') {
         before = {
            
            println "person delete filter params "+ params
            
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            
            return true
         }
      }
      
      person_show(controller:'person', action:'show') {
         before = {
            
            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            return true
         }
      }
      
      person_edit(controller:'person', action:'edit') {
         before = {
            
            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            return true
         }
      }
      
      organization_show(controller:'organization', action:'show') {
         before = {
            
            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Organization UID is required"
               chain controller: 'organization', action: 'index'
               return false
            }
            
            def o = Organization.findByUid(params.uid)
            
            if (!o || !orgs.uid.contains(o.uid))
            {
               flash.message = "You don't have access to that organization!"
               chain controller: 'organization', action: 'index' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.organizationInstance = o
            return true
         }
      }
      
      organization_edit(controller:'organization', action:'edit') {
         before = {
            
            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Organization UID is required"
               chain controller: 'organization', action: 'index'
               return false
            }
            
            def o = Organization.findByUid(params.uid)
            
            if (!o || !orgs.uid.contains(o.uid))
            {
               flash.message = "You don't have access to that organization!"
               chain controller: 'organization', action: 'index' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.organizationInstance = o
            return true
         }
      }
      
      organization_update(controller:'organization', action:'update') {
         before = {
            
            println "org update filter params "+ params
            
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            //def org = Organization.findByNumber(auth.organization) // organization used to login
            
            if (!params.uid || !orgs.uid.contains(params.uid))
            {
               flash.message = "You don't have access to the specified organization"
               chain controller: 'organization', action: 'index'
               return false
            }
            
            return true
         }
      }
   }
}
