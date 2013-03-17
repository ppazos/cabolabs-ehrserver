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

    void testCreate() {
        def model = controller.create()

        assert model.compositionIndexInstance != null
    }

    void testSave() {
        controller.save()

        assert model.compositionIndexInstance != null
        assert view == '/compositionIndex/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/compositionIndex/show/1'
        assert controller.flash.message != null
        assert CompositionIndex.count() == 1
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

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/compositionIndex/list'

        populateValidParams(params)
        def compositionIndex = new CompositionIndex(params)

        assert compositionIndex.save() != null

        params.id = compositionIndex.id

        def model = controller.edit()

        assert model.compositionIndexInstance == compositionIndex
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/compositionIndex/list'

        response.reset()

        populateValidParams(params)
        def compositionIndex = new CompositionIndex(params)

        assert compositionIndex.save() != null

        // test invalid parameters in update
        params.id = compositionIndex.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/compositionIndex/edit"
        assert model.compositionIndexInstance != null

        compositionIndex.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/compositionIndex/show/$compositionIndex.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        compositionIndex.clearErrors()

        populateValidParams(params)
        params.id = compositionIndex.id
        params.version = -1
        controller.update()

        assert view == "/compositionIndex/edit"
        assert model.compositionIndexInstance != null
        assert model.compositionIndexInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/compositionIndex/list'

        response.reset()

        populateValidParams(params)
        def compositionIndex = new CompositionIndex(params)

        assert compositionIndex.save() != null
        assert CompositionIndex.count() == 1

        params.id = compositionIndex.id

        controller.delete()

        assert CompositionIndex.count() == 0
        assert CompositionIndex.get(compositionIndex.id) == null
        assert response.redirectedUrl == '/compositionIndex/list'
    }
}
