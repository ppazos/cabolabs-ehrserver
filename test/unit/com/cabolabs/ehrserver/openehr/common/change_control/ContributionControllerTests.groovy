package com.cabolabs.ehrserver.openehr.common.change_control

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.ChangeType
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy

import org.junit.*

import com.cabolabs.ehrserver.openehr.common.change_control.ContributionController;
import com.cabolabs.ehrserver.openehr.ehr.Ehr;

import grails.test.mixin.*

@TestFor(ContributionController)
@Mock([Contribution, Ehr, Version, AuditDetails, PatientProxy, DoctorProxy])
class ContributionControllerTests {

   void setUp()
   {
      // Crea EHRs para los pacientes de prueba
      // Idem EhrController.createEhr
      def ehr = new Ehr(
         subject: new PatientProxy( value: '463456346345654' ),
         organizationUid: '1234-1234-1234'
      )
      if (!ehr.save()) println ehr.errors
   }

   void tearDown()
   {
      // Tear down logic here
   }
   
    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        params["ehr"] = Ehr.get(1)
        params["audit"] = new AuditDetails(
            systemId: "CABOLABS EHR",
            timeCommitted: new Date(),
            changeType: ChangeType.CREATION,
            committer: new DoctorProxy( name: "Dr. House")
        )
        params["uid"] = '35634634634634563'
        // no versions!
    }

    void testIndex() {
        controller.index()
        assert "/contribution/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.contributionInstanceList.size() == 0
        assert model.contributionInstanceTotal == 0
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/contribution/list'

        // FIXME: create a valid Contribution
        populateValidParams(params)
        def contribution = new Contribution(params)

        assert contribution.save() != null

        params.id = contribution.id

        def model = controller.show()

        assert model.contributionInstance == contribution
    }
}
