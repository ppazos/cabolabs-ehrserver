package com.cabolabs.ehrserver.notification

import grails.converters.*

class NotificationController {

   def springSecurityService
   
   def newNotifications(String forSection, String forOrganization, Long forUser)
   {
      def loggedInUser = springSecurityService.currentUser
      def notifications = Notification.newNotifications(forSection, session.organization.uid, loggedInUser.id, session.lang)
      render (contentType: "application/json", text: notifications as JSON, encoding:"UTF-8")
   }
   
   def dismiss(Notification notification)
   {
      def loggedInUser = springSecurityService.currentUser
      def status = NotificationStatus.findByNotificationAndUser(notification, loggedInUser)
      status.status = 'dismissed'
      status.save(failOnError: true)
      render (contentType: "application/json", text: status as JSON, encoding:"UTF-8")
   }
}
