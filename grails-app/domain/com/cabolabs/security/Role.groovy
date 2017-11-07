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

class Role implements Serializable {

	private static final long serialVersionUID = 1
   static final String AD = 'ROLE_ADMIN'
   static final String AM = 'ROLE_ACCOUNT_MANAGER'
   static final String OM = 'ROLE_ORG_MANAGER'
   static final String US = 'ROLE_USER'
   
	String authority

	Role(String authority) {
		this()
		this.authority = authority
	}

	@Override
	int hashCode() {
		authority?.hashCode() ?: 0
	}

	@Override
	boolean equals(other) {
		is(other) || (other.instanceOf(Role) && other.authority == authority)
	}

	@Override
	String toString() {
		authority
	}

	static constraints = {
		authority blank: false, unique: true
	}

	static mapping = {
		cache true
	}
   
   boolean higherThan (Object r)
   {
      //println this.authority +" higherThan "+ r.authority
      if (this.authority == r.authority) return true // we consider x higher than x in the role hierarchy
      
      if (this.authority == AD) return true // admins is higher than anything if r is not admin (both admins is considered in the 1st case)
      if (r.authority == AD) return false // admin on r, higher than anything
      
      if (this.authority == AM) return true // below admin, accmgt is higher, both accmgt is considered on case 1
      if (r.authority == AM) return false
      
      if (this.authority == OM) return true // below admin, orgman is higher, both orgmans is considered on case 1
      if (r.authority == OM) return false
      
      return true // all the other roles have the same power
   }
   
   static List coreRoles()
   {
      [AD, AM, OM, US]
   }
}
