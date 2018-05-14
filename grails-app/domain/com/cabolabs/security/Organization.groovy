/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.security

import com.cabolabs.ehrserver.account.Account

class Organization {

   String uid = java.util.UUID.randomUUID() as String
   String name
   String number // identifier of the organization to be used for user registration
   String preferredLanguage

   Date dateCreated
   Date lastUpdated

   static constraints = {
      number nullable: true, unique: true
      preferredLanguage nullable: true
   }

   static belongsTo = [account: Account]


   def beforeInsert()
   {
      if (!this.number) assignNumber()
   }

   def beforeUpdate()
   {
      if (!this.number) assignNumber()
   }

   private void assignNumber()
   {
      def _number = String.randomNumeric(6)

      while (Organization.countByNumber(_number) == 1) // avoids repeated number
      {
      println _number
         _number = String.randomNumeric(6)
      }

      this.number = _number
   }

   @Override
   boolean equals(other) {
      is(other) || (other instanceof Organization && other.id == this.id)
   }

   // this is needed so the map with key org used in UserController.update to assign roles works OK.
   @Override
   public int hashCode() {
      this.id.intValue()
   }
}
