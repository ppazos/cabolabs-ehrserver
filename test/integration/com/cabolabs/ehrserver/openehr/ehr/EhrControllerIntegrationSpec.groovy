package com.cabolabs.ehrserver.openehr.ehr

import grails.test.spock.IntegrationSpec
import com.cabolabs.security.Organization

class EhrControllerIntegrationSpec extends IntegrationSpec {

   def controller
   def messageSource
   
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
          //assert view == '/ehr/list' // view not defined
          println controller.response.text
   }
   
   void "test filtered list"()
   {
      when:
         controller.request.method = "GET"
         controller.params.uid = "22222" // only one EHR with UID that contains 22222
         def model = controller.list()
      
       then:
          assert controller.response.status == 200
          assert model.list.size() == 1
          assert model.total == 1

          println controller.response.text
   }
   
   void "test show without UID"()
   {
      when:
         controller.request.method = "GET"
         //controller.params.uid = "22222" // only one EHR with UID that contains 22222
         def model = controller.show()
      
       then:
          assert controller.response.status == 302 // makes a redirect to the page that triggered the show without the uid
          assert controller.flash.message == messageSource.getMessage('ehr.show.uidIsRequired', null, controller.request.getLocale())
   }
   
   void "test show non existing UID"()
   {
      when:
         controller.request.method = "GET"
         controller.params.uid = "1234"
         def model = controller.show()
      
       then:
          assert controller.response.status == 302 // makes a redirect to the page that triggered the show without the uid
          assert controller.flash.message == messageSource.getMessage('ehr.show.ehrDoesntExistsForUid', ['1234'] as Object[], controller.request.getLocale())
   }
   
   void "test show existing UID"()
   {
      when:
         controller.request.method = "GET"
         controller.params.uid = "11111111-1111-1111-1111-111111111111"
         controller.show()
      
      then:
         assert controller.response.status == 200 // makes a redirect to the page that triggered the show without the uid
         // The model is in modelAndView because show uses render instead of returning the model map directly
         // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/ModelAndView.html
         assert controller.modelAndView.model.ehr.uid == '11111111-1111-1111-1111-111111111111'
   }
   
   void "test ehrContributions no contributions"()
   {
      when:
         controller.request.method = "GET"
         controller.params.id = 1
         controller.ehrContributions() // result and modelAndView is null because it renders a template not a view...
      
      then:
         assert controller.response.status == 200 // makes a redirect to the page that triggered the show without the uid
         //println controller.modelAndView // modelAndView is empty because the render is for a template not a view
         // there is a workaround http://stackoverflow.com/questions/15141319/grails-controller-test-making-assertions-about-model-when-rendering-a-template
         //assert controller.modelAndView.model.contributions == []
   }
   
   // TODO: test ehrContributions with contributions
   
   
   void "test create"()
   {
      when:
         controller.request.method = "GET"
         //controller.params.id = 1
         def model = controller.create()
      
      then:
         assert controller.response.status == 200 // makes a redirect to the page that triggered the show without the uid
         // The model is in modelAndView because show uses render instead of returning the model map directly
         assert model.ehr
   }
   
   void "test save success"()
   {
      when:
         controller.request.method = "POST"
         controller.params.organizationUid = Organization.get(1).uid
         controller.params['subject.value'] = '12345678'
         def count1 = Ehr.count()
         def model = controller.save()
         def count2 = Ehr.count()
         
      then:
         assert count1 + 1 == count2
         println controller.flash.message // FIXME: it is not showing the UID of the new EHR
         assert controller.response.status == 302 // makes a redirect to the page that triggered the show without the uid
         assert controller.flash.message == messageSource.getMessage('ehr.save.ok', [Ehr.last().uid] as Object[], controller.request.getLocale())

   }
}
