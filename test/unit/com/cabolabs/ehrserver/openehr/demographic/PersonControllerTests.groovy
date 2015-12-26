package com.cabolabs.ehrserver.openehr.demographic

import org.junit.*
import com.cabolabs.ehrserver.openehr.demographic.PersonController
import com.cabolabs.ehrserver.openehr.demographic.Person
import grails.test.mixin.*

@TestFor(PersonController)
@Mock(Person)
class PersonControllerTests {

    def populateValidParams(params) {
        assert params != null
        
        params["firstName"] = 'Pablo'
        params["lastName"] = 'Pazos'
        params["dob"] = new Date()
        params["sex"] = 'M'
        params["idCode"] = '23542354'
        params["idType"] = 'CI'
        params["role"] = 'pat'
        params["uid"] = '345634634563456'
    }

    void testIndex() {
        controller.index()
        assert "/person/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.personInstanceList.size() == 0
        assert model.personInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.personInstance != null
    }

    void testSave() {
        controller.request.method = 'POST' // removing this will get response.status 405 method not allowed
        controller.save() // creates invalid person, redirects to create
        
        //println "status "+ response.status
        
        assert model.personInstance != null
        assert view == '/person/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/person/show/1'
        assert controller.flash.message != null
        assert Person.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/person/list'

        populateValidParams(params)
        def person = new Person(params)

        assert person.save() != null

        params.id = person.id

        def model = controller.show()

        assert model.personInstance == person
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/person/list'

        populateValidParams(params)
        def person = new Person(params)

        assert person.save() != null

        params.id = person.id

        def model = controller.edit()

        assert model.personInstance == person
    }

    void testUpdate() {
       
        // no instance to update, redirect to list -----------------
        controller.request.method = 'POST'
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/person/list'

        response.reset()

        
        populateValidParams(params)
        def person = new Person(params)

        assert person.save() != null

        
        // test invalid parameters in update -----------------------
        params.id = person.id
        
        // adds invalid values to params object
        params.uid = ''

        controller.update()

        assert view == "/person/edit"
        assert model.personInstance != null

        person.clearErrors()

        
        // valid update -------------------------------------------
        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/person/show/$person.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        person.clearErrors()

        populateValidParams(params)
        params.id = person.id
        params.version = -1
        controller.update()

        assert view == "/person/edit"
        assert model.personInstance != null
        assert model.personInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.request.method = 'POST'
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/person/list'

        response.reset()

        populateValidParams(params)
        def person = new Person(params)

        assert person.save() != null
        assert Person.count() == 1

        params.id = person.id

        controller.delete() // logical delete

        assert Person.count() == 1
        assert Person.get(person.id) != null
        assert Person.countByIdAndDeleted(person.id, true) == 1
        assert Person.countByIdAndDeleted(person.id, false) == 0
        assert response.redirectedUrl == '/person/list'
    }
}
