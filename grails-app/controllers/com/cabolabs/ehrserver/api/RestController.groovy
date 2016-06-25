package com.cabolabs.ehrserver.api

import grails.converters.*
import java.text.SimpleDateFormat
import com.cabolabs.ehrserver.openehr.demographic.Person
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.DataGet
import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DataValueIndex
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.security.Organization

import grails.util.Holders
import groovy.util.slurpersupport.GPathResult
import java.lang.reflect.UndeclaredThrowableException
import javax.xml.bind.ValidationException

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.AuthenticationProvider
import com.cabolabs.security.UserPassOrgAuthToken
import com.cabolabs.security.User
import grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder // passwordEncoder
import com.cabolabs.ehrserver.openehr.composition.CompositionService
import com.cabolabs.util.DateParser

import grails.transaction.Transactional

/**
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class RestController {

   static allowedMethods = [login: "POST", commit: "POST", createPerson: "POST", contributions: "GET"]
   
   def messageSource
   
   def xmlService // Utilizado por commit
   def jsonService // Query composition with format = json
   def compositionService

   // Para acceder a las opciones de localizacion 
   def config = Holders.config.app
   
   
   // TODO: un index con la lista de servicios y parametros de cada uno (para testing)
   
   def formatter = new SimpleDateFormat( config.l10n.datetime_format )
   def formatterDate = new SimpleDateFormat( config.l10n.date_format )
   
   
   // test stateless security
   def statelessTokenProvider
   def userService
   def passwordEncoder = Holders.grailsApplication.mainContext.getBean('passwordEncoder')
   
   // This is used to generate controlled error codes.
   // See: https://github.com/ppazos/cabolabs-ehrserver/wiki/API-error-codes-and-messages
   static String format_error_code = '0066'
   
   // Map controller.actionName => action code
   static Map endpoint_codes = [
      'login': '01',         // POST /login
      'commit': '02',        // POST /commit
      'checkout': '03',      // GET /checkout
      'ehrList': '04',       // GET /ehrs
      'ehrForSubject': '05', // GET /ehrs/subjectUid/$subjectUid
      'ehrGet': '06',        // GET /ehrs/ehrUid/$ehrUid
      'patientList': '07',   // GET /patients
      'patient': '08',       // GET /patients/$uid
      'queryShow': '09',     // GET /queries/$queryUid
      'query': '10',         // GET /queries/$queryUid/execute
      'queryList': '11',     // GET /queries
      'contributions': '12'  // GET /contributions
   ]
   
   // FIXME: move logic to service
   def login()
   {
      // https://github.com/ppazos/cabolabs-ehrserver/blob/rest_security/src/groovy/com/cabolabs/security/AuthFilter.groovy
      // TODO check JSON payload for params...
      String username = params.username
      String password = params.password
      String organization_number = params.organization
      
      //UserPassOrgAuthToken auth = new UserPassOrgAuthToken(username, password, organization)
      //def username = auth.principal
      //def password = auth.credentials // plain text entered by the user
      //def organization_number = auth.organization
      try
      {
         def user = userService.getByUsername(username) //User.findByUsername(username)
         if (user == null)
         {
            throw new UsernameNotFoundException("No matching account")
         }
         
         // Status checks
         if (!user.enabled)
         {
            throw new DisabledException("Account disabled")
         }
         
         if (user.accountExpired)
         {
            throw new AccountExpiredException("Account expired")
         }
         
         if (user.accountLocked)
         {
            throw new LockedException("Account locked")
         }
         
         
         // Check password
         assert this.passwordEncoder != null
         
         if (!passwordEncoder.isPasswordValid(user.password, password, null))
         {
            throw new BadCredentialsException("Authentication failed")
         }
         
         //println 'orgn '+ organization_number
         
         if (!organization_number) // null or empty
         {
            throw new BadCredentialsException("Authentication failed - organization number not provided")
         }
         
         // Check organization
         Organization org = Organization.findByNumber(organization_number)
         
         //println 'org '+ org
         
         if (org == null)
         {
            //System.out.println( "organization with number does not exists" )
            throw new BadCredentialsException("Authentication failed")
         }
         
         //println 'user orgs '+ user.organizations
         
         if (!user.organizations.find{ it.uid == org.uid })
         {
            //System.out.println( "organization is not associated with user 2" )
            throw new BadCredentialsException("Authentication failed - check the organization number")
         }
      
         // TODO: refresh token
         render (['token': statelessTokenProvider.generateToken(username, null, [organization: organization_number])] as JSON)
      }
      catch (Exception e)
      {
         println e.getClass()
         render(status: 401, text: e.message)
      }
  }
   
   
   // FIXME this is customized for the commit but used from other endpoints
   private void renderError(String msg, String errorCode, int status)
   {
      // Format comes from current request
      withFormat {
         xml {
            println "render error XML"
            render(status: status, contentType:"text/xml", encoding:"UTF-8") {
               result {
                  type {
                     code('AR')                         // application reject
                     codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
                  }
                  message(msg)
                  code('EHR_SERVER::API::ERRORS::'+ errorCode) // sys::service::concept::code
               }
            }
         }
         json {
            println "render error JSON"
            def error = [
               result: [
                  type: [
                     code: 'AR',
                     codeSystem: 'HL7::TABLES::TABLE_8'
                  ],
                  message: msg,
                  code: 'EHR_SERVER::API::ERRORS::'+ errorCode
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
         '*' {
            println "render error *"
            render(status: status, contentType:"text/xml", encoding:"UTF-8") {
               result {
                  type {
                     code('AR')                         // application reject
                     codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
                  }
                  message(msg)
                  code('EHR_SERVER::API::ERRORS::'+ errorCode) // sys::service::concept::code
               }
            }
         }
      }
   }
   
   private void renderFormatNotSupportedError()
   {
      def error_code = 'e'+ endpoint_codes[actionName] +'.'+ format_error_code
      
      // 400 Bad Request
      render(status: 400, contentType:"text/xml", encoding:"UTF-8") {
         result {
            type {
               code('AR')                         // application reject
               codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
            }
            message(message(code:'rest.error.formatNotSupported'))
            code('EHR_SERVER::API::ERRORS::'+ error_code) // sys::service::concept::code
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
   @SecuredStateless
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
      
      // check permissions of the logged user over the ehr
      def _username = request.securityStatelessMap.username
      def _user = User.findByUsername(_username)
      if (!_user.organizations.uid.contains(ehr.organizationUid))
      {
         renderError(message(code:'query.execute.error.user_cant_access_ehr'), '4764', 403)
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
      
      // TODO: these errors should be related to parsing errors not just that the result is empty.
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
         String stackTrace = writer.toString() // FIXME: return only on dev
         
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
   @SecuredStateless
   def checkout(String ehrUid, String compositionUid)
   {
      if (!ehrUid)
      {
         renderError(message(code:'rest.commit.error.ehrUidIsRequired'), '411', 400)
         return
      }
      
      if (!compositionUid)
      {
         renderError(message(code:'rest.commit.error.compositionUidIsRequired'), '411', 400)
         return
      }
      
      
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         eq ('uid', ehrUid)
      }
      
      if (!_ehr)
      {
         renderError(message(code:'rest.error.ehr_doesnt_exists', args:[ehrUid]), "478", 404)
         return
      }
      
      // Check if the org used to login is the org of the requested ehr
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      if (_ehr.organizationUid != _org.uid)
      {
         renderError(message(code:'rest.error.cant_access_ehr', args:[ehrUid]), "483", 401)
         return
      }
      
      
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
      
      // this case is impossible: a compo has one version that contains it.
      if (versions.size() > 1)
      {
         renderError(message(code:'rest.commit.error.moreThanOneVersion'), '413', 500)
         // LOG a disco este caso no se deberia dar
         return
      }
      
      // only the latest version can be checked out
      if (!versions[0].data.lastVersion)
      {
         renderError(message(code:'rest.commit.error.versionIsNotTheLatest'), '416', 400)
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
         // This is another case that shouldn't happen, will happen only if the files are deleted from disk
         renderError(message(code:'rest.commit.error.versionDataNotFound'), '415', 500)
         return
      }
      
      def xml = vf.getText()
      
      render(text: xml, contentType:"text/xml", encoding:"UTF-8")
   }
   
   @SecuredStateless
   def ehrList(String format, int max, int offset)
   {
      // TODO: fromDate, toDate
      
      // test rest security
      //println "hello ${request.securityStatelessMap}" // [extradata:[organization:1234], issued_at:2015-12-27T14:26:53.802-03:00, username:admin]
      
      // Paginacion
      if (!max) max = 30
      if (!offset) offset = 0
      
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      // Lista ehrs
      def _ehrs = Ehr.findAllByOrganizationUid(_org.uid, [max: max, offset: offset, readOnly: true])
      
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
                        organizationUid(_ehr.organizationUid)
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
               systemId: _ehr.systemId,
               organizationUid: _ehr.organizationUid
            ]
         }

         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         renderFormatNotSupportedError()
      }
   } // ehrList
   
   @SecuredStateless
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
      
      // Check if the org used to login is the org of the requested patient
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      if (_subject.organizationUid != _org.uid)
      {
         renderError(message(code:'rest.error.cant_access_patient', args:[subjectUid]), "484", 401)
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
         render(status: 404, text:"<result><code>error</code><message>"+ message(code:'rest.error.patient_doesnt_have_ehr', args:[subjectUid]) +"</message></result>", contentType:"text/xml", encoding:"UTF-8")
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
               organizationUid(_ehr.organizationUid)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            uid: _ehr.uid,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId,
            organizationUid: _ehr.organizationUid
         ]
         
         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         renderFormatNotSupportedError()
      }
   } // ehrForSubject
   
   
   @SecuredStateless
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
      
      // Check if the org used to login is the org of the requested ehr
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      if (_ehr.organizationUid != _org.uid)
      {
         renderError(message(code:'rest.error.cant_access_ehr', args:[ehrUid]), "483", 401)
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
               organizationUid(_ehr.organizationUid)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            uid: _ehr.uid,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId,
            organizationUid: _ehr.organizationUid
         ]

         def result = data as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         renderFormatNotSupportedError()
      }
   } // ehrGet
   
   
   @SecuredStateless
   def patientList(String format, int max, int offset)
   {
      // Paginacion
      if (!max) max = 15
      if (!offset) offset = 0
      
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      // ===========================================================================
      // 1. Lista personas con rol paciente
      //
      //def subjects = Person.findAllByRoleAndOrganizationUid('pat', _org.uid, [max: max, offset: offset, readOnly: true])
      
      def c = Person.createCriteria()
      def subjects = c.list (max: params.max, offset: params.offset) {

         eq("role", "pat")
         eq("organizationUid", _org.uid)
         
         // filters
         if (params.firstName)
         {
            like('firstName', '%'+params.firstName+'%')
         }
         if (params.lastName)
         {
            like('lastName', '%'+params.lastName+'%')
         }
         if (params.sex)
         {
            eq('sex', params.sex) // sex should be eq
         }
         if (params.idCode)
         {
            like('idCode', '%'+params.idCode+'%')
         }
         if (params.idType)
         {
            eq('idType', params.idType) // idcode should be eq
         }
         
         // TODO: filter by dob range
         
         setReadOnly true
      }
      
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
         renderFormatNotSupportedError()
      }
   } // patientList
   
   
   // Get patient data
   @SecuredStateless
   def patient(String uid, String format)
   {
      log.info( "patient "+ params.toString() )
      
      if (!uid)
      {
         renderError(message(code:'rest.error.patient_uid_required'), "455", 400)
         return
      }
      
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      def person = Person.findByRoleAndUid('pat', uid)
      if (!person)
      {
         renderError(message(code:'rest.error.patient_doesnt_exists', args:[uid]), "477", 404)
         return
      }
      
      if (person.organizationUid != _org.uid)
      {
         renderError(message(code:'rest.error.cant_access_patient', args:[uid]), "484", 401)
         return
      }
      
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
         renderFormatNotSupportedError()
      }
   }
   
   
   /*
    * Servicios sobre consultas.
    */
    // Get query
   @SecuredStateless
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
         renderFormatNotSupportedError()
      }
   }

   
   @SecuredStateless
   def queryList(String format,String queryName,String descriptionContains,int max, int offset)
   {
      //println params 
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
         '*' {
            renderFormatNotSupportedError()
         }
      }
   }
   
   
   /*
    * REST service to query data and compositions executing an existing Query instance.
    * @param retrieveData only used for composition queries
    * @param showUI only used for composition queries to retrieve HTML (FIXME: this might be another output format)
    * @param group grouping of datavalue queries, if not empty/null, will override the query group option ['composition'|'path']
    */
   @SecuredStateless
   def query(String queryUid, String ehrUid, String format, 
             boolean retrieveData, boolean showUI, String group,
             String fromDate, String toDate)
   {
      if (!queryUid)
      {
         renderError(message(code:'query.execute.error.queryUidMandatory'), '455', 400)
         return
      }
      
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      String organizationUid = _org.uid
      
      
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
      
      // check valid value for group
      if (group && group != 'composition' && group != 'path')
      {
         renderError(message(code:'rest.error.query.invalid_group', args:[group]), '488', 400)
         return
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

      // verify parsability and return errors, see: https://github.com/ppazos/cabolabs-ehrserver/wiki/API-error-codes-and-messages
      if (fromDate)
      {
         qFromDate = DateParser.tryParse(fromDate)
         if (!qFromDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['fromDate', fromDate]), "0010", 400)
            return
         }
      }
      
      if (toDate)
      {
         qToDate = DateParser.tryParse(toDate)
         if (!qToDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['toDate', toDate]), "0011", 400)
            return
         }
      }
      
      if (qFromDate && qToDate && qFromDate > qToDate)
      {
         renderError(message(code:'rest.error.from_bigger_than_to', args:[fromDate, toDate]), "0012", 400)
         return
      }
      
      
      // measuring query timing
      def start_time = System.currentTimeMillis()
      // /measuring query timing
      
      def res = query.execute(ehrUid, qFromDate, qToDate, group, organizationUid) // res is a list for composition queries and datavalue with group none, a map for datavalue of group path or compo
      
      // measuring query timing
      def end_time = System.currentTimeMillis()
      // /measuring query timing
      
      
      // If not format is specified, take the query format.
      if (!format) format = query.format
         
      
      // Output as XMl or JSON. For type=composition format is always XML.
      if (query.type == 'composition')
      {
         // If no ehrUid was specified, the results will be for different ehrs
         // we need to group those CompositionIndexes by EHR.
         if (!ehrUid)
         {
            res = res.groupBy { ci -> ci.ehrUid } // res is a map
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
            // we need a map to return the timing...
            if (res instanceof List)
            {
               def mapres = [results: res]
               res = mapres
            }
            
            res['timing'] = (end_time - start_time).toString() +' ms' // measuring query timing
            
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
         // measuring query timing
         out += '<timing>'+ (end_time - start_time) +' ms</timing>'
         out += '</list>'

         
         if (format == 'json')
            render(text: jsonService.xmlToJson(out), contentType:"application/json", encoding:"UTF-8")
         else
            render(text: out, contentType:"text/xml", encoding:"UTF-8")
         
      } // type = composition
      else
      {
         // type = datavalue
         
         // we need a map to return the timing...
         // dv queries with group none will return a list, not a map
         if (res instanceof List)
         {
            def mapres = [results: res]
            res = mapres
         }
         
         res['timing'] = (end_time - start_time).toString() +' ms' // measuring query timing
         
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
            renderFormatNotSupportedError()
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
   // Not stateless secured because is for internal use from web
   def queryData()
   {
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

      // verify parsability and return errors, see: https://github.com/ppazos/cabolabs-ehrserver/wiki/API-error-codes-and-messages
      if (fromDate)
      {
         qFromDate = DateParser.tryParse(fromDate)
         if (!qFromDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['fromDate', fromDate]), "479", 400)
            return
         }
      }
      
      if (toDate)
      {
         qToDate = DateParser.tryParse(toDate)
         if (!qToDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['toDate', toDate]), "480", 400)
            return
         }
      }
      
      if (qFromDate && qToDate && qFromDate > qToDate)
      {
         renderError(message(code:'rest.error.from_bigger_than_to', args:[fromDate, toDate]), "481", 400)
         return
      }
      
      def query = Query.newInstance(request.JSON.query)
      def res = query.executeDatavalue(qehrId, qFromDate, qToDate, group, organizationUid)
      
      println "format: "+ format
      
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
         // since this is not actually an endpoint, is just for query test from the UI, dont uses renderFormatNotSupportedError()
         render(status: 400, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
      }
      return
   }
   
   
   /**
    * Previo QueryController.testQueryByData
    * Para ejecutar queries desde la UI, recibe un objeto json con la query y los parametros.
    * Solo soporta XML.
    * @return
    */
   // Not stateless secured because is used from the web
   def queryCompositions()
   {
      println "queryCompositions"
      
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

       // verify parsability and return errors, see: https://github.com/ppazos/cabolabs-ehrserver/wiki/API-error-codes-and-messages
      if (fromDate)
      {
         qFromDate = DateParser.tryParse(fromDate)
         if (!qFromDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['fromDate', fromDate]), "479", 400)
            return
         }
      }
      
      if (toDate)
      {
         qToDate = DateParser.tryParse(toDate)
         if (!qToDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['toDate', toDate]), "480", 400)
            return
         }
      }
      
      if (qFromDate && qToDate && qFromDate > qToDate)
      {
         renderError(message(code:'rest.error.from_bigger_than_to', args:[fromDate, toDate]), "481", 400)
         return
      }
       
      def query = Query.newInstance(request.JSON.query)
      def cilist = query.executeComposition(qehrId, qFromDate, qToDate, organizationUid)
      def result = cilist
      
      // If no ehrUid was specified, the results will be for different ehrs
      // we need to group those CompositionIndexes by EHR.
      if (!qehrId)
      {
         result = cilist.groupBy { ci -> ci.ehrUid }
      }
       
      //println "Resultados (CompositionIndex): " + cilist
      
      
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
   @SecuredStateless
   def contributions(String ehrUid, String from, String to, int max, int offset, String format)
   {
      // verify permissions by organization of the EHR with ehrUid
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
      
      // Check if the org used to login is the org of the requested ehr
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      if (_ehr.organizationUid != _org.uid)
      {
         renderError(message(code:'rest.error.cant_access_ehr', args:[ehrUid]), "0004", 401)
         return
      }
      
      
      Date dateFrom
      Date dateTo

      // verify parsability and return errors, see: https://github.com/ppazos/cabolabs-ehrserver/wiki/API-error-codes-and-messages
      if (from)
      {
         dateFrom = DateParser.tryParse(from)
         if (!dateFrom)
         {
            renderError(message(code:'rest.error.invalid_format', args:['from', from]), "0001", 400)
            return
         }
      }
      
      if (to)
      {
         dateTo = DateParser.tryParse(to)
         if (!dateTo)
         {
            renderError(message(code:'rest.error.invalid_format', args:['to', to]), "0002", 400)
            return
         }
      }
      
      if (dateFrom && dateTo && dateFrom > dateTo)
      {
         renderError(message(code:'rest.error.from_bigger_than_to', args:[from, to]), "0003", 400)
         return
      }
      
      if (!max)
      {
         max = 50
         offset = 0
      }
      
      def res = Contribution.createCriteria().list(max: max, offset: offset) {
         
         /*
         ehr {
            eq('uid', ehrUid)
         }
         */
         eq('ehr', _ehr)
            
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
      
      // TODO: create a XML marshalled to not return lists or maps as XML (try to follow the openEHR XML)
      def result = []
      res.each { contrib ->
         result << [
            uid: contrib.uid,
            organizationUid: contrib.organizationUid,
            ehr: contrib.ehr.uid,
            versions: contrib.versions.uid, // list of uids
            audit: [
               timeCommitted: contrib.audit.timeCommitted,
               systemId:  contrib.audit.systemId,
               committer:  contrib.audit.committer.name
            ]
         ]
      }
      
      
      if (!format || format == 'xml')
      {
         render(text:(result as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         render(text:(result as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 400, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
      }
   }
   
   
   @SecuredStateless
   def getComposition(String uid, String format)
   {
      // uid required
      if (!uid)
      {
         if (!format || format == 'xml')
            render(status: 400, text:'<error>uid is required</error>', contentType:"text/xml", encoding:"UTF-8")
         else if (format == 'json')
            render(status: 400, text:'{"error": "uid is required"}', contentType:"application/json", encoding:"UTF-8")
            
         return
      }
      
      
      def cindex = CompositionIndex.findByUid(uid)
      
      if (!cindex)
      {
         if (!format || format == 'xml')
            render(status: 404, text:'<error>composition not found</error>', contentType:"text/xml", encoding:"UTF-8")
         else if (format == 'json')
            render(status: 404, text:'{"error": "composition not found"}', contentType:"application/json", encoding:"UTF-8")
            
         return
      }
      
      // check permissions of the logged user over the compo (cindex.organizationUid)
      def _username = request.securityStatelessMap.username
      def _user = User.findByUsername(_username)
      if (!_user.organizations.uid.contains(cindex.organizationUid))
      {
         renderError(message(code:'query.execute.error.user_cant_access_composition'), '479', 403)
         return
      }
      

      withFormat {
         json {
            render(text: compositionService.compositionAsJson(uid), contentType:"application/json", encoding:"UTF-8")
         }
         xml {
            render(text: compositionService.compositionAsXml(uid), contentType:"text/xml", encoding:"UTF-8")
         }
         html {
            render(text: compositionService.compositionAsHtml(uid), contentType:"text/html", encoding:"UTF-8")
         }
      }
   }
   
   
   /**
    * Usada desde EMRAPP para obtener compositions de un paciente.
    *
    * Utiliza CompositionIndex para buscar entre las compositions y devuelve el XML de las compositions que matchean.
    *
    * @param ehrUid
    * @param subjectId
    * @param fromDate yyyyMMdd
    * @param toDate yyyyMMdd
    * @param archetypeId
    * @return
    */
   @SecuredStateless
   def findCompositions(String ehrUid, String subjectId,
                        String fromDate, String toDate,
                        String archetypeId, String category,
                        String format, int max, int offset)
   {
      // Paginacion
      if (!max) max = 30
      if (!offset) offset = 0
      
      // 1. Todos los parametros son opcionales pero debe venir por lo menos 1
      // 2. La semantica de pasar 2 o mas parametros es el criterio de and
      // 3. Para implementar como un OR se usaria otro parametro booleano (TODO)
      //
      
      def dFromDate
      def dToDate
      
      // FIXME: cuando sea servicio no hay ui
      // mandatory parameters not present so it is 400 Bad Request
      if (!ehrUid && !subjectId && !fromDate && !toDate && !archetypeId && !category)
      {
         if (!format || format == 'xml')
            render(status: 400, text:'<error>ehrUid or subjectUid are required</error>', contentType:"text/xml", encoding:"UTF-8")
         else if (format == 'json')
            render(status: 400, text:'{"error": "ehrUid or subjectUid are required"}', contentType:"application/json", encoding:"UTF-8")
            
         return
      }
      
      // FIXME: Si el formato esta mal va a tirar una except!
      // https://github.com/ppazos/cabolabs-ehrserver/issues/364
      if (fromDate)
      {
         dFromDate = DateParser.tryParse(fromDate)
         if (!dFromDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['fromDate', fromDate]), "479", 400)
            return
         }
      }
      
      if (toDate)
      {
         dToDate = DateParser.tryParse(toDate)
         if (!dToDate)
         {
            renderError(message(code:'rest.error.invalid_format', args:['toDate', toDate]), "480", 400)
            return
         }
      }
      
      def idxs = CompositionIndex.withCriteria {
         
         if (ehrUid)
            eq('ehrUid', ehrUid)
         
         if (subjectId)
            eq('subjectId', subjectId)
         
         if (archetypeId)
            eq('archetypeId', archetypeId)
         
         if (category)
            eq('category', category)
            
         if (dFromDate)
            ge('startTime', dFromDate) // greater or equal
         
         if (dToDate)
            le('startTime', dToDate) // lower or equal
            
         eq('lastVersion', true)
         
         maxResults(max)
         firstResult(offset)
      }
      
      // TODO: fix the structure for XML, it will output the MAP marshaling.
      def result = [
         result: idxs,
         pagination: [
            'max': max,
            'offset': offset,
            nextOffset: offset+max, // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
            prevOffset: ((offset-max < 0) ? 0 : offset-max )
         ]
      ]
      
      // TODO: ui o xml o json (solo index o contenido), ahora tira solo index y en XML
      if (!format || format == 'xml')
         render(text: result as grails.converters.XML, contentType:"text/xml", encoding:"UTF-8")
      else if (format == 'json')
         render(text: result as grails.converters.JSON, contentType:"application/json", encoding:"UTF-8")
      else
         render(status: 400, text: '<result>format not supported</result>', contentType:"text/xml", encoding:"UTF-8")
   }
   
   
   @Transactional
   @SecuredStateless
   def createPerson(String firstName, String lastName, String dob, String sex, String idCode, String idType, 
                    String role, String organizationUid, boolean createEhr, String format)
   {
      if (!format)
      {
         params.format = 'json' // this is to make withFormat works because uses the request params
         format = 'json'
      }
      
      if (!organizationUid)
      {
         renderError('organizationUid required', '5556', 400) // Bad Request
         return
      }
      
      if (Organization.countByUid(organizationUid) == 0)
      {
         renderError("Organization $organizationUid doesn't exists", '1237', 400) // Bad Request
         return
      }
      
      // FIXME: this should be on a service
      // Does the request comes from someone who has access to the organization?
      def _username = request.securityStatelessMap.username
      def _user = User.findByUsername(_username)
      if (!_user.organizations.uid.contains(organizationUid))
      {
         renderError("Don't have permissions over the organization $organizationUid", '4764', 403)
         return
      }
      
      
      // I think this is binded automatically to the person on save
      // dob to date
      params.dob = DateParser.tryParse(dob)
      
      
      // same as personcontroller.save
      def personInstance = new Person(params)
      
      if (!personInstance.save(flush: true))
      {
         def errors = ""
         personInstance.errors.allErrors.each { 
            
            errors += messageSource.getMessage(it, null) + "\n"
         }
         
         renderError("Invalid data: \n" + errors, '1235', 400) // Bad Request
         return
      }
      
      if (personInstance.role == "pat" && createEhr)
      {
         // from EhrController.createEhr
         def ehr = new Ehr(
            subject: new PatientProxy(
               value: personInstance.uid
            ),
            organizationUid: personInstance.organizationUid
         )
         
         if (!ehr.save(flish:true))
         {
            renderError('Not able to save EHR', '1236', 500) // Internal Server Error
            return
         }
      }
      

         
      def data = [
         firstName: personInstance.firstName,
         lastName: personInstance.lastName,
         dob: personInstance.dob, // Date is marshalled by the JSON marshaller
         sex: personInstance.sex,
         idCode: personInstance.idCode,
         idType: personInstance.idType, 
         role: personInstance.organizationUid, 
         organizationUid: personInstance.organizationUid,
         uid: personInstance.uid
      ]
      if (!format || format == 'xml')
      {
         println "XML"
         render(text: data as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         println "JSON"
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         renderError("Format $format not supported", '44325', 400)
      }
   }
}
