package com.cabolabs.ehrserver.account

import com.cabolabs.security.*

class Account {

   String uid = java.util.UUID.randomUUID() as String

   String companyName
   boolean enabled = false

   User contact

   // sync
   boolean master = true
   Date dateCreated
   Date lastUpdated

   List organizations = []
   static hasMany = [organizations: Organization]

   // values in bytes
   // this values are calculated from time to time and cached here for quick access
   // to avoid querying the file system each time the size is needed, so these
   // might be out of sync between calculations, that's OK.
   long current_version_repo_size = 0
   long current_opt_repo_size = 0


   static constraints = {
   }

   static mapping = {
      master column:'sync_master'
      organizations lazy:false
   }

   static transients = ['currentPlan', 'totalRepoSizeInKb', 'allPlans']

   PlanAssociation getCurrentPlan()
   {
      Plan.associatedNow(this)
   }

   List getAllPlans()
   {
      PlanAssociation.findAllByAccount(this, [sort:'id', order:'desc'])
   }

   // Result is in kB = 1000 bytes
   long getTotalRepoSizeInKb()
   {
      (current_version_repo_size + current_opt_repo_size) / 1000
   }
}
