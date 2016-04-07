class UrlMappings {

   static mappings = {

      "/"(
         controller: 'login' // auth or if the user is logged in /app/index
      )
      
      "500"(view:'/error')

      "/rest/profile/${username}"(
         controller: 'user',
         action: 'profile'
      )
      
      "/rest/person"(
         controller: 'rest',
         action: 'createPerson'
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
      "/rest/ehrs/ehrUid/$ehrUid"(
         controller: 'rest',
         action: 'ehrGet'
      )
      // /rest/ehrs/subjecUid/xxx -- one EHR based on the subjectUid partition
      "/rest/ehrs/subjectUid/$subjectUid"(
         controller: 'rest',
         action: 'ehrForSubject'
      )

      // /rest/patientList por /rest/patients -- list of all patients
      "/rest/patients"(
         controller: 'rest',
         action: 'patientList'
      )
      "/rest/patients/$uid"( 
         controller: 'rest',
         action: 'patient'
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
