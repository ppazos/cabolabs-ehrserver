package com.cabolabs.ehrserver.sync

class SyncLog {

   String resourceType // domain class synced, e.g. Contribution
   String resourceUid // uid or id to string of the domain class
   Date resourceLastUpdated // to know if the resource is dirty
   SyncClusterConfig remote
   Date dateCreated

   static constraints = {
      resourceLastUpdated nullable:true /* Contributions can't be updated so they don't have lastUpdated */
   }
}
