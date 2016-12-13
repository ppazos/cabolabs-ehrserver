package com.cabolabs.ehrserver.account

import com.cabolabs.security.User
import com.cabolabs.security.Organization

class ApiKey {

   String token // auth token for the virtual user
   User user // virtual user generated for this key
   Organization organization // the key belongs to this org

   static constraints = {
      token(size:1..1023)
   }
}
