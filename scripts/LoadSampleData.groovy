import com.cabolabs.ehrserver.openehr.demographic.Person
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy

import com.cabolabs.security.RequestMap
import com.cabolabs.security.User
import com.cabolabs.security.Role
import com.cabolabs.security.UserRole
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*

import com.cabolabs.ehrserver.identification.PersonIdType
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.openehr.opt.manager.OptManager // load opts
import com.cabolabs.ehrserver.api.structures.PaginatedResults


// Init id types
if (PersonIdType.count() == 0)
{
   def idtypes = [
	  new PersonIdType(name:'DNI',  code:'DNI'),
	  new PersonIdType(name:'CI',   code:'CI'),
	  new PersonIdType(name:'Passport', code:'Passport'),
	  new PersonIdType(name:'SSN',  code:'SSN'),
	  new PersonIdType(name:'UUID', code:'UUID'),
	  new PersonIdType(name:'OID',  code:'OID')
   ]
   idtypes.each {
	  it.save(failOnError:true, flush:true)
   }
}

 def organizations = []
 if (Organization.count() == 0)
 {
	// Sample organizations
	organizations << new Organization(name: 'Hospital de Clinicas', number: '1234')
	organizations << new Organization(name: 'Clinica del Tratamiento del Dolor', number: '6666')
	organizations << new Organization(name: 'Cirugia Estetica', number: '5555')
	
	organizations.each {
	   it.save(failOnError:true, flush:true)
	}
 }
 else 
 	organizations = Organization.list()

 if (User.count() == 0)
 {
	def adminUser = new User(username: 'admin', email: 'pablo.pazos@cabolabs.com',  password: 'admin')
	adminUser.organizations = [organizations[0], organizations[1]]
	adminUser.save(failOnError: true,  flush: true)
	
	def orgManUser = new User(username: 'orgman', email: 'pablo.swp+orgman@gmail.com',  password: 'orgman')
	orgManUser.organizations = [organizations[0], organizations[1]]
	orgManUser.save(failOnError: true,  flush: true)
	
	//UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ADMIN')), true )
	//UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ORG_MANAGER')), true )
	//UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ORG_STAFF')), true )
	//UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_ORG_CLINICAL_MANAGER')), true )
	//UserRole.create( godlikeUser, (Role.findByAuthority('ROLE_USER')), true )
	
	UserRole.create( adminUser, (Role.findByAuthority('ROLE_ADMIN')), true )
	UserRole.create( orgManUser, (Role.findByAuthority('ROLE_ORG_MANAGER')), true )
}

 // Fake persons and roles
 def persons = []
 if (Person.count() == 0)
 {
	persons = [
	   new Person(
		   firstName: 'Pablo',
		   lastName: 'Pazos',
		   dob: new Date(81, 9, 24),
		   sex: 'M',
		   idCode: '4116238-0',
		   idType: PersonIdType.get(1).code,
		   role: 'pat',
		   uid: '11111111-1111-1111-1111-111111111111'
	   ),
	   new Person(
		   firstName: 'Barbara',
		   lastName: 'Cardozo',
		   dob: new Date(87, 2, 19),
		   sex: 'F',
		   idCode: '1234567-0',
		   idType: PersonIdType.get(1).code,
		   role: 'pat',
		   uid: '22222222-1111-1111-1111-111111111111'
	   ),
	   new Person(
		   firstName: 'Carlos',
		   lastName: 'Cardozo',
		   dob: new Date(80, 2, 20),
		   sex: 'M',
		   idCode: '3453455-0',
		   idType: PersonIdType.get(1).code,
		   role: 'pat',
		   uid: '33333333-1111-1111-1111-111111111111'
	   ),
	   new Person(
		   firstName: 'Mario',
		   lastName: 'Gomez',
		   dob: new Date(64, 8, 19),
		   sex: 'M',
		   idCode: '5677565-0',
		   idType: PersonIdType.get(1).code,
		   role: 'pat',
		   uid: '44444444-1111-1111-1111-111111111111'
	   ),
	   new Person(
		   firstName: 'Carla',
		   lastName: 'Martinez',
		   dob: new Date(92, 1, 5),
		   sex: 'F',
		   idCode: '84848884-0',
		   idType: PersonIdType.get(1).code,
		   role: 'pat',
		   uid: '55555555-1111-1111-1111-111111111111'
	   )
	   new Person(
		   firstName: 'test',
		   lastName: 'test',
		   dob: new Date(92, 1, 5),
		   sex: 'F',
		   idCode: '0000000-0',
		   idType: PersonIdType.get(1).code,
		   role: 'pat',
		   uid: '00000000-1111-0000-1111-000000000000'
	   )
	]
	
	def c = Organization.count()
	persons.eachWithIndex { p, i ->
	   
	   p.organizationUid = Organization.get(i % c + 1).uid
	   
	   if (!p.save())
	   {
		  println p.errors
	   }
	}
 }
 if (Ehr.count() == 0)
 {
	// Fake EHRs for patients
	// Idem EhrController.createEhr
	def ehr
	
	persons.eachWithIndex { p, i ->
	
	   if (p.role == 'pat')
	   {
		  ehr = new Ehr(
			 uid: p.uid, // the ehr id is the same as the patient just to simplify testing
			 subject: new PatientProxy(
				value: p.uid
			 ),
			 organizationUid: p.organizationUid
		  )
		
		  if (!ehr.save()) println ehr.errors
	   }
	}
 }
