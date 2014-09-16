package ehr

import grails.converters.*
import java.text.SimpleDateFormat
import demographic.Person
import query.Query
import query.DataGet
import query.DataCriteria
import ehr.clinical_documents.DataIndex
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.data.DataValueIndex
import common.generic.DoctorProxy
import common.generic.AuditDetails
import common.change_control.Contribution
import org.codehaus.groovy.grails.commons.ApplicationHolder

class RestController {

   def xmlService // Utilizado por commit

   // Para acceder a las opciones de localizacion 
   def config = ApplicationHolder.application.config.app
   
   /**
    * Auxiliar para consultas por datos (ej. queryCompositions)
    */
   // FIXME: must be part of Query
   static Map operandMap = [
     'eq': '=',
     'lt': '<',
     'gt': '>',
     'neq': '<>' // http://stackoverflow.com/questions/723195/should-i-use-or-for-not-equal-in-tsql
   ]
   
   // TODO: un index con la lista de servicios y parametros de cada uno (para testing)
   
   //def formatter = new SimpleDateFormat("yyyyMMdd'T'hhmmss.SSSSZ")
   //def formatterDate = new SimpleDateFormat("yyyyMMdd")
   def formatter = new SimpleDateFormat( config.l10n.datetime_format )
   def formatterDate = new SimpleDateFormat( config.l10n.date_format )
   
   
   
   /**
    * Envia una lista de versions para commitear al EHR(ehrId)
    * 
    * @param String ehrId
    * @param auditSystemId
    * @param auditCommitter
    * @param List versions
    * @return
    */
   def commit(String ehrId, String auditSystemId, String auditCommitter)
   {
      //println "commit "+ params
      
      //new File('params_debug.log') << params.toString()
      
      // TODO: todo debe ser transaccional, se hace toda o no se hace nada...
      
      // 1. ehrId debe venir
      if (!ehrId)
      {
         //render(text:'<result><code>error</code><message>No viene el parametro ehrId</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('El parametro ehrId es obligatorio')
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::400') // sys::service::concept::code
            }
         }
         
