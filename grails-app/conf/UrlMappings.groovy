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

class UrlMappings {

   static mappings = {

      "/"(
         controller: 'login' // auth or if the user is logged in /app/index
      )
      
      "500"(view:'/error')

      /**
       * Use UIDs on webconsole show/edit
       */
      
      "/organization/show/${uid}"(
         controller: 'organization',
         action: 'show'
      )
      "/organization/edit/${uid}"(
         controller: 'organization',
         action: 'edit'
      )
      "/organization/update/${uid}"(
         controller: 'organization',
         action: 'update'
      )
      
      
      "/rest/users/${username}"(
         controller: 'user',
         action: 'profile'
      )
      
      // /rest/ehrs -- list of all EHRs
      "/rest/ehrs"(
         controller: 'rest',
         action: 'ehrList'
      )
      
      "/rest/compositions"(
         controller: 'rest',
         action: 'findCompositions'
      )
      
      "/rest/compositions/$uid"(
         controller: 'rest',
         action: 'getComposition'
      )

      // /rest/ehrs/ehrUid/xxx -- one EHR based on ehrUid partition
      "/rest/ehrs/ehrUid/$uid"(
         controller: 'rest',
         action: 'ehrGet'
      )
      // /rest/ehrs/subjecUid/xxx -- one EHR based on the subjectUid partition
      "/rest/ehrs/subjectUid/$subjectUid"(
         controller: 'rest',
         action: 'ehrForSubject'
      )
      "/rest/ehrs"(
         method: 'POST',
         controller: 'rest',
         action: 'ehrCreate'
      )
      
      "/rest/users"(
         method: 'POST',
         controller: 'rest',
         action: 'userRegister'
      )
      
      // /rest/queryList por /rest/queries -- list of all queries
      "/rest/queries"( 
         controller: 'rest',
         action: 'queryList'
      )

      // /rest/queries/xxx -- one query based on queryUid partition
      "/rest/queries/$queryUid"(
         controller: 'rest',
         action: 'queryShow'
      )
      
      // executes query xxx, each query knows its own type, this can be a queryData or a queryComposition
      "/rest/queries/$queryUid/execute"(
         controller: 'rest',
         action: 'query'
      )

      // /rest/queries/name/this.is.a.name -- list queries which name is equal to "this.is.a.name" (spaces transformed into ".")
      "/rest/queries/queryName/$queryName"(
         controller: 'rest',
         action: 'queryList'
      )

      // por /rest/queries/descriptionContains/a.text.to.find -- list of queries that contains the "a.text.to.find" text ("description" partition with "contains" criteria)
      "/rest/queries/descriptionContains/$descriptionContains"(
         controller: 'rest',
         action: 'queryList'
      )

      "/$controller/$action?/$id?"{
         constraints {
            // apply constraints here
         }
      }
   }
}
