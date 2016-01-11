package com.cabolabs.ehrserver.ehr.clinical_documents

import org.junit.*

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex;
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndexController;

import grails.test.mixin.*

@TestFor(CompositionIndexController)
@Mock(CompositionIndex)
class CompositionIndexControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        params["uid"] = 'sfgsdfgssdgf'
        params["category"] = 'event'
        params["startTime"] = new Date()
        params["subjectId"] = '34563456345'
        params["ehrUid"] = '345634563534'
        params["templateId"] = 'Signos'
        params["archetypeId"] = 'openEHR-EHR-COMPOSITION.signos.v1'
    }

    void testIndex() {
        controller.index()
        assert "/compositionIndex/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.compositionIndexInstanceList.size() == 0
        assert model.compositionIndexInstanceTotal == 0
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/compositionIndex/list'

        populateValidParams(params)
        def compositionIndex = new CompositionIndex(params)

        assert compositionIndex.save() != null

        params.id = compositionIndex.id

        def model = controller.show()

        assert model.compositionIndexInstance == compositionIndex
    }
}
