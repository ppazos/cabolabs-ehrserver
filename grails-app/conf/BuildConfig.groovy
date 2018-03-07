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

grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.reload.enabled = true

forkConfig = [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 512]
grails.project.fork = [
   test: forkConfig, // configure settings for the test-app JVM
   run: forkConfig, // configure settings for the run-app JVM
   war: forkConfig, // configure settings for the run-war JVM
   console: forkConfig // configure settings for the Swing console JVM
]

grails {
   tomcat {
       jvmArgs = ["-Duser.timezone=UTC", "-Dserver.port=8090"]
   }
}


//grails.project.war.file = "target/${appName}-${appVersion}.war"

// include XSDs and XSLTs in the war
grails.war.resources = { stagingDir, args ->
   copy(todir: "${stagingDir}/xsd") {
       fileset(dir: "xsd", includes: "*.*")
   }
}

grails.server.port.http = 8090
grails.project.dependency.resolver = "maven"
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

        mavenRepo "http://repo.spring.io/milestone/"
        //mavenRepo "https://oss.sonatype.org/content/repositories/snapshots/"

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
         excludes "commons-logging", "xml-apis", "groovy", "nekohtml"
       }

       test "org.grails:grails-datastore-test-support:1.0-grails-2.4"

       //compile 'xerces:xercesImpl:2.11.0'
       compile group: 'de.odysseus.staxon', name: 'staxon', version: '1.2'

       compile "mysql:mysql-connector-java:5.1.43" //"mysql:mysql-connector-java:5.1.22"

       //compile "org.springframework.security:spring-security-crypto:4.0.2-RELEASE"
       //compile 'org.pac4j:pac4j-core:1.7.1'
    }

    plugins {

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:7.0.55.2"

        // plugins for the compile step
        compile ':scaffolding:2.1.2'
        compile ':cache:1.1.8'
        compile ':asset-pipeline:2.5.7'
        compile ':quartz:1.0.2'
        compile ':spring-security-core:2.0-RC6'
        compile ":mail:1.0.7"
        compile ":simple-captcha:1.0.0"

        // plugins needed at runtime but not for compilation
        runtime ':hibernate4:4.3.10' // or ':hibernate:3.6.10.14'
        runtime ':database-migration:1.4.0'
        runtime ':jquery:1.11.1'

        // https://github.com/davidtinker/grails-cors
        runtime ":cors:1.1.8"

        //compile ':spring-security-rest:1.5.2', {
        //   excludes 'spring-security-core', 'cors'
        //}
        compile "org.grails.plugins:security-stateless:0.0.9"
    }
}
