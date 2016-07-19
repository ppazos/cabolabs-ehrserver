package com.cabolabs.ehrserver.openehr.ehr

import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.demographic.Person
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvTextIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvCodedTextIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvDateTimeIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvQuantityIndex
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import grails.util.Holders

import grails.plugin.springsecurity.SpringSecurityUtils

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.composition.CompositionService

class EhrController {

   def springSecurityService
   def compositionService
   
   // Para acceder a las opciones de localizacion 
   def config = Holders.config.app
   
   
   def index() {
      redirect(action: "list", params: params)
   }
   
   /**
    * 
    * @param max
    * @param offset
    * @param uid filter of partial uid
    * @return
    */
   def list(int max, int offset, String sort, String order, String uid)
   {
      max = Math.min(max ?: 10, 100)
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      def list
      def c = Ehr.createCriteria()
      
      // TODO: filter by deleted
      
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         /*
          * if the criteria is empty, does the same as .list (works as expected)
          */
         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            if (uid)
            {
               like('uid', '%'+uid+'%')
            }
         }
      }
      else
      {
         // auth token used to login
         def auth = springSecurityService.authentication
         //def org = Organization.findByNumber(auth.organization)
         def un = auth.principal.username
         def us = User.findByUsername(un)
         def orgs = us.organizations

         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            //eq ('organizationUid', org.uid) // same org as used for login
            'in'('organizationUid', orgs.uid) // from all the orgs of the logged user
            if (uid)
            {
               like('uid', '%'+uid+'%')
            }
         }
         
         /*
          * Form the docs: http://docs.grails.org/2.5.3/ref/Domain%20Classes/createCriteria.html
          * 
          * Because that query includes pagination parameters (max and offset), this will return
          * a PagedResultList which has a getTotalCount() method to return the total number of
          * matching records for pagination. Two queries are still run, but they are run for
          * you and the results and total count are combined in the PagedResultList.
          *
          * So we can do subjects.totalCount
          */
      }

      [list: list, total: list.totalCount]
   }

   
   // GUI debug
   def showEhr(String patientUID)
   {
      if (!patientUID)
      {
         flash.message = message(code:'ehr.showEhr.patientUidIsRequired')
         redirect(url:request.getHeader('referer'))
         return
      }
      
      def c = Ehr.createCriteria()
      def ehr = c.get {
         subject {
            eq ('value', patientUID)
         }
      }
      
      if (!ehr)
      {
         flash.message = message(code:'ehr.showEhr.ehrDoesntExistsForPatientUid', args:[patientUid])
         //redirect(controller:'person', action:'list')
         redirect(url:request.getHeader('referer'))
         return
      }
      
      return [ehr: ehr] 
   }
   
   def show(String uid)
   {
      if (!uid)
      {
         flash.message = message(code:'ehr.show.uidIsRequired')
         redirect(url:request.getHeader('referer'))
         return
      }
      
      def ehr = Ehr.findByUid(uid)      
      if (!ehr)
      {
         flash.message = message(code:'ehr.show.ehrDoesntExistsForUid', args:[uid])
         redirect(url:request.getHeader('referer'))
         return
      }
      
      render(view:'showEhr', model:[ehr: ehr])
   }
   
   /**
    * Auxiliar de showEhr para mostrar las contributiosn y sus
    * compositions en una tabla y poder filtrarlas.
    * @return
    */
   def ehrContributions(long id, String fromDate, String toDate, String qarchetypeId)
   {
      def contribs
      def ehr = Ehr.get(id)
      
      // parse de dates
      Date qFromDate
      Date qToDate
      if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
      if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)

      contribs = Contribution.withCriteria {
         
         eq('ehr', ehr)
         
         // Busca por atributos de CompositionIndex
         // Puede no venir ningun criterio y se deberia devolver
         // todas las contribs del ehr, TODO: paginacion!
         versions {
            data {
               if (qarchetypeId)
                  eq('archetypeId', qarchetypeId)
               
               if (qFromDate)
                  ge('startTime', qFromDate)
                  
               if (qToDate)
                  le('startTime', qToDate)
            }
         }
      }
      
      render(template:'ehrContributions', model:[contributions:contribs]) 
   }
   
   /**
    * From person.list
    * 
    * @param patientUID uid de la Person con rol paciente
    * @return
    */
   def createEhr(String patientUID)
   {
      if (!patientUID)
      {
         flash.message = message(code:'ehr.createEhr.patientUidIsRequired')
         chain controller:'person', action: 'list' // with chain the action gets executed and maintains the flash
         return
      }
      
      def person = Person.findByUidAndRole(patientUID, 'pat')
      if (!person)
      {
         flash.message = message(code:'ehr.createEhr.patientDoesntExists', args:[patientUID])
         chain controller:'person', action: 'list'
         return
      }
      
      def c = Ehr.createCriteria()
      def ehr = c.get {
         subject {
            eq ('value', patientUID)
         }
      }
      
      if (ehr)
      {
         flash.message = message(code:'ehr.createEhr.patientAlreadyHasEhr', args:[ehr.uid])
         chain controller:'person', action: 'list'
         return
      }
      else
      {
         ehr = new Ehr(
            subject: new PatientProxy(
               value: patientUID
            ),
            organizationUid: person.organizationUid
         )
         
         if (!ehr.save(flush:true))
         {
            flash.message = message(code:'ehr.createEhr.patientAlreadyHasEhr', args:[ehr.uid])
            render (view : '/person/list', model: [ehr: ehr])
            return
         }
      }
      
      redirect(controller:'person', action:'list')
      
   } // createEhr
   
   
   /**
    * 
    * @param uid composition identifier
    * @return composition as HTML
    */
   def showCompositionUI(String uid)
   {
      return [compositionHtml: compositionService.compositionAsHtml(uid)]
   }
   
   
   /**
    * @param uid composition identifier
    * @return composition as XML
    */
   def showComposition(String uid)
   {
      render(text: compositionService.compositionAsXml(uid), contentType: "text/xml", encoding:"UTF-8")
   }
}
