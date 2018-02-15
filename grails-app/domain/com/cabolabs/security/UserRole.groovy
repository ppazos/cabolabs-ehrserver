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

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.apache.commons.lang.builder.HashCodeBuilder
/**
 * A User plays a Role in an Organization.
 */
@ToString(cache=true, includeNames=true, includePackage=false)
class UserRole implements Serializable {

	private static final long serialVersionUID = 1

	User user
	Role role
   Organization organization

	UserRole(User u, Role r, Organization o) {
		this()
		user = u
		role = r
      organization = o
	}

	@Override
	boolean equals(other) {
		if (!other.instanceOf(UserRole)) {
			return false
		}

		other.user?.id == user?.id && other.role?.id == role?.id && other.organization?.id == organization?.id 
	}

	@Override
	int hashCode() {
		def builder = new HashCodeBuilder()
		if (user) builder.append(user.id)
		if (role) builder.append(role.id)
      if (organization) builder.append(organization.id)
		builder.toHashCode()
	}

   // FIXME: this can return a List becuase the same user can have the same role in two orgs
	static UserRole get(long userId, long roleId) {
		criteriaFor(userId, roleId).get()
	}

	static boolean exists(long userId, long roleId, long orgId) {
		criteriaFor(userId, roleId, orgId).count()
	}

	private static DetachedCriteria criteriaFor(long userId, long roleId, long orgId) {
		UserRole.where {
			user == User.load(userId) &&
			role == Role.load(roleId)
         organization == Organization.load(orgId)
		}
	}

	static UserRole create(User user, Role role, Organization org, boolean flush = false) {
		def instance = new UserRole(user, role, org)
      if (!instance.save(flush: flush, insert: true))
      {
         println instance.errors
      }
		instance
	}

	static void remove(User u, Role r, Organization o, boolean flush = false) {
		if (u == null || r == null || o == null) return

		UserRole.where { user == u && role == r && organization == o }.list()*.delete()

		if (flush) { UserRole.withSession { it.flush() } }
	}

	static void removeAll(User u, boolean flush = false) {
		if (u == null) return

		UserRole.where { user == u }.list()*.delete()

		if (flush) { UserRole.withSession { it.flush() } }
	}

	static void removeAll(Role r, boolean flush = false) {
		if (r == null) return

		UserRole.where { role == r }.list()*.delete()

		if (flush) { UserRole.withSession { it.flush() } }
	}

	static constraints = {
		role validator: { Role r, UserRole ur ->
			if (ur.user == null || ur.user.id == null || ur.organization.id == null) return
			boolean existing = false
			UserRole.withNewSession {
				existing = UserRole.exists(ur.user.id, r.id, ur.organization.id)
			}
			if (existing) {
				return 'userRole.exists'
			}
		}
	}

	static mapping = {
		id composite: ['user', 'role', 'organization']
		version false
	}
}
