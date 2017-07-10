
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

package com.cabolabs.notification

import grails.transaction.Transactional

@Transactional
class NotificationService {

   def mailService
   def grailsApplication
   
   // User created by admin or org manager
   /**
    * 
    * @param recipient
    * @param messageData
    * @param userRegistered true if the user was created by registering, false if it was created from the admin console.
    * @return
    */
   def sendUserRegisteredOrCreatedEmail(String recipient, List messageData, boolean userRegistered = false)
   {
      def user = messageData[0]
      def token = user.passwordToken
      def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
      def url = g.createLink(controller:'user', action:'resetPassword', absolute:true, params:[token:token])
      
      def organizationNumbers = user.organizations*.number
      String message
      
      if (userRegistered)
         message = g.message(code:'notificationService.sendUserRegisteredOrCreatedEmail.registeredMessage', args:[user.username, organizationNumbers[0], url])
      else
         message = g.message(code:'notificationService.sendUserRegisteredOrCreatedEmail.createdMessage', args:[user.username, organizationNumbers.toString(), url])

      /*
      message = message.replaceFirst ( /\{0\}/ , user.username)
      
      if (organizationNumbers.size() == 1)
         message = message.replaceFirst ( /\{1\}/ , organizationNumbers[0].toString())
      else
         message = message.replaceFirst ( /\{1\}/ , organizationNumbers.toString())
      */
      
      this.sendMail(recipient, g.message(code:'notificationService.sendUserRegisteredOrCreatedEmail.subject'), message)
   }
   
   def sendForgotPasswordEmail(String recipient, List messageData, boolean userRegistered = false)
   {
      def user = messageData[0]
      
      //println "sendForgotPasswordEmail"
      
      def token = user.passwordToken
      def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib');
      def url = g.createLink(controller:'user', action:'resetPassword', absolute:true, params:[token:token])
      
      def organizationNumbers = user.organizations*.number
      String message
      
      message = "<p>We received a password reset request for your email {0}</p>"+
                "<p>If you didn't requested it, just ignore this email. If this was you, please go here: "+ url +"</p>"
 
      message = message.replaceFirst ( /\{0\}/ , user.email)
      
      this.sendMail(recipient, 'Your password reset for CaboLabs EHRServer!', message)
   }
   
   def sendMail(String recipient, String title = 'Message from CaboLabs EHRServer!', String message)
   {
      mailService.sendMail {
         from grailsApplication.config.grails.mail.default.from //.username //"pablo.pazos@cabolabs.com"
         to recipient
         subject title
         //body 'How are you?'
         html view: "/notification/email", model: [message: message]
      }
   }
}
