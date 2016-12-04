package com.cabolabs.ehrserver.webconsole

import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.security.User
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare
import com.cabolabs.ehrserver.reporting.ActivityLog
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexShare
import grails.converters.*
import javax.servlet.http.Cookie
import org.springframework.beans.propertyeditors.LocaleEditor
import org.springframework.web.servlet.support.RequestContextUtils

class SecurityFilters {
   
   def springSecurityService

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
   
   def filters = {
      all(controller:'*', action:'*') {
         before = {
            //request.cookies.each { println it.name +"> "+ it.value }
            //request.cookies.each { println "list: cookie ${it.properties}>" }
            //println 'lang cookie before '+ request.cookies.find{ it.name == 'lang' }?.value
            
            /**
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

            // TODO: refactor to a log filter
            def username
            def organizationUid
            if (controllerName == 'rest')
            {
               if (actionName == 'login')
                  username = params.username
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
                     username = request.securityStatelessMap.username
                     
                     def _orgnum = request.securityStatelessMap.extradata.organization
                     def _org = Organization.findByNumber(_orgnum)
                     organizationUid = _org.uid
                  }
               }
            }
            else
            {
               def auth = springSecurityService.authentication
               if (auth instanceof com.cabolabs.security.UserPassOrgAuthToken) // can be anonymous
               {
                  username = auth.principal.username

                  def _org = Organization.findByNumber(auth.organization)
                  organizationUid = _org.uid
               }
            }
            def alog = new ActivityLog(
                            username: username, // can be null
                            organizationUid: organizationUid,
                            action: controllerName+':'+actionName,
                            objectId: params.id, // can be null
                            objectUid: params.uid, // can be null
                            clientIp: request.remoteAddr)
            
            // TODO: file log failure
            if (!alog.save()) println "activity log is not saving "+ alog.errors
            
         }
         after = { Map model ->

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
               }
            }
         }
         afterView = { Exception e ->

         }
      }
      
      
      /**
       * All the lists already filter by organization, 
       * this checks authorization for show/edit/save.
       * 
       **/
      /*
      person_save(controller:'person', action:'save') {
         before = {
            // params.organizationUid should be one of the orgs associated with the current user
            // 
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            //def org = Organization.findByNumber(auth.organization) // organization used to login
            
            if (!params.organizationUid || !orgs.uid.contains(params.organizationUid))
            {
               flash.message = "You don't have access to the specified organization"
               chain controller: 'person', action: 'create' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            return true
         }
      }
      
      person_update(controller:'person', action:'update') {
         before = {
            
            println "a person update filter params "+ params
            
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            
            return true
         }
      }
      
      person_delete(controller:'person', action:'delete') {
         before = {
            
            println "person delete filter params "+ params
            
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            
            return true
         }
      }
      
      person_show(controller:'person', action:'show') {
         before = {
            
            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            return true
         }
      }
      
      person_edit(controller:'person', action:'edit') {
         before = {
            
            // user.organizationUid should be one of the orgs associated with the current user
            //
            def auth = springSecurityService.authentication
            def un = auth.principal.username
            def us = User.findByUsername(un)
            def orgs = us.organizations
            
            if (!params.uid)
            {
               flash.message = "Person UID is required"
               chain controller: 'person', action: 'list'
               return false
            }
            
            def p = Person.findByUid(params.uid)
            
            if (!p || !orgs.uid.contains(p.organizationUid))
            {
               flash.message = "You don't have access to that person!"
               chain controller: 'person', action: 'list' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.personInstance = p
            return true
         }
      }
      */
      
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
            
            // For now admins don't have edit access for organizations that belong to other users
            if (!orgs.uid.contains(o.uid))
            {
               flash.message = "You don't have access to that organization!"
               chain controller: 'organization', action: 'index' // back action uses chain to show the flash, with redirect that does not work.
               return false
            }
            
            params.organizationInstance = o
            return true
         }
      }
      
      organization_update(controller:'organization', action:'update') {
         before = {
            
            println "org update filter params "+ params
            
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def us = User.findByUsername(un)
            def orgs = us.organizations
            //def org = Organization.findByNumber(auth.organization) // organization used to login
            
            if (!params.uid || !orgs.uid.contains(params.uid))
            {
               flash.message = "You don't have access to the specified organization"
               chain controller: 'organization', action: 'index'
               return false
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
            
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_ORG_MANAGER"))
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
               println "is public"
               
               if (query.author.id != user.id) // the user should be the author
               {
                  flash.message = "Only the author of a public query can share it"
                  chain controller: 'query', action: 'list'
                  return false
               }
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
               }
            }
            
            // check that all the org uids submitted are accessible by the user
            def orgUids = params.list('organizationUid')
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
      
      
      /*
       * only the author can change a query from private to public.
       */
      query_update(controller:'query', action:'update') {
         before = {
            
            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def user = User.findByUsername(un)
            //def org = Organization.findByNumber(auth.organization) // org in the login
            //def orgs = user.organizations
            
            /* TODO check that json is coming
            if (!params.uid)
            {
               flash.message = "Query UID is required"
               chain controller: 'query', action: 'list'
               return false
            }
            */

            def json = request.JSON.query
            def query = Query.get(json.id) // the id comes in the json object
            
            if (!query)
            {
               response.status = 400 // bad request
               render (text: [message: "Query not found", status: 'error'] as JSON, contentType:"application/json", encoding:"UTF-8")
               return false
            }
            
            // want to make it public or private
            if (query.isPublic != json['isPublic'])
            {
               // not the author
               if (user.id != query.author.id)
               {
                  response.status = 400 // bad request
                  render (text: [message: "Only the author of the query can make it public or private", status: 'error'] as JSON, contentType:"application/json", encoding:"UTF-8")
                  return false
               }
            }
            
            params.json = json
            params.query = query
         }
      }
      
      opt_share(controller:'resource', action:'saveSharesOpt') {
         before = {
            
            if (!SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_ORG_MANAGER"))
            {
               flash.message = "You need and higher role to edit the shares"
               chain controller: 'operationalTemplateIndex', action: 'list'
               return false
            }
            

            def auth = springSecurityService.authentication
            def un = auth.principal.username // principal is the username before the login, but after is GrailsUser (see AuthProvider)
            def user = User.findByUsername(un)
            //def org = Organization.findByNumber(auth.organization) // org in the login
            def orgs = user.organizations
            
            if (!params.uid)
            {
               flash.message = "Template UID is required"
               chain controller: 'operationalTemplateIndex', action: 'list'
               return false
            }
            

            def opt = OperationalTemplateIndex.findByUid(params.uid)
            
            if (!opt)
            {
               flash.message = "Template not found"
               chain controller: 'operationalTemplateIndex', action: 'list'
               return false
            }
            
            if (opt.isPublic)
            {
               println "is public"

               flash.message = "Can't share a public template, make it private first"
               chain controller: 'operationalTemplateIndex', action: 'list'
               return false
            }
            else
            {
               println "is private"
               
               // check if query is shared with the login org
               def shares = OperationalTemplateIndexShare.findAllByOpt(opt)
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
                  flash.message = "The opt is not shared with the organization used to login, please login with an organization that the tempalte is shared with"
                  chain controller: 'operationalTemplateIndex', action: 'list'
                  return false
               }

            }
            
            // check that all the org uids submitted are accessible by the user
            def orgUids = params.list('organizationUid')
            orgUids.each { organizationUid ->
               if(!orgs.uid.contains(organizationUid))
               {
                  flash.message = "You don't have access to the specified organization ${organizationUid}"
                  chain controller: 'operationalTemplateIndex', action: 'list'
                  return false
               }
            }


            // pass the opt as param to avoid making the query again in the controller
            params.opt = opt // it can be set on request also

            return true
         }
      } // opt_share
      
      
   } // filters
}
