package com.cabolabs.ehrserver.openehr.common.change_control

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.ChangeType
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import grails.test.mixin.Mock


@TestMixin(GrailsUnitTestMixin)
@Mock([Contribution,Version,VersionedComposition,Ehr,Organization,PatientProxy,DoctorProxy,CompositionIndex])
class ContributionSpec extends Specification {

   static String patientUid = '1111-1111-1111'
   
    def setup()
    {
       def hospital = new Organization(name: 'Hospital de Clinicas', number: '1234')
       hospital.save(failOnError:true, flush:true)
       
       def ehr = new Ehr(
          subject: new PatientProxy(
             value: patientUid
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
                lifecycleState: '532',
                commitAudit: new AuditDetails(
                   systemId: 'EMR1',
                   timeCommitted: new Date(),
                   changeType:    ChangeType.CREATION,
                   committer: new DoctorProxy(
                      name: 'Dr. House'
                   )
                ),
                data: new CompositionIndex(
                   uid:         '3454-3456-3456',
                   category:    'event',
                   startTime:   new Date(),
                   subjectId:   patientUid,
                   ehrUid:      ehr.uid,
                   organizationUid: ehr.organizationUid,
                   archetypeId: 'openEHR-EHR-COMPOSITION.signos.v1',
                   templateId:  'Signos'
                )
             ),
             new Version(
                uid: '1234-1235::EMR1::1',
                lifecycleState: '532',
                commitAudit: new AuditDetails(
                   systemId: 'EMR1',
                   timeCommitted: new Date(),
                   changeType:    ChangeType.CREATION,
                   committer: new DoctorProxy(
                      name: 'Dr. House'
                   )
                ),
                data: new CompositionIndex(
                   uid:         '3454-3456-3457',
                   category:    'event',
                   startTime:   new Date(),
                   subjectId:   patientUid,
                   ehrUid:      ehr.uid,
                   organizationUid: ehr.organizationUid,
                   archetypeId: 'openEHR-EHR-COMPOSITION.signos.v1',
                   templateId:  'Signos'
                )
             ),
             new Version(
                uid: '1234-1236::EMR1::1',
                lifecycleState: '532',
                commitAudit: new AuditDetails(
                   systemId:     'EMR1',
                   timeCommitted: new Date(),
                   changeType:    ChangeType.CREATION,
                   committer: new DoctorProxy(
                      name: 'Dr. House'
                   )
                ),
                data: new CompositionIndex(
                   uid:         '3454-3456-3458',
                   category:    'event',
                   startTime:   new Date(),
                   subjectId:   patientUid,
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
