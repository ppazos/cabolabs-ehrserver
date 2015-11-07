package com.cabolabs.notification

import grails.transaction.Transactional

@Transactional
class NotificationService {

   def mailService
   def grailsApplication
   
   def sendUserRegisteredEmail(String recipient, List messageData)
   {
      // http://www.ygrails.com/2012/10/30/access-taglib-in-service-class/
      def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib');
      def url = g.createLink(controller:'login', absolute:true)
      
      // FIXME: messages should be part of the model and configurable through a GUI
      String message = '<p>Welcome to EHRServer!</p><p>The organization number assigned to {0} is {1}. That number will be used on calls to the EHRServer API, to store and query clinical information.</p><p>To login, please go here: '+ url +'</p>'

      messageData.eachWithIndex { data, i ->
        message = message.replaceFirst ( /\{\d*\}/ , data)
      }
      
      this.sendMail(recipient, 'Message from CaboLabs EHRServer!', message)
   }
   
   def sendMail(String recipient, String title = 'Message from CaboLabs EHRServer!', String message)
   {
      mailService.sendMail {
         from grailsApplication.config.grails.mail.username //"pablo.pazos@cabolabs.com"
         to recipient
         subject title
         //body 'How are you?'
         html view: "/notification/email", model: [message: message]
      }
   }
}
