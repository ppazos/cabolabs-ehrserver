package com.cabolabs.security

class Role implements Serializable {

	private static final long serialVersionUID = 1

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
      if (this.authority == 'ROLE_ADMIN') return true // admins is higher than anything if r is not admin (both admins is considered in the 1st case)
      if (r.authority == 'ROLE_ADMIN') return false // admin on r, higher than anything
      if (this.authority == 'ROLE_ORG_MANAGER') return true // below admin, orgman is higher, both orgmans is considered on case 1
      if (r.authority == 'ROLE_ORG_MANAGER') return false
      return true // all the other roles have the same power
   }
}
