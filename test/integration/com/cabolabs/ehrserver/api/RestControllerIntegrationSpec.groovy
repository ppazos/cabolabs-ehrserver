package com.cabolabs.ehrserver.api

import grails.test.spock.IntegrationSpec

import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.User

class RestControllerIntegrationSpec extends IntegrationSpec {

   def controller = new RestController()
   
   def setup()
   {
      def org = new Organization(name: 'Hospital de Clinicas 2', number: '99999').save(flush:true, failOnError:true)
      def user = new User(username:"user", password:"pass", email:"e@m.com", organizations:[org]).save(flush:true, failOnError:true)
      /*
      controller.springSecurityService = [
        encodePassword: 'admin',
        reauthenticate: { String u -> true},
        loggedIn: true,
        principal: loggedInUser
      ]
      // without this the index action fails
      SpringSecurityUtils.metaClass.static.ifAllGranted = { String role ->
         return true
      }
      */
   }

   def cleanup()
   {
   }

   
   def createPersonPopulateValidParams(params) {
      assert params != null

      params["firstName"] = 'Pablo'
      params["lastName"] = 'Pazos'
      params["dob"] = '19811024'
      params["sex"] = 'M'
      params["idCode"] = '34534534'
      params["idType"] = 'CI'
      params["role"] = 'pat'
      params["organizationUid"] = Organization.findByNumber('99999').uid
      params["createEhr"] = false
      params["format"] = null // xml or json
  }
   
   void "test create person with no data"()
   {
      //setup:
      //   println "setup"
         //def controller = new RestController()
          
      when:
         controller.request.method = "POST" // if GET, returns status 405
         controller.response.format = 'json' // this is the only thing that works with withFormat! for the response output.
                                             // TODO: check if this defines the Accept header of the HTTP Request.
         controller.createPerson()
          
      then:
         println controller.response.status
         controller.response.status == 400
         println controller.response.text
         //controller.response.text == 'organizationUid required'
         //controller.response.format == 'json'
         controller.response.contentType.startsWith('application/json')
   }
    
   // TODO test transactionality
    
    
   void "test create person with data but no organizationUID"()
   {
      //setup:
         
      when:
         controller.request.method = "POST" // if GET, returns status 405
         controller.request.securityStatelessMap = [username: 'user'] // mock JWT data
         
         createPersonPopulateValidParams(controller.params)
         
         controller.params.organizationUid = null // test with no orgUID
         controller.createPerson()
      
      then:
         controller.response.status == 400
   }
   
   void "test create person with correct data without EHR"()
   {
      //setup:
         
      when:
         controller.request.method = "POST" // if GET, returns status 405
         controller.request.securityStatelessMap = [username: 'user'] // mock JWT data
         createPersonPopulateValidParams(controller.params)
         controller.createPerson()
      
      then:
         controller.response.status == 200
   }
   
   void "test create person with correct data with EHR"()
   {
      //setup:
         
      when:
         def countEHRs = Ehr.count()
         controller.request.method = "POST" // if GET, returns status 405
         controller.request.securityStatelessMap = [username: 'user'] // mock JWT data
         createPersonPopulateValidParams(controller.params)
         controller.params.createEhr = true
         controller.createPerson()
      
      then:
         controller.response.status == 200
         Ehr.count() == countEHRs + 1
   }
}
