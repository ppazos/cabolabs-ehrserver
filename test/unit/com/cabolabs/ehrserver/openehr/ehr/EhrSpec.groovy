package com.cabolabs.ehrserver.openehr.ehr

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import grails.test.mixin.Mock

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.openehr.common.change_control.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Contribution,Version,VersionedComposition,Ehr,Organization,PatientProxy,DoctorProxy,CompositionIndex])
class EhrSpec extends Specification {

    def setup()
    {
       def hospital = new Organization(name: 'Hospital de Clinicas', number: '1234')
       hospital.save(failOnError:true, flush:true)
       
       def ehr = new Ehr(
          subject: new PatientProxy(
             value: '1111-1111-1111'
          ),
          organizationUid: patient.organizationUid
       )
       ehr.save(failOnError:true, flush:true)
    }

    def cleanup()
    {
    }

    void "add one versioned compo to ehr"()
    {
       when:
          def ehr = Ehr.get(1)
          
          def vc = new VersionedComposition(
             isPersistent: false,
             uid: '123-123-123',
             ehr: ehr)
          
          vc.save(failsOnError: true)
       
       then:
          ehr != null
          vc.id != null
          vc.ehr.id == ehr.id
          ehr.compositions.size() == 1
          VersionedComposition.count() == 1
    }
    
    void "add three versioned compo to ehr"()
    {
       when:
          def ehr = Ehr.get(1)
          
          def vc = new VersionedComposition(
             isPersistent: false,
             uid: '123-123-456',
             ehr: ehr)
          vc.save(failsOnError: true)
          
          vc = new VersionedComposition(
             isPersistent: false,
             uid: '123-123-567',
             ehr: ehr)
          vc.save(failsOnError: true)
          
          vc = new VersionedComposition(
             isPersistent: false,
             uid: '123-123-123',
             ehr: ehr)
          vc.save(failsOnError: true)
          
       
       then:
          ehr != null
          vc.ehr.id == ehr.id
          ehr.compositions.size() == 3
          VersionedComposition.count() == 3
    }

    
    void "add one contribution to ehr"()
    {
       when:
          def ehr = Ehr.get(1)
          
          def c = new Contribution(
             uid: '1234-4567-6789',
             ehr: ehr,
             organizationUid: ehr.organizationUid,
             audit: new AuditDetails(
                systemId: 'EMR1',
                timeCommitted: new Date(),
                committer: new DoctorProxy(
                   name: 'Dr. House'
                )
             )
          )
          //ehr.addToContributions(c)
          ehr.save(failOnError: true)
       
       then:
          ehr != null
          c.id != null
          c.ehr.id == ehr.id
          ehr.contributions.size() == 1
          Contribution.count() == 1
    }
    
    void "add three contributions to ehr"()
    {
       when:
          def ehr = Ehr.get(1)
          
          def c = new Contribution(
             uid: '1234-4567-6789',
             ehr: ehr,
             organizationUid: ehr.organizationUid,
             audit: new AuditDetails(
                systemId: 'EMR1',
                timeCommitted: new Date(),
                committer: new DoctorProxy(
                   name: 'Dr. House'
                )
             )
          )
          //ehr.addToContributions(c)
          
          c = new Contribution(
             uid: '1234-4567-7777',
             ehr: ehr,
             organizationUid: ehr.organizationUid,
             audit: new AuditDetails(
                systemId: 'EMR1',
                timeCommitted: new Date(),
                committer: new DoctorProxy(
                   name: 'Dr. House'
                )
             )
          )
          //ehr.addToContributions(c)
          
          c = new Contribution(
             uid: '1234-4567-8888',
             ehr: ehr,
             organizationUid: ehr.organizationUid,
             audit: new AuditDetails(
                systemId: 'EMR1',
                timeCommitted: new Date(),
                committer: new DoctorProxy(
                   name: 'Dr. House'
                )
             )
          )
          //ehr.addToContributions(c)
          
          ehr.save(failOnError: true)
       
       then:
          ehr != null
          ehr.contributions.size() == 3
          Contribution.count() == 3
    }
}
