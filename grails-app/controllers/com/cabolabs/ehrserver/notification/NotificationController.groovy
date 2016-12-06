package com.cabolabs.ehrserver.notification

import grails.converters.*

class NotificationController {

   def springSecurityService
   
   def newNotifications(String forSection, String forOrganization, Long forUser)
   {
      def loggedInUser = springSecurityService.currentUser
      def notifications = Notification.newNotifications(forSection, session.organization.uid, loggedInUser.id)
      render (contentType: "application/json", text: notifications as JSON, encoding:"UTF-8")
   }
}
