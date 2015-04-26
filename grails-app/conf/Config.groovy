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

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
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
    }
    production {
        grails.logging.jul.usebridge = false
        grails.dbconsole.enabled = true // FIXME: this is for testing in prod
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
	test {
	   grails.converters.default.pretty.print = true
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
   // debug 'org.hibernate.SQL'

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
           
    info 'org.codehaus.groovy.grails.web.servlet'        // controllers
}

def PS = System.getProperty("file.separator")

app {
   composition_repo = "compositions" + PS //"compositions\\" // FIXME: use OS independent file path separator
   version_repo = "versions" + PS // "versions\\"
   version_xsd = "xsd"+ PS +"Version.xsd"
   opt_repo = "opts" + PS
   
   l10n { // localization
      
      locale = 'es'
      
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
      datetime_format = "yyyyMMdd'T'HHmmss,SSSZ" 
      date_format = "yyyyMMdd"
      time_format = "HHmmss"
      
      // formatos para mostrar las fechas al usuario
      display_datetime_format = "yyyy/MM/dd HH:mm:ss (Z)" 
      display_date_format = "yyyy/MM/dd"
      display_time_format = "HH:mm:ss"
      
      db_datetime_format = "yyyy-MM-dd HH:mm:ss" // mysql no soporta fragment o timezone, otros dbms si
      db_date_format = "yyyy-MM-dd"
      db_time_format = "HH:mm:ss"
   }
}
