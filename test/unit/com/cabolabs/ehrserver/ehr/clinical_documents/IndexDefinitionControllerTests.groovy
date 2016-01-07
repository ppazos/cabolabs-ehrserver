package com.cabolabs.ehrserver.ehr.clinical_documents

import org.junit.*

import com.cabolabs.ehrserver.ehr.clinical_documents.IndexDefinition
import com.cabolabs.ehrserver.ehr.clinical_documents.IndexDefinitionController
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex

import grails.test.mixin.support.*
import grails.test.mixin.*

@TestFor(IndexDefinitionController)
@Mock([IndexDefinition, OperationalTemplateIndex, IndexDefinition])
class IndexDefinitionControllerTests {

    def populateValidParams(params) {
        assert params != null

        params["templateId"] = 'Encuentro'
        params["path"] = '/content[archetype_id=openEHR-EHR-SECTION.vital_signs.v1]/items[archetype_id=openEHR-EHR-OBSERVATION.blood_pressure.v1]/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'
        params["archetypeId"] = 'openEHR-EHR-OBSERVATION.blood_pressure.v1'
        params["archetypePath"] = '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'
        params["rmTypeName"] = 'DV_QUANTITY'
        params["name"] = 'Systolic Blood pressure'
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

    void testGenerate() {
       
       assert IndexDefinition.count() == 0
       assert OperationalTemplateIndex.count() == 0
       
       controller.generate()
       
       assert IndexDefinition.count() > 0
       assert OperationalTemplateIndex.count() > 0
       
       assert response.redirectedUrl == '/indexDefinition/list'
   }
}
