class UrlMappings {

  static mappings = {
    "/$controller/$action?/$id?"{
      constraints {
        // apply constraints here
      }
    }

    //"/"(view:"/index")
    "/"( // por defecto va al escritorio del EHR Server
      controller: 'app',
      action: 'index'
    )
    
    "500"(view:'/error')
  }
}