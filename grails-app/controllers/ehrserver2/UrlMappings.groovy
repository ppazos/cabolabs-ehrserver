package ehrserver2

class UrlMappings {

   static mappings = {

      // root tries to show the dashboard, if not logged in, the auth interceptor will redirect to login
      //"/"(controller:'app', action:'index')
      //"/"(uri:'/app')

      "500"(view:'/error')

      // "/"(view:"/index")
      // "500"(view:'/error')
      "404"(view:'/notFound')

      // new
      "/rest/v1/auth"(
         controller: 'restAuth',
         action: 'auth'
      )


      // REST TEMPLATE

      "/rest/v1/templates"(
         controller: 'rest',
         action: 'templates'
      )
      "/rest/v1/templates/$uid"(
         controller: 'rest',
         action: 'getTemplate'
      )


      // REST EHR

      "/rest/v1/ehrs"(
         method: 'GET',
         controller: 'rest',
         action: 'ehrList'
      )
      // /ehrs/ehrUid/xxx -- one EHR based on ehrUid partition
      "/rest/v1/ehrs/$uid"(
         method: 'GET',
         controller: 'rest',
         action: 'ehrGet'
      )
      // /ehrs/subjecUid/xxx -- one EHR based on the subjectUid partition
      "/rest/v1/ehrs/subjectUid/$subjectUid"(
         controller: 'rest',
         action: 'ehrForSubject'
      )
      "/rest/v1/ehrs"(
         method: 'POST',
         controller: 'rest',
         action: 'ehrCreate'
      )


      // REST COMPOSITIONS

      "/rest/v1/compositions"(
         method: 'GET',
         controller: 'rest',
         action: 'listCompositions'
      )
      "/rest/v1/compositions/$uid"(
         controller: 'rest',
         action: 'getComposition'
      )
      "/rest/v1/ehrs/$ehrUid/compositions"(
         method: 'POST',
         controller: 'rest',
         action: 'commit'
      )


      // REST CONTRIBUTION

      // /contributions?ehrUid to GET /ehrs/{ehdUid}/contributions
      "/rest/v1/ehrs/$ehrUid/contributions"(
         controller: 'rest',
         action: 'contributions'
      )


      "/rest/v1/organizations"(
         method: 'GET',
         controller: 'rest',
         action: 'organizations'
      )
      "/rest/v1/users"(
         method: 'GET',
         controller: 'user',
         action: 'get_users'
      )
      "/rest/v1/users/$username"(
         method: 'GET',
         controller: 'user',
         action: 'profile'
      )
      "/rest/v1/users"(
         method: 'POST',
         controller: 'rest',
         action: 'userRegister'
      )


      // REST QUERY

      // execute given query (not stored)
      "/rest/v1/query/composition/execute"(
         method: 'POST',
         controller: 'rest',
         action: 'executedNotStoredCompositionQuery'
      )

      "/rest/v1/queries"(
         controller: 'rest',
         action: 'queryList'
      )
      // /queries/xxx -- one query based on queryUid partition
      "/rest/v1/queries/$queryUid"(
         controller: 'rest',
         action: 'queryShow'
      )
      // executes query xxx, each query knows its own type, this can be a queryData or a queryComposition
      "/rest/v1/queries/$queryUid/execute"(
         controller: 'rest',
         action: 'query'
      )
      "/rest/v1/multiqueries"(
         method: 'GET',
         controller: 'rest',
         action: 'getEhrQueries'
      )
      "/rest/v1/multiqueries/$ehrQueryUid/ehrcheck/$ehrUid"(
         method: 'GET',
         controller: 'rest',
         action: 'ehrChecker'
      )
      "/rest/v1/multiqueries/$ehrQueryUid/ehrs"(
         method: 'GET',
         controller: 'rest',
         action: 'getMatchingEhrs'
      )


      "/rest/**"(
         controller: 'rest',
         action: 'notFound'
      )


      // management API
      "/mgt/v1/stats"(
         controller: 'stats',
         action: 'stats'
      )

      /**
       * Use UIDs on webconsole show/edit
       */

      "/operationalTemplate/index" {
         controller = 'operationalTemplate'
         action = 'index'
         deleted = false
      }
      "/operationalTemplate/trash" {
         controller = 'operationalTemplate'
         action = 'index'
         deleted = true
      }

      "/organization/show/$uid"(
         method: 'GET',
         controller: 'organization',
         action: 'show'
      )
      "/organization/edit/$uid"(
         method: 'GET',
         controller: 'organization',
         action: 'edit'
      )
      "/organization/update/$uid"(
         method: 'PUT',
         controller: 'organization',
         action: 'update'
      )

      "/logs/show/$id" {
         controller = 'activityLog'
         action = 'show'
      }
      "/logs/index" {
         controller = 'activityLog'
         action = 'index'
      }

/*
      "/api/v1/login"(
         method: 'POST',
         controller: 'rest',
         action: 'login'
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
      */

      "/$controller/$action?/$id?(.$format)?"{
         constraints {
            // apply constraints here
         }
      }
   }
}
