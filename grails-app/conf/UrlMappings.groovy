class UrlMappings {

   static mappings = {

      "/"(
         controller: 'login' // auth or if the user is logged in /app/index
      )
      
      "500"(view:'/error')

      /**
       * Use UIDs on webconsole show/edit
       */
      "/person/show/${uid}"(
         controller: 'person',
         action: 'show'
      )
      "/person/edit/${uid}"(
         controller: 'person',
         action: 'edit'
      )
      "/person/update/${uid}"(
         controller: 'person',
         action: 'update'
      )
      "/person/delete/${uid}"(
         controller: 'person',
         action: 'delete'
      )
      
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
      
      
      "/rest/profile/${username}"(
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
      "/rest/patients/$uid?"(
         controller: 'rest',
         action: 'patientList',
         method: 'GET'
      )
      "/rest/patients"(
         controller: 'rest',
         action: 'createPerson',
         method: 'POST'
      )/*          
      "/rest/patients/$uid"( 
         controller: 'rest',
         action: 'patient',
         method: 'GET'
      )*/
      "/rest/person"( /* FIXME: we should have one url for creating people, now we have also /patients ^ */
         controller: 'rest',
         action: 'createPerson',
         method: 'POST'
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
