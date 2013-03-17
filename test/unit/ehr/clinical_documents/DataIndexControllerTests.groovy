package ehr.clinical_documents



import org.junit.*
import grails.test.mixin.*

@TestFor(DataIndexController)
@Mock(DataIndex)
class DataIndexControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/dataIndex/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.dataIndexInstanceList.size() == 0
        assert model.dataIndexInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.dataIndexInstance != null
    }

    void testSave() {
        controller.save()

        assert model.dataIndexInstance != null
        assert view == '/dataIndex/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/dataIndex/show/1'
        assert controller.flash.message != null
        assert DataIndex.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/dataIndex/list'

        populateValidParams(params)
        def dataIndex = new DataIndex(params)

        assert dataIndex.save() != null

        params.id = dataIndex.id

        def model = controller.show()

        assert model.dataIndexInstance == dataIndex
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/dataIndex/list'

        populateValidParams(params)
        def dataIndex = new DataIndex(params)

        assert dataIndex.save() != null

        params.id = dataIndex.id

        def model = controller.edit()

        assert model.dataIndexInstance == dataIndex
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/dataIndex/list'

        response.reset()

        populateValidParams(params)
        def dataIndex = new DataIndex(params)

        assert dataIndex.save() != null

        // test invalid parameters in update
        params.id = dataIndex.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/dataIndex/edit"
        assert model.dataIndexInstance != null

        dataIndex.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/dataIndex/show/$dataIndex.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        dataIndex.clearErrors()

        populateValidParams(params)
        params.id = dataIndex.id
        params.version = -1
        controller.update()

        assert view == "/dataIndex/edit"
        assert model.dataIndexInstance != null
        assert model.dataIndexInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/dataIndex/list'

        response.reset()

        populateValidParams(params)
        def dataIndex = new DataIndex(params)

        assert dataIndex.save() != null
        assert DataIndex.count() == 1

        params.id = dataIndex.id

        controller.delete()

        assert DataIndex.count() == 0
        assert DataIndex.get(dataIndex.id) == null
        assert response.redirectedUrl == '/dataIndex/list'
    }
}
