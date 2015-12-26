import demographic.Person
import common.generic.PatientProxy
import ehr.clinical_documents.IndexDefinition
import grails.util.Holders

import com.cabolabs.security.RequestMap
import com.cabolabs.security.User
import com.cabolabs.security.Role
import com.cabolabs.security.UserRole
import com.cabolabs.security.Organization

import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils

import com.cabolabs.ehrserver.identification.PersonIdType
import com.cabolabs.ehrserver.openehr.ehr.Ehr;
import com.cabolabs.openehr.opt.manager.OptManager // load opts

class BootStrap {

   private static String PS = System.getProperty("file.separator")
   
   def mailService
   def grailsApplication
   
   def init = { servletContext ->
     
      // test
      /*
      mailService.sendMail {
        from grailsApplication.config.grails.mail.username //"pablo.pazos@cabolabs.com"
        to "pablo.pazos@cabolabs.com"     
        subject "Hello EHRServer user!" 
        body 'How are you?' 
      }
      */
      
     // Used by query builder, all return String
     String.metaClass.asSQLValue = { operand ->
        if (operand == 'contains') return "'%"+ delegate +"%'" // Contains is translated to LIKE, we need the %
        return "'"+ delegate +"'"
     }
     Double.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     Integer.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     Long.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     Date.metaClass.asSQLValue = { operand ->
        def formatterDateDB = new java.text.SimpleDateFormat( Holders.config.app.l10n.db_datetime_format )
        return "'"+ formatterDateDB.format( delegate ) +"'" 
     }
     Boolean.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     
     // call String.randomNumeric(5)
     String.metaClass.static.randomNumeric = { digits ->
        def alphabet = ['0','1','2','3','4','5','6','7','8','9']
        new Random().with {
          (1..digits).collect { alphabet[ nextInt( alphabet.size() ) ] }.join()
        }
     }
     
     if (PersonIdType.count() == 0)
     {
        def idtypes = [
           new PersonIdType(name:'DNI',  code:'DNI'),
           new PersonIdType(name:'CI',   code:'CI'),
           new PersonIdType(name:'Passport', code:'Passport'),
           new PersonIdType(name:'SSN',  code:'SSN'),
           new PersonIdType(name:'UUID', code:'UUID'),
           new PersonIdType(name:'OID',  code:'OID')
        ]
        idtypes.each {
           it.save(failOnError:true, flush:true)
        }
     }
     
     
     
     //****** SECURITY *******
     
     // Register custom auth filter
     // ref: https://objectpartners.com/2013/07/11/custom-authentication-with-the-grails-spring-security-core-plugin/
     // See 'authFilter' in grails-app/conf/spring/resources.groovy
     // ref: http://grails-plugins.github.io/grails-spring-security-core/guide/filters.html
     SpringSecurityUtils.clientRegisterFilter('authFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)
     
     //println "configured filters "+ SpringSecurityUtils.configuredOrderedFilters
     
     def organizations = []
     if (Organization.count() == 0)
     {
        // Sample organizations
        organizations << new Organization(name: 'Hospital de Clinicas', number: '1234')
        organizations << new Organization(name: 'Clinica del Tratamiento del Dolor', number: '6666')
        organizations << new Organization(name: 'Cirugia Estetica', number: '5555')
        
        organizations.each {
           it.save(failOnError:true, flush:true)
        }
     }
     else organizations = Organization.list()
     
     if (RequestMap.count() == 0)
     {
        for (String url in [
         '/', // redirects to login, see UrlMappings
         '/error', '/index', '/index.gsp', '/**/favicon.ico', '/shutdown',
         '/assets/**', '/**/js/**', '/**/css/**', '/**/images/**', '/**/fonts/**',
         '/login', '/login.*', '/login/*',
         '/logout', '/logout.*', '/logout/*',
         '/user/register', '/user/resetPassword',
         '/simpleCaptcha/**',
         '/j_spring_security_logout',
         '/rest/**',
         '/test/findCompositions', // will be refactores to /rest
         '/ehr/showCompositionUI', // will be added as a rest service via url mapping
         '/user/profile'
        ])
        {
            new RequestMap(url: url, configAttribute: 'permitAll').save()
        }
   
        // sections
        // works for /app
        //new RequestMap(url: '/app/**', configAttribute: 'ROLE_ADMIN').save()
        
        new RequestMap(url: '/app/index', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ORG_STAFF').save()
        new RequestMap(url: '/person/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ORG_STAFF').save()
        new RequestMap(url: '/ehr/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ORG_STAFF').save()
        new RequestMap(url: '/versionedComposition/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER').save()
        new RequestMap(url: '/contribution/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ORG_STAFF').save()
        new RequestMap(url: '/folder/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ORG_STAFF').save()
        new RequestMap(url: '/query/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ORG_STAFF').save()
        new RequestMap(url: '/indexDefinition/**', configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/compositionIndex/**', configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/operationalTemplate/**', configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/user/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER').save()
        new RequestMap(url: '/role/**', configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/organization/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER').save()
        new RequestMap(url: '/personIdType/**', configAttribute: 'ROLE_ADMIN').save()

        new RequestMap(url: '/j_spring_security_switch_user', configAttribute: 'ROLE_SWITCH_USER,isFullyAuthenticated()').save()
     }
     if (Role.count() == 0 )
     {
        def adminRole = new Role(authority: 'ROLE_ADMIN').save(failOnError: true, flush: true)
        def orgManagerRole = new Role(authority: 'ROLE_ORG_MANAGER').save(failOnError: true, flush: true)
        def clinicalManagerRole = new Role(authority: 'ROLE_ORG_CLINICAL_MANAGER').save(failOnError: true, flush: true)
        def staffRole = new Role(authority: 'ROLE_ORG_STAFF').save(failOnError: true, flush: true)
        def userRole = new Role(authority: 'ROLE_USER').save(failOnError: true, flush: true)
     }
     if (User.count() == 0)
     {
        def adminUser = new User(username: 'admin', email: 'pablo.pazos@cabolabs.com',  password: 'admin')
        adminUser.organizations = [organizations[0], organizations[1]]
        adminUser.save(failOnError: true,  flush: true)
        
        def orgManUser = new User(username: 'orgman', email: 'pablo.swp+orgman@gmail.com',  password: 'orgman')
        orgManUser.organizations = [organizations[0], organizations[1]]
        orgManUser.save(failOnError: true,  flush: true)
        
        //UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ADMIN')), true )
        //UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ORG_MANAGER')), true )
        //UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ORG_STAFF')), true )
        //UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ORG_CLINICAL_MANAGER')), true )
        //UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_USER')), true )
        
        UserRole.create( adminUser, (Role.findByAuthority('ROLE_ADMIN')), true )
        UserRole.create( orgManUser, (Role.findByAuthority('ROLE_ORG_MANAGER')), true )
     }

     //****** SECURITY *******
     
     
     log.debug( 'Current working dir: '+ new File(".").getAbsolutePath() ) // Current working directory
     
     
     // Initial index loading
     if (IndexDefinition.count() == 0)
     {
		  def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
		  ti.indexAll()
     }
     
     // OPT loading
     def optMan = OptManager.getInstance( Holders.config.app.opt_repo )
     optMan.loadAll()
     
     // Fake persons and roles
     def persons = []
     if (Person.count() == 0)
     {
        persons = [
           new Person(
               firstName: 'Pablo',
               lastName: 'Pazos',
               dob: new Date(81, 9, 24),
               sex: 'M',
               idCode: '4116238-0',
               idType: PersonIdType.get(1).code,
               role: 'pat',
               uid: '11111111-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Barbara',
               lastName: 'Cardozo',
               dob: new Date(87, 2, 19),
               sex: 'F',
               idCode: '1234567-0',
               idType: PersonIdType.get(1).code,
               role: 'pat',
               uid: '22222222-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Carlos',
               lastName: 'Cardozo',
               dob: new Date(80, 2, 20),
               sex: 'M',
               idCode: '3453455-0',
               idType: PersonIdType.get(1).code,
               role: 'pat',
               uid: '33333333-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Mario',
               lastName: 'Gomez',
               dob: new Date(64, 8, 19),
               sex: 'M',
               idCode: '5677565-0',
               idType: PersonIdType.get(1).code,
               role: 'pat',
               uid: '44444444-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Carla',
               lastName: 'Martinez',
               dob: new Date(92, 1, 5),
               sex: 'F',
               idCode: '84848884-0',
               idType: PersonIdType.get(1).code,
               role: 'pat',
               uid: '55555555-1111-1111-1111-111111111111'
           )
        ]
        
        def c = Organization.count()
        persons.eachWithIndex { p, i ->
           
           p.organizationUid = Organization.get(i % c + 1).uid
           
           if (!p.save())
           {
              println p.errors
           }
        }
     }
     if (Ehr.count() == 0)
     {
        // Fake EHRs for patients
        // Idem EhrController.createEhr
        def ehr
        
        persons.eachWithIndex { p, i ->
        
           if (p.role == 'pat')
           {
              ehr = new Ehr(
                 uid: p.uid, // the ehr id is the same as the patient just to simplify testing
                 subject: new PatientProxy(
                    value: p.uid
                 ),
                 organizationUid: p.organizationUid
              )
            
              if (!ehr.save()) println ehr.errors
           }
        }
     }
   }
   
   def destroy = {
   }
}
