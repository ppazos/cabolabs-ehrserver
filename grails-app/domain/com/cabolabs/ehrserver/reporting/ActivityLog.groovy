package com.cabolabs.ehrserver.reporting

class ActivityLog {

   Date timestamp = new Date()
   String username
   String action
   Long objectId    // when using db ids (we try to avoid this case)
   String objectUid // most ids will be uids
   String clientIp
   
   static constraints = {
      username nullable: true
      objectId nullable: true
      objectUid nullable: true
   }
}
