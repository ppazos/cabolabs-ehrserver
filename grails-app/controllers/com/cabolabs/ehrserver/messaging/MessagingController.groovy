package com.cabolabs.ehrserver.messaging

import com.cabolabs.security.User
import grails.converters.JSON
import com.cabolabs.ehrserver.conf.ConfigurationItem

class MessagingController {

   def mailService
   def springSecurityService
   def grailsApplication

   // users send feedback from Web Console
   def feedback(String text, String about)
   {
      // from current user
      def loggedInUser = springSecurityService.currentUser
      def from_email = loggedInUser.email
      
      // need to know the source server instance
      def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
      def server_url = g.createLink(uri:'/', absolute:true)
      def ci = ConfigurationItem.findByKey('ehrserver.instance.id')
      
      
      def title, preview, salute, message, actions, closing, bye
      
      title   = g.message(code:'notificationService.feedback.title', args:[about])
      preview = g.message(code:'notificationService.feedback.preview', args:[loggedInUser.username])
      salute  = g.message(code:'notificationService.feedback.salute')
      message = g.message(code:'notificationService.feedback.message', args:[loggedInUser.username, about, text, server_url, ci.value])
      actions = g.message(code:'notificationService.feedback.actions')
      closing = g.message(code:'notificationService.feedback.closing')
      bye     = g.message(code:'notificationService.feedback.bye')
      

      // to all admins
      def admins = User.allForRole('ROLE_ADMIN')
      admins.each { admin ->
         mailService.sendMail {
            from    from_email
            to      admin.email
            subject title
            html    view: "/messaging/email",
                    model: [title: title, preview: preview, salute: salute,
                            message: message, actions: actions, closing: closing, bye: bye]
         }
      }
      
      // TODO: copy to the user

      render(contentType: 'text/json') {[
        'message': 'Feedback sent, thanks!'
      ]}
   }
}
