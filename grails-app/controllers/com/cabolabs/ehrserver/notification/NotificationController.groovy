package com.cabolabs.ehrserver.notification

import grails.converters.*
import grails.util.Holders
import com.cabolabs.security.*
import grails.plugin.springsecurity.SpringSecurityUtils

class NotificationController {

   def springSecurityService
   def config = Holders.config.app
   
   def index(int max, int offset, String sort, String order)
   {
      max = Math.min(max ?: config.list_max, 100)
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      [notificationInstanceList: Notification.list(max: max, offset: offset, sort: sort, order: order), total: Notification.count()]
   }
   
   def create()
   {
      def users
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         users = User.list()
      }
      else
      {
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
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
      def statuses = []
      if (!notificationInstance.forUser)
      {
         User.list().each { user ->
            statuses << new NotificationStatus(user:user, notification:notificationInstance)
         }
      }
      else
      {
         statuses << new NotificationStatus(user:User.get(notificationInstance.forUser), notification:notificationInstance)
      }
      
      notificationInstance.save(failOnError: true)
      
      statuses.each { status ->
         status.save(failOnError: true)
      }
      
      redirect action: 'show', id: notificationInstance.id
   }
   
   def show(Notification notificationInstance)
   {
      [notificationInstance: notificationInstance, statuses: NotificationStatus.findAllByNotification(notificationInstance)]
   }
   
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
