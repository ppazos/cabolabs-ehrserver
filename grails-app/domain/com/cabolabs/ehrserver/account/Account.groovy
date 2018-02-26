package com.cabolabs.ehrserver.account

import com.cabolabs.security.*

class Account {

   boolean enabled = false

   User contact

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

   static transients = ['activePlan', 'totalRepoSizeInKb']

   PlanAssociation getActivePlan()
   {
      Plan.active(this)
   }

   long getTotalRepoSizeInKb()
   {
      (current_version_repo_size + current_opt_repo_size) / 1024
   }
}
