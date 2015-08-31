import demographic.Person
import common.generic.PatientProxy
import ehr.Ehr
import ehr.clinical_documents.IndexDefinition
import grails.util.Holders

import com.cabolabs.openehr.opt.manager.OptManager // load opts

class BootStrap {

   private static String PS = System.getProperty("file.separator")
   
   def init = { servletContext ->
     
     
     // Used by query builder, all return String
     String.metaClass.asSQLValue = { operand ->
        if (operand == 'contains') return "'%"+ delegate +"%'" // Contains is translated to LIKE, we need the %
        return "'"+ delegate +"'"
     }
     Double.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     Integer.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     Long.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     Date.metaClass.asSQLValue = { operand ->
        def formatterDateDB = new java.text.SimpleDateFormat( Holders.config.app.l10n.db_datetime_format )
        return "'"+ formatterDateDB.format( delegate ) +"'" 
     }
     Boolean.metaClass.asSQLValue = { operand ->
        return delegate.toString()
     }
     
      
     log.debug( 'Current working dir: '+ new File(".").getAbsolutePath() ) // Current working directory
     
     
     // Initial index loading
     if (IndexDefinition.count() == 0)
     {
		  def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
		  ti.indexAll()
     }
     
     // OPT loading
     def optMan = OptManager.getInstance( Holders.config.app.opt_repo )
     optMan.loadAll()
     
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
               idType: 'CI',
               role: 'pat',
               uid: '11111111-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Barbara',
               lastName: 'Cardozo',
               dob: new Date(87, 2, 19),
               sex: 'F',
               idCode: '1234567-0',
               idType: 'CI',
               role: 'pat',
               uid: '22222222-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Carlos',
               lastName: 'Cardozo',
               dob: new Date(80, 2, 20),
               sex: 'M',
               idCode: '3453455-0',
               idType: 'CI',
               role: 'pat',
               uid: '33333333-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Mario',
               lastName: 'Gomez',
               dob: new Date(64, 8, 19),
               sex: 'M',
               idCode: '5677565-0',
               idType: 'CI',
               role: 'pat',
               uid: '44444444-1111-1111-1111-111111111111'
           ),
           new Person(
               firstName: 'Carla',
               lastName: 'Martinez',
               dob: new Date(92, 1, 5),
               sex: 'F',
               idCode: '84848884-0',
               idType: 'CI',
               role: 'pat',
               uid: '55555555-1111-1111-1111-111111111111'
           )
        ]
         
        persons.each { p ->
            
           if (!p.save())
           {
              println p.errors
           }
        }
     }
     
     // Fake EHRs for patients
     // Idem EhrController.createEhr
     def ehr
     persons.each { p ->
     
        if (p.role == 'pat')
        {
           ehr = new Ehr(
              ehrId: p.uid, // the ehr id is the same as the patient just to simplify testing
              subject: new PatientProxy(
                 value: p.uid
              )
           )
         
           if (!ehr.save()) println ehr.errors
        }
     }
   }
   
   def destroy = {
   }
}
