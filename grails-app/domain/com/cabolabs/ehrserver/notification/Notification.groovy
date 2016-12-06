package com.cabolabs.ehrserver.notification

class Notification {

   String name
   String language // language of the text
   String text
   
   String forSection // ehrs, contributions, versions, directory, queries, templates, users, roles, organizations, notifications
   String forOrganization
   Long forUser // user.id
   
   Date dateCreated
   
   static constraints = {
      forSection nullable: true
      forOrganization nullable: true
      forUser nullable: true
   }
   
   static newNotifications(String forSection, String forOrganization, Long forUser)
   {
      def c = NotificationStatus.createCriteria()
      def list = c.list {
         eq('status', 'new')
         if (forUser)
         {
            user {
               eq('id', forUser)
            }
         }
         if (forOrganization) // forOrganization alwas comes, but it should match also when it is null on the notification
         {
            or {
               notification {
                  eq('forOrganization', forOrganization)
               }
               notification {
                  isNull('forOrganization')
               }
            }
         }
         if (forSection) // forSection always comes, but if should match also when it is null on the notification
         {
            or {
               notification {
                  eq('forSection', forSection)
               }
               notification {
                  isNull('forSection')
               }
            }
         }
      }
      
      return list.notification
   }
}
