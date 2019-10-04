package com.cabolabs.security

import grails.util.Holders
import com.cabolabs.ehrserver.reporting.ActivityLog
import com.cabolabs.ehrserver.notification.Notification

class AuthController {

   static defaultAction = "auth"

   def remoteNotificationsService

   def login() {}

   def logout()
   {
      def sessman = SessionManager.instance
      sessman.kill(session.id.toString())
      redirect action: 'login'
   }

   def auth(String email, String pass)
   {
      if (!email) return

      def sessman = SessionManager.instance
      def authprov = new ClosureAuthProvider()

      // TODO: the controller is doing all the work, the authenticate should manage
      //       the session in the manager if the result is positive
      def user
      def isauth = authprov.authenticate([email: email, pass: pass]) { ->
         user = User.findByEmail(email)
         if (!user) return false
         return PasswordUtils.isPasswordValid(user.password, pass)
      }

      if (isauth)
      {
         def usess = new Session(jsessid: session.id.toString(),
                                 userId: email,
                                 authenticated: true,
                                 payload: [user:user])

         try
         {
            sessman.addSession(usess)
         }
         catch (Exception e)
         {
            // the user already has a session
         }


         // read remote notifications
         // Get date of the last read of remote notifications by the current user, null if no reads were done
         // This avoids reading the same notifications twice by the same user
         def lastALogs = ActivityLog.findAllByActionAndUsername('remote_notifications', email, [max: 1, sort: 'timestamp', order:'desc'])
         def from
         if (lastALogs.size() > 0) from = lastALogs[0].timestamp

         def notifications = remoteNotificationsService.getNotifications('ehrserver', session.lang, from)

         notifications.each { notification ->
            new Notification(name:'remote', language:session.lang, text:notification.nt, forUser:User.findByEmail(email).id).save()
         }

         // Mark current read of the remote notifications
         new ActivityLog(username: email, action: 'remote_notifications', sessionId: session.id.toString()).save()

         // /remote notifications


         // Select the first Organization of the user as the current org, then they can change it from the GUI
         def user_first_organization = user.firstOrganization
         session.organization = user_first_organization
         session.account =  user_first_organization.account

         redirect controller:'app', action:'index'
         return
      }

      flash.message = 'wrong credentials'
      render view: 'login'
   }

   // token comes always and is required for reset
   def resetPassword(String token, String newPassword, String confirmNewPassword)
   {
      // GET: display reset view
      // POST: try to reset the pass

      if (!token)
      {
         flash.message = message(code:"user.resetPassword.noToken")
         redirect action:'login'
         return
      }

      def user = User.findByResetPasswordToken(token)
      if (!user)
      {
         flash.message = message(code:"user.resetPassword.alreadyResetOrExpired")
         redirect action:'login'
         return
      }

      if (request.post)
      {
         if (!newPassword || !confirmNewPassword)
         {
            flash.message = message(code:"user.resetPassword.passwordConfirmationNeeded")
            return
         }

         def min_length = Holders.config.app.security.min_password_length
         if (newPassword.size() < min_length)
         {
            flash.message = message(code:"user.resetPassword.passNotLongEnough", args:[min_length])
            return
         }

         if (newPassword != confirmNewPassword)
         {
            flash.message = message(code:"user.resetPassword.confirmDoesntMatch")
            return
         }


         user.password = PasswordUtils.encodePassword(newPassword)
         user.enabled = true
         user.emptyPasswordToken()
         user.save(flush:true)

         flash.message = message(code:"user.resetPassword.passwordResetOK")
         redirect controller:'login', action:'auth'
         return
      }
   }

   def forgotPassword(String email)
   {
      if (request.post)
      {
         def user = User.findByEmail(email)

         if (!user)
         {
            flash.message = message(code:"user.forgotPassword.emailDoesntExists")
            return
         }

         // generates a password reset token, used in the email notification
         user.setPasswordToken()
         user.save(flush:true)

         try
         {
            notificationService.sendForgotPasswordEmail( user.email, [user] )
         }
         catch (Exception e) // FIXME: should rollback the user update if the email fails or retry the email send
         {
            log.error e.message

            flash.message = message(code:"user.forgotPassword.errorSendingEmail")
            return
         }


         flash.message = message(code:"user.forgotPassword.passResetSend")
         redirect controller:'login', action:'auth'
         return
      }
      // display the forgotPassword view
   }


   // same as forgotPassword but it can be triggered by an admin / org manager
   // from the user/show to let users reset their password even if they forgot
   // the email used to register.
   def resetPasswordRequest(String email)
   {
      def user = User.findByEmail(email)

      if (!user)
      {
         flash.message = message(code:"user.forgotPassword.emailDoesntExists")
         redirect(action:'show', id:params.id)
         return
      }


      // generates a password reset token, used in the email notification
      user.setPasswordToken()
      user.enabled = false // if enabled, password token is cleaned beforeInsert
      user.save(flush:true)


      try
      {
         notificationService.sendForgotPasswordEmail( user.email, [user] )
      }
      catch (Exception e) // FIXME: should rollback the user update if the email fails or retry the email send
      {
         log.error e.message

         flash.message = message(code:"user.forgotPassword.errorSendingEmail")
         redirect(action:'show', id:params.id)
         return
      }


      flash.message = message(code:"user.forgotPassword.passResetSend")
      redirect(action:'show', id:params.id)
      return
   }
}
