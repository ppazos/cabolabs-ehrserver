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

package com.cabolabs.security

import com.cabolabs.ehrserver.account.Account

class User implements Serializable {

   private static final long serialVersionUID = 1

   transient springSecurityService

   String username
   String password
   String email
   boolean isVirtual = false // virtual users for ApiKey
   boolean enabled // false until the password is set
   boolean accountExpired
   boolean accountLocked
   boolean passwordExpired
   
   // This is set when the user is created from the backend and the password is not set.
   // The user will be disabled, and the system sends an email to the new user with a
   // link to the reset password action, including this token in the link.
   String resetPasswordToken
   Date resetPasswordTokenSet // for expiration
   
   Date dateCreated
   Date lastUpdated
   
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
   String toString()
   {
      username
   }

   Set<Role> getAuthorities(Organization org)
   {
      // Avoids error of finding by a non saved instance.
      if (!this.id) return [] as Set
      UserRole.findAllByUser(this)*.role
   }
   
   /**
    * returns the highest role assigned to the user.
    * ROLE_ADMIN > ROLE_ACCOUNT_MANAGER,ROLE_ORG_MANAGER > any other role
    * @return
    */
   Role getHigherAuthority(Organization org)
   {
      // custom logic to avoid many queries for using authoritiesContains
      def roles = UserRole.findAllByUserAndOrganization(this, org)*.role
      
      def role = roles.find { it.authority == Role.AD }
      if (role) return role
      
      role = roles.find { it.authority == Role.AM }
      if (role) return role
      
      role = roles.find { it.authority == Role.OM }
      if (role) return role
      
      return roles[0] // any other role
   }
   
   boolean authoritiesContains(String role, Organization org)
   {
      return this.getAuthorities(org).find { it.authority == role } != null
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
      
      //if (this.password && this.enabled) this.resetPasswordToken = null
   }

   protected void encodePassword()
   {
      password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
   }

   static transients = ['springSecurityService', 'passwordToken', 'authorities', 'higherAuthority', 'organizations', 'account']

   static constraints = {
      username blank: false, unique: true, matches:'^[A-Za-z\\d\\.\\-_]*$' // https://github.com/ppazos/cabolabs-ehrserver/issues/460
      
      // if user is disabled, password can be blank, is used to allow the user to reset the password
      password nullable: true, validator: { val, obj ->
      
          if (obj.enabled && !val) return false
          return true
      }
      
      email blank: false, email: true, unique: true
      
      resetPasswordToken nullable: true
      resetPasswordTokenSet nullable: true
      
      /*
      organizations validator: { val, obj ->
         //println "validator "+ val
         if (!val || val.size() == 0)
         {
            // We set the error, if this returns false, grails adds another error.
            obj.errors.rejectValue('organizations', 'user.organizations.empty')
         }
      }
      */
   }

   static mapping = {
      password column: '`password`'
      //organizations lazy: false
   }
   
   def getOrganizations()
   {
      UserRole.withNewSession { 
         UserRole.findAllByUser(this).organization
      }
   }
   
   static List allForRole(authority)
   {
      def urs = UserRole.withCriteria {
         role {
            eq('authority', authority)
         }
      }
      return urs.user
   }
   
   static List allForAccount(Account account)
   {
      def urs = UserRole.withCriteria{
        'in'('organization', account.organizations)
      }
      
      urs.user
   }
   
   Account getAccount()
   {
      // Just need one UserRole because all URs will be on the same Account
      def org = UserRole.findByUser(this).organization
      org.account
   }
   
   def setPasswordToken()
   {
      this.resetPasswordToken = java.util.UUID.randomUUID() as String
      this.resetPasswordTokenSet = new Date()
   }
   
   def getPasswordToken()
   {
      return this.resetPasswordToken
   }
   
   def emptyPasswordToken()
   {
      this.resetPasswordToken = null
      this.resetPasswordTokenSet = null
   }
}
