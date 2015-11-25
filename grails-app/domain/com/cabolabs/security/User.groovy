package com.cabolabs.security

import com.cabolabs.security.Organization

class User implements Serializable {

   private static final long serialVersionUID = 1

   transient springSecurityService

   String username
   String password
   String email
   
   boolean enabled = true
   boolean accountExpired
   boolean accountLocked
   boolean passwordExpired
   
   // This is set when the user is created from the backend and the password is not set.
   // The user will be disabled, and the system sends an email to the new user with a
   // link to the reset password action, including this token in the link.
   String resetPasswordToken
   
   List organizations = []
   //static hasMany = [organizations: String] // UIDs of related organizations
   static hasMany = [organizations: Organization]
   
   User(String username, String password)
   {
      this()
      this.username = username
      this.password = password
   }

   @Override
   int hashCode()
   {
      username?.hashCode() ?: 0
   }

   @Override
   boolean equals(other)
   {
      is(other) || (other instanceof User && other.username == username)
   }

   @Override
   String toString()
   {
      username
   }

   Set<Role> getAuthorities()
   {
      // Avoids error of finding by a non saved instance.
      if (!this.id) return [] as Set
      UserRole.findAllByUser(this)*.role
   }
   
   boolean authoritiesContains(String role)
   {
      def roles = this.authorities
      return roles.find { it.authority == role } != null
   }

   def beforeInsert()
   {
      if (this.password)
      {
         encodePassword()
         
         if (this.enabled) this.resetPasswordToken = null
      }
   }

   def beforeUpdate()
   {
      if (isDirty('password'))
      {
         encodePassword()
      }
      
      if (this.password && this.enabled) this.resetPasswordToken = null
   }

   protected void encodePassword()
   {
      password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
   }

   static transients = ['springSecurityService', 'passwordToken']

   static constraints = {
      username blank: false, unique: true
      
      // if user is disabled, password can be blank, is used to allow the user to reset the password
      password nullable: true, validator: { val, obj ->
      
          if (obj.enabled && !val) return false
          return true
      }
      
      email blank: false, email: true
      
      resetPasswordToken nullable: true
      
      organizations validator: { val, obj ->
         //println "validator "+ val
         if (val.size() == 0)
         {
            //println "validator returns false"
            
            // We set the error, if this returns false, grails adds another error.
            obj.errors.rejectValue('organizations', 'user.organizations.empty')
            //return false
            //return ['user.organizations.empty']
         }
         
         //println "validator returns true"
         //return true
      }
   }

   static mapping = {
      password column: '`password`'
      organizations lazy: false
   }
   
   def setPasswordToken()
   {
      this.resetPasswordToken = java.util.UUID.randomUUID() as String
   }
   
   def getPasswordToken()
   {
      return this.resetPasswordToken
   }
}
