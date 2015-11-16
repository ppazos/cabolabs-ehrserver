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
   
   static hasMany = [organizations: String] // UIDs of related organizations

	User(String username, String password) {
		this()
		this.username = username
		this.password = password
	}

	@Override
	int hashCode() {
		username?.hashCode() ?: 0
	}

	@Override
	boolean equals(other) {
		is(other) || (other instanceof User && other.username == username)
	}

	@Override
	String toString() {
		username
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this)*.role
	}

	def beforeInsert() {
      if (this.password) {
		   encodePassword()
      }
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
	}

	static transients = ['springSecurityService', 'organizationObjects']

	static constraints = {
		username blank: false, unique: true
      
      // if user is disabled, password can be blank, is used to allow the user to reset the password
		password nullable: true, validator: { val, obj ->
      
          if (obj.enabled && !val) return false
          return true
      }
      
      email blank: false, email: true
	}

	static mapping = {
		password column: '`password`'
      organizations lazy: false
	}
   
   def getOrganizationObjects()
   {
      return Organization.findAllByUidInList(this.organizations)
   }
}
