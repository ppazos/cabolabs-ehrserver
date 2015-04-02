package common.change_control

import common.generic.AuditDetails
import ehr.Ehr
import common.change_control.Version

import org.junit.*
import grails.test.mixin.*

@TestFor(ContributionController)
@Mock([Contribution, Ehr, Version, AuditDetails])
class ContributionControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
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
