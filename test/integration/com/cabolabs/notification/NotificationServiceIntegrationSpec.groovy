package com.cabolabs.notification

import grails.test.spock.IntegrationSpec
import spock.lang.Shared
import com.cabolabs.security.*

class NotificationServiceIntegrationSpec extends IntegrationSpec {

   def notificationService
   
   @Shared
   def dummyUser = [username:'user', passwordToken:'123', email:'a@b.com', organizations:[[number:'555']],
   getHigherAuthority: { org -> [authority: Role.US] }]
   
   def setup()
   {
   }

   def cleanup()
   {
   }

   void "sendUserRegisteredOrCreatedEmail"()
   {
      when:
         def email = notificationService.sendUserRegisteredOrCreatedEmail(recipient, messageData, userRegistered)
         
         def html = new String(email.mimeMessage.content) // https://docs.oracle.com/javaee/7/api/javax/mail/internet/MimeMessage.html?is-external=true
         def body = html.substring(html.indexOf("<body"), html.indexOf("</body>")+7)

         body = body.replace('&nbsp;', '')
         def xml = new XmlSlurper().parseText(body)
         
      then:
         email != null
         email instanceof org.springframework.mail.javamail.MimeMailMessage

         //xml.div.div[1].p.text().contains( out )
         xml.table.tr.td[1].div.table.tr.td.table.tr.td.p[1].text().contains('your username '+ dummyUser.username)
         
         if (userRegistered)
            xml.table.tr.td[1].div.table.tr.td.table.tr.td.p[1].text().contains('organization number '+ dummyUser.organizations[0].number)
         else
            xml.table.tr.td[1].div.table.tr.td.table.tr.td.p[1].text().contains('organization numbers '+ dummyUser.organizations.number)
      where:
         recipient | messageData               | userRegistered | out
         'a@b.com' | [dummyUser, 'org_number'] | false          | 'A user was created for you'
         'a@b.com' | [dummyUser, 'org_number'] | true           | 'We received your registration'
   }
   
   void "sendForgotPasswordEmail"()
   {
      when:
         def email = notificationService.sendForgotPasswordEmail(recipient, messageData)
         
         def html = new String(email.mimeMessage.content) // https://docs.oracle.com/javaee/7/api/javax/mail/internet/MimeMessage.html?is-external=true
         def body = html.substring(html.indexOf("<body"), html.indexOf("</body>")+7)

         body = body.replace('&nbsp;', '')
         def xml = new XmlSlurper().parseText(body)
         
      then:
         email != null
         email instanceof org.springframework.mail.javamail.MimeMailMessage
         
         xml.table.tr.td[1].div.table.tr.td.table.tr.td.p[1].text().contains( out )
         
      where:
         recipient | messageData               |  out
         'a@b.com' | [dummyUser, 'org_number'] | 'We received a password reset request for your email a@b.com'
   }
}
