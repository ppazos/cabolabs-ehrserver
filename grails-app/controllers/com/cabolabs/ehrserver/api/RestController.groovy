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

import java.text.SimpleDateFormat
import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.api.structures.*
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
import com.cabolabs.security.*
import com.cabolabs.ehrserver.notification.Notification
import groovy.util.slurpersupport.GPathResult
import java.lang.reflect.UndeclaredThrowableException
import javax.xml.bind.ValidationException
import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.AuthenticationProvider
import com.cabolabs.ehrserver.account.Plan
import grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder // passwordEncoder
import com.cabolabs.ehrserver.openehr.composition.CompositionService
import com.cabolabs.util.DateParser
import com.cabolabs.ehrserver.versions.VersionFSRepoService
import com.cabolabs.ehrserver.exceptions.*
import grails.transaction.Transactional
import grails.util.Environment
import grails.util.Holders
import grails.converters.*

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
   def apiResponsesService
   def queryService

   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   def formatter = new SimpleDateFormat( config.l10n.datetime_format )
   def formatterDate = new SimpleDateFormat( config.l10n.date_format )

   // test stateless security
   def statelessTokenProvider
   //def userService
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

   // TODO: I18N
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
         if (!username || !password || !organization_number)
         {
            throw new BadCredentialsException("username, password and organization are required, at least one is empty")
         }

         def user = User.findByUsername(username)
         if (user == null)
         {
            throw new UsernameNotFoundException("No matching account")
         }

         // Status checks
         if (!user.enabled)
         {
            throw new DisabledException("Account disabled")
         }

         if (!user.account.enabled)
         {
            log.info("Company account disabled")
            throw new DisabledException("Company account disabled")
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
         //assert this.passwordEncoder != null

         if (!passwordEncoder.isPasswordValid(user.password, password, null))
         {
            throw new BadCredentialsException("Authentication failed")
         }

         if (!organization_number) // null or empty
         {
            throw new BadCredentialsException("Authentication failed - organization number not provided")
         }

         // Check organization
         Organization org = Organization.findByNumber(organization_number)

         if (org == null)
         {
            throw new BadCredentialsException("Authentication failed")
         }

         if (!user.organizations.find{ it.uid == org.uid })
         {
            throw new BadCredentialsException("Authentication failed - check the organization number")
         }

         // TODO: refresh token

         def _token = statelessTokenProvider.generateToken(username, null, [organization: organization_number, org_uid: org.uid])

         withFormat {
            json {
               render (['token': _token] as JSON)
            }
            xml {
               render(contentType:"text/xml", encoding:"UTF-8") {
                  result {
                     token(_token)
                     //type('AA')                         // application reject
                     //code('EHR_SERVER::API::ERRORS::0066') // sys::service::concept::code
                  }
               }
            }
         }
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

      def type = ((status in 200..299) ? 'AA' : 'AR')
      def result

      // Format comes from current request
      withFormat {
         xml {
            result = apiResponsesService.feedback_xml(msg, type, errorCode, detailedErrors, ex)

            render( status:status, text:result, contentType:"text/xml", encoding:"UTF-8")
         }
         json {
            result = apiResponsesService.feedback_json(msg, type, errorCode, detailedErrors, ex)

            // JSONP
            if (params.callback) result = "${params.callback}( ${result} )"

            // with the status in render doesnt return the json to the client
            // http://stackoverflow.com/questions/10726318/easy-way-to-render-json-with-http-status-code-in-grails
            response.status = status
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
         '*' {
            result = apiResponsesService.feedback_xml(msg, type, errorCode, detailedErrors, ex)
            render( status:status, text:result, contentType:"text/xml", encoding:"UTF-8")
         }
      }
   }


   /**
    * Envia una lista de versions para commitear al EHR(ehrUid)
    *
    * @param String ehrUid
    * @param auditCommitter
    * @param List versions
    * @return
    */
   @SecuredStateless
   def commit(String ehrUid, String auditCommitter)
   {
      log.info( "commit received "+ params.list('versions').size() + " versions"  )

      if (!ehrUid)
      {
         commitLoggerService.log(request, null, false, null, session, params)
         renderError(message(code:'rest.error.ehr_uid_required'), '400', 400)
         return
      }

      if (!auditCommitter)
      {
         commitLoggerService.log(request, null, false, null, session, params)
         renderError(message(code:'rest.error.auditCommitter_required'), '400', 400)
         return
      }

      def ehr = Ehr.findByUid(ehrUid)
      if (!ehr)
      {
         commitLoggerService.log(request, null, false, null, session, params)
         renderError(message(code:'rest.error.ehr_doesnt_exists', args:[ehrUid]), '403', 404)
         return
      }

      // check permissions of the logged user over the ehr
      def _username = request.securityStatelessMap.username
      def _user = User.findByUsername(_username)
      if (!_user.organizations.uid.contains(ehr.organizationUid))
      {
         commitLoggerService.log(request, null, false, null, session, params)
         renderError(message(code:'rest.error.user_cant_access_ehr'), '4764', 403)
         return
      }

      // Repo size check
      def account = _user.account
      def plan_assoc = Plan.associatedNow(account) // can be null on dev envs, size check is not done on that case.
      if (plan_assoc)
      {
         if (plan_assoc.plan.repo_total_size_in_kb <= account.totalRepoSizeInKb)
         {
           commitLoggerService.log(request, null, false, null, session, params)
           renderError(message(code:'rest.commit.error.cant_commit.insufficient_storage', args:[plan_assoc.plan.repo_total_size_in_kb, account.totalRepoSizeInKb]), '4507', 507) // 507 Insufficient Storage
           return
         }
      }

      //println request.getClass() // org.springframework.security.web.servletapi.HttpServlet3RequestFactory$Servlet3SecurityContextHolderAwareRequestWrapper
      //println request.contentType // application/xml, application/json, MIGHT INCLUDE charset: text/xml; charset=UTF-8

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
         commitLoggerService.log(request, null, false, null, session, params)
         renderError(message(code:'rest.commit.error.emptyRequest'), '4012', 400)
         return
      }

      // contentType can include charset, and here we need to check only the MIME type
      // text/xml; charset=UTF-8
      //println "request.contentType "+ request.contentType
      def requestContentTypeOnly = request.contentType.split(';')[0] // will do nothing if charset is not present
      //println "request content type only: '"+ requestContentTypeOnly + "'"

      def versionsXML, _parsedVersions
      if (requestContentTypeOnly == "application/json")
      {
         // JSON to XML, then process as XML
         // the json is transformed to xml and processed as an xml commit internally
         versionsXML = jsonService.json2xml(content)

         if (!versionsXML) // if empty, JSON is invalid, excepts are cached internally in the service
         {
            commitLoggerService.log(request, null, false, null, session, params)
            renderError(message(code:'rest.commit.error.invalidJSON'), '50112', 400)
            return
         }

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
      else if (["application/xml", "text/xml"].contains(requestContentTypeOnly))
      {
         def slurper = new XmlSlurper(false, false)
         try
         {
            _parsedVersions = slurper.parseText(content)
         }
         catch (org.xml.sax.SAXParseException sex) // checks malformed XML
         {
            commitLoggerService.log(request, null, false, null, session, params)
            renderError(message(code:'rest.commit.error.invalidXML'), '50111', 400, [], sex)
            return
         }
      }
      else // only json or xml are allowed
      {
         commitLoggerService.log(request, null, false, null, session, params)
         renderError(message(code:'rest.commit.error.contentTypeNotSupported'), '50112', 400)
         return
      }


      if (!_parsedVersions)
      {
         commitLoggerService.log(request, null, false, null, session, params)
         renderError(message(code:'rest.commit.error.versionsRequired'), '401', 400)
         return
      }

      // TODO: these errors should be related to parsing errors not just that the result is empty.
      if (_parsedVersions.isEmpty())
      {
         commitLoggerService.log(request, null, false, content, session, params)
         renderError(message(code:'rest.commit.error.versionsEmpty'), '402', 400)
         return
      }
      if (_parsedVersions.version.size() == 0)
      {
         commitLoggerService.log(request, null, false, content, session, params)
         renderError(message(code:'rest.commit.error.versionsEmpty'), '402.1', 400)
         return
      }


      // ---------------------------------------------
      // TODO: check the template is active
      // ---------------------------------------------

      // CHECK: all referenced template IDs should exist
      // gets the template ID from each version in the commit
      def template_ids = _parsedVersions.version.collect { it.data.archetype_details.template_id.value.text() }
      def _check_opt
      def _org = Organization.findByUid(request.securityStatelessMap.extradata.org_uid)
      for (String tid: template_ids)
      {
         // is findByX not findAllByX because if it is lastVersion for a tid, should exist 0..1
         _check_opt = OperationalTemplateIndex.forOrg(_org).lastVersions().findByTemplateId(tid)
         if (!_check_opt)
         {
            commitLoggerService.log(request, null, false, content, session, params)
            renderError(message(code:'api.commit.warning.optNotLoaded', args:[tid]), '1324', 412)
            return
         }
         if (_check_opt.isDeleted)
         {
            commitLoggerService.log(request, null, false, content, session, params)
            renderError(message(code:'api.commit.warning.optIsDeleted', args:[tid]), '1324.1', 412)
            return
         }
         if (!_check_opt.isActive)
         {
            commitLoggerService.log(request, null, false, content, session, params)
            renderError(message(code:'api.commit.warning.optExistsButInactive', args:[tid]), '1324.2', 412)
            return
         }
      }


      def contribution
      try
      {
         // throws exceptions for any error
         contribution = xmlService.processCommit(ehr, _parsedVersions, new Date(), auditCommitter)
         if (!contribution.save())
         {
            // FIXME: log and notification! an admin should review this situation
            println contribution.errors
         }

         /* **
          * The time_committed attribute in both the Contribution and Version audits
          * should reflect the time of committal to an EHR server, i.e. the time of
          * availability to other users in the same system. It should therefore be
          * computed on the server in implementations where the data are created
          * in a separate client context.
          *
          * Note that this will override the time_committed from the version in the XML received.
          */

         commitLoggerService.log(request, contribution.uid, true, content, session, params)

         def msg = message(code:'api.commit.ok', args:[ehrUid])

         def result = new CommitResult(
            type: 'AA',
            message: msg,
            versions: contribution.versions
         )

         withFormat {
            xml {
               render( status:201, text:result as XML, contentType:"text/xml", encoding:"UTF-8")
            }
            json {
               render( status:201, text:result as JSON, contentType:"application/json", encoding:"UTF-8")
            }
         }
      }
      catch (CommitWrongChangeTypeException e)
      {
         commitLoggerService.log(request, null, false, content, session, params)

         def detailedErrors = []

         xmlService.validationErrors.each { i, errorList ->
            errorList.each { errorText ->

               detailedErrors << message(code:'api.commit.changeType.errors', args:[i]) +': '+ errorText
            }
         }

         renderError(message(code:'rest.commit.error.wrongChangeType', args:[e.message]), 'e02.0009.0', 400, detailedErrors, e)
         return
      }
      catch (XmlValidationException | XmlSemanticValidationExceptionLevel1 e) // xsd validation errors
      {
         // TODO: the XML validation errors might need to be adapted to the JSON commit because line numbers might not match.
         commitLoggerService.log(request, null, false, content, session, params)

         def detailedErrors = []

         xmlService.validationErrors.each { i, errorList ->
            errorList.each { errorText ->

               detailedErrors << message(code:'api.commit.versionValidation.errors', args:[i]) +': '+ errorText
            }
         }

         renderError(message(code:'rest.commit.error.versionsDontValidate'), 'e02.0009.1', 400, detailedErrors, e)
         return
      }
      catch (UndeclaredThrowableException e)
      {
         commitLoggerService.log(request, null, false, content, session, params)

         // http://docs.oracle.com/javase/7/docs/api/java/lang/reflect/UndeclaredThrowableException.html
         renderError(message(code:'rest.commit.error.cantProcessCompositions', args:[e.cause.message]), '481', 400)
         return
      }
      catch (RuntimeException | Exception e)
      {
         println "e message " + e.message
         log.error( e.message +" "+ e.getClass().getSimpleName() ) // FIXME: the error might be more specific, see which errors we can have.

         commitLoggerService.log(request, null, false, content, session, params)
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
   def checkout(String ehrUid, String compositionUid, String format)
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

      if (_ehr.organizationUid != request.securityStatelessMap.extradata.org_uid)
      {
         renderError(message(code:'rest.error.cant_access_ehr', args:[ehrUid]), "483", 401)
         return
      }

      def ci = CompositionIndex.findByUid(compositionUid)

      if (!ci)
      {
         renderError(message(code:'rest.commit.error.versionDoesntExists'), '412', 404)
         return
      }

      // only the latest version can be checked out
      if (!ci.lastVersion)
      {
         renderError(message(code:'rest.commit.error.versionIsNotTheLatest'), '416', 400)
         return
      }

      try
      {
         withFormat {
            json {
               render(text: compositionService.compositionAsJson(compositionUid), contentType:"application/json", encoding:"UTF-8")
            }
            xml {
               render(text: compositionService.compositionAsXml(compositionUid), contentType:"text/xml", encoding:"UTF-8")
            }
         }
      }
      catch (FileNotFoundException e)
      {
         renderError(message(code:'rest.commit.error.versionDataNotFound'), '415', 500) // this should not happen :)
         return
      }
      catch (Exception e)
      {
         renderError(message(code:'rest.error.getComposition.compoDoesntExists'), '415', 404)
         return
      }
   }

   @SecuredStateless
   def ehrList(String format, int max, int offset)
   {
      if (!max) max = 30
      if (!offset) offset = 0

      def _ehrs = Ehr.findAllByOrganizationUid(request.securityStatelessMap.extradata.org_uid, [max: max, offset: offset, readOnly: true])
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
   }


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
            def o = Organization.findByUid(request.securityStatelessMap.extradata.org_uid)

            u.save(failOnError: true)

            UserRole.create( u, (Role.findByAuthority('ROLE_USER')), o, true )

            // reset password request notification
            notificationService.sendUserRegisteredOrCreatedEmail( u.email, [u], false )
         }
         catch (Exception e)
         {
            status.setRollbackOnly()
            error = true
         }
      }

      if (error)
      {
         renderError(message(code:'rest.userRegister.errorRegisteringUser'), '400', 400, u.errors.getAllErrors(), null)
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
         renderError(message(code:'rest.ehrCreate.error.subjectUid.required'), '9990', 400)
         return
      }

      if (!(subjectUid ==~ /([a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})/))
      {
         renderError(message(code:'rest.ehrCreate.error.subjectUid.invalidUUID'), '9991', 400)
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

         if (!(uid ==~ /([a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})/))
         {
            renderError(message(code:'rest.ehrCreate.error.uid.invalidUUID'), '9991', 400)
            return
         }
      }

      // Create the new EHR
      def ehr = new Ehr(
         organizationUid: request.securityStatelessMap.extradata.org_uid,
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
         render(text: ehr as XML, contentType:"text/xml", encoding:"UTF-8", status: 201)
      }
      else if (format == "json")
      {
         def result = ehr as JSON
         // JSONP
         if (params.callback) result = "${params.callback}( ${result} )"
         render(text: result, contentType:"application/json", encoding:"UTF-8", status: 201)
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
         renderError(message(code:'rest.error.patient_doesnt_have_ehr', args:[subjectUid]), "455", 404)
         return
      }


      // Check if the org used to login is the org of the requested ehr
      // organization number used on the API login
      if (_ehr.organizationUid != request.securityStatelessMap.extradata.org_uid)
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
   }


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
      if (_ehr.organizationUid != request.securityStatelessMap.extradata.org_uid)
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
   }


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
      def _org = Organization.findByUid(request.securityStatelessMap.extradata.org_uid)
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
             String fromDate, String toDate, int max, int offset,
             String composerUid, String composerName)
   {
      if (!queryUid)
      {
         renderError(message(code:'query.execute.error.queryUidMandatory'), '455', 400)
         return
      }

      // organization number used on the API login
      String organizationUid = request.securityStatelessMap.extradata.org_uid

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

      // query can be accessed by current org?
      if (!query.isPublic)
      {
         def shares = QueryShare.findAllByQuery(query)
         def orgCanAccess = (shares.organization.find{ it.uid == organizationUid } != null)
         if (!orgCanAccess)
         {
            renderError(message(code:'query.execute.error.cantAccessQuery', args:[queryUid]), '446', 401)
            return
         }
      }

      // --------------------------------------------------------------
      // TODO: do query execution and output processing in a service
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

      def res

      try
      {
         // res is a list for composition queries and datavalue with group none, a map for datavalue of group path or compo
         res = query.execute(ehrUid, qFromDate, qToDate, group, organizationUid, max, offset, composerUid, composerName)
      }
      catch (QuerySnomedServiceException e)
      {
         renderError(message(code:e.message), "4801", 424)
         return
      }
      catch (Exception e)
      {
         renderError(e.message, "4802", 424)
         return
      }


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
         //if (!ehrUid)
         //{
            res = res.groupBy { ci -> ci.ehrUid } // res is a map
         //}

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

         // retrieveData

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
         //if (!ehrUid) // group by ehrUid
         //{
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

                  try
                  {
                     vf = versionFSRepoService.getExistingVersionFile(organizationUid, version)
                     buff = vf.getText()
                  }
                  catch (VersionRepoNotAccessibleException e)
                  {
                     log.warning e.message
                     return // continue with next compoIndex
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
         //}
         //else
         //{
         /*
            res.each { compoIndex ->

               // FIXME: verificar que esta en disco, sino esta hay un problema
               //        de sincronizacion entre la base y el FS, se debe omitir
               //        el resultado y hacer un log con prioridad alta para ver
               //        cual fue el error.

               // adds the version, not just the composition
               version = compoIndex.getParent()

               try
               {
                  vf = versionFSRepoService.getExistingVersionFile(organizationUid, version)
                  buff = vf.getText()
               }
               catch (VersionRepoNotAccessibleException e)
               {
                  log.warning e.message
                  return // continue with next compoIndex
               }
               catch (FileNotFoundException e)
               {
                  log.warning e.message
                  return // continue with next compoIndex
               }

               buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
               buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
               buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')

               //Composition queda:
               //<data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">

               out += buff + "\n"
            }
         */
         //}
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

      String composerUid = request.JSON.composerUid
      String composerName = request.JSON.composerName

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
         organizationUid = session.organization.uid // session.organization exists only from Web Console, not on API
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

      request.JSON.query.organizationUid = organizationUid
      def query = Query.newInstance(request.JSON.query)
      def res = query.executeDatavalue(qehrId, qFromDate, qToDate, group, organizationUid, composerUid, composerName)


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


   @SecuredStateless
   def executedNotStoredCompositionQuery()
   {
      if (!request.JSON)
      {
         renderError(message(code:'rest.error.query_not_provided'), "4800", 400)
         return
      }

      // TODO: check malformed query
      def result = queryService.executedNotStoredCompositionQuery(request.JSON, request.securityStatelessMap.extradata.org_uid)

      if (result.error)
      {
         renderError(result.error.message, result.error.code, result.error.status)
         return
      }


      String qehrId        = request.JSON.qehrId
      String format        = request.JSON.format
      // http://mrhaki.blogspot.com/2009/11/groovy-goodness-convert-string-to.html
      boolean retrieveData = request.JSON.retrieveData ? request.JSON.retrieveData.toBoolean() : false
      boolean showUI       = request.JSON.showUI ? request.JSON.showUI.toBoolean() : false


      // group results by ehr uid
      //if (!qehrId)
      //{
         result = result.result.groupBy { ci -> ci.ehrUid }
      //}
      //else
      //{
      //   result = result.result
      //}

      if (showUI)
      {
          // FIXME: hay que ver el tema del paginado
          render(template:'/compositionIndex/listTable',
                 model:[
                    compositionIndexInstanceList:  result,
                    groupedByEhr: (!qehrId)
                 ],
                 contentType: "text/html")
          return
      }


      // Do not retrieve data
      if (!retrieveData)
      {
         if (format == 'json')
            render(text:(result as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
         else
            render(text:(result as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
         return
      }


      // Retrieve data
      String data = queryService.retrieveDataFromCompositionQueryResult(result, qehrId, request.securityStatelessMap.extradata.org_uid)

      if (format == 'json')
         render(text: jsonService.xmlToJson(data), contentType:"application/json", encoding:"UTF-8")
      else
         render(text: data, contentType:"text/xml", encoding:"UTF-8")

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

      String composerUid = request.JSON.composerUid
      String composerName = request.JSON.composerName

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
         organizationUid = session.organization.uid // session.organization exists only from Web Console, not on API
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

      request.JSON.query.organizationUid = organizationUid
      def query = Query.newInstance(request.JSON.query)
      def cilist = []

      try
      {
         cilist = query.executeComposition(qehrId, qFromDate, qToDate, organizationUid, max, offset, composerUid, composerName)
      }
      catch (QuerySnomedServiceException e)
      {
         renderError(message(code:e.message), "4801", 424)
         return
      }
      catch (Exception e)
      {
         renderError(e.message, "4802", 424)
         return
      }


      def result = cilist

      // count queries do not return compos, so we cant group
      if (!query.isCount)
      {
         // If no ehrUid was specified, the results will be for different ehrs
         // we need to group those CompositionIndexes by EHR.
         // Update, to have the same structure even for 1 EHR, we do the group on all cases
         result = cilist.groupBy { ci -> ci.ehrUid }
      }
      else
      {
         result = [count: result[0]] // long
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

      // ===============================================================================
      // Retrieve Data


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

            try
            {
               vf = versionFSRepoService.getExistingVersionFile(organizationUid, version)
               buff = vf.getText()
            }
            catch (VersionRepoNotAccessibleException e)
            {
               log.warning e.message
               return // continue with next compoIndex
            }
            catch (FileNotFoundException e)
            {
               log.warning e.message
               return // continue with next compoIndex
            }

            buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
            buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
            buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')

            // Composition queda:
            // <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">

            out += buff + "\n"
         }
         out += '</ehr>'
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
      if (_ehr.organizationUid != request.securityStatelessMap.extradata.org_uid)
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
      if (!uid)
      {
         renderError(message(code:'ehr.show.uidIsRequired'), '479', 400)
         return
      }

      def cindex = CompositionIndex.findByUid(uid)

      if (!cindex)
      {
         renderError(message(code:'rest.error.getComposition.compoDoesntExists'), '479', 404)
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

      // mandatory parameters not present so it is 400 Bad Request
      if (!ehrUid && !subjectId)
      {
         renderError(message(code:'rest.error.findCompositions.subjectIdOrEhrUidRequire'), '403', 400)
         return
      }

      def ehr
      if (ehrUid)
      {
         ehr = Ehr.findByUid(ehrUid)
      }
      else
      {
         ehr = Ehr.withCriteria {
            subject {
               eq('value', subjectId)
            }
         }
      }

      if (!ehr)
      {
         renderError(message(code:'rest.error.ehr_doesnt_exists_no_id'), "465", 404)
         return
      }

      if (ehr.organizationUid != request.securityStatelessMap.extradata.org_uid)
      {
         renderError(message(code:'rest.error.ehr_doesnt_belong_to_organization', args:[ehr.uid, request.securityStatelessMap.extradata.org_uid]), "462", 401)
         return
      }

      // Si el formato esta mal va a tirar una except!
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

      // we know that ehrUid or sujectId are present, and the ehr belongs to the current org
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

      if (!format || format == 'xml')
         render(text: res as XML, contentType:"text/xml", encoding:"UTF-8")
      else if (format == 'json')
         render(text: res as JSON, contentType:"application/json", encoding:"UTF-8")
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

   @SecuredStateless
   def templates(String format)
   {
      def opts = OperationalTemplateIndex.findAllByOrganizationUidAndLastVersion(request.securityStatelessMap.extradata.org_uid, true)

      if (!format || format == 'xml')
      {
         render(text: opts as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         render(text: opts as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         renderError("Format $format not supported", '44325', 400)
      }
   }

   @SecuredStateless
   def getTemplate(String uid, String externalUid, String format)
   {
      def opt = OperationalTemplateIndex.findByUidAndOrganizationUid(uid, request.securityStatelessMap.extradata.org_uid)

      if (!opt)
      {
         renderError("OPT ${uid} not found", '444555', 404)
         return
      }

      def src = config.opt_repo.withTrailSeparator() + request.securityStatelessMap.extradata.org_uid.withTrailSeparator() + opt.fileUid + '.opt'
      File opt_file = new File( src )
      def opt_xml = opt_file.getText()

      render(text: opt_xml, contentType:"text/xml", encoding:"UTF-8")
   }

   @SecuredStateless
   def getEhrQueries(String format)
   {
      // TODO: queries should be associated to an org
      def queries = EhrQuery.list()
      def data = []
      queries.each {
         data << [uid: it.uid, name: it.name, description: it.description]
      }

      if (!format || format == 'xml')
      {
         render(text: data as XML, contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
   }

   @SecuredStateless
   def ehrChecker(String ehrQueryUid, String ehrUid, String format)
   {
      if (!ehrQueryUid)
      {
         renderError("ehrQueryUid is required", '9456', 404)
         return
      }

      def equery = EhrQuery.findByUid(ehrQueryUid)

      if (!equery)
      {
         renderError("Query not found for ehrQueryUid ${ehrQueryUid}", '9457', 404)
         return
      }

      // TODO: move to service checkEhr
      def matching_compo_index_counts = []
      equery.queries.each { query ->

         matching_compo_index_counts << query.executeComposition(ehrUid, null, null, null, 1, 0, null, null, true)
      }

      //println matching_compo_index_counts // [[1], [0]]
      //println matching_compo_index_counts.flatten() // [1, 0]

      // the count should be > 0 on all results to return true
      def res = matching_compo_index_counts.flatten().every { it > 0 }

      /*
      println "ehrQurry dirty "+ equery.dirty
      equery.queries.each { q ->
         println "query dirty "+ q.dirty
         q.where.each { exp ->
            println "expression dirty "+ exp.dirty
            println "expression criteria dirty "+ exp.criteria.dirty
         }
      }
      */

      render ( [res] as JSON)
   }

   @SecuredStateless
   def getMatchingEhrs(String ehrQueryUid, String format)
   {
      if (!ehrQueryUid)
      {
         renderError("ehrQueryUid is required", '9456', 404)
         return
      }

      def equery = EhrQuery.findByUid(ehrQueryUid)

      if (!equery)
      {
         renderError("Query not found for ehrQueryUid ${ehrQueryUid}", '9457', 404)
         return
      }

      def orgUid = request.securityStatelessMap.extradata.org_uid
      def ehrUids = equery.getEhrUids2(orgUid)
      render (ehrUids as JSON)
   }

}
