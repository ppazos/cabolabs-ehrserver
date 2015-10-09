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
    */
    "/"(
       controller: 'login' // auth or if the user is logged in /app/index
    )
    
    "500"(view:'/error')
  }
}