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
         def org = Organization.findByNumber(auth.organization)

         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            eq ('organizationUid', org.uid)
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
      // TODO: patientUID existe?
      
      def c = Ehr.createCriteria()
      def ehr = c.get {
         subject {
            eq ('value', patientUID)
         }
      }
      
      if (!ehr)
      {
         // TODO: back to referrer
         flash.message = "No existe el ehr para el paciente $patientUID"
         redirect(controller:'person', action:'list')
         return
      }
      
      return [ehr: ehr] 
   }
   
   def show(String uid)
   {
      def ehr = Ehr.findByUid(uid)
      
      println request.getHeader('referer')
      
      if (!ehr)
      {
         // TODO: back to referrer
         flash.message = "No existe el ehr $uid"
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
      println "ehrComtrbutions " + params
      def contribs
      def ehr = Ehr.get(id)
      
      // parse de dates
      Date qFromDate
      Date qToDate
      if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
      if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)
      
      // TODO: filtro de 
      //if (qarchetypeId || fromDate || toDate)
      //{
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
      //}
      
      render(template:'ehrContributions', model:[contributions:contribs]) 
   }
   
   /**
    * GUI
    * 
    * @param patientUID uid de la Person con rol paciente
    * @return
    */
   def createEhr(String patientUID)
   {
      // TODO: no tirar excepciones porque pueden llegar a la gui
      if (!patientUID)
      {
         throw new Exception("patientUID es obligatorio")
      }
      
      def person = Person.findByUidAndRole(patientUID, 'pat')
      
      // 1. existe paciente?
      if (!person)
      {
         throw new Exception("el paciente $patientUID no existe")
      }
      
      // 2. el paciente ya tiene EHR?
      def c = Ehr.createCriteria()
      def ehr = c.get {
         subject {
            eq ('value', patientUID)
         }
      }
      
      if (ehr)
      {
         // TODO: ya tiene ehr, no creo nada
         throw new Exception("ya tiene ehr")
      }
      else
      {
         ehr = new Ehr(
            subject: new PatientProxy(
               value: patientUID
            ),
            organizationUid: person.organizationUid
         )
         
         if (!ehr.save())
         {
            // TODO: error
            println ehr.errors
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
