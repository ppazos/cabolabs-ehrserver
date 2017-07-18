package com.cabolabs.security

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import grails.test.mixin.Mock

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Role])
class RoleSpec extends Specification {

    def setup()
    {
    }

    def cleanup()
    {
    }

    void "test role comparison"()
    {
       when:
          def adminRole = new Role(authority: 'ROLE_ADMIN').save(failOnError: true, flush: true)
          def orgManagerRole = new Role(authority: 'ROLE_ORG_MANAGER').save(failOnError: true, flush: true)
          def clinicalManagerRole = new Role(authority: 'ROLE_ORG_CLINICAL_MANAGER').save(failOnError: true, flush: true)
          def userRole = new Role(authority: 'ROLE_USER').save(failOnError: true, flush: true)
       
       
       then:
          adminRole.higherThan orgManagerRole
          adminRole.higherThan clinicalManagerRole
          adminRole.higherThan staffRole
          adminRole.higherThan userRole
          orgManagerRole.higherThan clinicalManagerRole
          orgManagerRole.higherThan staffRole
          orgManagerRole.higherThan userRole
          
          adminRole.higherThan adminRole
          !(clinicalManagerRole.higherThan(adminRole))
          staffRole.higherThan(userRole)
          
          staffRole.higherThan userRole
    }
    
    void "test equals"()
    {
       when:
          def role1 = new Role(authority:'ROLE_ADMIN')
          def role2 = new Role(authority:'ROLE_ADMIN')
          def role3 = new Role(authority:'ROLE_OTHER')
       
       then:
          role1.equals(role2)
          role2.equals(role1)
          !role3.equals(role1)
    }
}
