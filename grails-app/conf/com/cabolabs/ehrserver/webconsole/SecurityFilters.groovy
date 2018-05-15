/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 * at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI,
 * you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
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

package com.cabolabs.ehrserver.webconsole

import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.security.*
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare
import com.cabolabs.ehrserver.reporting.ActivityLog
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import grails.converters.*
import javax.servlet.http.Cookie
import org.springframework.beans.propertyeditors.LocaleEditor
import org.springframework.web.servlet.support.RequestContextUtils
import grails.util.Holders
import com.cabolabs.ehrserver.account.Account

class SecurityFilters {

   def springSecurityService
   def messageSource
   def apiResponsesService

   static def config = Holders.config.app

   String format(request, params)
   {
      def format
      def accept = request.getHeader('Accept')

      if (params.format)
      {
         if (params.format == 'json') return 'json'
         if (params.format == 'xml') return 'xml'
         return 'json' // format not supported but present, take json as default
      }
      if (accept.contains('application/json')) return 'json'
      if (accept.contains('application/xml')) return 'xml'
      if (accept.contains('text/xml')) return 'xml'

      return 'json' // take json as default
   }

   /**
    * map = [json: {}, xml: {}]
    */
   def forFormat( Map map, request, params )
   {
      map[format(request, params)]()
   }

   // REF https://github.com/Grails-Plugin-Consortium/grails-cookie/blob/master/src/main/groovy/grails/plugin/cookie/CookieHelper.groovy
   /*
   String getDefaultCookiePath(String path) {
      String cookiePath
      if (path) {
          cookiePath = path
      } else if (grailsApplication.config.grails.plugins.cookie.path.defaultStrategy == 'root') {
          cookiePath = '/'
      } else if (grailsApplication.config.grails.plugins.cookie.path.defaultStrategy == 'current') {
          cookiePath = null
      } else {
          cookiePath = request.contextPath
      }
      cookiePath
   }
   */
   def setLangCookie(String lang, response)
   {
      //println "set lang cookie ${lang}"

      Cookie langCookie = new Cookie( 'lang', lang )
      langCookie.path = '/'
      langCookie.maxAge = 604800
      response.addCookie langCookie
   }

   def setRequestLocale(String lang, request, response)
   {
      // sets the request locale
      // REF https://github.com/grails/grails-core/blob/246b7264e8a638ada188ddba7a7a8812ba153399/grails-web-common/src/main/groovy/org/grails/web/i18n/ParamsAwareLocaleChangeInterceptor.groovy
      def localeResolver = RequestContextUtils.getLocaleResolver(request)
      def localeEditor = new LocaleEditor()
      localeEditor.setAsText lang
      localeResolver?.setLocale request, response, (Locale)localeEditor.value
   }

   def getRequestLocale(request)
   {
      org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
   }

   def canCreateLog(controller, action)
   {
      def avoid_activity_log = [
        [controller:'activityLog', action:'*'], // avoid loading activity logs of loading activity logs :()
        [controller:'notification', action:'newNotifications'], // avoid checking for notifications via AJAX
        [controller:'notification', action:'dismiss'] // avoid logging the dismiss of a notification in the web console
      ]

      def matching_controller_avoids = avoid_activity_log.findAll { it.controller == controller } // can be a list of rules
      def avoid = false
      matching_controller_avoids.each { rule ->
         if (rule.action == '*' || rule.action == action)
         {
            avoid = true
            return true
         }
      }

      return !avoid
   }

