package com.cabolabs.ehrserver.messaging

import com.cabolabs.security.User
import grails.converters.JSON

class MessagingController {

   def mailService
   def springSecurityService


   // users send feedback from Web Console
   def feedback(String message, String about)
   {
      // from current user
      def loggedInUser = springSecurityService.currentUser
      def from_email = loggedInUser.email
      
      // to all admins
      def admins = User.allForRole('ROLE_ADMIN')
      admins.each { admin ->
         mailService.sendMail {
            from from_email
            to admin.email
            subject 'EHRServer Feedback'
            text '('+ about +')'+ message
         }
      }

      render(contentType: 'text/json') {[
        'message': 'Feedback sent'
      ]}
   }
}
