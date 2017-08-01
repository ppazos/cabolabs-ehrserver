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

// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

def PS = System.getProperty("file.separator")


// CORS
cors.url.pattern = '/api/*'
cors.headers = ['Access-Control-Allow-Origin': '*']
cors.enabled = true


// upgrade to 2.5.3
beans {
  cacheManager {
    shared = true
  }
}


// test stateless security
grails.plugin.security.stateless.secretKey = System.getenv('EHRSERVER_REST_SECRET') //'88f0435c-ff45-4b5b-874f-689ad94adcad'
grails.plugin.security.stateless.springsecurity.integration = false
grails.plugin.security.stateless.format = "JWT"
//grails.plugin.security.stateless.expirationTime = 1440 // 1 day
//grails.plugin.security.stateless.expiresStatusCode = 401


grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = true // enable the Accept header to determine the response format
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
//grails.resources.adhoc.patterns = ['/images/*', '/images/icons/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.views.javascript.library="jquery"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
  development {
    grails.logging.jul.usebridge = true
    //grails.serverURL = "http://localhost:8090/ehr"
    app {
      //opt_repo = new File(".").getAbsolutePath() + 'opts' + PS // OPT file upload destination
      version_repo = "versions" + PS
      commit_logs = "commits" + PS
      opt_repo = "opts" + PS
      allow_web_user_register = System.getenv('EHRSERVER_ALLOW_WEB_USER_REGISTER')
    }
  }
  production { // use on server prod environment, https, root = /
    grails.logging.jul.usebridge = false
    //grails.dbconsole.enabled = true // this is for testing in prod
    // System.getenv('OPENSHIFT_APP_DNS') == "ehrserver-cabolabs2.rhcloud.com"
    grails.serverURL = "https://" + System.getenv('OPENSHIFT_APP_DNS') //"https://cabolabs-ehrserver.rhcloud.com/ehr" // comment this if testing prod on localhost
    grails.app.context = '/' // use domain.com/ instead of domain.com/ehr
    app {
      //opt_repo = System.getenv('OPENSHIFT_DATA_DIR') + 'opts' + PS  // OPT file upload destination
      version_repo = System.getenv('EHRSERVER_WORKING_FOLDER') + "versions" + PS
      commit_logs = System.getenv('EHRSERVER_WORKING_FOLDER') + "commits" + PS
      opt_repo = System.getenv('EHRSERVER_WORKING_FOLDER') + "opts" + PS
      allow_web_user_register = System.getenv('EHRSERVER_ALLOW_WEB_USER_REGISTER')
    }
  }
  local_prod { // use to run locally without https or root context /
     grails.logging.jul.usebridge = false
     app {
      version_repo = System.getenv('EHRSERVER_WORKING_FOLDER') + "versions" + PS
      commit_logs = System.getenv('EHRSERVER_WORKING_FOLDER') + "commits" + PS
      opt_repo = System.getenv('EHRSERVER_WORKING_FOLDER') + "opts" + PS
      allow_web_user_register = System.getenv('EHRSERVER_ALLOW_WEB_USER_REGISTER')
    }
  }
  local_prod_tomcat { // use for testing prod in local env deploying in tomcat (running on port 8085)
    grails.logging.jul.usebridge = false
    grails.serverURL = "http://localhost:8085/ehr-0.8"
    app {
      version_repo = System.getenv('EHRSERVER_WORKING_FOLDER') + "versions" + PS
      commit_logs = System.getenv('EHRSERVER_WORKING_FOLDER') + "commits" + PS
      opt_repo = System.getenv('EHRSERVER_WORKING_FOLDER') + "opts" + PS
      allow_web_user_register = true
    }
  }
  test {
    grails.converters.default.pretty.print = true
    app {
       version_repo = "test"+ PS +"resources"+ PS +"temp_versions" + PS
       commit_logs = "commits" + PS
       opt_repo = "opts" + PS + "tests" + PS
       allow_web_user_register = true
    }
  }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}
   // trace 'org.hibernate.type'
    //debug 'org.hibernate.SQL'

    error  'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework'
           //'org.hibernate',
           //'net.sf.ehcache.hibernate'
    //debug  'org.codehaus.groovy.grails.orm.hibernate.cfg'
    info 'org.codehaus.groovy.grails.web.servlet'        // controllers
    info 'grails.app.services.com.cabolabs.ehrserver.data.DataIndexerService'
    
    // EHRServer logs
    info 'com.cabolabs.security.AuthProvider'
    info 'com.cabolabs.archetype.OperationalTemplateIndexer'
}

