package com.cabolabs.ehrserver.notification

import com.cabolabs.security.User

class NotificationStatus {
   
   User user
   Notification notification
   String status = "new"
   
   static constraints = {
      status inList: ["new", "dismissed", "deleted"]
   }
}
