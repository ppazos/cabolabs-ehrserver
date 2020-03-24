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
package com.cabolabs.ehrserver.notification

import grails.converters.*
import grails.util.Holders
import com.cabolabs.security.*
import grails.validation.ValidationException

class NotificationController {

   def authService
   def configurationService

   def config = Holders.config.app

   def index(int offset, String sort, String order)
   {
      int max = configurationService.getValue('ehrserver.console.lists.max_items')
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'

      [notificationInstanceList: Notification.list(max: max, offset: offset, sort: sort, order: order), total: Notification.count()]
   }

   def create()
   {
      def users
      if (authService.loggedInUserHasAnyRole("ROLE_ADMIN"))
      {
         users = User.list()
      }
      else
      {
         def org = session.organization
         def c = User.createCriteria()
         users = c.list (max: max, offset: offset, sort: sort, order: order) {
            organizations {
               eq ('uid', org.uid)
            }
         }
      }
      [notificationInstance: new Notification(), users: users]
   }

   def save(Notification notificationInstance)
   {
      try
      {
         notificationInstance.save(failOnError: true) // TODO: validation and return to notification create
      }
      catch (ValidationException e)
      {
         notificationInstance.errors = e.errors
         render view: "create", model: [notificationInstance:notificationInstance]
         return
      }

      redirect action: 'show', id: notificationInstance.id
   }

   def show(Notification notificationInstance)
   {
      [notificationInstance: notificationInstance, statuses: NotificationStatus.findAllByNotification(notificationInstance)]
   }

   def newNotifications(String forSection, String forOrganization, Long forUser)
   {
      def loggedInUser = authService.loggedInUser

      // List<NotificationStatus>
      def notifications = Notification.lastNotifications(forSection, session.organization.uid, loggedInUser.uid, session.lang)
      notifications = notifications.collect{ [status: it.status, notification: [text: it.notification.text, timestamp: it.notification.timestamp, id: it.notification.id]] }
      render (contentType: "application/json", text: notifications as JSON, encoding:"UTF-8")
   }

   def dismiss(Notification notification)
   {
      def loggedInUser = authService.loggedInUser
      def status = NotificationStatus.findByNotificationAndUser(notification, loggedInUser)
      status.status = 'dismissed'
      status.save(failOnError: true)
      render (contentType: "application/json", text: status as JSON, encoding:"UTF-8")
   }
}
