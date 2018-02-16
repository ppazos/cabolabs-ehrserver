package com.cabolabs.ehrserver.account

import com.cabolabs.security.*

class Account {

   boolean enabled = false

   User contact
   
   List organizations = []
   static hasMany = [organizations: Organization]

   static constraints = {
   }
   
   static transients = ['activePlan']
   
   PlanAssociation getActivePlan()
   {
      Plan.active(this)
   }
}
