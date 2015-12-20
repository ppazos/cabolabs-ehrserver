package ehr

import grails.converters.*

import java.text.SimpleDateFormat

import demographic.Person
import query.Query
import query.DataGet
import query.DataCriteria
import ehr.clinical_documents.IndexDefinition
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.data.DataValueIndex
import common.generic.DoctorProxy
import common.generic.AuditDetails
import common.change_control.Contribution
import common.change_control.VersionedComposition
import common.change_control.Version

import com.cabolabs.security.Organization

import grails.util.Holders
import groovy.util.slurpersupport.GPathResult

import java.lang.reflect.UndeclaredThrowableException
import javax.xml.bind.ValidationException

/**
 * TODO:
 * 
 * Make a list of all the error types and assign an error code, publish that list, and make an endpoint to get the list.
 * 
 * @author pab
 *
 */

class RestController {

   static allowedMethods = [commit: "POST", contributions: "GET"]
   
   def xmlService // Utilizado por commit
   def jsonService // Query composition with format = json

   // Para acceder a las opciones de localizacion 
   def config = Holders.config.app
   
   
   // TODO: un index con la lista de servicios y parametros de cada uno (para testing)
   
   def formatter = new SimpleDateFormat( config.l10n.datetime_format )
   def formatterDate = new SimpleDateFormat( config.l10n.date_format )
   
   /*
   def testVersionedObject()
   {
      def vo = VersionedComposition.get(1)
      render vo.getAllVersions() as JSON
   }
   */
   
