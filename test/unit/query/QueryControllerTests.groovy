package query



import org.junit.*
import grails.test.mixin.*

@TestFor(QueryController)
@Mock(Query)
class QueryControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/query/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.queryInstanceList.size() == 0
        assert model.queryInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.queryInstance != null
    }

    void testSave() {
        controller.save()

        assert model.queryInstance != null
        assert view == '/query/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/query/show/1'
        assert controller.flash.message != null
        assert Query.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/query/list'

        populateValidParams(params)
        def query = new Query(params)

        assert query.save() != null

        params.id = query.id

        def model = controller.show()

        assert model.queryInstance == query
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/query/list'

        populateValidParams(params)
        def query = new Query(params)

        assert query.save() != null

        params.id = query.id

        def model = controller.edit()

        assert model.queryInstance == query
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/query/list'

        response.reset()

        populateValidParams(params)
        def query = new Query(params)

        assert query.save() != null

        // test invalid parameters in update
        params.id = query.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/query/edit"
        assert model.queryInstance != null

        query.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/query/show/$query.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        query.clearErrors()

        populateValidParams(params)
        params.id = query.id
        params.version = -1
        controller.update()

        assert view == "/query/edit"
        assert model.queryInstance != null
        assert model.queryInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/query/list'

        response.reset()

        populateValidParams(params)
        def query = new Query(params)

        assert query.save() != null
        assert Query.count() == 1

        params.id = query.id

        controller.delete()

        assert Query.count() == 0
        assert Query.get(query.id) == null
        assert response.redirectedUrl == '/query/list'
    }
}
