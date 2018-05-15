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

      "/organization/show/$uid"(
         controller: 'organization',
         action: 'show'
      )
      "/organization/edit/$uid"(
         controller: 'organization',
         action: 'edit'
      )
      "/organization/update/$uid"(
         controller: 'organization',
         action: 'update'
      )

/*
      "/operationalTemplate/trash"(
         controller: 'operationalTemplate',
         action: 'list',
         deleted: true
      )
      */
      "/operationalTemplate/list" {
         controller = 'operationalTemplate'
         action = 'list'
         deleted = false
      }
      "/operationalTemplate/trash" {
         controller = 'operationalTemplate'
         action = 'list'
         deleted = true
      }

      "/logs/show/$id" {
         controller = 'activityLog'
         action = 'show'
      }
      name logs: "/logs" {
         controller = 'activityLog'
         action = 'index'
      }

      "/api/v1/login"(
         method: 'POST',
         controller: 'rest',
         action: 'login'
      )
      "/api/v1/organizations"(
         method: 'GET',
         controller: 'rest',
         action: 'organizations'
      )
      "/api/v1/users/$username"(
         method: 'GET',
         controller: 'user',
         action: 'profile'
      )
      "/api/v1/users"(
         method: 'POST',
         controller: 'rest',
         action: 'userRegister'
      )

      "/api/v1/ehrs"(
         controller: 'rest',
         action: 'ehrList'
      )
      // /ehrs/ehrUid/xxx -- one EHR based on ehrUid partition
      "/api/v1/ehrs/$uid"(
         method: 'GET',
         controller: 'rest',
         action: 'ehrGet'
      )
      // /ehrs/subjecUid/xxx -- one EHR based on the subjectUid partition
      "/api/v1/ehrs/subjectUid/$subjectUid"(
         controller: 'rest',
         action: 'ehrForSubject'
      )
      "/api/v1/ehrs"(
         method: 'POST',
         controller: 'rest',
         action: 'ehrCreate'
      )
      /*
      "/api/v1/contributions"(
         controller: 'rest',
         action: 'contributions'
      )
      */
      // /contributions?ehrUid to GET /ehrs/{ehdUid}/contributions
      "/api/v1/ehrs/$ehrUid/contributions"(
         controller: 'rest',
         action: 'contributions'
      )
      "/api/v1/ehrs/$ehrUid/compositions"(
         method: 'POST',
         controller: 'rest',
         action: 'commit'
      )

      "/api/v1/compositions"(
         method: 'GET',
         controller: 'rest',
         action: 'findCompositions'
      )
      "/api/v1/compositions/$uid"(
         controller: 'rest',
         action: 'getComposition'
      )

      // execute given query (not stored)
      "/api/v1/query/composition/execute"(
         method: 'POST',
         controller: 'rest',
         action: 'executedNotStoredCompositionQuery'
      )

      "/api/v1/queries"(
         controller: 'rest',
         action: 'queryList'
      )
      // /queries/xxx -- one query based on queryUid partition
      "/api/v1/queries/$queryUid"(
         controller: 'rest',
         action: 'queryShow'
      )
      // executes query xxx, each query knows its own type, this can be a queryData or a queryComposition
      "/api/v1/queries/$queryUid/execute"(
         controller: 'rest',
         action: 'query'
      )
      // /queries/name/this.is.a.name -- list queries which name is equal to "this.is.a.name" (spaces transformed into ".")
      "/api/v1/queries/queryName/$queryName"(
         controller: 'rest',
         action: 'queryList'
      )

      // /queries/descriptionContains/a.text.to.find -- list of queries that contains the "a.text.to.find" text ("description" partition with "contains" criteria)
      "/api/v1/queries/descriptionContains/$descriptionContains"(
         controller: 'rest',
         action: 'queryList'
      )
      "/api/v1/ehrs/$ehrUid/compositions/$compositionUid/checkout"(
         controller: 'rest',
         action: 'checkout'
      )

      "/api/v1/templates"(
         controller: 'rest',
         action: 'templates'
      )
      "/api/v1/templates/$uid"(
         controller: 'rest',
         action: 'getTemplate'
      )

      // TODO: fancy restful urls
      "/api/v1/getEhrQueries"(
         method: 'GET',
         controller: 'rest',
         action: 'getEhrQueries'
      )
      "/api/v1/ehrChecker"(
         method: 'GET',
         controller: 'rest',
         action: 'ehrChecker'
      )
      "/api/v1/getMatchingEhrs"(
         method: 'GET',
         controller: 'rest',
         action: 'getMatchingEhrs'
      )

      // This is to grab wrong requested URLs to the API and return 404
      // https://github.com/ppazos/cabolabs-ehrserver/issues/901
      "/api/v1/**"(
         controller: 'rest'
      )

      // management API
      "/mgt/v1/stats/$username"(
         controller: 'stats',
         action: 'userAccountStats'
      )

      "/$controller/$action?/$id?"{
         constraints {
            // apply constraints here
         }
      }
   }
}
