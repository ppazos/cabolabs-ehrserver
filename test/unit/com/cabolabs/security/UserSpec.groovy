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
          
          println "A"
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
                u.addToOrganizations(o)
             }
          }

          println "B"

       then:
          User.count() == 1
          User.get(1).organizations.size() == 4
          println User.get(1).organizations.number // is not assigning the number, maybe because the withNewSession...
    }
}
