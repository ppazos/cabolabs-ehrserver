package com.cabolabs.ehrserver.openehr.common.change_control

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.demographic.Person
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import grails.test.mixin.Mock


@TestMixin(GrailsUnitTestMixin)
@Mock([Contribution,Version,VersionedComposition,Ehr,Person,Organization,PatientProxy,DoctorProxy,CompositionIndex])
class ContributionSpec extends Specification {

    def setup()
    {
       def hospital = new Organization(name: 'Hospital de Clinicas', number: '1234')
       hospital.save(failOnError:true, flush:true)
       
       def patient = new Person(
          firstName: 'Pablo', lastName: 'Pazos',
          dob: new Date(81, 9, 24), sex: 'M',
          idCode: '4116238-0', idType: 'CI',
          role: 'pat',
          uid: '1111-1111-1111',
          organizationUid: hospital.uid
       )
       patient.save(failOnError:true, flush:true)
       
       def ehr = new Ehr(
          subject: new PatientProxy(
             value: patient.uid
          ),
          organizationUid: patient.organizationUid
       )
       ehr.save(failOnError:true, flush:true)
    }

    def cleanup()
    {
    }
    
    void "create contribution for ehr"()
    {
       when:
          def ehr = Ehr.get(1)
          def pat = Person.get(1)
          
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
          
          c.save(flush:true, failOnError:true)
          
          def versions = [
             new Version(
                uid: '1234-1234::EMR1::1',
                lifecycleState: 'completed',
                commitAudit: new AuditDetails(
                   systemId: 'EMR1',
                   timeCommitted: new Date(),
                   changeType:    'creation',
                   committer: new DoctorProxy(
                      name: 'Dr. House'
                   )
                ),
                data: new CompositionIndex(
                   uid:         '3454-3456-3456',
                   category:    'event',
                   startTime:   new Date(),
                   subjectId:   pat.uid,
                   ehrUid:      ehr.uid,
                   organizationUid: ehr.organizationUid,
                   archetypeId: 'openEHR-EHR-COMPOSITION.signos.v1',
                   templateId:  'Signos'
                )
             ),
             new Version(
                uid: '1234-1235::EMR1::1',
                lifecycleState: 'completed',
                commitAudit: new AuditDetails(
                   systemId: 'EMR1',
                   timeCommitted: new Date(),
                   changeType:    'creation',
                   committer: new DoctorProxy(
                      name: 'Dr. House'
                   )
                ),
                data: new CompositionIndex(
                   uid:         '3454-3456-3457',
                   category:    'event',
                   startTime:   new Date(),
                   subjectId:   pat.uid,
                   ehrUid:      ehr.uid,
                   organizationUid: ehr.organizationUid,
                   archetypeId: 'openEHR-EHR-COMPOSITION.signos.v1',
                   templateId:  'Signos'
                )
             ),
             new Version(
                uid: '1234-1236::EMR1::1',
                lifecycleState: 'completed',
                commitAudit: new AuditDetails(
                   systemId: 'EMR1',
                   timeCommitted: new Date(),
                   changeType:    'creation',
                   committer: new DoctorProxy(
                      name: 'Dr. House'
                   )
                ),
                data: new CompositionIndex(
                   uid:         '3454-3456-3458',
                   category:    'event',
                   startTime:   new Date(),
                   subjectId:   pat.uid,
                   ehrUid:      ehr.uid,
                   organizationUid: ehr.organizationUid,
                   archetypeId: 'openEHR-EHR-COMPOSITION.signos.v1',
                   templateId:  'Signos'
                )
             )
          ]
          
          versions.each { v ->
             c.addToVersions(v)
          }
       
       then:
          Contribution.count() == 1
          Contribution.get(1).versions.size() == 3
          println Contribution.get(1).versions.uid
    }
}
