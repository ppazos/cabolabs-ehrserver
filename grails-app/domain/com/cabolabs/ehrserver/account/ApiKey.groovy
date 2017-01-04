package com.cabolabs.ehrserver.account

import com.cabolabs.security.User
import com.cabolabs.security.Organization

class ApiKey {

   String token // auth token for the virtual user
   User user // virtual user generated for this key
   Organization organization // the key belongs to this org
   String systemId // name or id of the external system that uses the apikey, used on version_uid.creating_system_id

   static constraints = {
      token(size:1..1023)
   }
}
