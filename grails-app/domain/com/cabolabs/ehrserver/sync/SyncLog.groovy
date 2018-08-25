package com.cabolabs.ehrserver.sync

class SyncLog {

   String resourceType // domain class synced, e.g. Contribution
   String resourceUid // uid or id to string of the domain class
   SyncClusterConfig remote
   Date dateCreated

   static constraints = {
   }
}
