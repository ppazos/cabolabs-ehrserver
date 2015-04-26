import demographic.Person
import common.generic.PatientProxy
import ehr.Ehr
import ehr.clinical_documents.IndexDefinition

class BootStrap {

   private static String PS = System.getProperty("file.separator")
   
   def init = { servletContext ->
      
     // TEST
     if (IndexDefinition.count() == 0)
     {
		  /*
        def ai = new com.cabolabs.archetype.ArchetypeIndexer()
        ai.index("openEHR-EHR-COMPOSITION.signos.v1")
        ai.index("openEHR-EHR-COMPOSITION.orden_de_estudio_de_laboratorio.v1")
        ai.index("openEHR-EHR-COMPOSITION.orden_de_estudios_imagenologicos.v1")
		  */
		  def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
		  //ti.indexAll()
        
        def path = "opts" + PS + "Signos.opt"
        def signos = new File( path )
        ti.index(signos)
     }
     // /TEST
     
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
     
     // Crea EHRs para los pacientes de prueba
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
      
      
      // ================================================
      // Indices de datos que definen los indices 
      // que pueden ser creados sobre instancias
      // de compositions XML que se commitean. 
      //
      // En base a estas definiciones se van a generar
      // indices de instancias de tipos de datos simples
      // sobre los cuales se van a poder consultar, incluso
      // mediante consultas con condicioens complejas.
      // ================================================
      
      // TODO: las definiciones de indices de datos (nivel 2)
      //       deben derivarse automaticamente de los arquetipos
      //       en la base de conocimiento.
      //       por ahora sn solo se usan arquetipos planos, luego
      //       se usan las referencias a los arquetipos originales
      //       que se usaron para aplanar.
      /*
      def dataIndexes = [
         
         // TODO: ver si necesito llegar hasta los tipos basicos o si alcanza con los tipos de openehr
         
         // RULE: Los nombres de las estructuras intermedias no importan para los indices!!!
         
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/', rmTypeName:'COMPOSITION'],
         
         // el uid de la compo es inyectado por el server
         // El uid no se indexa porque no es parte del criterio de busqueda
         // Ademas, la busqueda de Compositions por uid no es busqueda, es get
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/uid', gpath:'uid', rmTypeName:'HIER_OBJECT_ID'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/uid/value', gpath:'uid.value', rmTypeName:'String'],
         
         // FIXME: agregarlo a n1
         // name es el valor de la ontologia para at0000
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/name', rmTypeName:'DV_TEXT'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/name/value', rmTypeName:'String'],
         
         // category esta indexado en composition index
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/category', rmTypeName:'DV_CODED_TEXT'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/category/value', rmTypeName:'String'],
         // ... TODO category DV_CODED_TEXT
         
         // El composer se indexa a nivel 1 (CompositionIndex)
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/composer', gpath:'composer', rmTypeName:'PARTY_REF'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/composer/name', gpath:'composer.name', rmTypeName:'String'],
         
         // El start_time se indexa a nivel 1 (CompositionIndex)
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/context/start_time', gpath:'context.start_time', rmTypeName:'DV_DATE_TIME'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/context/start_time/value', gpath:'context.start_time.value', rmTypeName:'Date'], // hay una date serializada a string
         
         // TODO: indexar en nivel 1
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/context/setting', rmTypeName:'DV_CODED_TEXT'],
         // ... TODO setting DV_CODED_TEXT
         
         // TODO: ver que en una misma gpath puedo tener objetos de distinto tipo, la path es distinta pero la gpath es la misma...
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content',                      rmTypeName:'OBSERVATION'],
         
         // WARNING:
         // Todo Pathable tiene un nombre en el RM, es la copia del texto del arquetipo para el nodeID correspondiente
         // Para esto no se va a crear un indice porque no son datos, es mas para saber como mostrar la info, y no es realmente necesario guardar
         // ese dato en la base, si se tiene el nodeID y el arquetipo, se saca del arquetipo.
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/name',                 rmTypeName:'DV_TEXT'],
         
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/name/value',         rmTypeName:'String'],
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/subject',              rmTypeName:'PARTY_PROXY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]',     rmTypeName:'ITEM_LIST', name:'protocol'],
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/name', rmTypeName:'DV_TEXT'],
         
         // items es un contenedor de ELEMENT, tiene uno o mas
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]', rmTypeName:'ELEMENT', name:'location of measurement'],
         // cada ELEMENT en items tiene un nombre
         // en este caso el nombre es "location of measurement" (at0014)
         // se puede saber que es sin ver el nombre, usando el nodeID (que esta en la path!)
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/name', rmTypeName:'DV_TEXT'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/name/value', rmTypeName:'String'],
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/value', rmTypeName:'DV_CODED_TEXT', name:'location of measure'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/value/value', rmTypeName:'String'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/value/defining_code', rmTypeName:'CODE_PHRASE'],
         // La path no llega a los valores simples (terminology_id y code_string se codifican en un solo valor term_id::code)
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/value/defining_code/terminology_id', rmTypeName:'TERMINOLOGY_ID'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/value/defining_code/terminology_id/value', rmTypeName:'String'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/protocol[at0011]/items[at0014]/value/defining_code/code_string', rmTypeName:'String', name:'code'],
         
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]', rmTypeName:'HISTORY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/origin', rmTypeName:'DV_DATE_TIME', name:'history origin'],
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/origin/value', rmTypeName:'Date'], // hay una date serializada a string
         
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]', rmTypeName:'POINT_EVENT', name:'event'],
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/time', rmTypeName:'DV_DATE_TIME', name:'event date time'],
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/time/value', rmTypeName:'Date'], // hay una date serializada a string
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]', rmTypeName:'ITEM_LIST'],
         
         // ===================================================================
         // TODO: ver que para systolic y diastolic las gpaths son iguales,
         //       no diferencia segun nodos hermanos como si hacen las paths
         //       con los nodeID.
         //       esto es porque las paths son paths sobre definiciones, y
         //       las gpaths son paths sobre instancias (se deberian usar
         //       indices o algo parecido para poder hacer las consultas
         //       usando gpaths y usando las gpaths para extraer los datos
         //       correctos del XML para crear las instancias de indices
         //       de datos).
         // ===================================================================
         
         // systolic
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0004]', rmTypeName:'ELEMENT', name:'systolic'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/name', rmTypeName:'DV_TEXT'],
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/name/value', rmTypeName:'String'],
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value', rmTypeName:'DV_QUANTITY', name:'systolic'],
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude', rmTypeName:'Float'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units', rmTypeName:'String'],
         
         // diastolic
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]', rmTypeName:'ELEMENT', name:'diastolic'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/name', rmTypeName:'DV_TEXT'],
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/name/value', rmTypeName:'String'],
         [archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value', rmTypeName:'DV_QUANTITY', name:'diastolic'],
         // La path no llega a los valores simples
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude', rmTypeName:'Float'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.encounter.v1', path:'/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units', rmTypeName:'String']
         
         
         // ========================================================================================
         // Indices para registro de signos
         // name es el valor de la ontologia para at0000
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/name', rmTypeName:'DV_TEXT'],
         // category esta indexado en composition index
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/category', rmTypeName:'DV_CODED_TEXT'],
         
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/context/setting',           rmTypeName:'DV_CODED_TEXT'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content',                   rmTypeName:'OBSERVATION'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content/name',              rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content/subject',           rmTypeName:'PARTY_PROXY'],
         
         // presion arterial
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]', rmTypeName:'HISTORY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/origin', rmTypeName:'DV_DATE_TIME', name:'history origin'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]', rmTypeName:'POINT_EVENT', name:'event'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/time', rmTypeName:'DV_DATE_TIME', name:'event date time'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/data[at0003]', rmTypeName:'ITEM_TREE'],
         
         // systolic
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/data[at0003]/items[at0004]',      rmTypeName:'ELEMENT', name:'sistólica'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/data[at0003]/items[at0004]/name', rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/data[at0003]/items[at0004]/value', rmTypeName:'DV_QUANTITY', name:'sistólica'],
         
         // diastolic
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/data[at0003]/items[at0005]', rmTypeName:'ELEMENT', name:'diastólica'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/data[at0003]/items[at0005]/name', rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0006]/data[at0007]/events[at0002]/data[at0003]/items[at0005]/value', rmTypeName:'DV_QUANTITY', name:'diastólica'],
         // /presion arterial
         
         // Temperatura corporal
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]', rmTypeName:'HISTORY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]/origin', rmTypeName:'DV_DATE_TIME', name:'history origin'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]/events[at0010]', rmTypeName:'POINT_EVENT', name:'event'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]/events[at0010]/time', rmTypeName:'DV_DATE_TIME', name:'event date time'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]/events[at0010]/data[at0011]', rmTypeName:'ITEM_TREE'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]/events[at0010]/data[at0011]/items[at0012]',       rmTypeName:'ELEMENT', name:'temperatura'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]/events[at0010]/data[at0011]/items[at0012]/name',  rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0008]/data[at0009]/events[at0010]/data[at0011]/items[at0012]/value', rmTypeName:'DV_QUANTITY', name:'temperatura'],
         // /Temperatura corporal
         
         // Frecuencia cardiaca
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]', rmTypeName:'HISTORY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]/origin', rmTypeName:'DV_DATE_TIME', name:'history origin'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]/events[at0015]', rmTypeName:'POINT_EVENT', name:'event'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]/events[at0015]/time', rmTypeName:'DV_DATE_TIME', name:'event date time'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]/events[at0015]/data[at0016]', rmTypeName:'ITEM_TREE'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]/events[at0015]/data[at0016]/items[at0017]',       rmTypeName:'ELEMENT', name:'frecuencia cardiaca'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]/events[at0015]/data[at0016]/items[at0017]/name',  rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0013]/data[at0014]/events[at0015]/data[at0016]/items[at0017]/value', rmTypeName:'DV_QUANTITY', name:'frecuenca cardiaca'],
         // /Frecuencia cardiaca
         
         // Frecuencia respiratoria
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]', rmTypeName:'HISTORY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]/origin', rmTypeName:'DV_DATE_TIME', name:'history origin'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]/events[at0020]', rmTypeName:'POINT_EVENT', name:'event'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]/events[at0020]/time', rmTypeName:'DV_DATE_TIME', name:'event date time'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]/events[at0020]/data[at0021]', rmTypeName:'ITEM_TREE'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]/events[at0020]/data[at0021]/items[at0022]',       rmTypeName:'ELEMENT', name:'frecuencia respiratoria'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]/events[at0020]/data[at0021]/items[at0022]/name',  rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0018]/data[at0019]/events[at0020]/data[at0021]/items[at0022]/value', rmTypeName:'DV_QUANTITY', name:'frecuenca respiratoria'],
         // /Frecuencia respiratoria
         
         // Peso corporal
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]', rmTypeName:'HISTORY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]/origin', rmTypeName:'DV_DATE_TIME', name:'history origin'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]/events[at0025]', rmTypeName:'POINT_EVENT', name:'event'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]/events[at0025]/time', rmTypeName:'DV_DATE_TIME', name:'event date time'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]/events[at0025]/data[at0026]', rmTypeName:'ITEM_TREE'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]/events[at0025]/data[at0026]/items[at0027]',       rmTypeName:'ELEMENT', name:'peso'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]/events[at0025]/data[at0026]/items[at0027]/name',  rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0023]/data[at0024]/events[at0025]/data[at0026]/items[at0027]/value', rmTypeName:'DV_QUANTITY', name:'peso'],
         // /Peso corporal
         
         // Estatura
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]', rmTypeName:'HISTORY'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]/origin', rmTypeName:'DV_DATE_TIME', name:'history origin'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]/events[at0030]', rmTypeName:'POINT_EVENT', name:'event'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]/events[at0030]/time', rmTypeName:'DV_DATE_TIME', name:'event date time'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]/events[at0030]/data[at0031]', rmTypeName:'ITEM_TREE'],
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]/events[at0030]/data[at0031]/items[at0032]',       rmTypeName:'ELEMENT', name:'estatura'],
         // el nombre lo saco del arquetipo para el nodeID de la path
         //[archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]/events[at0030]/data[at0031]/items[at0032]/name',  rmTypeName:'DV_TEXT'],
         [archetypeId:'openEHR-EHR-COMPOSITION.signos.v1', path:'/content[at0028]/data[at0029]/events[at0030]/data[at0031]/items[at0032]/value', rmTypeName:'DV_QUANTITY', name:'estatura'],
         // /Estatura
      ]
      
      dataIndexes.each { map ->
         
         def di = new IndexDefinition(map)
         if (!di.save())
         {
            println di.errors
         }
      }
      */
      
   }
   def destroy = {
   }
}