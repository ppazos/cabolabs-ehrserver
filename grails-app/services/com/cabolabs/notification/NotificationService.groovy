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
import com.cabolabs.security.*

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
      def url //= g.createLink(controller:'user', action:'resetPassword', absolute:true, params:[token:token])
      def organizationNumbers = user.organizations*.number
      def title, preview, salute, message, actions, closing, bye

      if (userRegistered)
      {
         title   = g.message(code:'notificationService.userRegistered.title')
         preview = g.message(code:'notificationService.userRegistered.preview')
         salute  = g.message(code:'notificationService.userRegistered.salute', args:[user.username])
         message = g.message(code:'notificationService.userRegistered.message', args:[user.username, organizationNumbers[0]])

         url     = g.createLink(controller:'user', action:'resetPassword', absolute:true, params:[token:token])
         actions = g.message(code:'notificationService.userRegistered.actions', args:[url])

         closing = g.message(code:'notificationService.userRegistered.closing')
         bye     = g.message(code:'notificationService.userRegistered.bye')
      }
      else
      {
         // Need to tell the user on which orgs they can login to the web console and on which
         // ones only login via the API (highest role is User)
         def role
         def login_on_web_console = [:]
         user.organizations.each { org ->

            role = user.getHigherAuthority(org)
            login_on_web_console[org.number] = (role.authority != Role.US)
         }

         def access_table = '<table width="100%" border="1"><tr><th>'+ g.message(code:'notificationService.userCreated.organizationNumber') +'</th><th>'+ g.message(code:'notificationService.userCreated.webConsoleAccess') +'</th></tr>'
         login_on_web_console.each { orgnum, login_web ->
            access_table += "<tr><td>$orgnum</td><td>$login_web</td></tr>"
         }
         access_table += '</table>'

         title   = g.message(code:'notificationService.userCreated.title')
         preview = g.message(code:'notificationService.userCreated.preview')
         salute  = g.message(code:'notificationService.userCreated.salute', args:[user.username])
         message = g.message(code:'notificationService.userCreated.message', args:[user.username, organizationNumbers.toString(), access_table])
         message = message.replace('&lt;', '<').replace('&gt;', '>').replace('&quot;', '"') // the table in the args is converted to XML entities. the email needs the tags.
         //message += access_table

         url     = g.createLink(controller:'user', action:'resetPassword', absolute:true, params:[token:token])
         actions = g.message(code:'notificationService.userCreated.actions', args:[url])

         closing = g.message(code:'notificationService.userCreated.closing')
         bye     = g.message(code:'notificationService.userCreated.bye')
      }

      this.sendMail(recipient, title, preview, salute, message, actions, closing, bye)
   }

   def sendForgotPasswordEmail(String recipient, List messageData)
   {
      def user = messageData[0]
      def token = user.passwordToken
      def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib');
      def url
      def title, preview, salute, message, actions, closing, bye

      title   = g.message(code:'notificationService.forgot.title')
      preview = g.message(code:'notificationService.forgot.preview')
      salute  = g.message(code:'notificationService.forgot.salute')
      message = g.message(code:'notificationService.forgot.message', args:[user.email])

      url     = g.createLink(controller:'user', action:'resetPassword', absolute:true, params:[token:token])
      actions = g.message(code:'notificationService.forgot.actions', args:[url])

      closing = g.message(code:'notificationService.forgot.closing')
      bye     = g.message(code:'notificationService.forgot.bye')

      this.sendMail(recipient, title, preview, salute, message, actions, closing, bye)
   }

   /**
    * When an organization is created, we notify the ACCMAN of the Account containing the Organization so he has access to it.
    * messageData contains the account and the new organization
    */
   def sendNewOrganizationAssociatedEmail(Map messageData)
   {
      def account = messageData['account']
      def accman = account.contact
      def organization = messageData['organization']

      def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')

      def title, preview, salute, message, url, actions, closing, bye

      title   = g.message(code:'notificationService.organizationAssociated.title')
      preview = g.message(code:'notificationService.organizationAssociated.preview')
      salute  = g.message(code:'notificationService.organizationAssociated.salute', args:[accman.username])
      message = g.message(code:'notificationService.organizationAssociated.message', args:[organization.number])

      url     = g.createLink(controller:'login', absolute:true)
      actions = g.message(code:'notificationService.organizationAssociated.actions', args:[url])

      closing = g.message(code:'notificationService.organizationAssociated.closing')
      bye     = g.message(code:'notificationService.organizationAssociated.bye')

      this.sendMail(accman.email, title, preview, salute, message, actions, closing, bye)
   }

   def sendMail(String recipient, String title = 'Message from CaboLabs EHRServer!',
                String preview,
                String salute,
                String message,
                String actions,
                String closing,
                String bye)
   {
      mailService.sendMail {
         from grailsApplication.config.grails.mail.default.from
         to recipient
         subject title
         //html view: "/notification/email", model: [message: message]
         html view: "/messaging/email",
                 model: [title: title, preview: preview, salute: salute,
                         message: message, actions: actions, closing: closing, bye: bye]
      }
   }
}
