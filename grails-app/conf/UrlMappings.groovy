class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		//"/"(view:"/index")
    /*
        "/"( // por defecto va al escritorio del EHR Server
            controller: 'app',
            action: 'index'
        )
		
		
		"500"(view:'/error')

		//	/rest/ehrs -- list of all EHRs
		"/rest/ehrs"( 
            controller: 'rest',
            action: 'ehrList'
        )

		//	/rest/ehrs/ehrUid/xxx -- one EHR based on ehrUid partition
		"/rest/ehrs/ehrUid/$ehrUid"( 
            controller: 'rest',
            action: 'ehrGet'
        )
		//	/rest/ehrs/subjecUid/xxx -- one EHR based on the subjectUid partition
		"/rest/ehrs/subjecUid/$subjectUid"( 
            controller: 'rest',
            action: 'ehrForSubject'
        )

		//	/rest/patientList por /rest/patients -- list of all patients
		"/rest/patients"( 
            controller: 'rest',
            action: 'patientList'
        )
		
		//	/rest/queryList por /rest/queries -- list of all queries
		"/rest/queries"( 
            controller: 'rest',
            action: 'queryList'
        )

		//	/rest/queries/queryUid/xxx -- one query based on queryUid partition
		"/rest/queries/queryUid/$queryUid"( 
            controller: 'rest',
            action: 'queryShow'
        )
		
		// executes query xxx, each query knows its own type, this can be a queryData or a queryComposition
		"/rest/queries/queryUid/$queryUid/execute"( 
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
	}
}