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

   // Checks if the logged in user has any of the roles
   def loggedInUserHasAnyRole(String roles)
   {
      def sessman = SessionManager.instance
      def sess = sessman.getSession(session.id.toString())

      return userHasAnyRole(sess.payload.user, roles)
   }

   // Checks if a given user has any of the roles
   def userHasAnyRole(User user, String roles)
   {
      if (!roles) throw new Exception('roles param is required')

      def _roles = roles.split(",")

      def user_roles = user.getAuthorities(session.organization)
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
