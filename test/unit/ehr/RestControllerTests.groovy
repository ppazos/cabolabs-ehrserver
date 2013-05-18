package ehr

import static org.junit.Assert.*
import demographic.Person
import common.generic.PatientProxy
import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(RestController)
@Mock([Ehr,Person,PatientProxy])
class RestControllerTests {

    void setUp()
	{
	    // Copiado de bootstrap porque no agarra las instancias en testing.
		def persons = [
         new Person(
            firstName: 'Pablo',
            lastName: 'Pazos',
            dob: new Date(81, 9, 24),
            sex: 'M',
            idCode: '4116238-0',
            idType: 'CI',
            role: 'pat'
         ),
         new Person(
            firstName: 'Barbara',
            lastName: 'Cardozo',
            dob: new Date(87, 2, 19),
            sex: 'F',
            idCode: '1234567-0',
            idType: 'CI',
            role: 'pat'
         ),
         new Person(
            firstName: 'Carlos',
            lastName: 'Cardozo',
            dob: new Date(80, 2, 20),
            sex: 'M',
            idCode: '3453455-0',
            idType: 'CI',
            role: 'pat'
         )
         ,
         new Person(
            firstName: 'Mario',
            lastName: 'Gomez',
            dob: new Date(64, 8, 19),
            sex: 'M',
            idCode: '5677565-0',
            idType: 'CI',
            role: 'pat'
         )
         ,
         new Person(
            firstName: 'Carla',
            lastName: 'Martinez',
            dob: new Date(92, 1, 5),
            sex: 'F',
            idCode: '84848884-0',
            idType: 'CI',
            role: 'pat'
         )
      ]
      
      persons.each { p ->
         
         if (!p.save())
         {
            println p.errors
         }
      }
	  
	  
	  // Crea EHRs para los pacientes de prueba
	  // Idem EhrController.createEhr
	  def ehr
	  persons.each { p ->
	  
	     if (p.role == 'pat')
		 {
			ehr = new Ehr(
			   subject: new PatientProxy(
			      value: p.uid
			   )
		    )
         
            if (!ehr.save()) println ehr.errors
		 }
	  }
    }

    void tearDown()
	{
        // Tear down logic here
    }

	void testPatientList()
	{
	    // Personas creados en el setUp
	    assert Person.count() == 5
		
		// Formato XML por defecto
		controller.patientList()
		println groovy.xml.XmlUtil.serialize( controller.response.text )
		assert controller.response.xml.patients.patient.size() == 5
		response.reset()
		
		
		// Formato incorrecto
		params.format = 'text'
		controller.patientList()
		assert controller.response.xml.code.text() == "error"
		response.reset()
	}
	
	
    void testEhrList()
	{
	    // EHRs creados en el setUp
	    assert Ehr.count() == 5
	
	
	    // sin format devuelve XML por defecto
		controller.ehrList()
		
		assert controller.response.contentType == "text/xml;charset=UTF-8"
		
		// pretty print del XML
		// Se puede cambiar la opcion en Config: 
		// test {
	    //   grails.converters.default.pretty.print = true
	    // }
		println groovy.xml.XmlUtil.serialize( controller.response.text )
		//println controller.response.text
		
		// ehrs debe tener 5 tags ehr
		// con .text es solo el texto, con .xml es el xml :)
		assert controller.response.xml.ehrs.ehr.size() == 5
		response.reset()
		
		
		// para que withFormat considere el param format> controller.request.format = 'json' 
		// http://grails.1312388.n4.nabble.com/withFormat-tests-in-ControllerUnitTestCase-td3343763.html
		params.format = 'json'
		controller.ehrList()
		assert controller.response.contentType == 'application/json;charset=UTF-8'
		println controller.response.text
		
		// json es un array y debe tener 5 objetos
		assert controller.response.json.ehrs.size() == 5
		response.reset()
		
		
		// Debe tirar error en XML porque no es un formato recocnocido
		params.format = 'text'
		controller.ehrList()
		//println controller.response.text
		println groovy.xml.XmlUtil.serialize( controller.response.text )
		assert controller.response.xml.code.text() == "error"
		response.reset()
		
		
		// Prueba paginacion con max=3
		params.format = 'xml'
		params.max = 3
		controller.ehrList()
		assert controller.response.xml.ehrs.ehr.size() == 3
		response.reset()
		
		// Prueba paginacion con offset=3 debe devolver 2 ehrs porque hay 5
		params.format = 'xml'
		params.offset = 3
		controller.ehrList()
		assert controller.response.xml.ehrs.ehr.size() == 2
		response.reset()
    }
}