app {
   list_max = 20 // items per page for all the lists
   version_xsd = "xsd"+ PS +"Version.xsd"
   xslt = "xsd"+ PS +"openEHR_RMtoHTML.xsl"
   opt_xsd = "xsd"+ PS +"OperationalTemplate.xsd"
   
   security {
      min_password_length = 6
   }
   
   l10n { // localization
      
      locale = 'en' // FIXME: this will depend on the organization
      
      // general
      decimal_symbol = ',' // separa numero enteros de la fraccion decimal
      decimal_digits = 2   // digitos luego de decimal_symbol
      digit_grouping = '.' // agrupador de a3 digitos para escribir numeros grandes ej. 1.000
      display_leading_zeros = true // ej. si es false, 0,7 se escribe ,7
      
      // formatos de fechas
      // ==================
      //  - ref: http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
      //
      // h hora 1-12
      // H hora 0-23
      // a marcador AM/PM
      // m minutis
      // S milisegundos
      // Z zona horaria (RFC 822)
      
      // formatos para procesamiento de fechas
      // incluye fraccion (debe estar separado con el decimal_symbol) y zona horaria
      datetime_format = "yyyyMMdd'T'HHmmss,SSSX" 
      date_format = "yyyyMMdd"
      time_format = "HHmmss"
      
      
      // Extended formats supported by openEHR --------------------------------------------------------------
      //2015-12-02T17:41:56.809Z
      ext_datetime_format = "yyyy-MM-dd'T'HH:mm:ss,SSSX" // contains timezone e.g. -0300
      ext_datetime_format_point = "yyyy-MM-dd'T'HH:mm:ss.SSSX"
      // If the time is in UTC, add a Z directly after the time without a space. Z is the zone
      // designator for the zero UTC offset. "09:30 UTC" is therefore represented as "09:30Z" or "0930Z".
      ext_datetime_utcformat = "yyyy-MM-dd'T'HH:mm:ss,SSS'Z'"
      ext_datetime_utcformat_point = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
      // ----------------------------------------------------------------------------------------------------
      
      
      // Dates without seconds fraction
      datetime_format_nof = "yyyyMMdd'T'HHmmssX"
      ext_datetime_format_nof = "yyyy-MM-dd'T'HH:mm:ssX"
      ext_datetime_utcformat_nof = "yyyy-MM-dd'T'HH:mm:ss'Z'"
      
      
      // formatos para mostrar las fechas al usuario
      display_datetime_format = "yyyy/MM/dd HH:mm:ss (X)" 
      display_date_format = "yyyy/MM/dd"
      display_time_format = "HH:mm:ss"
      
      db_datetime_format = "yyyy-MM-dd HH:mm:ss" // mysql no soporta fragment o timezone, otros dbms si
      db_date_format = "yyyy-MM-dd"
      db_time_format = "HH:mm:ss"
   }
}


// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.cabolabs.security.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.cabolabs.security.UserRole'
grails.plugin.springsecurity.authority.className = 'com.cabolabs.security.Role'
grails.plugin.springsecurity.requestMap.className = 'com.cabolabs.security.RequestMap'
grails.plugin.springsecurity.securityConfigType = 'Requestmap'
grails.plugin.springsecurity.rejectIfNoRule = true
grails.plugin.springsecurity.successHandler.defaultTargetUrl = '/app/index'
grails.plugin.springsecurity.successHandler.useReferer = false
grails.plugin.springsecurity.apf.filterProcessesUrl = "/j_ehrserver_security_check" //"/user/login" // custom login, overrides: plugins/spring-security-core-2.0-RC5/conf/DefaultSecurityConfig.groovy
grails.plugin.springsecurity.providerNames = ['authProvider']


// Allow logout through GET operation (by default only POSTs are accepted since plugin v2.0)
grails.plugin.springsecurity.logout.postOnly = false

// Mail
grails {
   mail {
     host = System.getenv('EHRSERVER_EMAIL_HOST')
     port = System.getenv('EHRSERVER_EMAIL_PORT')
     username = System.getenv('EHRSERVER_EMAIL_USER')
     password = System.getenv('EHRSERVER_EMAIL_PASS')
     'default' {
        from = System.getenv('EHRSERVER_EMAIL_FROM')
     }
     /*
     props = ["mail.smtp.auth":"true",
              "mail.smtp.starttls.enable":"true",
              //"mail.smtp.port":"587"
              "mail.smtp.socketFactory.port":"465",
              "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
              "mail.smtp.socketFactory.fallback":"false"
              ]
     */
     /*
     for testing
     grails.mail.disabled=true
     
     ref http://padcom13.blogspot.com.uy/2011/01/testing-sending-emails-with-grails.html
     
     or ref: https://stackoverflow.com/questions/8884186/how-to-integration-test-email-body-in-a-grails-service-that-uses-the-mail-plugin
     */
   }
}
