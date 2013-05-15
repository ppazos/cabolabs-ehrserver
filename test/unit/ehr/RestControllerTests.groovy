package ehr

import static org.junit.Assert.*
import demographic.Person
import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(RestController)
@Mock([Ehr,Person])
class RestControllerTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testEhrList()
	{
	    println Person.count()
	    println Ehr.count()
	
        //fail "Implement me"
		controller.ehrList()
		
		println controller.response.text
    }
}
