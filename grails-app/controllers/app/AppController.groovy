package app

import grails.plugin.springsecurity.annotation.Secured

class AppController {

   /**
    * Muestra escritorio del EHR Server.
    * 
    * @return
    */
   @Secured(value = ['ROLE_ADMIN'])
   def index()
   {
      //println "vvvvv dfdf sds"
   }

}