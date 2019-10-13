package com.cabolabs.ehrserver.notification

import com.cabolabs.security.User
import com.cabolabs.security.UserRole

class NotificationJob {

   static concurrent = false

   static triggers = {
      simple repeatInterval: 60000l, startDelay: 120000l // execute job once in 5 seconds
   }

   def execute()
   {
      log.info "Notification Job Executing"

      Notification.findAllBySent(false).each { notificationInstance ->

         //println "send notification "+ notificationInstance.id

         def statuses = []
         if (!notificationInstance.forUser)
         {
            def users
            if (notificationInstance.forOrganization)
            {
               def urs = UserRole.withCriteria {
                  organization {
                     eq('uid', notificationInstance.forOrganization)
                  }
                  user {
                     eq('isVirtual', false)
                  }
               }
               users = urs.user.unique() // unique avoids the same notif to go to a user that has 2 roles in the same org
            }
            else
            {
               users = User.list()
            }

            users.each { user ->
               statuses << new NotificationStatus(user:user, notification:notificationInstance)
            }
         }
         else
         {
            statuses << new NotificationStatus(user:User.get(notificationInstance.forUser), notification:notificationInstance)
         }

         statuses.each { status ->
            status.save(failOnError: true)
         }

         notificationInstance.sent = true
         notificationInstance.save(failOnError: true)
      }
   }
}
