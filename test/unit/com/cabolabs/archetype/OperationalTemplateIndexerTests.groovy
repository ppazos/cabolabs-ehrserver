package com.cabolabs.archetype

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock(ehr.clinical_documents.IndexDefinition) // to allow calls to .save
class OperationalTemplateIndexerTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testSomething() {
        def opti = new com.cabolabs.archetype.OperationalTemplateIndexer()
        opti.indexAll()
    }
}