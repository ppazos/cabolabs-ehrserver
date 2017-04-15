package com.cabolabs.ehrserver.openehr.common.change_control

import com.cabolabs.ehrserver.openehr.common.change_control.VersionedCompositionController
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition;
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import grails.test.mixin.*
import spock.lang.*

@TestFor(VersionedCompositionController)
@Mock([VersionedComposition, Ehr, PatientProxy])
class VersionedCompositionControllerSpec extends Specification {

    void setupSpec()
    {
        /*
        def pat = new Person(
            firstName: 'Pablo',
            lastName: 'Pazos',
            dob: new Date(81, 9, 24),
            sex: 'M',
            idCode: '4116238-0',
            idType: 'CI',
            role: 'pat',
            uid: '463456346345654')

        if (!pat.save(flush:true)) println pat.errors
        
        
        // Crea EHRs para los pacientes de prueba
        // Idem EhrController.createEhr
        def ehr = new Ehr(
            subject: new PatientProxy( value: '463456346345654' )
        )
        if (!ehr.save(flush:true)) println ehr.errors
        */
    }
   
    def populateValidParams(params) {
        assert params != null
        params["isPersistent"] = false
        params["ehrUid"] = '254235423235'
        params["uid"] = '345354353455'
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.versionedCompositionInstanceList
            model.versionedCompositionInstanceCount == 0
    }


    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def versionedComposition = new VersionedComposition(params)
            versionedComposition.save(flush:true)
            controller.show(versionedComposition.uid)

        then:"A model is populated containing the domain instance"
            model.versionedCompositionInstance == versionedComposition
    }
}
