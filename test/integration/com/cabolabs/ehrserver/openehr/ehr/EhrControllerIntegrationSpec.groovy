package com.cabolabs.ehrserver.openehr.ehr

import grails.test.spock.IntegrationSpec

class EhrControllerIntegrationSpec extends IntegrationSpec {

   def controller
   
   def setup()
   {
      controller = new EhrController()
      
      // mock logged user
      controller.springSecurityService = [
         authentication : [
            principal: [
               username: 'admin'
            ]
         ]
      ]
   }

   def cleanup()
   {
   }

   void "test default list"()
   {
      when:
         controller.request.method = "GET"
         def model = controller.list()
      
       then:
          assert controller.response.status == 200
          //assert controller.response.list.size() == 5
          //assert controller.response.total == 5
          //println controller.modelAndView // null
          
          // there are 5 EHRs but admin has access only to 4 because
          // of organization constraints checked on the list action
          assert model.list.size() == 4
          assert model.total == 4
          //assert view == '/ehr/list'
          println controller.response.text
   }
}
