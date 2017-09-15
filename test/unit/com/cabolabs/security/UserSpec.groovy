package com.cabolabs.security

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import grails.test.mixin.Mock

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([User,Organization,UserRole,Role])
class UserSpec extends Specification {

    def setup()
    {
       // metaprogramming done in bootstrap
       String.metaClass.static.randomNumeric = { digits ->
          def alphabet = ['0','1','2','3','4','5','6','7','8','9']
          new Random().with {
            (1..digits).collect { alphabet[ nextInt( alphabet.size() ) ] }.join()
          }
       }
       
       // role setup
       def adminRole = new Role(authority: 'ROLE_ADMIN').save(failOnError: true, flush: true)
       def orgManagerRole = new Role(authority: 'ROLE_ORG_MANAGER').save(failOnError: true, flush: true)
       def clinicalManagerRole = new Role(authority: 'ROLE_ORG_CLINICAL_MANAGER').save(failOnError: true, flush: true)
       def userRole = new Role(authority: 'ROLE_USER').save(failOnError: true, flush: true)
    }

    def cleanup()
    {
    }

    void "create disabled user without password"()
    {
       when:
          def u = new User(
             username: 'user',
             email: 'pablo@pazos.com',
             enabled: false
          )
          u.save(failOnError: true, flush:true)
       
       then:
          User.count() == 1
    }
    
    void "create user and add many organizations"()
    {
       when:
          def u = new User(
             username: 'user',
             email: 'pablo@pazos.com',
             password: 'secret',
             enabled: true
          )
          
          /* weird: this executed the organization.beforeInsert for ever = stack overflow
          u.save(failOnError: true, flush:true)
          
          def names = ['a', 'b', 'c', 'd']
          names.each {
             u.addToOrganizations( new Organization(name: it) )
          }
          */
          
          u.save(failOnError: true)
          
          // FIXME
          Organization.withNewSession { // without this we get an stack overflow, has something to do with countByNumber called from assignNumber called from beforeInsert on Organization.

             def orgs = [
                new Organization(name: 'a'),
                new Organization(name: 'b'),
                new Organization(name: 'c'),
                new Organization(name: 'd')
             ]
             
             orgs.each { o ->
                o.save(failOnError: true)
             }
             
             orgs.each { o ->
                UserRole.create( u, (Role.findByAuthority('ROLE_ADMIN')), o, true )
             }
          }

       then:
          User.count() == 1
          User.get(1).organizations.size() == 4
          println User.get(1).organizations.number // is not assigning the number, maybe because the withNewSession...
    }
    
    
    void "test highest roles"()
    {
       when:
          println Organization.countByNumber('123456')
          def org = new Organization(name: 'h').save(failOnError: true)
       
          // testing users with just one role
          def uadmin = new User(
             username: 'user1',
             email: 'pablo@pazos.com',
             password: 'secret',
             enabled: true
          )
          uadmin.save(failOnError: true)
          
          UserRole.create( uadmin, (Role.findByAuthority('ROLE_ADMIN')), org, true )
          
          
          def uorgman = new User(
             username: 'user2',
             email: 'pablo2@pazos.com',
             password: 'secret',
             enabled: true
          )
          uorgman.save(failOnError: true)
          
          UserRole.create( uorgman, (Role.findByAuthority('ROLE_ORG_MANAGER')), org, true )

          
          // users with many roles
          def uadmin2 = new User(
             username: 'user3',
             email: 'pablo3@pazos.com',
             password: 'secret',
             enabled: true
          )
          uadmin2.save(failOnError: true)
          
          UserRole.create( uadmin2, (Role.findByAuthority('ROLE_ADMIN')), true )
          UserRole.create( uadmin2, (Role.findByAuthority('ROLE_ORG_MANAGER')), org, true )
          
          
          def uorgman2 = new User(
             username: 'user4',
             email: 'pablo4@pazos.com',
             password: 'secret',
             enabled: true
          )
          uorgman2.save(failOnError: true)
          
          UserRole.create( uorgman2, (Role.findByAuthority('ROLE_ORG_MANAGER')), org, true )
          
       then:
          uadmin.higherAuthority.authority == 'ROLE_ADMIN'
          uorgman.higherAuthority.authority == 'ROLE_ORG_MANAGER'
          uadmin2.higherAuthority.authority == 'ROLE_ADMIN'
          uorgman2.higherAuthority.authority == 'ROLE_ORG_MANAGER'
    }
}
