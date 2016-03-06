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
   
   
   def index() { }
   
   def list(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
      
      def list, count
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = Ehr.list(params)
         count = Ehr.count()
      }
      else
      {
         // auth token used to login
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         
         list = Ehr.findAllByOrganizationUid(org.uid, params)
         count = Ehr.countByOrganizationUid(org.uid)
      }
      
      [list: list, total: count]
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
         flash.message = "No existe el ehr para el paciente $patientUID"
         redirect(controller:'person', action:'list')
         return
      }
      
      return [ehr: ehr] 
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