   private void renderError(String msg, String errorCode, int status)
   {
      // Format comes from current request
      withFormat {
         xml {
            render(status: status, contentType:"text/xml", encoding:"UTF-8") {
               result {
                  type {
                     code('AR')                         // application reject
                     codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
                  }
                  message(msg)
                  code('ISIS_EHR_SERVER::COMMIT::ERRORS::'+ errorCode) // sys::service::concept::code
               }
            }
         }
         json {
            //println "error json"
            
            def error = [
               result: [
                  type: [
                     code: 'AR',
                     codeSystem: 'HL7::TABLES::TABLE_8'
                  ],
                  message: msg,
                  code: 'ISIS_EHR_SERVER::COMMIT::ERRORS::'+ errorCode
               ]
            ]
            
            //println "error json struct"
            
            //render error as JSON
            def result = error as JSON
            // JSONP
            if (params.callback) result = "${params.callback}( ${result} )"
            
            // with the status in render doesnt return the json to the client
            // http://stackoverflow.com/questions/10726318/easy-way-to-render-json-with-http-status-code-in-grails
            response.status = status
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
         html {
            render(status: status, contentType:"text/xml", encoding:"UTF-8") {
               result {
                  type {
                     code('AR')                         // application reject
                     codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
                  }
                  message(message(code:'rest.error.formatNotSupported'))
                  code('ISIS_EHR_SERVER::COMMIT::ERRORS::499') // sys::service::concept::code
               }
            }
         }
      }
   }
   
   
   /**
    * Envia una lista de versions para commitear al EHR(ehrUid)
    * 
    * @param String ehrUid
    * @param auditSystemId
    * @param auditCommitter
    * @param List versions
    * @return
    */
   def commit(String ehrUid, String auditSystemId, String auditCommitter)
   {
      log.info( "commit received "+ params.list('versions').size() + " versions"  )

      if (!ehrUid)
      {
         renderError(message(code:'rest.error.ehr_uid_required'), '400', 400)
         return
      }
      if (!auditSystemId)
      {
         renderError(message(code:'rest.error.auditSystemId_required'), '400', 400)
         return
      }
      if (!auditCommitter)
      {
         renderError(message(code:'rest.error.auditCommitter_required'), '400', 400)
         return
      }

      def ehr = Ehr.findByUid(ehrUid)
      if (!ehr)
      {
         renderError(message(code:'rest.error.ehr_doesnt_exists', args:[ehrUid]), '403', 404)
         return
      }
      

      /*
       * <versions>
       *  <version>
       *  ...
       *  </version>
       * </version>
       */
      def versionsXML = request.reader?.text // GString
      
      // 2. versions deben venir 1 por lo menos haber una
      if (!versionsXML)
      {
         renderError(message(code:'rest.commit.error.versionsRequired'), '401', 400)
         return
      }
      
      
      def slurper = new XmlSlurper(false, false)
      def _parsedVersions = slurper.parseText(versionsXML)
      
      
      //println "class "+ _parsedVersions.getClass() // groovy.util.slurpersupport.NodeChild
      //println _parsedVersions.children()*.name()
      //println _parsedVersions.version.size()
      
      
      if (_parsedVersions.isEmpty())
      {
         renderError(message(code:'rest.commit.error.versionsEmpty'), '402', 400)
         return
      }
      if (_parsedVersions.version.size() == 0)
      {
         renderError(message(code:'rest.commit.error.versionsEmpty'), '402.1', 400)
         return
      }
      

      try
      {
         // throws exceptions for any error
         xmlService.processCommit(ehr, _parsedVersions, auditSystemId, new Date(), auditCommitter)

         /* **
          * The time_committed attribute in both the Contribution and Version audits
          * should reflect the time of committal to an EHR server, i.e. the time of
          * availability to other users in the same system. It should therefore be
          * computed on the server in implementations where the data are created
          * in a separate client context.
          * 
          * Note that this will override the time_committed from the version in the XML received.
          */
      }
      catch (ValidationException e) // xsd error
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message(message(code:'rest.commit.error.versionsDontValidate'))
               details {
                  
                  xmlService.validationErrors.each { i, errorList ->
                     errorList.each { errorText ->
                        
                        item('Error on version #'+ i +') '+ errorText)
                     }
                  }
               }
            }
         }
         return
      }
      catch (UndeclaredThrowableException e)
      {
         // http://docs.oracle.com/javase/7/docs/api/java/lang/reflect/UndeclaredThrowableException.html
         renderError(message(code:'rest.commit.error.cantProcessCompositions', args:[e.cause.message]), '481', 400)
         return
      }
      catch (Exception e)
      {
         log.error( e.message +" "+ e.getClass().getSimpleName() ) // FIXME: the error might be more specific, see which errors we can have.
         println e.message +" "+ e.getClass().getSimpleName()
         
         // trace
         StringWriter writer = new StringWriter()
         PrintWriter printWriter = new PrintWriter( writer )
         e.printStackTrace( printWriter )
         printWriter.flush()
         String stackTrace = writer.toString()
         
         //println stackTrace
         //println "Mensaje >: "+ g.message(code:'rest.commit.error.cantProcessCompositions', args:[e.message +" trace: "+ stackTrace])
//         def appCtx = grailsApplication.getMainContext()
//         def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
//         println "Mensaje >: "+ appCtx.getMessage("rest.commit.error.cantProcessCompositions",
//            [e.message +" trace: "+ stackTrace] as Object[],
//            "error",
//            locale)
         
         renderError(g.message(code:'rest.commit.error.cantProcessCompositions', args:[e.message +" trace: "+ stackTrace]), '468', 400)
         return
      }
      
      render(contentType:"text/xml", encoding:"UTF-8") {
         result {
            type {
               code('AA')                         // application reject
               codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
            }
            message('Versions successfully committed to EHR '+ ehrUid)
            // has no error code
         }
      }
   } // commit


   /**
    * Enpoint for checking out the last version of a composition in order to create a new one.
    * The query services don't allow versioning the retrieved compositions because don't include
    * the version id that is necessary to create a new version of a composition. 
    * 
    * @param ehrUid
    * @param compositionUid
    * @return
    */
   def checkout(String ehrUid, String compositionUid)
   {
      //println params
      
      def versions = Version.withCriteria {
         data {
            eq('uid', compositionUid)
         }
      }
      
      // Error cases, just 1 version should be found
      if (versions.size() == 0)
      {
         renderError(message(code:'rest.commit.error.versionDoesntExists'), '412', 404)
         return
      }
      
      if (versions.size() > 1)
      {
         renderError(message(code:'rest.commit.error.moreThanOneVersion'), '413', 500)
         // LOG a disco este caso no se deberia dar
         return
      }
      
      def version = versions[0]
      
      // Double check: not really necessary (if the client has the compoUid is because it already has permissions.
      if(version.contribution.ehr.uid != ehrUid)
      {
         renderError(message(code:'rest.commit.error.contributionInconsistency'), '414', 500)
         return
      }
      
      // ======================================================================
      // The result Version have the same XML format as the one used for commit
      
      // Get the version file
      def vf = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml")
      if (!vf.exists() || !vf.canRead())
      {
         renderError(message(code:'rest.commit.error.versionDataNotFound'), '415', 500)
         return
      }
      
      def xml = vf.getText()
      
      render(text: xml, contentType:"text/xml", encoding:"UTF-8")
   }
   
   
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
                        uid(_ehr.uid)
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
               uid: _ehr.uid,
               dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
               subjectUid: _ehr.subject.value,
               systemId: _ehr.systemId
            ]
         }

         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrList
   
   
   def ehrForSubject(String subjectUid, String format)
   {
      if (!subjectUid)
      {
         renderError(message(code:'rest.error.patient_uid_required'), "455", 400)
         return
      }
      
      // ===========================================================================
      // 1. Paciente existe?
      //
      def _subject = Person.findByUidAndRole(subjectUid, 'pat')
      if (!_subject)
      {
         //render(status: 500, text:"<result><code>error</code><message>No existe el paciente $subjectUid</message></result>", contentType:"text/xml", encoding:"UTF-8")
         renderError(message(code:'rest.error.patient_doesnt_exists', args:[subjectUid]), "477", 404)
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
               uid(_ehr.uid)
               dateCreated( this.formatter.format( _ehr.dateCreated ) )
               delegate.subjectUid(_ehr.subject.value) // delegate para que no haya conflicto con la variable con el mismo nombre
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            uid: _ehr.uid,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]
         
         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrForSubject
   
   
   def ehrGet(String ehrUid, String format)
   {
      if (!ehrUid)
      {
         renderError(message(code:'rest.error.ehr_uid_required'), "456", 400)
         return
      }
      
      // 1. EHR existe?
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         eq ('uid', ehrUid)
      }
      
      if (!_ehr)
      {
         renderError(message(code:'rest.error.ehr_doesnt_exists', args:[ehrUid]), "478", 404)
         return
      }
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehr'{
               uid(_ehr.uid)
               dateCreated( this.formatter.format( _ehr.dateCreated ) ) // TODO: format
               subjectUid(_ehr.subject.value)
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            uid: _ehr.uid,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]

         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
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
                     delegate.patient { // THIS IS CALLING getPatient!!!
                        uid(person.uid)
                        firstName(person.firstName)
                        lastName(person.lastName)
                        dob(this.formatterDate.format( person.dob ) )
                        sex(person.sex)
                        idCode(person.idCode)
                        idType(person.idType)
                        organizationUid(person.organizationUid)
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
               idType: person.idType,
               organizationUid: person.organizationUid
            ]
         }
         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // patientList
   
   
   // Get patient data
   def patient(String uid, String format)
   {
      println "patient "+ params
      
      if (!uid)
      {
         //render(status: 500, text:"<result><code>error</code><message>uid es obligatorio</message></result>", contentType:"text/xml", encoding:"UTF-8")
         renderError(message(code:'rest.error.patient_uid_required'), "455", 400)
         return
      }
      
      def person = Person.findByRoleAndUid('pat', uid)
      if (!person)
      {
         //render(status: 500, text:"<result><code>error</code><message>patient doesnt exists</message></result>", contentType:"text/xml", encoding:"UTF-8")
         renderError(message(code:'rest.error.patient_doesnt_exists', args:[uid]), "477", 404)
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
               organizationUid(person.organizationUid)
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
            idType: person.idType,
            organizationUid: person.organizationUid
         ]
         
         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   }
   
   /*
    * Servicios sobre consultas.
    */
    // Get query
   def queryShow(String queryUid,String format)
   {
      println params
      
      if (!queryUid)
      {
         //render(status: 500, text:"<result><code>error</code><message>uid es obligatorio</message></result>", contentType:"text/xml", encoding:"UTF-8")
          renderError(message(code:'rest.error.query_uid_required'), "455", 400)
         return
      }
      
      def query=Query.findByUid(queryUid)
       
      if (!query)
      {
         //render(status: 500, text:"<result><code>error</code><message>patient doesnt exists</message></result>", contentType:"text/xml", encoding:"UTF-8")
         renderError(message(code:'rest.error.query_doesnt_exists', args:[queryUid]), "477", 404)
         return
      }
      
      //println query
      
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            delegate.query{
               uid(query.uid)
               name(query.name)
               format(query.format)
               type(query.type)
               
               if (query.type == 'composition')
               {
                  for (criteria in query.where)
                  {
                     delegate.criteria {
                        archetypeId(criteria.archetypeId)
                        path(criteria.path)
                        delegate.criteria(criteria.toSQL())
                        //value(criteria.value)
                     }
                  }
               }
               else
               {
                  group(query.group) // Group is only for datavalue
                  
                  for (proj in query.select)
                  {
                     projection {
                        archetypeId(proj.archetypeId)
                        path(proj.path)
                     }
                  }
               }
               
               
               //template_id(query.templateId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            uid: query.uid,
            name: query.name,
            format: query.format,
            type: query.type
         ]
         if (query.type == 'composition')
         {
            data.criteria = query.where.collect { [archetypeId: it.archetypeId, path: it.path, criteria: it.toSQL()] }
         }
         else
         {
            data.group = query.group // Group is only for datavalue
            data.projections = query.select.collect { [archetypeId: it.archetypeId, path: it.path] }
         }
         
         
         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</message></result>", contentType:"text/xml", encoding:"UTF-8")
      }
   }


   def queryList(String format,String queryName,String descriptionContains,int max, int offset)
   {
      println params 
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      def _queries 
      if (!queryName && !descriptionContains)
      {
         _queries = Query.list(max: max, offset: offset, readOnly: true)
      }
      else
      {
         if (!descriptionContains)
         {
            def criteria = Query.createCriteria()
            _queries = criteria.list (max: max, offset: offset, readOnly: true) {
                          like('name', '%'+queryName.replace('.',' ')+'%')
                       }
         }
         else
         {
            def criteria = Query.createCriteria()
            _queries = criteria.list (max: max, offset: offset, readOnly: true) {
                          like('name', '%'+descriptionContains+'%')
                       }
         }
      }     
      
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
                           delegate.format(query.format)
                           type(query.type)
                           
                           if (query.type == 'composition')
                           {
                              for (criteria in query.where)
                              {
                                 delegate.criteria {
                                    archetypeId(criteria.archetypeId)
                                    path(criteria.path)
                                    delegate.criteria(criteria.toSQL())
                                    //value(criteria.value)
                                 }
                              }
                           }
                           else
                           {
                              group(query.group) // Group is only for datavalue
                              
                              for (proj in query.select)
                              {
                                 projection {
                                    archetypeId(proj.archetypeId)
                                    path(proj.path)
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
         
            def data = [
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
                  'format': query.format,
                  type: query.type
               ]
               
               if (query.type == 'composition')
               {
                  jquery.criteria = query.where.collect { [archetypeId: it.archetypeId, path: it.path, criteria: it.toSQL()] }
               }
               else
               {
                  jquery.group = query.group // Group is only for datavalue
                  jquery.projections = query.select.collect { [archetypeId: it.archetypeId, path: it.path] }
               }
               
               data.queries << jquery
            }
         
            
            def result = data as JSON
            // JSONP
            if (params.callback) result = "${params.callback}( ${result} )"
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
      }
   }
   
   /*
    * REST service to query data and compositions executing an existing Query instance.
    * @param retrieveData only used for composition queries
    * @param showUI only used for composition queries to retrieve HTML (FIXME: this might be another output format)
    * @param group grouping of datavalue queries, if not empty/null, will override the query group option ['composition'|'path']
    */
   def query(String queryUid, String ehrUid, String format, 
             boolean retrieveData, boolean showUI, String group,
             String fromDate, String toDate, String organizationUid)
   {
      //println "rest/query"
      //println params
      
      if (!queryUid)
      {
         renderError(message(code:'query.execute.error.queryUidMandatory'), '455', 400)
         return
      }
      if (!organizationUid)
      {
         renderError(message(code:'query.execute.error.organizationUidMandatory'), '457', 400)
         return
      }
      if (Organization.countByUid(organizationUid) == 0)
      {
         renderError(message(code:'query.execute.error.organizationDoesntExists', args:[organizationUid]), '456', 404)
         return
      }
      
      if (ehrUid)
      {
         def ehr = Ehr.findByUid(ehrUid)
         if (!ehr)
         {
            renderError(message(code:'rest.error.ehr_doesnt_exists', args:[ehrUid]), '403', 404)
            return
         }
         
         if (ehr.organizationUid != organizationUid)
         {
            renderError(message(code:'rest.error.ehr_doesnt_belong_to_organization', args:[ehrUid, organizationUid]), '458', 400)
            return
         }
      }
      
      
      def query = Query.findByUid(queryUid)      
      if (!query)
      {
         renderError(message(code:'query.execute.error.queryDoesntExists', args:[queryUid]), '456', 404)
         return
      }
      
      // --------------------------------------------------------------
      // FIXME: do query execution and output processing in a service
      // --------------------------------------------------------------
      
      // parse de dates
      Date qFromDate
      Date qToDate

      if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
      if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)
      
      def res = query.execute(ehrUid, qFromDate, qToDate, group, organizationUid)
      
      
      // If not format is specified, take the query format.
      if (!format) format = query.format
         
      
      // Output as XMl or JSON. For type=composition format is always XML.
      if (query.type == 'composition')
      {
         // If no ehrUid was specified, the results will be for different ehrs
         // we need to group those CompositionIndexes by EHR.
         if (!ehrUid)
         {
            res = res.groupBy { ci -> ci.ehrUid }
         }
         
         // Muestra compositionIndex/list
         if (showUI)
         {
            // FIXME: hay que ver el tema del paginado
            render(template:'/compositionIndex/listTable',
                   model:[
                      compositionIndexInstanceList: res,
                      //compositionIndexInstanceTotal:res.size(),
                      groupedByEhr: (!ehrUid)
                   ],
                   contentType: "text/html")
            return
         }
         
         // Devuelve CompositionIndex, si quiere el contenido es buscar las
         // compositions que se apuntan por el index
         if (!retrieveData)
         {
            if (format == 'json')
               render(text:(res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
            else
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
          def version
          String buff
          String out = '<?xml version="1.0" encoding="UTF-8"?><list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">\n'
          
          if (!ehrUid) // group by ehrUid
          {
             res.each { _ehrUid, compoIndexes ->
                
                out += '<ehr uid="'+ _ehrUid +'">'
                
                // idem else, TODO refactor
                compoIndexes.each { compoIndex ->
                   
                   // FIXME: verificar que esta en disco, sino esta hay un problema
                   //        de sincronizacion entre la base y el FS, se debe omitir
                   //        el resultado y hacer un log con prioridad alta para ver
                   //        cual fue el error.
                   
                   // adds the version, not just the composition
                   version = compoIndex.getParent()
                   buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()
      
                   
                   buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
                   buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
                   buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
                   
                   /**
                    * Composition queda:
                    *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
                    */
                   
                   out += buff + "\n"
                }
                out += '</ehr>'
             }
          }
          else
          {
             res.each { compoIndex ->
                
                // FIXME: verificar que esta en disco, sino esta hay un problema
                //        de sincronizacion entre la base y el FS, se debe omitir
                //        el resultado y hacer un log con prioridad alta para ver
                //        cual fue el error.
                
                // adds the version, not just the composition
                version = compoIndex.getParent()
                buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()
   
                buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
                buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
                buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
                
                /**
                 * Composition queda:
                 *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
                 */
                
                out += buff + "\n"
            }
         }
         out += '</list>'

         
         if (format == 'json')
            render(text: jsonService.xmlToJson(out), contentType:"application/json", encoding:"UTF-8")
         else
            render(text: out, contentType:"text/xml", encoding:"UTF-8")
         
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
   def queryData()
   {
      println "queryData"
      println params
      
      String qehrId = request.JSON.qehrId
      String fromDate = request.JSON.fromDate
      String toDate = request.JSON.toDate
      String qarchetypeId = request.JSON.qarchetypeId
      String format = request.JSON.format
      String group = request.JSON.group
      String organizationUid = request.JSON.organizationUid
      
      
      if (qehrId && organizationUid)
      {
         def ehr = Ehr.findByUid(qehrId)
         if (!ehr)
         {
            renderError(message(code:'rest.error.ehr_doesnt_exists', args:[qehrId]), '403', 404)
            return
         }
         
         if (ehr.organizationUid != organizationUid)
         {
            renderError(message(code:'rest.error.ehr_doesnt_belong_to_organization', args:[qehrId, organizationUid]), '458', 400)
            return
         }
      }
      
      
      // parse de dates
      Date qFromDate
      Date qToDate

      if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
      if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)
      
      def query = Query.newInstance(request.JSON.query)
      def res = query.executeDatavalue(qehrId, qFromDate, qToDate, group, organizationUid)
      
      
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
    * Para ejecutar queries desde la UI, recibe un objeto json con la query y los parametros.
    * Solo soporta XML.
    * @return
    */
   def queryCompositions()
   {
       println "queryCompositions"
       println params.toString()
       
       // all params come in the JSON object from the UI
       // all are strings
       boolean retrieveData = request.JSON.retrieveData.toBoolean() // http://mrhaki.blogspot.com/2009/11/groovy-goodness-convert-string-to.html
       boolean showUI = request.JSON.showUI.toBoolean()
       String qehrId = request.JSON.qehrId
       String fromDate = request.JSON.fromDate
       String toDate = request.JSON.toDate
       String qarchetypeId = request.JSON.qarchetypeId
       String format = request.JSON.format
       String organizationUid = request.JSON.organizationUid
       
       /*
       println request.JSON.retrieveData.getClass().getSimpleName()
       println request.JSON.showUI.getClass().getSimpleName()
       */
       println retrieveData.toString() +" "+ showUI.toString()
       
       
       if (qehrId && organizationUid)
       {
          def ehr = Ehr.findByUid(qehrId)
          if (!ehr)
          {
             renderError(message(code:'rest.error.ehr_doesnt_exists', args:[qehrId]), '403', 404)
             return
          }
          
          if (ehr.organizationUid != organizationUid)
          {
             renderError(message(code:'rest.error.ehr_doesnt_belong_to_organization', args:[qehrId, organizationUid]), '458', 400)
             return
          }
       }
       
       
       // parse de dates
       Date qFromDate
       Date qToDate
 
       if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
       if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)
       
       def query = Query.newInstance(request.JSON.query)
       def cilist = query.executeComposition(qehrId, qFromDate, qToDate, organizationUid)
       def result = cilist
       
       // If no ehrUid was specified, the results will be for different ehrs
       // we need to group those CompositionIndexes by EHR.
       if (!qehrId)
       {
          result = cilist.groupBy { ci -> ci.ehrUid }
       }
       
       println "Resultados (CompositionIndex): " + cilist
       
       
       // Muestra compositionIndex/list
       if (showUI)
       {
          // FIXME: hay que ver el tema del paginado
          render(template:'/compositionIndex/listTable',
                 model:[
                    compositionIndexInstanceList:  result,
                    //compositionIndexInstanceTotal: cilist.size(),
                    groupedByEhr: (!qehrId)
                 ],
                 contentType: "text/html")
          return
       }
       
       // Devuelve CompositionIndex, si quiere el contenido es buscar las
       // compositions que se apuntan por el index
       if (!retrieveData)
       {
          if (format == 'json')
             render(text:(result as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
          else
             render(text:(result as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
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
       def version
       String buff
       String out = '<?xml version="1.0" encoding="UTF-8"?><list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">\n'
       
       if (!qehrId) // group by ehrUid
       {
          result.each { ehrUid, compoIndexes ->
             
             out += '<ehr uid="'+ ehrUid +'">'
             
             // idem else, TODO refactor
             compoIndexes.each { compoIndex ->
                
                // FIXME: verificar que esta en disco, sino esta hay un problema
                //        de sincronizacion entre la base y el FS, se debe omitir
                //        el resultado y hacer un log con prioridad alta para ver
                //        cual fue el error.
                
                // adds the version, not just the composition
                version = compoIndex.getParent()
                buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()
   
                
                buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
                buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
                buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
                
                /**
                 * Composition queda:
                 *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
                 */
                
                out += buff + "\n"
             }
             out += '</ehr>'
          }
       }
       else
       {
          result.each { compoIndex ->
             
             // FIXME: verificar que esta en disco, sino esta hay un problema
             //        de sincronizacion entre la base y el FS, se debe omitir
             //        el resultado y hacer un log con prioridad alta para ver
             //        cual fue el error.
             
             // adds the version, not just the composition
             version = compoIndex.getParent()
             buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()

             
             buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
             buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
             buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
             
             /**
              * Composition queda:
              *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
              */
             
             out += buff + "\n"
          }
       }
       out += '</list>'
       
       
       if (format == 'json')
          render(text: jsonService.xmlToJson(out), contentType:"application/json", encoding:"UTF-8")
       else
          render(text: out, contentType:"text/xml", encoding:"UTF-8")
       
   }
   
   
   /**
    * 
    * @param ehrUid
    * @param from   date in string format yyyyMMdd
    * @param to     date in string format yyyyMMdd
    * @param max
    * @param offset
    * @param format xml or json
    * @return
    */
   def contributions(String ehrUid, String from, String to, int max, int offset, String format)
   {
      Date dateFrom
      Date dateTo

      if (from) dateFrom = Date.parse(config.l10n.date_format, from)
      if (to) dateTo = Date.parse(config.l10n.date_format, to)
      
      println params
      println from
      println to
      
      if (!max)
      {
         max = 50
         offset = 0
      }
      
      def res = Contribution.createCriteria().list(max: max, offset: offset) {
         
         if (ehrUid)
         {
            ehr {
               eq('uid', ehrUid)
            }
         }
         if (dateFrom && !dateTo)
         {
            audit {
               ge('timeCommitted', dateFrom)
            }
         }
         if (!dateFrom && dateTo)
         {
            audit {
               le('timeCommitted', dateTo)
            }
         }
         if (dateFrom && dateTo)
         {
            audit {
               ge('timeCommitted', dateFrom)
               le('timeCommitted', dateTo)
            }
         }
         
      }
      
      
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
}
