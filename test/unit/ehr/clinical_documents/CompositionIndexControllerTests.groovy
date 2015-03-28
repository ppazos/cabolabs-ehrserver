package ehr.clinical_documents



import org.junit.*
import grails.test.mixin.*

@TestFor(CompositionIndexController)
@Mock(CompositionIndex)
class CompositionIndexControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
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
