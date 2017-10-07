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

package com.cabolabs.ehrserver.notification

import com.cabolabs.security.User
import com.cabolabs.security.UserRole

/**
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class NotificationJob {
   
   def concurrent = false
   
   static triggers = {
      simple repeatInterval: 600000l // execute job once in 10 minutes
   }
   
   /**
    * Creates Notification statuses for non sent ontifications.
    */
   def execute()
   {
      //println "NotificationJob"
      
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
