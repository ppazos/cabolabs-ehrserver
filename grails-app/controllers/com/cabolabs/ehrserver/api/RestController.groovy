/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.api

import grails.converters.*

import java.text.SimpleDateFormat

import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.DataGet
import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.ehrserver.api.structures.PaginatedResults
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
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
import com.cabolabs.security.UserRole
import com.cabolabs.security.Organization
import com.cabolabs.security.Role

import grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder // passwordEncoder

import com.cabolabs.ehrserver.openehr.composition.CompositionService
import com.cabolabs.util.DateParser
import com.cabolabs.ehrserver.versions.VersionFSRepoService
import com.cabolabs.ehrserver.exceptions.XmlValidationException

import grails.transaction.Transactional
import com.cabolabs.ehrserver.query.QueryShare
import grails.util.Environment

/**
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class RestController {

   static allowedMethods = [login: "POST", commit: "POST", contributions: "GET", ehrCreate: "POST", userRegister: "POST"]
   
   def xmlService // Utilizado por commit
   def jsonService // Query composition with format = json
   def compositionService
   def versionFSRepoService
   def commitLoggerService
   def notificationService

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
      'contributions': '12', // GET /contributions
      'ehrCreate': '13'
   ]
   
   // FIXME: move logic to service
   def login()
   {
      // https://github.com/ppazos/cabolabs-ehrserver/blob/rest_security/src/groovy/com/cabolabs/security/AuthFilter.groovy
      // TODO check JSON payload for params...
      String username = params.username
      String password = params.password
      String organization_number = params.organization
      
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
         renderError(e.message, 'e01.0001', 401)
      }
   }
   
   
   private void renderError(String msg, String errorCode, int status)
   {
      renderError(msg, errorCode, status, [], null)
   }
   
   // FIXME this is customized for the commit but used from other endpoints
   private void renderError(String msg, String errorCode, int status, List detailedErrors, Exception ex)
   {
      /*
       * result
       *   type
       *   status
       *   message
       *   details
       *     item detailedErrors[0]
       *     item detailedErrors[1]
       *     ...
       *   trace // DEV ONLY
       */
      
      // Format comes from current request
      withFormat {
         xml {
            render(status: status, contentType:"text/xml", encoding:"UTF-8") {
               result {
                  type('AR')                         // application reject
                  message(msg)
                  code('EHR_SERVER::API::ERRORS::'+ errorCode) // sys::service::concept::code
                  
                  if (detailedErrors)
                  {
                     details {
                        detailedErrors.each { error ->
                           item(error)
                        }
                     }
                  }
                  
                  if (ex && Environment.current == Environment.DEVELOPMENT)
                  {
                     StringWriter writer = new StringWriter()
                     PrintWriter printWriter = new PrintWriter( writer )
                     org.codehaus.groovy.runtime.StackTraceUtils.sanitize(ex).printStackTrace(printWriter)
                     printWriter.flush()
                     String _trace = writer.toString()
                     
                     trace( _trace )
                  }
               }
            }
         }
         json {
            def error = [
               result: [
                  type: 'AR',
                  message: msg,
                  code: 'EHR_SERVER::API::ERRORS::'+ errorCode
               ]
            ]
            
            if (detailedErrors)
            {
               error.result.details = detailedErrors
            }
            if (ex && Environment.current == Environment.DEVELOPMENT)
            {
               StringWriter writer = new StringWriter()
               PrintWriter printWriter = new PrintWriter( writer )
               org.codehaus.groovy.runtime.StackTraceUtils.sanitize(ex).printStackTrace(printWriter)
               printWriter.flush()
               String _trace = writer.toString()
               
               error.result.trace = _trace
            }
            
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
                  type ('AR')                         // application reject
                  message(msg)
                  code('EHR_SERVER::API::ERRORS::'+ errorCode) // sys::service::concept::code
                  
                  if (detailedErrors)
                  {
                     details {
                        detailedErrors.each { error ->
                           item(error)
                        }
                     }
                  }
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
   @SecuredStateless
   def commit(String ehrUid, String auditSystemId, String auditCommitter)
   {
      log.info( "commit received "+ params.list('versions').size() + " versions"  )

      if (!ehrUid)
      {
         commitLoggerService.log(request, null, false, null)
         renderError(message(code:'rest.error.ehr_uid_required'), '400', 400)
         return
      }
      if (!auditSystemId)
      {
         commitLoggerService.log(request, null, false, null)
         renderError(message(code:'rest.error.auditSystemId_required'), '400', 400)
         return
      }
      if (!auditCommitter)
      {
         commitLoggerService.log(request, null, false, null)
         renderError(message(code:'rest.error.auditCommitter_required'), '400', 400)
         return
      }

      def ehr = Ehr.findByUid(ehrUid)
      if (!ehr)
      {
         commitLoggerService.log(request, null, false, null)
         renderError(message(code:'rest.error.ehr_doesnt_exists', args:[ehrUid]), '403', 404)
         return
      }
      
      // check permissions of the logged user over the ehr
      def _username = request.securityStatelessMap.username
      def _user = User.findByUsername(_username)
      if (!_user.organizations.uid.contains(ehr.organizationUid))
      {
         commitLoggerService.log(request, null, false, null)
         renderError(message(code:'query.execute.error.user_cant_access_ehr'), '4764', 403)
         return
      }
      
      //println request.getClass() // org.springframework.security.web.servletapi.HttpServlet3RequestFactory$Servlet3SecurityContextHolderAwareRequestWrapper
      //println request.contentType // application/xml, application/json

      // FIXME: if the request is XML we can access an XmlSlurper instance from request.XML, so
      //        there is no need of accessing request.reader.text, the only problem is that request.XML
      //        will be null if Content-Type is not specified on the request.
      
      /*
       * <versions>
       *  <version>
       *  ...
       *  </version>
       * </version>
       */
      // Works only if the XML is directly in the body, if the content type
      // is form urlencoded, it should be accessed via a param. If the content
      // type is multipart/form-data, it should be request.getFile('paramname')
      // content type application/xml is accessed via request.reader?.text
      // ref: http://stackoverflow.com/questions/3831680/httpservletrequest-get-json-post-data
      // ref: http://stackoverflow.com/questions/9464398/reading-from-a-file-using-the-input-type-file-in-grails
      
      def content = request.reader?.text
      
      if (!content)
      {
         commitLoggerService.log(request, null, false, null)
         renderError(message(code:'rest.commit.error.emptyRequest'), '4012', 400)
         return
      }
      
      def versionsXML, _parsedVersions
      if (request.contentType == "application/json")
      {
         println "JSON"
         
         // JSON to XML, then process as XML
         // the json is transformed to xml and processed as an xml commit internally
         versionsXML = jsonService.json2xml(content)
         
         //println "versionsXML from JSON "+ versionsXML
         
         def slurper = new XmlSlurper(false, false)
         try {
            _parsedVersions = slurper.parseText(versionsXML)
         }
         catch (Exception e)
         {
            // if the JSON fails to convert to XML, parsedVersions will be empty
            // if _parsedVersions is empty, the error is reported below
         }
      }
      else if (["application/xml", "text/xml"].contains(request.contentType))
      {
         println "XML"
         def slurper = new XmlSlurper(false, false)
         _parsedVersions = slurper.parseText(content)
      }
      
      if (!_parsedVersions)
      {
         commitLoggerService.log(request, null, false, null)
         renderError(message(code:'rest.commit.error.versionsRequired'), '401', 400)
         return
      }
      
      //println "class "+ _parsedVersions.getClass() // groovy.util.slurpersupport.NodeChild
      //println _parsedVersions.children()*.name()
      //println _parsedVersions.version.size()
      
      // TODO: these errors should be related to parsing errors not just that the result is empty.
      if (_parsedVersions.isEmpty())
      {
         commitLoggerService.log(request, null, false, content)
         renderError(message(code:'rest.commit.error.versionsEmpty'), '402', 400)
         return
      }
      if (_parsedVersions.version.size() == 0)
      {
         commitLoggerService.log(request, null, false, content)
         renderError(message(code:'rest.commit.error.versionsEmpty'), '402.1', 400)
         return
      }
      

      try
      {
         // throws exceptions for any error
         def contribution = xmlService.processCommit(ehr, _parsedVersions, auditSystemId, new Date(), auditCommitter)

         /* **
          * The time_committed attribute in both the Contribution and Version audits
          * should reflect the time of committal to an EHR server, i.e. the time of
          * availability to other users in the same system. It should therefore be
          * computed on the server in implementations where the data are created
          * in a separate client context.
          * 
          * Note that this will override the time_committed from the version in the XML received.
          */

         commitLoggerService.log(request, contribution.uid, true, content)
          
          
          
         // Check if the OPT is loaded for each compo committed, return warning if not.
          
         def _orgnum = request.securityStatelessMap.extradata.organization
         def _org = Organization.findByNumber(_orgnum)
         def warnings = []
         contribution.versions.each { version ->
             
            if (OperationalTemplateIndex.forOrg(_org).countByTemplateId(version.data.templateId) == 0)
            {
               warnings << message(code:'api.commit.warning.optNotLoaded', args:[version.data.templateId, version.data.uid])
            }
         }
          
         if (warnings.size() > 0)
         {
            // TODO: this is not an error, but we use the same method for simplicity, we might want to ad a warning type of result.
            renderError(message(code:'api.commit.warning.verionsCommittedWithWarnings'), '1324', 200, warnings, null)
         }
         else
         {
            def msg = message(code:'api.commit.ok', args:[ehrUid])
            
            withFormat {
               xml {
                  render(contentType:"text/xml", encoding:"UTF-8") {
                     result {
                        type ('AA')                         // application reject
                        message(msg)
                        // has no error code
                     }
                  }
               }
               json {
                  render(contentType:"application/json", encoding:"UTF-8") {
                     [
                        result: [
                           type: 'AA',
                           message: msg
                        ]
                     ]
                  }
               }
            }
         }
      }
      catch (XmlValidationException e) // xsd error
      {
         // TODO: the XML validation errors might need to be adapted to the JSON commit because line numbers might not match.
         commitLoggerService.log(request, null, false, content)
         
         def detailedErrors = []
         
         xmlService.validationErrors.each { i, errorList ->
            errorList.each { errorText ->
               
               detailedErrors << message(code:'api.commit.versionValidation.errors', args:[i]) +': '+ errorText
            }
         }
         
         renderError(message(code:'rest.commit.error.versionsDontValidate'), 'e02.0009', 400, detailedErrors, null)
         return
      }
      catch (UndeclaredThrowableException e)
      {
         commitLoggerService.log(request, null, false, content)
         
         // http://docs.oracle.com/javase/7/docs/api/java/lang/reflect/UndeclaredThrowableException.html
         renderError(message(code:'rest.commit.error.cantProcessCompositions', args:[e.cause.message]), '481', 400)
         return
      }
      catch (Exception e)
      {
         commitLoggerService.log(request, null, false, content)
         
         log.error( e.message +" "+ e.getClass().getSimpleName() ) // FIXME: the error might be more specific, see which errors we can have.
         
         renderError(g.message(code:'rest.commit.error.cantProcessCompositions', args:[e.message]), '468', 400, [], e)
         return
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
      
      def vf
      try
      {
         vf = versionFSRepoService.getExistingVersionFile(version)
      }
      catch (FileNotFoundException e)
      {
         renderError(message(code:'rest.commit.error.versionDataNotFound'), '415', 500)
         return
      }
      
      def xml = vf.getText()
      
      render(text: xml, contentType:"text/xml", encoding:"UTF-8")
   }
   
   @SecuredStateless
   def ehrList(String format, int max, int offset)
   {
      if (!max) max = 30
      if (!offset) offset = 0
      
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      def _ehrs = Ehr.findAllByOrganizationUid(_org.uid, [max: max, offset: offset, readOnly: true])
      def res = new PaginatedResults(listName:'ehrs', list:_ehrs, max:max, offset:offset)
      
      if (!format || format == "xml")
      {
         render(text: res as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == "json")
      {
         def result = res as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
   } // ehrList
   
   
   /**
    * The register is only for the same org as the auth user.
    * The default role is ROLE_USER, it can be changed from the web console.
    * @param username
    * @param email
    * @return
    */
   @SecuredStateless
   def userRegister(String username, String email)
   {
      if (!username || !email)
      {
         renderError(message(code:'rest.userRegister.error.usernameAndEmail.required'), '999', 400)
         return
      }
      
      def u = new User(
         username: params.username,
         email: params.email,
         enabled: false
      )
      u.setPasswordToken()
      
      def error = false
      User.withTransaction{ status ->
      
         try
         {
            def _orgnum = request.securityStatelessMap.extradata.organization
            def o = Organization.findByNumber(_orgnum)
            
            
            // needs an organization before saving
            u.addToOrganizations(o)
            u.save(failOnError: true)
            
            
            // TODO: UserRole ORG_* needs a reference to the org, since the user
            //      can be ORG_ADMIN in one org and ORG_STAFF in another org.
            UserRole.create( u, (Role.findByAuthority('ROLE_USER')), true ) // the user is creating the organization, it should be manager also
            
            // reset password request notification
            notificationService.sendUserCreatedEmail( u.email, [u], true )
         }
         catch (Exception e)
         {
            println e.message
            println u.errors
            
            status.setRollbackOnly()
            
            error = true
         }
      }
      
      if (error)
      {
         renderError(message(code:'rest.userRegister.errorRegisteringUser'), '400', 400)
         return
      }
      
      def data = [
         username: u.username,
         email: u.email,
         organizations: u.organizations
      ]
      
      withFormat {
         xml {
            def result = data as XML
            render(text: result, contentType:"text/xml", encoding:"UTF-8")
         }
         json {

            def result = data as JSON
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
      }
   }
   
   
   
   /**
    * 
    * @param uid optional, if the client wants to set the uid externally.
    * @param subjectUid
    * @return
    */
   @SecuredStateless
   def ehrCreate(String uid, String subjectUid, String format)
   {
      if (!subjectUid)
      {
         renderError(message(code:'rest.ehrCreate.error.subjectUid.required'), '999', 400)
         return
      }
      
      // Check if there is an EHR for the same subject UID
      def c = Ehr.createCriteria()
      def existing_ehr = c.get {
         subject {
            eq('value', subjectUid)
         }
      }
      if (existing_ehr)
      {
         renderError(message(code:'ehr.createEhr.patientAlreadyHasEhr', args:[subjectUid, existing_ehr.uid]), '998', 400)
         return
      }
      
      // Check if the uid is unique
      if (uid)
      {
         existing_ehr = Ehr.findByUid(uid)
         if (existing_ehr)
         {
            renderError(message(code:'ehr.createEhr.ehrUidAlreadyExists', args:[uid]), '997', 400)
            return
         }
      }
      
      
      // Get organization of the current user
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      
      // Create the new EHR
      def ehr = new Ehr(
         organizationUid: _org.uid, 
         subject: new PatientProxy(value: subjectUid)
      )
      
      if (uid)
      {
         ehr.uid = uid
      }
      
      try
      {
         ehr.save(failOnError: true)
      }
      catch (Exception e)
      {
         renderError(message(code:'ehr.createEhr.saveError'), '159', 400, [], e)
         return
      }
      
      if (!format || format == "xml")
      {
         render(text: ehr as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == "json")
      {
         def result = ehr as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
   }
   
   // TODO: should use the ehr.subject.value key not the Person.uid
   @SecuredStateless
   def ehrForSubject(String subjectUid, String format)
   {
      if (!subjectUid)
      {
         renderError(message(code:'rest.error.patient_uid_required'), "455", 400)
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
         // FIXME error in the output format
         render(status: 404, text:"<result><code>error</code><message>"+ message(code:'rest.error.patient_doesnt_have_ehr', args:[subjectUid]) +"</message></result>", contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      
      // Check if the org used to login is the org of the requested ehr
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      if (_ehr.organizationUid != _org.uid)
      {
         renderError(message(code:'rest.error.user_cant_access_ehr', args:[_ehr.uid]), "483", 401)
         return
      }
      
      // ===========================================================================
      // 3. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(text: _ehr as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == "json")
      {
         def result = _ehr as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
   } // ehrForSubject
   
   
   @SecuredStateless
   def ehrGet(String uid, String format)
   {
      if (!uid)
      {
         renderError(message(code:'rest.error.ehr_uid_required'), "456", 400)
         return
      }
      
      // 1. EHR existe?
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         eq ('uid', uid)
      }
      
      if (!_ehr)
      {
         renderError(message(code:'rest.error.ehr_doesnt_exists', args:[uid]), "478", 404)
         return
      }
      
      // Check if the org used to login is the org of the requested ehr
      // organization number used on the API login
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      
      if (_ehr.organizationUid != _org.uid)
      {
         renderError(message(code:'rest.error.cant_access_ehr', args:[uid]), "483", 401)
         return
      }
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(text: _ehr as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == "json")
      {
         def result = _ehr as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
   } // ehrGet
   
   
   /*
    * Servicios sobre consultas.
    */
    // Get query
   @SecuredStateless
   def queryShow(String queryUid, String format)
   {
      if (!queryUid)
      {
         renderError(message(code:'rest.error.query_uid_required'), "455", 400)
         return
      }
      
      def query = Query.findByUid(queryUid)
       
      if (!query)
      {
         renderError(message(code:'rest.error.query_doesnt_exists', args:[queryUid]), "477", 404)
         return
      }
      
      if (!format || format == "xml")
      {
         render(text: query as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == "json")
      {
         def result = query as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8")
      }
   }

   
   @SecuredStateless
   def queryList(String format, String name, String sort, String order, int max, int offset)
   {
      if (!max) max = 15
      if (!offset) offset = 0
      if (!sort) sort = 'id'
      if (!order) order = 'asc'
      
      // login organization
      def _orgnum = request.securityStatelessMap.extradata.organization
      def _org = Organization.findByNumber(_orgnum)
      def shares = QueryShare.findAllByOrganization(_org)
      
      def c = Query.createCriteria()
      def _queries = c.list (max: max, offset: offset, sort: sort, order: order, readOnly: true) {
        if (name)
        {
          like('name', '%'+name+'%')
        }
        
        // return public or shared with the current org
        if (shares)
        {
           or {
              eq('isPublic', true)
              'in'('id', shares.query.id)
           }
        }
        else
        {
           eq('isPublic', true)
        }
      }
      
      def res = new PaginatedResults(listName:'queries', list:_queries, max:max, offset:offset)
      
      withFormat {
         xml {
            render(text: res as XML, contentType:"text/xml", encoding:"UTF-8")
         }
         json {
            def result = res as JSON
            // JSONP
            if (params.callback) result = "${params.callback}( ${result} )"
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
         '*' {
            render(text: res as XML, contentType:"text/xml", encoding:"UTF-8")
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
             String fromDate, String toDate, int max, int offset)
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
      
      if (!max)
      {
         max = 20
         offset = 0
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
      
      def res = query.execute(ehrUid, qFromDate, qToDate, group, organizationUid, max, offset) // res is a list for composition queries and datavalue with group none, a map for datavalue of group path or compo
      
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
            def paginated_res = new PaginatedResults(listName:'results', max:max, offset:offset)
            
            // we need a map to return the timing...
            if (res instanceof List)
            {
               paginated_res.list = res
            }
            else
            {
               paginated_res.map = res
            }
            
            paginated_res.timing = end_time - start_time
            
            if (format == 'json')
               render(text:(paginated_res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
            else
               render(text:(paginated_res as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
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
          def vf
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
                   //buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()
                   
                   try
                   {
                      vf = versionFSRepoService.getExistingVersionFile(version)
                      buff = vf.getText()
                   }
                   catch (FileNotFoundException e)
                   {
                      log.warning e.message
                      return // continue with next compoIndex
                   }
      
                   
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
                //buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()
                
                try
                {
                   vf = versionFSRepoService.getExistingVersionFile(version)
                   buff = vf.getText()
                }
                catch (FileNotFoundException e)
                {
                   log.warning e.message
                   return // continue with next compoIndex
                }
   
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
         
      } // /type = composition
      else
      {
         // type = datavalue
         
         def paginated_res = new PaginatedResults(listName:'results', max:max, offset:offset)
         
         // we need a map to return the timing...
         if (res instanceof List)
         {
            paginated_res.list = res
         }
         else
         {
            paginated_res.map = res
         }
         
         paginated_res.timing = end_time - start_time
         
         // Format
         if (!format || format == 'xml')
         {
            render(text:(paginated_res as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
         }
         else if (format == 'json')
         {
            render(text:(paginated_res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
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
      
      String organizationUid
      if (qehrId)
      {
         def ehr = Ehr.findByUid(qehrId)
         if (!ehr)
         {
            renderError(message(code:'rest.error.ehr_doesnt_exists', args:[qehrId]), '403', 404)
            return
         }
         
         organizationUid = ehr.organizationUid
      }
      else
      {
         // use the orguid of the org used to login
         organizationUid = session.organization.uid
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
      // all params come in the JSON object from the UI
      // all are strings
      boolean retrieveData = request.JSON.retrieveData.toBoolean() // http://mrhaki.blogspot.com/2009/11/groovy-goodness-convert-string-to.html
      boolean showUI = request.JSON.showUI.toBoolean()
      String qehrId = request.JSON.qehrId
      String fromDate = request.JSON.fromDate
      String toDate = request.JSON.toDate
      String qarchetypeId = request.JSON.qarchetypeId
      String format = request.JSON.format
       
      /*
       println request.JSON.retrieveData.getClass().getSimpleName()
       println request.JSON.showUI.getClass().getSimpleName()
      */
      //println retrieveData.toString() +" "+ showUI.toString()
      
      
      String organizationUid
      if (qehrId)
      {
         def ehr = Ehr.findByUid(qehrId)
         if (!ehr)
         {
            renderError(message(code:'rest.error.ehr_doesnt_exists', args:[qehrId]), '403', 404)
            return
         }
         
         organizationUid = ehr.organizationUid
      }
      else
      {
         // use the orguid of the org used to login
         organizationUid = session.organization.uid
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
      
      // For testing there is no need to pass pagination params, so we define them here:
      def max = 20
      def offset = 0
      
      def query = Query.newInstance(request.JSON.query)
      def cilist = query.executeComposition(qehrId, qFromDate, qToDate, organizationUid, max, offset)
      def result = cilist
      
      // If no ehrUid was specified, the results will be for different ehrs
      // we need to group those CompositionIndexes by EHR.
      if (!qehrId)
      {
         result = cilist.groupBy { ci -> ci.ehrUid }
      }
      
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
      def vf
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
                //buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()
   
                try
                {
                   vf = versionFSRepoService.getExistingVersionFile(version)
                   buff = vf.getText()
                }
                catch (FileNotFoundException e)
                {
                   log.warning e.message
                   return // continue with next compoIndex
                }
                
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
            //buff = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml").getText()
            
            try
            {
               vf = versionFSRepoService.getExistingVersionFile(version)
               buff = vf.getText()
            }
            catch (FileNotFoundException e)
            {
               log.warning e.message
               return // continue with next compoIndex
            }
            
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
      
      def result = [
         contributions: res,
         pagination: [
            'max': max,
            'offset': offset,
            nextOffset: offset+max, // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
            prevOffset: ((offset-max < 0) ? 0 : offset-max )
         ]
      ]
      
      
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
      
      def res = new PaginatedResults(listName:'result', list:idxs, max:max, offset:offset)
      
      // TODO: ui o xml o json (solo index o contenido), ahora tira solo index
      if (!format || format == 'xml')
         render(text: res as XML, contentType:"text/xml", encoding:"UTF-8")
      else if (format == 'json')
         render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
      else
         render(status: 400, text: '<result>format not supported</result>', contentType:"text/xml", encoding:"UTF-8")
   }
   
   
   @SecuredStateless
   def organizations(String format)
   {
      def _username = request.securityStatelessMap.username
      def _user = User.findByUsername(_username)
      
      if (!format || format == 'xml')
      {
         render(text: _user.organizations as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         render(text: _user.organizations as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         renderError("Format $format not supported", '44325', 400)
      }
   }
}
