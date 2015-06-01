package common.change_control



import grails.test.mixin.*
import spock.lang.*

@TestFor(VersionedCompositionController)
@Mock(VersionedComposition)
class VersionedCompositionControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.versionedCompositionInstanceList
            model.versionedCompositionInstanceCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.versionedCompositionInstance!= null
    }

    void "Test the save action correctly persists an instance"() {

        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            def versionedComposition = new VersionedComposition()
            versionedComposition.validate()
            controller.save(versionedComposition)

        then:"The create view is rendered again with the correct model"
            model.versionedCompositionInstance!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            versionedComposition = new VersionedComposition(params)

            controller.save(versionedComposition)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/versionedComposition/show/1'
            controller.flash.message != null
            VersionedComposition.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def versionedComposition = new VersionedComposition(params)
            controller.show(versionedComposition)

        then:"A model is populated containing the domain instance"
            model.versionedCompositionInstance == versionedComposition
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def versionedComposition = new VersionedComposition(params)
            controller.edit(versionedComposition)

        then:"A model is populated containing the domain instance"
            model.versionedCompositionInstance == versionedComposition
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/versionedComposition/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def versionedComposition = new VersionedComposition()
            versionedComposition.validate()
            controller.update(versionedComposition)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.versionedCompositionInstance == versionedComposition

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            versionedComposition = new VersionedComposition(params).save(flush: true)
            controller.update(versionedComposition)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/versionedComposition/show/$versionedComposition.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/versionedComposition/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def versionedComposition = new VersionedComposition(params).save(flush: true)

        then:"It exists"
            VersionedComposition.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(versionedComposition)

        then:"The instance is deleted"
            VersionedComposition.count() == 0
            response.redirectedUrl == '/versionedComposition/index'
            flash.message != null
    }
}
