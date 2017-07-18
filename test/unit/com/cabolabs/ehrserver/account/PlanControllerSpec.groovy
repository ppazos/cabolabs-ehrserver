package com.cabolabs.ehrserver.account



import grails.test.mixin.*
import spock.lang.*

@TestFor(PlanController)
@Mock(Plan)
class PlanControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.planInstanceList
            model.planInstanceCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.planInstance!= null
    }

    void "Test the save action correctly persists an instance"() {

        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'POST'
            def plan = new Plan()
            plan.validate()
            controller.save(plan)

        then:"The create view is rendered again with the correct model"
            model.planInstance!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            plan = new Plan(params)

            controller.save(plan)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/plan/show/1'
            controller.flash.message != null
            Plan.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def plan = new Plan(params)
            controller.show(plan)

        then:"A model is populated containing the domain instance"
            model.planInstance == plan
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def plan = new Plan(params)
            controller.edit(plan)

        then:"A model is populated containing the domain instance"
            model.planInstance == plan
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/plan/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def plan = new Plan()
            plan.validate()
            controller.update(plan)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.planInstance == plan

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            plan = new Plan(params).save(flush: true)
            controller.update(plan)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/plan/show/$plan.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'DELETE'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/plan/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def plan = new Plan(params).save(flush: true)

        then:"It exists"
            Plan.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(plan)

        then:"The instance is deleted"
            Plan.count() == 0
            response.redirectedUrl == '/plan/index'
            flash.message != null
    }
}
