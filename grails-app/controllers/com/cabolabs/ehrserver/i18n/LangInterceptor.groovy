package com.cabolabs.ehrserver.i18n

import javax.servlet.http.Cookie
import org.springframework.beans.propertyeditors.LocaleEditor
import org.springframework.web.servlet.support.RequestContextUtils

class LangInterceptor {

   int order = HIGHEST_PRECEDENCE + 300

   public LangInterceptor()
   {
      matchAll().excludes(controller:'rest')
                .excludes(controller:'restAuth')

   }

   def getRequestLocale(request)
   {
      org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
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

   def setLangCookie(String lang, response)
   {
      //println "set lang cookie ${lang}"

      Cookie langCookie = new Cookie( 'lang', lang )
      langCookie.path = '/'
      langCookie.maxAge = 604800
      response.addCookie langCookie
   }

   def getLangCookie(request)
   {
      request.cookies.find{ it.name == 'lang' } // could be null
   }


   boolean before()
   {
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

      def langCookie = getLangCookie(request)
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
            setLangCookie(params.lang, response) // update the cookie value
         }
         else // 1. get the lang from the request as the browser sends it
         {
            session.lang = getRequestLocale(request).language
         }
      }

      log.debug "lang: "+ session.lang

      true
   }

   boolean after() { true }

   void afterView() {
      // no-op
   }
}
