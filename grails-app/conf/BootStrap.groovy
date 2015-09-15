import demographic.Person
import common.generic.PatientProxy
import ehr.Ehr
import ehr.clinical_documents.IndexDefinition
import grails.util.Holders
import com.cabolabs.security.RequestMap
import com.cabolabs.security.User
import com.cabolabs.security.Role
import com.cabolabs.security.UserRole

import com.cabolabs.openehr.opt.manager.OptManager // load opts

class BootStrap {

   private static String PS = System.getProperty("file.separator")
   
   def init = { servletContext ->
     
     
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
     
     
     //****** SECURITY *******
     for (String url in [
      //'/', 
      '/error', '/index', '/index.gsp', '/**/favicon.ico', '/shutdown',
      '/assets/**', '/**/js/**', '/**/css/**', '/**/images/**', '/**/fonts/**',
      '/login', '/login.*', '/login/*',
      '/logout', '/logout.*', '/logout/*'])
     {
         new RequestMap(url: url, configAttribute: 'permitAll').save()
     }
     
     //new RequestMap(url: '/profile/**',    configAttribute: 'ROLE_USER').save()
     //new RequestMap(url: '/admin/**',      configAttribute: 'ROLE_ADMIN').save()
     //new RequestMap(url: '/admin/role/**', configAttribute: 'ROLE_SUPERVISOR').save()
     //new RequestMap(url: '/admin/user/**', configAttribute: 'ROLE_ADMIN,ROLE_SUPERVISOR').save()
     new RequestMap(url: '/', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/', configAttribute: 'ROLE_ORG_MANAGER').save()
     new RequestMap(url: '/', configAttribute: 'ROLE_CLINICAL_MANAGER').save()
     
     new RequestMap(url: '/person/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/ehr/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/contribution/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/folder/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/query/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/indexDefinition/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/compositionIndex/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/operationalTemplate/**', configAttribute: 'ROLE_ADMIN').save()
     
     new RequestMap(url: '/user/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/role/**', configAttribute: 'ROLE_ADMIN').save()
     new RequestMap(url: '/organization/**', configAttribute: 'ROLE_ADMIN').save()
     
     new RequestMap(url: '/j_spring_security_switch_user', configAttribute: 'ROLE_SWITCH_USER,isFullyAuthenticated()').save()
     
     if (Role.count() == 0 )
     {
        def adminRole = new Role(authority: 'ROLE_ADMIN').save(failOnError: true, flush: true)
        def orgManagerRole = new Role(authority: 'ROLE_ORG_MANAGER').save(failOnError: true, flush: true)
        def clinicalManagerRole = new Role(authority: 'ROLE_CLINICAL_MANAGER').save(failOnError: true, flush: true)
     }
     if (User.count() == 0)
     {
        def godlikeUser = new User(username: 'godlike', email: 'pablo.pazos@cabolabs.com',  password: 'godlike')
        godlikeUser.save(failOnError: true,  flush: true)
        
        def adminUser = new User(username: 'admin', email: 'pablo.pazos@cabolabs.com',  password: 'admin')
        adminUser.save(failOnError: true,  flush: true)
        
        UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ADMIN')), true )
        UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ORG_MANAGER')), true )
        UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_CLINICAL_MANAGER')), true )
        
        UserRole.create( adminUser, (Role.findByAuthority('ROLE_ADMIN')), true )
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
               idType: 'CI',
               role: 'pat',
               uid: '11111111-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Barbara',
               lastName: 'Cardozo',
               dob: new Date(87, 2, 19),
               sex: 'F',
               idCode: '1234567-0',
               idType: 'CI',
               role: 'pat',
               uid: '22222222-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Carlos',
               lastName: 'Cardozo',
               dob: new Date(80, 2, 20),
               sex: 'M',
               idCode: '3453455-0',
               idType: 'CI',
               role: 'pat',
               uid: '33333333-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Mario',
               lastName: 'Gomez',
               dob: new Date(64, 8, 19),
               sex: 'M',
               idCode: '5677565-0',
               idType: 'CI',
               role: 'pat',
               uid: '44444444-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Carla',
               lastName: 'Martinez',
               dob: new Date(92, 1, 5),
               sex: 'F',
               idCode: '84848884-0',
               idType: 'CI',
               role: 'pat',
               uid: '55555555-1111-1111-1111-111111111111'
           )
        ]
         
        persons.each { p ->
            
           if (!p.save())
           {
              println p.errors
           }
        }
     }
     
     // Fake EHRs for patients
     // Idem EhrController.createEhr
     def ehr
     persons.each { p ->
     
        if (p.role == 'pat')
        {
           ehr = new Ehr(
              ehrId: p.uid, // the ehr id is the same as the patient just to simplify testing
              subject: new PatientProxy(
                 value: p.uid
              )
           )
         
           if (!ehr.save()) println ehr.errors
        }
     }
   }
   
   def destroy = {
   }
}