         return
      }
      
      // 2. versions deben venir 1 por lo menos haber una
      if (!params.versions)
      {
         //render(text:'<result><code>error</code><message>No viene el parametro versions</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('El parametro versions es obligatorio')
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::401') // sys::service::concept::code
            }
         }
         return
      }
      
      def xmlVersions = params.list('versions')
      if (xmlVersions.size() == 0)
      {
         //render(text:'<result><code>error</code><message>No viene ninguna version</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('El parametro versions esta vacio y debe enviarse por lo menos una version')
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::402') // sys::service::concept::code
            }
         }
         return
      }
      
      
      
      def ehr = Ehr.findByEhrId(ehrId)
      
      // 3. ehr debe existir
      if (!ehr)
      {
         //render(text:'<result><code>error</code><message>EHR no existe</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('No existe el EHR con ehrId '+ ehrId)
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::403') // sys::service::concept::code
            }
         }
         return
      }
      
      
      // En data esta el XML de la composition recibida
      List parsedCompositions = [] // List<GPathResult>
      def contributions = []
      try
      {
         contributions = xmlService.parseVersions(
            ehr, xmlVersions, 
            auditSystemId, new Date(), auditCommitter, // time_committed is calculated by the server to be compliant with the specs ** (see below)
            parsedCompositions)
         
         /* **
          * The time_committed attribute in both the Contribution and Version audits
          * should reflect the time of committal to an EHR server, i.e. the time of
          * availability to other users in the same system. It should therefore be
          * computed on the server in implementations where the data are created
          * in a separate client context.
          */
         
         // TEST: in general only one contribution will be created from a commit
         if (contributions.size() > 1) println "WARNING: there is more than one contribution from a commit"
      }
      catch (Exception e)
      {
         println e.message // FIXME: the error might be more specific, see which errors we can have.
         
         // Parsing error
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AA')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('Bad content, could not parse compositions')
               // ok va sin codigo de error
               //code('ISIS_EHR_SERVER::COMMIT::ERRORS::200') // sys::service::concept::code
            }
         }
         return
      }
      
      // test
      // muestra los uids en vacio porque el escritor de xml es lazy,
      // se escribe solo cuando se hace una salida ej. a un stream
      
      /*
      println "ehrController parsedCompositions uids " // + parsedCompositions.uid.value*.text() esto me muestra vacio!!!!
      parsedCompositions.each { pcomp ->
         //println pcomp.uid // vacio
         println new groovy.xml.StreamingMarkupBuilder().bind{ out << pcomp} // mejor que XMLUtil para serializar a String (TODO ver cual es mas rapido)
         println ""
      }
      */
      
      
      /* 
       * Now is created by the XmlService ...
       * 
      // uid se establece automaticamente
      def contribution = new Contribution(
         audit: new AuditDetails(
            systemId:      auditSystemId,
            timeCommitted: Date.parse(config.l10n.datetime_format, auditTimeCommitted),
            //,
            // changeType solo se soporta 'creation' por ahora
            //
            // El committer de la contribution es el mismo committer de todas
            // las versiones, cada version tiene su committer debe ser el mismo.
            committer: new DoctorProxy(
               name: auditCommitter
               // TODO: 'value' con el id
            )
         ),
         versions: versions
      )
            
      // test
      if (!contribution.audit.validate())
      {
         // FIXME: debe ser transaccional, sino salva esto, no salva nada...
         println contribution.audit.errors
      }
      */
      
      // ==============================================================================
      //
      // FIXME: parseVersions should return a list of contributions with versions,
      //        here I don't have the contribution variable...
      //
      // ==============================================================================
      
      // FIXME: dejar esta tarea a un job
      // Guarda compositions y crea indices a nivel de documento (nivel 1)
      def compoFile
      def compoIndex
      def startTime
      
      contributions.each { contribution ->
         contribution.versions.eachWithIndex { version, i ->
            
            // Cuidado, genera los xmls con <?xml version="1.0" encoding="UTF-8"?>
            // Guardar la composition en el filesystem
            // TODO: path configurable
            // TODO: guardar en repositorio temporal, no en el de commit definitivo
            // COMPOSITION tiene su uid asignado por el servidor como nombre
            //compoFile = new File("compositions\\"+version.data.value+".xml")
            compoFile = new File(config.composition_repo + version.data.uid +".xml")
            compoFile << groovy.xml.XmlUtil.serialize( parsedCompositions[i] )
            
            
            // Agrega composition al EHR
            ehr.addToCompositions( version.data ) // version.data ~ CompositionRef
            
            
            /* 
             * Codigo movido a XmlService
             * 
            // =====================================================================
            // Crea indice para la composition
            // =====================================================================
            
            // -----------------------
            // Obligatorios en el XML: lo garantiza xmlService.parseVersions
            // -----------------------
            //  - composition.category.value con valor 'event' o 'persistent'
            //    - si no esta o tiene otro valor, ERROR
            //  - composition.context.start_time.value
            //    - DEBE ESTAR SI category = 'event'
            //    - debe tener formato completo: 20070920T104614,0156+0930
            //  - composition.@archetype_node_id
            //    - obligatorio el atributo
            //  - composition.'@xsi:type' = 'COMPOSITION'
            // -----------------------
            */
         } // contribution.versions.each
      } // contributions.each
      
      
      //render(text:'<result><code>ok</code><message>EHR guardado</message></result>', contentType:"text/xml", encoding:"UTF-8")
      render(contentType:"text/xml", encoding:"UTF-8") {
         result {
            type {
               code('AA')                         // application reject
               codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
            }
            message('Commit exitoso al EHR '+ ehrId)
            // ok va sin codigo de error
            //code('ISIS_EHR_SERVER::COMMIT::ERRORS::200') // sys::service::concept::code
         }
      }
      
   } // commit
   
   
   def ehrList(String format, int max, int offset)
   {
      // TODO: fromDate, toDate
      
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      // Lista ehrs
      def _ehrs = Ehr.list(max: max, offset: offset, readOnly: true)
      
      
      /*
      println params
      
      withFormat { 
         xml { println "xml" } 
         json { println "json" }
         html { println "html" }
         text { println "text" }         
      }
      */
      
      
      // ===========================================================================
      // 3. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         /*
         <result>
          <ehrs>
            <ehr>
              <ehrId>33b94e05-3da5-4291-872e-07b3a4664837</ehrId>
              <dateCreated>20121105T113730.0890-0200</dateCreated>
              <subjectUid>bf529d1c-b74a-4c4f-b6dd-c44c44cd9a3f</subjectUid>
              <systemId>ISIS_EHR_SERVER</systemId>
            </ehr>
            <ehr>
              <ehrId>d06e3256-d65e-436e-95da-5c9bffd05dbd</ehrId>
              <dateCreated>20121105T113732.0171-0200</dateCreated>
              <subjectUid>43a399c9-a5e0-4b51-9422-99c3991ea941</subjectUid>
              <systemId>ISIS_EHR_SERVER</systemId>
            </ehr>
          </ehrs>
          <pagination>...</pagination>
          </result>
          */
         //render(text: ehrs as XML, contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            'result' {
               'ehrs' {
                  _ehrs.each { _ehr ->
                     'ehr'{
                        ehrId(_ehr.ehrId)
                        dateCreated( this.formatter.format( _ehr.dateCreated ) ) // TODO: format
                        subjectUid(_ehr.subject.value)
                        systemId(_ehr.systemId)
                     }
                  }
               }
               pagination {
                  delegate.max(max)
                  delegate.offset(offset)
                  nextOffset(offset+max) // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
                  prevOffset( ((offset-max < 0) ? 0 : offset-max) )
               }
            }
         }
      }
      else if (format == "json")
      {
         /*
         {
          "ehrs": [
            {
              "ehrId": "33b94e05-3da5-4291-872e-07b3a4664837",
              "dateCreated": "20121105T113730.0890-0200",
              "subjectUid": "bf529d1c-b74a-4c4f-b6dd-c44c44cd9a3f",
              "systemId": "ISIS_EHR_SERVER"
            },
            {
              "ehrId": "d06e3256-d65e-436e-95da-5c9bffd05dbd",
              "dateCreated": "20121105T113732.0171-0200",
              "subjectUid": "43a399c9-a5e0-4b51-9422-99c3991ea941",
              "systemId": "ISIS_EHR_SERVER"
            }
          ],
          "pagination": {...}
        }
        */
         def data = [
           ehrs: [],
           pagination: [
               'max': max,
               'offset': offset,
               nextOffset: offset+max, // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
               prevOffset: ((offset-max < 0) ? 0 : offset-max )
            ]
         ]
         
         _ehrs.each { _ehr ->
            data.ehrs << [
               ehrId: _ehr.ehrId,
               dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
               subjectUid: _ehr.subject.value,
               systemId: _ehr.systemId
            ]
         }
         
         //render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
         render data as JSON
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrList
   
   
   def ehrForSubject(String subjectUid, String format)
   {
      // ===========================================================================
      // 1. Paciente existe?
      //
      def _subject = Person.findByUidAndRole(subjectUid, 'pat')
      if (!_subject)
      {
         render(status: 500, text:"<result><code>error</code><message>No existe el paciente $subjectUid</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 2. Paciente tiene EHR?
      //
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         subject {
            eq ('value', subjectUid)
         }
      }
      if (!_ehr)
      {
         render(status: 500, text:"<result><code>error</code><message>EHR no encontrado para el paciente $subjectUid, se debe crear un EHR para el paciente</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 3. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehr'{
               ehrId(_ehr.ehrId)
               dateCreated( this.formatter.format( _ehr.dateCreated ) )
               delegate.subjectUid(_ehr.subject.value) // delegate para que no haya conflicto con la variable con el mismo nombre
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            ehrId: _ehr.ehrId,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrForSubject
   
   
   def ehrGet(String ehrUid, String format)
   {
      // 1. EHR existe?
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         eq ('ehrId', ehrUid)
      }
      if (!_ehr)
      {
         render(status: 500, text:"<result><code>error</code><message>EHR no encontrado para el ehrUid $ehrUid</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehr'{
               ehrId(_ehr.ehrId)
               dateCreated( this.formatter.format( _ehr.dateCreated ) ) // TODO: format
               subjectUid(_ehr.subject.value)
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            ehrId: _ehr.ehrId,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrGet
   
   
   
   def patientList(String format, int max, int offset)
   {
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      
      // ===========================================================================
      // 1. Lista personas con rol paciente
      //
      def subjects = Person.findAllByRole('pat', [max: max, offset: offset, readOnly: true])
      
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'result' {
               'patients' {
                  subjects.each { person ->
                     delegate.patient{
                        uid(person.uid)
                        firstName(person.firstName)
                        lastName(person.lastName)
                        dob(this.formatterDate.format( person.dob ) )
                        sex(person.sex)
                        idCode(person.idCode)
                        idType(person.idType)
                     }
                  }
               }
               pagination {
                  delegate.max(max)
                  delegate.offset(offset)
                  nextOffset(offset+max) // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
                  prevOffset( ((offset-max < 0) ? 0 : offset-max) )
               }
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            patients: [],
            pagination: [
               'max': max,
               'offset': offset,
               nextOffset: offset+max, // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
               prevOffset: ((offset-max < 0) ? 0 : offset-max )
            ]
         ]
         subjects.each { person ->
            data.patients << [
               uid: person.uid,
               firstName: person.firstName,
               lastName: person.lastName,
               dob: this.formatterDate.format( person.dob ),
               sex: person.sex,
               idCode: person.idCode,
               idType: person.idType
            ]
         }
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // patientList
   
   
   // Get patient data
   def getPatient(String uid, String format)
   {
      println params
      
      if (!uid)
      {
         render(status: 500, text:"<result><code>error</code><message>uid es obligatorio</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      def person = Person.findByRoleAndUid('pat', uid)
      
      if (!person)
      {
         render(status: 500, text:"<result><code>error</code><message>patient doesnt exists</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      //println person
      
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            delegate.patient{
               delegate.uid(person.uid)
               firstName(person.firstName)
               lastName(person.lastName)
               dob(this.formatterDate.format( person.dob ) )
               sex(person.sex)
               idCode(person.idCode)
               idType(person.idType)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            uid: person.uid,
            firstName: person.firstName,
            lastName: person.lastName,
            dob: this.formatterDate.format( person.dob ),
            sex: person.sex,
            idCode: person.idCode,
            idType: person.idType
         ]
         
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   }
   
   /*
    * Servicios sobre consultas.
    */
   def queryList(String format, int max, int offset)
   {
      println params
      
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      // Lista ehrs
      def _queries = Query.list(max: max, offset: offset, readOnly: true)
      
      // Si format es cualquier otra cosa, tira XML por defecto (no se porque)
      /*
      withFormat {
      
         xml { render 'xml' }
         json { render 'json' }
      }
      */
      
      withFormat {
      
         xml {
            render(contentType:"text/xml", encoding:"UTF-8") {
               'result' {
                  'queries' {
                     _queries.each { query ->
                        delegate.query {
                           uid(query.uid)
                           name(query.name) // FIXME: debe tener uid
                           type(query.type)
                           delegate.format(query.format)
                           qarchetypeId(query.qarchetypeId)
                           group(query.group)
                           
                           delegate.select {
                             query.select.each { _dataGet ->
                                get {
                                  archetypeId(_dataGet.archetypeId)
                                  path(_dataGet.path)
                                }
                             }
                           }
                        }
                     }
                  }
                  pagination {
                     delegate.max(max)
                     delegate.offset(offset)
                     nextOffset(offset+max) // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
                     prevOffset( ((offset-max < 0) ? 0 : offset-max) )
                  }
               }
            }
         }
         json {
         
            def result = [
               queries: [],
               pagination: [
                  max: max,
                  offset: offset,
                  nextOffset: offset + max,
                  prevoffset: ((offset-max < 0) ? 0 : offset-max)
               ]
            ]
            
            _queries.each { query ->
            
               def jquery = [
                  uid: query.uid,
                  name: query.name, // FIXME: debe tener uid
                  type: query.type,
                  'format': query.format,
                  qarchetypeId: query.qarchetypeId,
                  group: query.group,
                  
                  select: []
               ]
               
               query.select.each { _dataGet ->
                  jquery.select << [
                    archetypeId: _dataGet.archetypeId,
                    path: _dataGet.path
                  ]
               }
               
               
               result.queries << jquery
            }
         
            render(contentType:"application/json", encoding:"UTF-8") {
               
               result
            }
         }
      }
   }
   
   /*
    * REST service to query data and compositions executing an existing Query instance.
    * @param retrieveData only used for composition queries
    * @param showUI only used for composition queries to retrieve HTML (FIXME: this might be another output format)
    */
   def query(String queryUid, String ehrId, String format, boolean retrieveData, boolean showUI) // TODO: fechas
   {
      println "rest/query"
      println params
      
      // FIXME: all these returns should have a proper error JSON response. See commit service.
      if (!queryUid)
      {
         render "queryUid is mandatory"
         return
      }
      
      def query = Query.findByUid(queryUid)
      
      if (!query)
      {
         render "No existe la query con uid = $queryUid"
         return
      }
      
      if (!ehrId)
      {
         render "ehrId is mandatory"
         return
      }
      
      def ehr = Ehr.findByEhrId(ehrId)
      
      if (!ehr)
      {
         render "No existe la ehr con uid = $ehrId"
         return
      }
      
      // TODO: fechas
      def res = query.execute(ehrId, null, null)
      

      // Output as XMl or JSON. For type=composition format is always XML.
      if (query.type == 'composition')
      {
         // Muestra compositionIndex/list
         if (showUI)
         {
            // FIXME: hay que ver el tema del paginado
            render(template:'/compositionIndex/listTable',
                   model:[compositionIndexInstanceList: res, compositionIndexInstanceTotal:res.size()])
            return
         }
         
         // Devuelve CompositionIndex, si quiere el contenido es buscar las
         // compositions que se apuntan por el index
         if (!retrieveData)
         {
            // TODO: support for JSON
            render(text:(res as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
            return
         }

          // FIXME: hay que armar bien el XML: declaracion de xml solo al
          //        inicio y namespaces en el root.
          //
          //  REQUERIMIENTO:
          //  POR AHORA NO ES NECESARIO ARREGLARLO, listando los index y luego
          //  haciendo get por uid de la composition alcanza. Esto es mas para XRE
          //  para extraer datos con reglas sobre un conjunto de compositions en un
          //  solo XML.
          //
          // FIXME: no genera xml valido porque las compos se guardan con:
          // <?xml version="1.0" encoding="UTF-8"?>
          //
          String buff
          String out = '<?xml version="1.0" encoding="UTF-8"?><list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">\n'
          res.each { compoIndex ->
             
             // FIXME: verificar que esta en disco, sino esta hay un problema
             //        de sincronizacion entre la base y el FS, se debe omitir
             //        el resultado y hacer un log con prioridad alta para ver
             //        cual fue el error.
             
             // Tiene declaracion de xml
             // Tambien tiene namespace, eso deberia estar en el nodo root
             //buff = new File("compositions\\"+compoIndex.uid+".xml").getText()
             buff = new File(config.composition_repo + compoIndex.uid +".xml").getText()
             
             buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
             buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
             buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
             
             /**
              * Composition queda:
              *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
              */
             
             out += buff + "\n"
         }
         out += '</list>'
         
         render(text: out, contentType:"text/xml", encoding:"UTF-8")
         return
         
      } // type = composition
      else
      {
         // type = datavalue
         
         // Format
         if (!format || format == 'xml')
         {
            render(text:(res as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
         }
         else if (format == 'json')
         {
            render(text:(res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
         }
         else
         {
            render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
         }
      }
   } // query
   
   /**
    * Busqueda de datos simples dentro de compositions que cumplen cierto criterio.
    * Datos de nivel 2 por criterio nivel 1.
    * Se utiliza para mostrar datos tabulados y graficas.
    * 
    * @param archetypeId arquetipo donde esta la path al dato que se busca, uno o mas
    * @param path ruta dentro del arquetipo al dato que se busca, una o mas
    * @param qehrId id del ehr (obligatorio, los datos deben ser del mismo ehr/paciente)
    * @param qarchetypeId tipo de composition donde buscar (opcional)
    * @param format xml o json, xml por defecto
    * 
    * @return List<DataValueIndex>
    */
   // FIXME: verify that this is used only for query testing while creating a
   //        query. Query execution from the UI should use the "query" action.
   // To query by queryUID use "query" action.
   def queryData(String qehrId, String qarchetypeId, String fromDate, String toDate, String format, String group)
   {
      println "queryData"
      println params
      
      def type = 'datavalue'
      
      // =================================================================
      // Crea query temporal para ejecutar (idem QueryController.save)
      def query = new Query(qarchetypeId:qarchetypeId, type:type, format:format, group:group) // qarchetypeId puede ser vacio
      
      // En una consulta EQL archetypeId+path seria el SELECT
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
       
      // Crea seleccion para query de type datavaule
      archetypeIds.eachWithIndex { archId, i ->
          
         query.addToSelect(
            new DataGet(archetypeId:archId, path:paths[i])
         )
      }
      // =================================================================
      
      
      // parse de dates
      Date qFromDate
      Date qToDate

      if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
      if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)
      
      
      def res = query.executeDatavalue(qehrId, qFromDate, qToDate)
      

      // Format
      if (!format || format == 'xml')
      {
         render(text:(res as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         render(text:(res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
      }
      return
   }
   
   
   /**
    * Previo QueryController.testQueryByData
    * Solo soporta XML.
    * @return
    */
   def queryCompositions(String qehrId, String qarchetypeId, String fromDate, String toDate, boolean retrieveData, boolean showUI)
   {
       println "queryCompositions"
       println params
       
       
       // Viene una lista de cada parametro
       // String archetypeId, String path, String operand, String value
       // El mismo indice en cada lista corresponde con un atributo del mismo criterio de busqueda
       
       // Datos de criterios
       List archetypeIds = params.list('archetypeId')
       List paths = params.list('path')
       //List operands = params.list('operand')
       List values = params.list('value')
       
       // Con nombres eq, lt, ...
       // Hay que transformarlo a =, <, ...
       // No vienen los operadores directamente porque rompen en HTML, ej. <, >
       List operands = params.list('operand')
       operands = operands.collect {
          operandMap[it] // 'gt' => '>'
       }
       
       DataIndex dataidx
       String idxtype
 
       
       // parse de dates
       Date qFromDate
       Date qToDate
 
       if (fromDate)
          qFromDate = Date.parse(config.l10n.date_format, fromDate)
       
       if (toDate)
          qToDate = Date.parse(config.l10n.date_format, toDate)
       
       
       // Armado de la query
       String q = "FROM CompositionIndex ci WHERE "
       
       // ===============================================================
       // Criteria nivel 1 ehrId
       if (qehrId) q += "ci.ehrId = '" + qehrId + "' AND "
       
       // Criteria nivel 1 archetypeId (solo de composition)
       if (qarchetypeId) q += "ci.archetypeId = '" + qarchetypeId +"' AND "
       
       // Criterio de rango de fechas para ci.startTime
       // Formatea las fechas al formato de la DB
       if (qFromDate) q += "ci.startTime >= '"+ formatterDateDB.format( qFromDate ) +"' AND " // higher or equal
       if (qToDate) q += "ci.startTime <= '"+ formatterDateDB.format( qToDate ) +"' AND " // lower or equal
       
       //
       // ===============================================================
       
       /**
        * FIXME: issue #6
        * si en el create se verifican las condiciones para que a aqui no
        * llegue una path a un tipo que no corresponde, el error de tipo
        * no sucederia nunca, asi no hay que tirar except aca.
        */

       archetypeIds.eachWithIndex { archId, i ->
          
          // Lookup del tipo de objeto en la path para saber los nombres de los atributos
          // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
          dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
          idxtype = dataidx?.rmTypeName
          
          
          // Subqueries sobre los DataValueIndex de los CompositionIndex
          q +=
          " EXISTS (" +
          "  SELECT dvi.id" +
          "  FROM DataValueIndex dvi" +
          "  WHERE dvi.owner.id = ci.id" + // Asegura de que todos los EXISTs se cumplen para el mismo CompositionIndex (los criterios se consideran AND, sin esta condicion es un OR y alcanza que se cumpla uno de los criterios que vienen en params)
          "        AND dvi.archetypeId = '"+ archId +"'" +
          "        AND dvi.path = '"+ paths[i] +"'"
          
          // Consulta sobre atributos del DataIndex dependiendo de su tipo
          switch (idxtype)
          {
             // ADL Parser bug: uses Java class names instead of RM Type Names...
             case ['DV_DATE_TIME', 'DvDateTime']:
                q += "        AND dvi.value"+ operands[i] + values[i] // TODO: verificar formato, transformar a SQL
             break
             case ['DV_QUANTITY', 'DvQuantity']:
                q += "        AND dvi.magnitude"+ operands[i] + new Float(values[i])
             break
             case ['DV_CODED_TEXT', 'DvCodedText']:
                q += "        AND dvi.code"+ operands[i] +"'"+ values[i]+"'"
             break
             case ['DV_TEXT', 'DvText']:
                q += "        AND dvi.value"+ operands[i] +"'"+ values[i]+"'"
             break
             case ['DV_BOOLEAN', 'DvBoolean']:
                q += "        AND dvi.value"+ operands[i] + new Boolean(values[i])
             break
             default:
               throw new Exception("type $idxtype not supported")
          }
          q += ")"
          
          
          // Agrega ANDs para los EXISTs, menos el ultimo
          if (i+1 < archetypeIds.size()) q += " AND "
       }
       
       println "queryCompositions query: "
       println q
       
       /*
       EXISTS (
         SELECT dvi.id
         FROM DataIndex dvi
         WHERE dvi.owner.id = ci.id
               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value
               AND dvi.magnitude>140.0
       ) AND EXISTS (
         SELECT dvi.id
         FROM DataIndex dvi
         WHERE dvi.owner.id = ci.id
               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
               AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value
               AND dvi.magnitude<130.0
       ) AND EXISTS (
         SELECT dvi.id
         FROM DataIndex dvi
         WHERE dvi.owner.id = ci.id
               AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
               AND dvi.path = /content/data[at0001]/origin
               AND dvi.value>20080101
       )
       */
       
       
       // TODO: criterio por atributos del ci
       def cilist = CompositionIndex.findAll( q )
 
       println "Resultados (CompositionIndex): " + cilist
       
       
       // Muestra compositionIndex/list
       if (showUI)
       {
          // FIXME: hay que ver el tema del paginado
          render(view:'/compositionIndex/list',
                 model:[compositionIndexInstanceList: cilist, compositionIndexInstanceTotal:cilist.size()])
          return
       }
       
       // Devuelve CompositionIndex, si quiere el contenido es buscar las
       // compositions que se apuntan por el index
       if (!retrieveData)
       {
          render(text:(cilist as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
       }
       else
       {
          // FIXME: hay que armar bien el XML: declaracion de xml solo al
          //        inicio y namespaces en el root.
          //
          //  REQUERIMIENTO:
          //  POR AHORA NO ES NECESARIO ARREGLARLO, listando los index y luego
          //  haciendo get por uid de la composition alcanza. Esto es mas para XRE
          //  para extraer datos con reglas sobre un conjunto de compositions en un
          //  solo XML.
          //
          // FIXME: no genera xml valido porque las compos se guardan con:
          // <?xml version="1.0" encoding="UTF-8"?>
          //
          String buff
          String out = '<?xml version="1.0" encoding="UTF-8"?><list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">\n'
          cilist.each { compoIndex ->
             
             // FIXME: verificar que esta en disco, sino esta hay un problema
             //        de sincronizacion entre la base y el FS, se debe omitir
             //        el resultado y hacer un log con prioridad alta para ver
             //        cual fue el error.
             
             // Tiene declaracion de xml
             // Tambien tiene namespace, eso deberia estar en el nodo root
             //buff = new File("compositions\\"+compoIndex.uid+".xml").getText()
             buff = new File(config.composition_repo + compoIndex.uid +".xml").getText()
             
             buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
             buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
             buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
             
             /**
              * Composition queda:
              *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
              */
             
             out += buff + "\n"
          }
          out += '</list>'
          
          render(text: out, contentType:"text/xml", encoding:"UTF-8")
       }
   }
}