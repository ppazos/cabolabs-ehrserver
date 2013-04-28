package common.change_control



import org.junit.*
import grails.test.mixin.*

@TestFor(ContributionController)
@Mock(Contribution)
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

    void testCreate() {
        def model = controller.create()

        assert model.contributionInstance != null
    }

    void testSave() {
        controller.save()

        assert model.contributionInstance != null
        assert view == '/contribution/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/contribution/show/1'
        assert controller.flash.message != null
        assert Contribution.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/contribution/list'

        populateValidParams(params)
        def contribution = new Contribution(params)

        assert contribution.save() != null

        params.id = contribution.id

        def model = controller.show()

        assert model.contributionInstance == contribution
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/contribution/list'

        populateValidParams(params)
        def contribution = new Contribution(params)

        assert contribution.save() != null

        params.id = contribution.id

        def model = controller.edit()

        assert model.contributionInstance == contribution
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/contribution/list'

        response.reset()

        populateValidParams(params)
        def contribution = new Contribution(params)

        assert contribution.save() != null

        // test invalid parameters in update
        params.id = contribution.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/contribution/edit"
        assert model.contributionInstance != null

        contribution.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/contribution/show/$contribution.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        contribution.clearErrors()

        populateValidParams(params)
        params.id = contribution.id
        params.version = -1
        controller.update()

        assert view == "/contribution/edit"
        assert model.contributionInstance != null
        assert model.contributionInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/contribution/list'

        response.reset()

        populateValidParams(params)
        def contribution = new Contribution(params)

        assert contribution.save() != null
        assert Contribution.count() == 1

        params.id = contribution.id

        controller.delete()

        assert Contribution.count() == 0
        assert Contribution.get(contribution.id) == null
        assert response.redirectedUrl == '/contribution/list'
    }
}
