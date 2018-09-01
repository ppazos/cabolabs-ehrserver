package com.cabolabs.ehrserver.sync

class SyncClusterConfig {

   String remoteServerName
   String remoteAPIKey // the sync API key
   String remoteServerIP
   int remoteServerPort
   String remoteServerPath
   boolean isActive = false

   static constraints = {
      remoteAPIKey(size:1..1023)
   }
}
