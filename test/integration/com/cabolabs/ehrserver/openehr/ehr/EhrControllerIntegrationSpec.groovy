package com.cabolabs.ehrserver.openehr.ehr

import grails.test.spock.IntegrationSpec

import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.security.*
import com.cabolabs.ehrserver.account.*

class EhrControllerIntegrationSpec extends IntegrationSpec {

   EhrController controller = new EhrController()

   private static String PS  = System.getProperty("file.separator")
   private String ehrUid     = '11111111-1111-1111-1111-111111111123'
   private String patientUid = '11111111-1111-1111-1111-111111111145'
   private String orgUid     = '11111111-1111-1111-1111-111111111178'

   private createAdmin()
   {
      println "createAdmin"
      def user = new User(
         username: 'testadmin',
         password: 'testadmin',
         email: 'testadmin@domain.com'
      ).save(failOnError:true, flush: true)

      def adminRole = new Role(authority: Role.AD).save(failOnError: true, flush: true)

      UserRole.create( user, adminRole, Organization.findByUid(orgUid), true )
   }

   private createEHR()
   {
      println "createEHR"
      def ehr = new Ehr(
         uid: ehrUid, // the ehr id is the same as the patient just to simplify testing
         subject: new PatientProxy(
            value: patientUid
         ),
         organizationUid: Organization.findByUid(orgUid).uid
      )

      ehr.save(failOnError: true)
   }

   def setup()
   {
      // 1. Account setup: create account manager user
      def accman = new User(
         username: 'testaccman',
         password: 'testaccman',
         email: 'testaccman@domain.com',
      ).save(failOnError:true, flush: true)

      // 2. Account setup: create account
      def account = new Account(contact: accman, companyName:'Test company')

      // 3. Account setup: create organization
      def org = new Organization(uid: orgUid, name: 'CaboLabs', number: '123456')
      account.addToOrganizations(org)
      account.save(failOnError: true) // saves the org

      // 4. Account setup: create ACCMAN role
      def accmanRole = new Role(authority: Role.AM).save(failOnError: true, flush: true)

      // 5. Account setup: create user role association
      UserRole.create( accman, accmanRole, org, true )



      createEHR()
      createAdmin()


      // message in controllers return always the code, useful for testing
      controller.metaClass.message = {args ->
        //println args // keys: code (M), default (O), args (O)
        args.code
      }

      // mock logged user
      // TODO: check why for some reason this user is an admin but the SpringSecurityUtils on the controller list says its not.
      controller.springSecurityService = [
         authentication : [
            principal: [
               username: 'testadmin'
            ]
         ]
      ]

      // mock configurationService and ConfigurationItem
      controller.configurationService = [
         getValue: { key -> if (key == 'ehrserver.console.lists.max_items' ) return 20 }
      ]
   }

   def cleanup()
   {
   /*
      def user = User.findByUsername("testadmin")
      def role = Role.findByAuthority(Role.AD)
      def org = Organization.findByUid(orgUid)

      UserRole.remove(user, role, org)
      user.delete(flush: true)
      */
      def ehr = Ehr.findByUid(ehrUid)
      ehr.delete(flush: true)

 //     org.delete(flush: true)

      Account.list()*.delete() // should delete the orgs

      UserRole.list()*.delete()
      User.list()*.delete()
      Role.list()*.delete()
   }

   /*
    * try this for controller.list with role admin...
    *
    * SpringSecurityUtils.doWithAuth('superuser') {
         controller.save()
      }
    */

   void "test default list"()
   {
      println "1"
      when:
         controller.request.method = "GET"
         def model = controller.list()
         println model
         println "ehrs "+ Ehr.count()
         println Ehr.findByUid(ehrUid).deleted

       then:
          assert controller.response.status == 200
          //assert controller.response.list.size() == 5
          //assert controller.response.total == 5
          //println controller.modelAndView // null

          // there are 5 EHRs but admin has access only to 4 because
          // of organization constraints checked on the list action
          assert model.list.size() == 1
          assert model.total == 1
          //assert view == '/ehr/list' // view not defined
          println controller.response.text
   }

   void "test filtered list"()
   {
      println "2"
      when:
         controller.request.method = "GET"
         controller.params.uid = "123" // only one EHR with UID that contains 123
         def model = controller.list()

       then:
          assert controller.response.status == 200
          assert model.list.size() == 1
          assert model.total == 1

          println controller.response.text
   }

   void "test show without UID"()
   {
      println "3"
      when:
         controller.request.method = "GET"
         def model = controller.show()

       then:
          assert controller.response.status == 302 // makes a redirect to the page that triggered the show without the uid
          assert controller.flash.message == 'ehr.show.uidIsRequired'
   }

   void "test show non existing UID"()
   {
      println "4"
      when:
         controller.request.method = "GET"
         controller.params.uid = '1234542324'
         def model = controller.show()

       then:
          assert controller.response.status == 302 // makes a redirect to the page that triggered the show without the uid
          assert controller.flash.message == 'ehr.show.ehrDoesntExistsForUid' //messageSource.getMessage('ehr.show.ehrDoesntExistsForUid', ['1234542324'] as Object[], controller.request.getLocale())
   }

   void "test show existing UID"()
   {
      when:
         controller.request.method = "GET"
         controller.params.uid = ehrUid
         controller.show()

      then:
         assert controller.response.status == 200 // makes a redirect to the page that triggered the show without the uid
         // The model is in modelAndView because show uses render instead of returning the model map directly
         // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/ModelAndView.html
         assert controller.modelAndView.model.ehr.uid == ehrUid
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

   /* removed, the test is OK but the @transactional in the save action gives with NullPointerException
   void "test save without params"()
   {
      when:
         controller.request.method = "POST"
         controller.save()

      then:
         controller.response.status == 200
   }

   void "test save success"()
   {
      when:
         controller.request.method = "POST"
         //controller.request.contentType = FORM_CONTENT_TYPE

         //controller.params.organizationUid = Organization.get(1).uid
         //controller.params['subject.value'] = '12345678'

         //def e = new Ehr([organizationUid: Organization.get(1).uid, 'subject.value': '12345678'])

         def e = new Ehr(
            subject: new PatientProxy(
               value: '12345678'
            ),
            organizationUid: Organization.get(1).uid
         )

         //println "valid ehr "+ e.validate() +" "+ e.errors

         //controller.params.ehr = e

         def count1 = Ehr.count()
         //def model = controller.save()

         try {
            controller.params.ehr = e
            controller.save(e)
         } catch (ex) {
            println ex // java.lang.NullPointerException
            //println ex.message // null
            //println ex.getCause()?.message // null
         }

         def count2 = Ehr.count()

      then:
         assert count1 + 1 == count2
         println controller.flash.message // FIXME: it is not showing the UID of the new EHR
         assert controller.response.status == 302 // makes a redirect to the page that triggered the show without the uid
         assert controller.flash.message == 'ehr.save.ok' //messageSource.getMessage('ehr.save.ok', [Ehr.last().uid] as Object[], controller.request.getLocale())
   }
   */
}
