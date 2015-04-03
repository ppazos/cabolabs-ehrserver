package ehr.clinical_documents



import org.junit.*
import grails.test.mixin.*

@TestFor(IndexDefinitionController)
@Mock(IndexDefinition)
class IndexDefinitionControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/indexDefinition/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.indexDefinitionInstanceList.size() == 0
        assert model.indexDefinitionInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.indexDefinitionInstance != null
    }

    void testSave() {
        controller.save()

        assert model.indexDefinitionInstance != null
        assert view == '/indexDefinition/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/indexDefinition/show/1'
        assert controller.flash.message != null
        assert IndexDefinition.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/indexDefinition/list'

        populateValidParams(params)
        def indexDefinition = new IndexDefinition(params)

        assert indexDefinition.save() != null

        params.id = indexDefinition.id

        def model = controller.show()

        assert model.indexDefinitionInstance == indexDefinition
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/indexDefinition/list'

        populateValidParams(params)
        def indexDefinition = new IndexDefinition(params)

        assert indexDefinition.save() != null

        params.id = indexDefinition.id

        def model = controller.edit()

        assert model.indexDefinitionInstance == indexDefinition
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/indexDefinition/list'

        response.reset()

        populateValidParams(params)
        def indexDefinition = new IndexDefinition(params)

        assert indexDefinition.save() != null

        // test invalid parameters in update
        params.id = indexDefinition.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/indexDefinition/edit"
        assert model.indexDefinitionInstance != null

        indexDefinition.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/indexDefinition/show/$indexDefinition.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        indexDefinition.clearErrors()

        populateValidParams(params)
        params.id = indexDefinition.id
        params.version = -1
        controller.update()

        assert view == "/indexDefinition/edit"
        assert model.indexDefinitionInstance != null
        assert model.indexDefinitionInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/indexDefinition/list'

        response.reset()

        populateValidParams(params)
        def indexDefinition = new IndexDefinition(params)

        assert indexDefinition.save() != null
        assert IndexDefinition.count() == 1

        params.id = indexDefinition.id

        controller.delete()

        assert IndexDefinition.count() == 0
        assert IndexDefinition.get(indexDefinition.id) == null
        assert response.redirectedUrl == '/indexDefinition/list'
    }
}
