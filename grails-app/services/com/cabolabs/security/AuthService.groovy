package com.cabolabs.security

import grails.gorm.transactions.Transactional

// to be able to access session from service
// https://stackoverflow.com/questions/42704309/how-to-test-a-grails-service-that-retrieves-information-from-the-http-session
import grails.web.api.ServletAttributes

@Transactional
class AuthService implements ServletAttributes {

   def getLoggedInUser()
   {
      def sessman = SessionManager.instance
      def sess = sessman.getSession(session.id.toString())
      if (sess) return sess.payload.user
      return null
   }

   def isLoggedIn()
   {
      def sessman = SessionManager.instance
      def sess = sessman.getSession(session.id.toString())
      sess != null
   }

   def loggedInUserHasAnyRole(String roles)
   {
      if (!roles) throw new Exception('roles param is required')

      def _roles = roles.split(",")

      def sessman = SessionManager.instance
      def sess = sessman.getSession(session.id.toString())
      def user_roles = sess.payload.user.getAuthorities(session.organization)
      def has_role = false
      user_roles.each { role ->
         if (_roles.contains(role.authority))
         {
            has_role = true
            return
         }
      }

      return has_role
   }
}
