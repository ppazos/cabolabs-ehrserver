package com.cabolabs.ehrserver.api

import grails.test.spock.IntegrationSpec

import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.User
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy

class RestControllerIntegrationSpec extends IntegrationSpec {

   def controller = new RestController()
   
   def setup()
   {
      def org = new Organization(name: 'Hospital de Clinicas 2', number: '99999').save(flush:true, failOnError:true)
      def user = new User(username:"testuser", password:"testpass", email:"e@m.com", organizations:[org]).save(flush:true, failOnError:true)
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
      // deletes the created instances
      User.findByUsername("testuser").delete()
      Organization.findByNumber("99999").delete()
   }

   /*
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
   
   void "test patient list structure"()
   {
      setup:
         def hospital = Organization.findByNumber('99999')
         
         def persons = [
            new Person(
               firstName: 'Pablo',
               lastName: 'Pazos',
               dob: new Date(81, 9, 24),
               sex: 'M',
               idCode: '4116238-0',
               idType: 'CI',
               role: 'pat',
               uid: '11111111-1111-1111-1111-111111111111',
               organizationUid: hospital.uid
            ),
            new Person(
               firstName: 'Barbara',
               lastName: 'Cardozo',
               dob: new Date(87, 2, 19),
               sex: 'F',
               idCode: '1234567-0',
               idType: 'CI',
               role: 'pat',
               uid: '22222222-1111-1111-1111-111111111111',
               organizationUid: hospital.uid
            ),
            new Person(
               firstName: 'Carlos',
               lastName: 'Cardozo',
               dob: new Date(80, 2, 20),
               sex: 'M',
               idCode: '3453455-0',
               idType: 'CI',
               role: 'pat',
               uid: '33333333-1111-1111-1111-111111111111',
               organizationUid: hospital.uid
            )
         ]
         
         persons.each { p ->
            
            if (!p.save())
            {
               println p.errors
            }
         }
         
      when:
         controller.request.method = "POST" // if GET, returns status 405
         controller.request.securityStatelessMap = [username: 'user', extradata: [organization: hospital.number, org_uid: orgUid]] // mock JWT data
         controller.params.format = 'xml'
         controller.patientList()
         
         println groovy.xml.XmlUtil.serialize( controller.response.text )
		   //response.reset()
      
      then:
         controller.response.status == 200
         !controller.response.xml.patients.isEmpty()
         controller.response.xml.patients.person.size() == 3
   }
   
   void "test ehr list structure"()
   {
      setup:
         def hospital = Organization.findByNumber('99999')
         
         def persons = [
            new Person(
               firstName: 'Pablo',
               lastName: 'Pazos',
               dob: new Date(81, 9, 24),
               sex: 'M',
               idCode: '4116238-0',
               idType: 'CI',
               role: 'pat',
               uid: '11111111-1111-1111-1111-111111111111',
               organizationUid: hospital.uid
            ),
            new Person(
               firstName: 'Barbara',
               lastName: 'Cardozo',
               dob: new Date(87, 2, 19),
               sex: 'F',
               idCode: '1234567-0',
               idType: 'CI',
               role: 'pat',
               uid: '22222222-1111-1111-1111-111111111111',
               organizationUid: hospital.uid
            ),
            new Person(
               firstName: 'Carlos',
               lastName: 'Cardozo',
               dob: new Date(80, 2, 20),
               sex: 'M',
               idCode: '3453455-0',
               idType: 'CI',
               role: 'pat',
               uid: '33333333-1111-1111-1111-111111111111',
               organizationUid: hospital.uid
            )
         ]
         
         persons.each { p ->
            
            if (!p.save())
            {
               println p.errors
            }
         }
         
         def ehr
         persons.eachWithIndex { p, i ->
        
            if (p.role == 'pat')
            {
               ehr = new Ehr(
                  subject: new PatientProxy(
                     value: p.uid
                  ),
                  organizationUid: p.organizationUid
               )
               
               if (!ehr.save()) println ehr.errors
            }
         }
         
      
      when:
         controller.request.method = "POST" // if GET, returns status 405
         controller.request.securityStatelessMap = [username: 'user', extradata: [organization: hospital.number, org_uid: orgUid]] // mock JWT data
         controller.params.format = 'xml'
         controller.ehrList()
         
         println groovy.xml.XmlUtil.serialize( controller.response.text )
		   //response.reset()
      
      then:
         controller.response.status == 200
         !controller.response.xml.ehrs.isEmpty()
         controller.response.xml.ehrs.ehr.size() == 3
   }
   */
}
