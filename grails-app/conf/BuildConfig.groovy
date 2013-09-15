grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.20'
       
       // Este error da si uso http-builder 0.6 con 0.5.2 anda ok
       // FIX: Me da un error luego de poner referencia a http-builder
       // http://jira.grails.org/browse/GPEXPORT-18?focusedCommentId=69307&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-69307
       // http://stackoverflow.com/questions/10697312/use-rest-plugin-with-grails-project
       /*
       compile (group:'org.apache.poi', name:'poi', version:'3.7') {
        excludes 'xmlbeans'
       }
       compile (group:'org.apache.poi', name:'poi-ooxml', version:'3.7') {
        excludes 'xmlbeans'
       }
       */
       
/*
       runtime('com.thoughtworks.xstream:xstream:1.4.3') {
	      // loader constraint violation: loader (instance of <bootloader>) previously
	      // initiated loading for a different type with name "org/w3c/dom/TypeInfo"
	      //excludes 'xmlbeans'
	   }
       */
	   
       runtime('org.codehaus.groovy.modules.http-builder:http-builder:0.5.2') {
         excludes "commons-logging", "xml-apis", "groovy"
       }
    }

    plugins {
        runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.7.2"
        runtime ":resources:1.2"

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:$grailsVersion"

        runtime ":database-migration:1.1"

        compile ':cache:1.0.0'
    }
}
