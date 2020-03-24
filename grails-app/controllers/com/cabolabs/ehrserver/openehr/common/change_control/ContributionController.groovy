/*
 * Copyright 2011-2020 CaboLabs Health Informatics
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

package com.cabolabs.ehrserver.openehr.common.change_control

import org.springframework.dao.DataIntegrityViolationException
import com.cabolabs.security.Organization
import com.cabolabs.security.User
import grails.converters.*
import grails.util.Holders
import groovy.json.*
import com.cabolabs.ehrserver.parsers.JsonService
import com.cabolabs.ehrserver.sync.SyncMarshallersService

class ContributionController {

   def authService
   def configurationService
   def jsonService
   def syncMarshallersService

   static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

   def config = Holders.config.app

   def index(int offset, String sort, String order, String ehdUid, String organizationUid)
   {
      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'desc'

      def list, org, orgs
      def c = Contribution.createCriteria()
      def us = authService.loggedInUser

      if (organizationUid)
      {
         if (Organization.countByUid(organizationUid) == 0)
         {
            flash.message = "contribution.list.feedback.orgNotFoundShowingForCurrentOrg"
            organizationUid = null
         }
         else
         {
            // Have access to organizationUid?
            if (!us.organizations.uid.contains(organizationUid) && !authService.loggedInUserHasAnyRole("ROLE_ADMIN"))
            {
               flash.message = "contribution.list.feedback.cantAccessOrgShowingForCurrentOrg"
               organizationUid = null
            }
         }
      }

      if (authService.loggedInUserHasAnyRole("ROLE_ADMIN"))
      {
         // for now admins can see contributions for all the orgs

         list = c.list (max: max, offset: offset, sort: sort, order: order) {

            // for admins, if not organizationUid, display for all orgs

            if (organizationUid)
            {
               ehr {
                 eq("organizationUid", organizationUid)
               }
            }

            if (ehdUid)
            {
               ehr {
                  like('uid', '%'+ehdUid+'%')
               }
            }
         }

         orgs = Organization.list() // for the org filter
      }
      else
      {
         org = session.organization

         list = c.list (max: max, offset: offset, sort: sort, order: order) {
            if (!organizationUid)
            {
               flash.message = message(code:"contribution.list.feedback.showingForCurrentOrg")
               eq("organizationUid", org.uid)
            }
            else
            {
               eq("organizationUid", organizationUid)
            }
            if (ehdUid)
            {
               ehr {
                  like('uid', '%'+ehdUid+'%')
               }
            }
         }

         orgs = us.organizations // for the org filter
      }

      // =========================================================================
      // For charting

      // Show 1 year by month
      def now = new Date()
      def oneyearbehind = now - 365

      def data = Contribution.withCriteria {
          projections {
              count('id')
              groupProperty('yearMonthGroup') // count contributions in the same month
          }
          if (organizationUid) // for any role if orgUid filter comes, filter by it the data on the chart
          {
             eq('organizationUid', organizationUid)
          }
          else if (!authService.loggedInUserHasAnyRole("ROLE_ADMIN")) // or filter by current org for non admins
          {
             eq('organizationUid', org.uid)
          }
          audit {
             between('timeCommitted', oneyearbehind, now)
          }
      }

      //println data
      // =========================================================================

      return [contributionInstanceList: list, total: list.totalCount,
              data: data, start: oneyearbehind, end: now,
              organizations: orgs]
   }

   def show(Long id)
   {
      def contributionInstance = Contribution.get(id)
      if (!contributionInstance) {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
         redirect(action: "index")
         return
      }

      [contributionInstance: contributionInstance]
   }
}