   def filters = {
      all(controller:'*', action:'*') {
         before = {

            /**
             * Lang check
             *
             * 1. enter first time
             *    cookie == null
             *    params lang == null
             *    request locale == browser lang
             *    set session lang = request locale
             *
             * 2. change lang without login in
             *    cookie == null
             *    params lang != null
             *    set request locale = params lang (grails does this)
             *    set session lang = params lang
             *
             * 3. login
             *    cookie == null
             *    set org pref lang = session lang
             *    set cookie = org pref lang // cookie is always equal to the latest change of the org pref lang
             *    cookie != null
             *
             * 4. enter with cookie set (use cookie to let grails know the previous used lang without login in)
             *    cookie != null
             *    set session lang = cookie
             *    set request locale = cookie
             *
             * 5. login with cookie set (nothing to do here...)
             *    cookie != null
             *    cookie == org pref lang (asset this just to test)
             *
             * 6. enter with cookie set, and change the lang (this is the same as 4.)
             *    cookie != null
             *    params lang == null
             *    set session lang = cookie
             *    set request locale = cookie
             *
             * 7. change the lang with cookie set
             *    cookie != null
             *    params lang != null
             *    set request locale = params lang (grails does this)
             *    set session lang = params lang
             *    set cookie = params lang // updates the cookie to avoid the next request to take the old language,
             *                             // on the login the same value will be used to set the org pref lang that
             *                             // is equals to the session lang
             *
             * 8. login
             *    cookie != null
             *    set org pref lang = session lang
             *    // no need to update the cookie because will already have the same lang as the org, but we can double check
             */

            def langCookie = request.cookies.find{ it.name == 'lang' }
            if (langCookie)
            {
               if (params.lang) // 7. user changes the lang?
               {
                  session.lang = params.lang
                  setRequestLocale(params.lang, request, response)
                  //langCookie.value = params.lang // doesnt update the cookie value on the client
                  setLangCookie(params.lang, response) // update the cookie value
               }
               else // 4. & 6. get lang from cookie
               {
                  session.lang = langCookie.value
                  setRequestLocale(session.lang, request, response)
               }
            }
            else
            {
               if (params.lang) // 2. set the params lang to session
               {
                  session.lang = params.lang
               }
               else // 1. get the lang from the request as the browser sends it
               {
                  session.lang = getRequestLocale(request).language
               }
            }

            if (canCreateLog(controllerName, actionName))
            {
               /**
                * Activity log.
                */
               def username
               def organizationUid

               // also consider endpoints outside rest, TODO: stats API.
               if (controllerName == 'rest' || ['user:profile'].contains(controllerName+':'+actionName)) // API call?
               {
                  if (actionName == 'login')
                  {
                     username = params.username // no need of checking if the username exists, that is done by the login itself :)
                  }
                  else
                  {
                     // exceptions for rest: actions that are not endpoints
                     // FIXME: we need to move these actions to the query controller or just use the query endpoint for data testing
                     if (['queryCompositions', 'queryData'].contains(actionName))
                     {
                        def auth = springSecurityService.authentication
                        if (auth instanceof com.cabolabs.security.UserPassOrgAuthToken) // can be anonymous
                        {
                           username = auth.principal.username
                        }
                     }
                     else
                     {
                        // FIXME: do the checks after the activity log, so if there is an error, it gets logged.

                        def result, status = 400

                        // 404 case, requested url is not valid in the API
                        // if there is no securityStatelessMap it means the action requested
                        // was not annotated and was requetsed to the RestController because
                        // controllerName == 'rest', so the URL to the API is wrong.
                        // https://github.com/ppazos/cabolabs-ehrserver/issues/901
                        if (!request.securityStatelessMap)
                        {
                           status = 404
                           result = apiResponsesService.feedback(
                              messageSource.getMessage('rest.error.notFound', null, getRequestLocale(request)),
                              'AR',
                              '987600',
                              params.format)
                        }
                        else
                        {
                           // check data in the JWT token: username and organization
                           username = request.securityStatelessMap.username

                           if (User.countByUsername(username) == 0)
                           {
                              if (username.startsWith("apikey"))
                              {
                                 result = apiResponsesService.feedback(
                                    messageSource.getMessage('rest.error.token.apiKeyExpired', null, getRequestLocale(request)),
                                    'AR',
                                    '987656',
                                    params.format)
                              }
                              else
                              {
                                 result = apiResponsesService.feedback(
                                    messageSource.getMessage('rest.error.token.usernameDoesntExists', [username] as Object[], getRequestLocale(request)),
                                    'AR',
                                    '987653',
                                    params.format)
                              }
                           }
                        }

                        if (result) // there was a problem
                        {
                           switch (params.format?.toLowerCase())
                           {
                              case 'xml':
                                 render(status:status, text:result, contentType:"text/xml", encoding:"UTF-8")
                              break
                              case 'json':
                                 response.status = status
                                 render(text:result, contentType:"application/json", encoding:"UTF-8")
                              break
                              default:
                                 render(status:status, text:result, contentType:"text/xml", encoding:"UTF-8")
                           }

                            // TODO: should create activity log and admin notification
                           return false
                        }



                        def org_uid = request.securityStatelessMap.extradata.org_uid
                        if (Organization.countByUid(org_uid) == 0)
                        {
                           // TODO: send in the request format (add support to json)
                           render(status: 400, contentType:"text/xml", encoding:"UTF-8") {
                              result {
                                 type('AR')                         // application reject
                                 message(
                                    messageSource.getMessage('rest.error.token.organizationDoesntExists', [org_uid] as Object[], getRequestLocale(request))
                                 )
                                 code('EHR_SERVER::API::ERRORS::987657') // sys::service::concept::code
                              }
                           }

                           // TODO: should create activity log and admin notification
                           return false
                        }

                        organizationUid = org_uid

                        /*
                        if the user was removed from the org, the token is still valid but it can't access that org
                        */
                        def us = User.findByUsername(username)
                        def orgs = us.organizations
                        if (!orgs.find{ it.uid == org_uid })
                        {
                           render(status: 400, contentType:"text/xml", encoding:"UTF-8") {
                              result {
                                 type('AR')                         // application reject
                                 message(
                                    messageSource.getMessage('rest.error.token.userDoesntBelongToOrganization', [username, org_uid] as Object[], getRequestLocale(request))
                                 )
                                 code('EHR_SERVER::API::ERRORS::987654') // sys::service::concept::code
                              }
                           }

                           // TODO: should create activity log and admin notification
                           return false
                        }
                     }
                  }
               }
               else // Web Console, not API
               {
                  def auth = springSecurityService.authentication

                  if (auth instanceof com.cabolabs.security.UserPassOrgAuthToken) // can be anonymous
                  {
                     username = auth.principal.username

                     def _org = Organization.findByNumber(auth.organization) // FIXME: can be null!

                     organizationUid = _org.uid
                  }
                  else // not logged in,
                  {
                     // captures login/auth, login/authFail
                     if (controllerName == 'login')
                     {
                        username = params.username // username is empty, asked on stack overflow how to get the username on failed logins
                     }
                  }
               }

               def alog = new ActivityLog(
                  username:        username, // can be null
                  organizationUid: organizationUid,
                  action:          controllerName+':'+actionName,
                  objectId:        params.id, // can be null
                  objectUid:       params.uid, // can be null
                  remoteAddr:      request.remoteAddr,
                  clientIp:        request.getHeader("Client-IP"), // can be null
                  xForwardedFor:   request.getHeader("X-Forwarded-For"), // can be null
                  referer:         request.getHeader('referer'), // can be null
                  requestURI:      request.forwardURI,
                  matchedURI:      request.requestURI,
                  sessionId:       session.id)


               // TODO: file log failure
               if (!alog.save()) println "activity log is not saving "+ alog.errors.toString()

               // experiment to link commit log to this activityLog
               session.activity_log_id = alog.id
            }


            /**
             * set session.organization to be used on actions to filter by current org without querying.
             */
            // this only applies to UI, avoid processing for API
            // forwardURI condition was added since some user endpoints are not in the RestController
            if (controllerName != 'rest' && !request.forwardURI.contains('rest'))
            {
               def auth = springSecurityService.authentication

               // GrailsAnonymousAuthenticationToken instance is returned forthat dont require authentication
               // and UserPassOrgAuthToken when the user is logged in
               if (!session.organization && auth instanceof com.cabolabs.security.UserPassOrgAuthToken)
               {
                  def org = Organization.findByNumber(auth.organization)
                  if (org.preferredLanguage != session.lang)
                  {
                     org.preferredLanguage = session.lang // 3. & 8. set org pref lang
                     org.save(failOnError: true)

                     // add a cookie with the latest language selected,
                     // so the next time that cookie can be checked and
                     // the language set, even if the user is not yet
                     // logged in.
                     setLangCookie(session.lang, response) // 3. sets cookie with the org pref lang
                  }
                  session.organization = org // to show the org name in the ui

                  // Load OPTS if not loaded for the current org
                  def optMan = OptManager.getInstance()
                  optMan.loadAll(org.uid)
               }
            }

         }
         /*
         after = { Map model ->
            // this is AFTER: login authfail null when the login failed.
            //println "AFTER: ${controllerName} ${actionName} ${model}"
         }
         afterView = { Exception e ->
            //println "AFTER VIEW: ${controllerName} ${actionName}"
         }
         */
      }

      // account management is only for admins
      // index is included because admins can see all the accounts
      // each account manager will see just his account (next filter) // THIS MIGHT NOT BE NEEDED since Accounts don't have much data to show
      // other users will not have access to the account info
      account_management_access(controller:'account', action:'index|create|save|edit|update|delete') {
         before = {

            if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
            {
               flash.message = "You don't have access to account management!"

               // back action uses chain to show the flash, with redirect that does not work.
               if (request.getHeader('referer'))
                  chain url: request.getHeader('referer')
               else
                  chain controller: 'app', action: 'index'
               return false
            }

            return true
         }
      }

      account_show_access(controller:'account', action:'show') {
         before = {

            if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
            {
               return true
            }

            def loggedInUser = springSecurityService.currentUser

            //println params.id.getClass() // String!
            //println loggedInUser.account.id.getClass() // Long

            if (loggedInUser.account.id != Long.valueOf(params.id))
            {
               flash.message = "You don't have access to this account!"
               chain controller: 'app', action: 'index'
               return false
            }

            return true
         }
      }


      /*
      account_show_access(controller:'account', action:'show') {
         before = {

            if (!SpringSecurityUtils.ifAllGranted("ROLE_ACCOUNT_MANAGER"))
            {
               flash.message = "You don't have access to account management!"

               // back action uses chain to show the flash, with redirect that does not work.
               if (request.getHeader('referer'))
                  chain url: request.getHeader('referer')
               else
                  chain controller: 'app', action: 'index'
               return false
            }

            return true
         }
      }
      */


      rest_check_format(controller:'rest', action:'*') {
         before = {

            if (!['', null, 'xml', 'json', 'html'].contains(params.format)) // queryCompositions support html
            {
               // bad request in XML
               render(status: 400, contentType:"text/xml", encoding:"UTF-8") {
                  result {
                     type('AR')                         // application reject
                     message(
                        messageSource.getMessage('rest.error.formatNotSupported', [params.format] as Object[], getRequestLocale(request))
                     )
                     code('EHR_SERVER::API::ERRORS::0066') // sys::service::concept::code
                  }
               }
               return false
            }

            return true
         }
      }


      allow_user_register_check(controller:'user', action:'register') {
         before = {

            if (!config.allow_web_user_register.toBoolean()) // toBoolean needed because the env var is string
            {
               flash.message = messageSource.getMessage('user.register.notAllowed', null, getRequestLocale(request))
               redirect controller: 'app'
               return false
            }

            return true
         }
      }


      organization_show(controller:'organization', action:'show') {
         before = {

            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations

            if (!params.uid)
            {
               flash.message = "Organization UID is required"
               chain controller: 'organization', action: 'index'
               return false
            }

            def o = Organization.findByUid(params.uid)

            if (!o)
            {
               flash.message = "The organization doesn't exists"
               chain controller: 'organization', action: 'index' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }

            if (!orgs.uid.contains(o.uid) && !SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
            {
               flash.message = "You don't have access to that organization!"
               chain controller: 'organization', action: 'index' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }

            params.organizationInstance = o
            return true
         }
      }

      organization_edit(controller:'organization', action:'edit') {
         before = {

            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username
            def us = User.findByUsername(un)
            def orgs = us.organizations

            if (!params.uid)
            {
               flash.message = "Organization UID is required"
               chain controller: 'organization', action: 'index'
               return false
            }

            def o = Organization.findByUid(params.uid)

            if (!o)
            {
               flash.message = "The organization doesn't exists"
               chain controller: 'organization', action: 'index' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }

            // admins can edit any org
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN"))
            {
               // user is associated with the org?
               if (!orgs.uid.contains(o.uid))
               {
                  flash.message = "You don't have access to that organization!"
                  chain controller: 'organization', action: 'index' // back action uses chain to show the flash, with redirect that does not work.
                  return false
               }
            }

            params.organizationInstance = o
            return true
         }
      }

      organization_update(controller:'organization', action:'update') {
         before = {

            //println "org update filter params "+ params

            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            //def org = Organization.findByNumber(auth.organization) // organization used to login

            // admins can edit any org
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN"))
            {
               if (!params.uid || !orgs.uid.contains(params.uid))
               {
                  flash.message = "You don't have access to the specified organization"
                  chain controller: 'organization', action: 'index'
                  return false
               }
            }

            return true
         }
      }

      /*
       * Any ADMIN or ORG_MANAGER can share the query to other organizations of their own,
       * if the query is not public AND is shared with the organization used for the login OR the user is the author,
       * OR is public AND the logged user is the author.
       */
      query_share(controller:'resource', action:'saveSharesQuery') {
         before = {

            if (!SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER"))
            {
               flash.message = "You need and higher role to edit the shares"
               chain controller: 'query', action: 'list'
               return false
            }

            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def user = User.findByUsername(un)
            //def org = Organization.findByNumber(auth.organization) // org in the login
            def orgs = user.organizations

            if (!params.uid)
            {
               flash.message = "Query UID is required"
               chain controller: 'query', action: 'list'
               return false
            }

            def query = Query.findByUid(params.uid)

            if (!query)
            {
               flash.message = "Query not found"
               chain controller: 'query', action: 'list'
               return false
            }

            if (query.isPublic)
            {
               log.error "User tries to access public query to share "+ params +", public queries can't be shared"
               flash.message = "Public queries can't be shared"
               chain controller: 'query', action: 'list'
               return false
            }
            else
            {
               println "is private"

               // if the user is the author, let it pass, the check below is only for non authors

               // the login org should be in the currently shared orgs of the query
               // WARNING: if the user unshares the query with the org, won't be able
               // to share it again until the user logs in with another organization
               if (query.author.id != user.id) // if the user is not the authro
               {
                  // check if query is shared with the login org
                  def shares = QueryShare.findAllByQuery(query)
                  def orgCanAccess = (shares.organization.find{ it.uid == session.organization.uid } != null)
                  if (!orgCanAccess)
                  {
                     flash.message = "The query is not shared with the organization used to login, please login with an organization that the query is shared with"
                     chain controller: 'query', action: 'list'
                     return false
                  }
                  /*
                  def found = false
                  shares.organization.each { share_org ->
                     if (share_org.number == auth.organization)
                     {
                        found = true
                        return true // break
                     }
                  }
                  if (!found)
                  {
                     flash.message = "The query is not shared with the organization used to login, please login with an organization that the query is shared with"
                     chain controller: 'query', action: 'list'
                     return false
                  }
                  */
               }
            }

            // check that all the org uids submitted are accessible by the user
            def orgUids = params.list('organizationUid') - [null, '']

            orgUids.each { organizationUid ->
               if(!orgs.uid.contains(organizationUid))
               {
                  flash.message = "You don't have access to the specified organization ${organizationUid}"
                  chain controller: 'query', action: 'list'
                  return false
               }
            }


            // pass the query as param to avoid making the query again in the controller
            params.query = query // it can be set on request also

            return true
         }
      } // query_share

      query_edit(controller:'query', action:'edit') {
         before = {

            if (!params.uid)
            {
               flash.message = 'query.execute.error.queryUidMandatory'
               redirect(action:'list')
               return
            }

            def query = Query.findByUid(params.uid)

            // only the author can edit a public query
            // this is to avoid chaos :)
            // not the author
            if (query.isPublic && query.organizationUid != session.organization.uid)
            {
               flash.message = "A public query can be edited only from the organization in which it was created"
               chain controller: 'query', action: 'show', params: params
               return false
            }

            params.query = query
         }
      }

      /*
       * Query update is done via AJAX from the Query Builder screen.
       */
      query_update(controller:'query', action:'update') {
         before = {

            def user = springSecurityService.getCurrentUser()

            def json = request.JSON.query
            def query = Query.get(json.id) // the id comes in the json object

            if (!query)
            {
               response.status = 400 // bad request
               render (text: [message: "Query not found", status: 'error'] as JSON, contentType:"application/json", encoding:"UTF-8")
               return false
            }

            // only the author can edit a public query
            if (query.isPublic && query.organizationUid != session.organization.uid)
            {
               response.status = 400 // bad request
               render (text: [message: "A public query can be edited only from the organization in which it was created", status: 'error'] as JSON, contentType:"application/json", encoding:"UTF-8")
               return false
            }

            params.json = json
            params.query = query
         }
      }

      stats_repo_usage_permissions(controller:'stats', action:'accountRepoUsage') {
         before = {

            if (!params.id)
            {
               render(text: [error: 'id is required'] as JSON, status: 400, contentType:"application/json", encoding:"UTF-8")
               return false
            }

            def account = Account.get(params.id)

            if (!account)
            {
               render(text: [error: 'account not found'] as JSON, status: 400, contentType:"application/json", encoding:"UTF-8")
               return false
            }

            // admins access stats for all the accounts
            // other users access only stats from their accounts
            if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN"))
            {
               return true
            }
            else
            {
               def user = springSecurityService.getCurrentUser() // not admin
               if (account.id == user.account.id) return true
            }

            render(text: [error: "you don't have access to this account"] as JSON, status: 400, contentType:"application/json", encoding:"UTF-8")
            return false
         }
      }

      mgt_api_stats(controller:'stats', action:'userAccountStats') {
         before = {

            // 0. auth user is admin
            // 1. username is not empty
            // 2. user for username exists
            // 3. username param is for an account manager user

            def _token_username = request.securityStatelessMap.username
            def _token_user = User.findByUsername(_token_username)
            //def _org_uid = request.securityStatelessMap.extradata.org_uid
            //def org = Organization.findByUid(_org_uid)

            // Note API Keys can't access this, should be a fully authenticated user.
            if (!_token_user.authoritiesContains(Role.AD, session.organization))
            {
               forFormat([
                  xml : {
                     render(status: 403, contentType:"text/xml", encoding:"UTF-8") {
                        result {
                           type('AR')
                           message("You don't have access to this API")
                           code('99999') // FIXME
                        }
                     }
                  },
                  json : {
                     def error = [
                        result: [
                           type: 'AR',
                           message: "You don't have access to this API",
                           code: '99999' // FIXME
                        ]
                     ]

                     response.status = 403

                     // JSONP
                     if (params.callback) result = "${params.callback}( ${result} )"
                     render(text: error as JSON, contentType:"application/json", encoding:"UTF-8")
                  }
               ], request, params)

               return false
            }

            if (!params.username)
            {
               forFormat([
                  xml : {
                     render(status: 400, contentType:"text/xml", encoding:"UTF-8") {
                        result {
                           type('AR')
                           message("username is required")
                           code('99999') // FIXME
                        }
                     }
                  },
                  json : {
                     def error = [
                        result: [
                           type: 'AR',
                           message: "username is required",
                           code: '99999' // FIXME
                        ]
                     ]

                     response.status = 400

                     // JSONP
                     if (params.callback) result = "${params.callback}( ${result} )"
                     render(text: error as JSON, contentType:"application/json", encoding:"UTF-8")
                  }
               ], request, params)

               return false
            }


            def accmgt = User.findByUsername(params.username)

            if (!accmgt)
            {
               forFormat([
                  xml : {
                     render(status: 404, contentType:"text/xml", encoding:"UTF-8") {
                        result {
                           type('AR')
                           message("User not found")
                           code('99999') // FIXME
                        }
                     }
                  },
                  json : {
                     def error = [
                        result: [
                           type: 'AR',
                           message: "User not found",
                           code: '99999' // FIXME
                        ]
                     ]

                     response.status = 404

                     // JSONP
                     if (params.callback) result = "${params.callback}( ${result} )"
                     render(text: error as JSON, contentType:"application/json", encoding:"UTF-8")
                  }
               ], request, params)

               return false
            }

            if (!accmgt.authoritiesContains(Role.AM, session.organization)) // FIXME org is the admin org, not the accmgt org.
            {
               forFormat([
                  xml : {
                     render(status: 400, contentType:"text/xml", encoding:"UTF-8") {
                        result {
                           type('AR')
                           message("User is not account manager")
                           code('99999') // FIXME
                        }
                     }
                  },
                  json : {
                     def error = [
                        result: [
                           type: 'AR',
                           message: "User is not account manager",
                           code: '99999' // FIXME
                        ]
                     ]

                     response.status = 400

                     // JSONP
                     if (params.callback) result = "${params.callback}( ${result} )"
                     render(text: error as JSON, contentType:"application/json", encoding:"UTF-8")
                  }
               ], request, params)


               return false
            }
         }
      } // stats

   } // filters
}
