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

package com.cabolabs.ehrserver.openehr.ehr

import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
//import com.cabolabs.ehrserver.openehr.demographic.Person
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
import com.cabolabs.security.User
import com.cabolabs.ehrserver.openehr.composition.CompositionService

import grails.transaction.Transactional

class EhrController {

   def springSecurityService
   def compositionService
   def configurationService

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
   def list(int max, int offset, String sort, String order, String uid, String organizationUid)
   {
      max = configurationService.getValue('ehrserver.console.lists.max_items')
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
            eq('deleted', false)
            // filters
            if (uid)
            {
               like('uid', '%'+uid+'%')
            }
            if (organizationUid)
            {
               like('organizationUid', '%'+organizationUid+'%')
            }
            // TODO: subject.value
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
            eq('deleted', false)
            //eq ('organizationUid', org.uid) // same org as used for login
            if (organizationUid)
            {
               and {
                  like('organizationUid', '%'+organizationUid+'%') // filter
                  'in'('organizationUid', orgs.uid) // from all the orgs of the logged user
               }
            }
            else
            {
               'in'('organizationUid', orgs.uid) // from all the orgs of the logged user
            }
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

      render(view:'show', model:[ehr: ehr])
   }

   /**
    * Auxiliar de show para mostrar las contributiosn y sus
    * compositions en una tabla y poder filtrarlas.
    * @return
    */
   def ehrContributions(long id, String fromDate, String toDate, String qarchetypeId)
   {
      def contribs

      if (!id)
      {
         render(status: 404, text: 'EHR id is required')
         return
      }

      def ehr = Ehr.get(id) // TODO: use UID

      if (!ehr)
      {
         render(status: 404, text: "EHR doesn't exists")
         return
      }

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
         if (qarchetypeId || qFromDate || qToDate)
         {
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
      }

      render(template:'ehrContributions', model:[contributions:contribs])
   }

   def create()
   {
      return [ehr: new Ehr()]
   }

   @Transactional
   def save(Ehr ehr)
   {
      if (!ehr)
      {
         flash.message = message(code:'ehr.save.error')
         redirect action:'create'
         return
      }
      //println ehr.subject.value // it's binded

      // Check if there is an EHR for the same subject UID
      def c = Ehr.createCriteria()
      def existing_ehr = c.get {
         subject {
            eq('value', ehr.subject.value)
         }
      }

      if (existing_ehr)
      {
         flash.message = message(code:'ehr.createEhr.patientAlreadyHasEhr', args:[ehr.subject.value, existing_ehr.uid])
         redirect action: 'create'
         return
      }


      if (!ehr.save(flush:true))
      {
         flash.message = message(code:'ehr.save.error')
         render (view: 'create', model: [ehr: ehr])
         return
      }

      flash.message = message(code:'ehr.save.ok', args:[ehr.uid])
      redirect action:'list'
   }

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

   def delete(String uid)
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

      ehr.deleted = true

      ehr.save(failOnError: true)

      flash.message = message(code:'ehr.delete.deletedOk')
      redirect action: 'list'
   }
}
