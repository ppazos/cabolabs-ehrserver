package com.cabolabs.ehrserver.openehr.directory

import com.cabolabs.ehrserver.openehr.directory.FolderController
import com.cabolabs.ehrserver.openehr.directory.Folder
import grails.test.mixin.*
import spock.lang.*

@TestFor(FolderController)
@Mock(Folder)
class FolderControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        params["name"] = 'folder x'
        params["organizationUid"] = '1234'
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.folderInstanceList
            model.folderInstanceCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.folderInstance!= null
    }

    void "Test the save action correctly persists an instance"() {
       
        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            request.format = 'form'
            controller.request.method = "POST"
            def folder = new Folder()
            folder.validate()
            controller.save(folder)
            //println folder.errors // error null name
            //println model // null
            //println view // create
            //println "status "+ response.status

        then:"The create view is rendered again with the correct model"
            model.folderInstance!= null
            view == 'create'

       
        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            folder = new Folder(params)
            
            controller.save(folder)
            //println folder.errors
            //println model
            //println "status "+ response.status

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/folder/show/1'
            controller.flash.message != null
            Folder.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def folder = new Folder(params)
            controller.show(folder)

        then:"A model is populated containing the domain instance"
            model.folderInstance == folder
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def folder = new Folder(params)
            controller.edit(folder)

        then:"A model is populated containing the domain instance"
            model.folderInstance == folder
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            controller.request.method = "PUT"
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/folder/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def folder = new Folder()
            folder.validate()
            controller.update(folder)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.folderInstance == folder

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            folder = new Folder(params).save(flush: true)
            controller.update(folder)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/folder/show/$folder.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            controller.request.method = "DELETE"
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/folder/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def folder = new Folder(params).save(flush: true)

        then:"It exists"
            Folder.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(folder)

        then:"The instance is deleted"
            Folder.count() == 0
            response.redirectedUrl == '/folder/index'
            flash.message != null
    }
}
