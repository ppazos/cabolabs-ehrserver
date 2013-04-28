class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

<<<<<<< HEAD
		//"/"(view:"/index")
      "/"( // por defecto va al escritorio del EHR Server
         controller: 'app',
         action: 'index'
      )
=======
		"/"(view:"/index")
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
		"500"(view:'/error')
	}
}
