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

dataSource {
    pooled = true
    driverClassName = 'com.mysql.jdbc.Driver'
    dialect = org.hibernate.dialect.MySQL5InnoDBDialect
    username = "root"
    password = "root"
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    //cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
    //cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    cache.region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory' // Hibernate 4
    format_sql = true // format SQL logs
}
// environment specific settings
environments {
    development {
        dataSource {
           dbCreate = "create-drop" //"update" // one of 'create', 'create-drop', 'update', 'validate', ''
           //logSql = true
           String host = "localhost"
           String port = 3306
           String dbName = "ehrserver"
           url = "jdbc:mysql://$host:$port/$dbName" // ?useTimezone=true&serverTimezone=UTC

           username = 'root'
           password = 'toor'
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            String host = "localhost"
            String port = 3306
            String dbName = "ehrservertest"
            url = "jdbc:mysql://$host:$port/$dbName" // ?useTimezone=true&serverTimezone=UTC

            username = 'root'
            password = ''
        }
    }
    production {
        dataSource {

           /* For testing prod on localhost
           dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
           url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"

           driverClassName = 'com.mysql.jdbc.Driver'
           dialect = org.hibernate.dialect.MySQL5InnoDBDialect
           */

            dbCreate = "update"

            String host = System.getenv('EHRSERVER_MYSQL_DB_HOST')
            String port = System.getenv('EHRSERVER_MYSQL_DB_PORT')
            String dbName = System.getenv('EHRSERVER_DB_NAME')

            url = "jdbc:mysql://$host:$port/$dbName?useSSL=false" // ?useTimezone=true&serverTimezone=UTC

            username = System.getenv('EHRSERVER_MYSQL_DB_USERNAME')
            password = System.getenv('EHRSERVER_MYSQL_DB_PASSWORD')

            pooled = true
            properties {
               // From http://grails.github.io/grails-doc/2.3.7/guide/conf.html
               // Documentation for Tomcat JDBC Pool
               // http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html#Common_Attributes
               // https://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/tomcat/jdbc/pool/PoolConfiguration.html
               jmxEnabled = true
               initialSize = 5
               maxActive = 50
               minIdle = 5
               maxIdle = 25
               maxWait = 10000
               maxAge = 10 * 60000
               timeBetweenEvictionRunsMillis = 5000
               minEvictableIdleTimeMillis = 60000
               validationQuery = "SELECT 1"
               validationQueryTimeout = 3
               validationInterval = 15000
               testOnBorrow = true
               testWhileIdle = true
               testOnReturn = false
               ignoreExceptionOnPreLoad = true
               // http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html#JDBC_interceptors
               jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
               defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED // safe default
               // controls for leaked connections
               abandonWhenPercentageFull = 100 // settings are active only when pool is full
               removeAbandonedTimeout = 120
               removeAbandoned = true
               // use JMX console to change this setting at runtime
               logAbandoned = false // causes stacktrace recording overhead, use only for debugging
            }
        }
    }
    local_prod {
       dataSource {

           dbCreate = "update"

           // ===========================================================
           // Config for EHRSERVER ======================================

           String host = System.getenv('EHRSERVER_MYSQL_DB_HOST')
           String port = System.getenv('EHRSERVER_MYSQL_DB_PORT')
           String dbName = System.getenv('EHRSERVER_APP_NAME')

           url = "jdbc:mysql://$host:$port/$dbName?useSSL=false" // ?useTimezone=true&serverTimezone=UTC

           username = System.getenv('EHRSERVER_MYSQL_DB_USERNAME')
           password = System.getenv('EHRSERVER_MYSQL_DB_PASSWORD')

           // ===========================================================

           pooled = true
           properties {
              // From http://grails.github.io/grails-doc/2.3.7/guide/conf.html
              // Documentation for Tomcat JDBC Pool
              // http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html#Common_Attributes
              // https://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/tomcat/jdbc/pool/PoolConfiguration.html
              jmxEnabled = true
              initialSize = 5
              maxActive = 50
              minIdle = 5
              maxIdle = 25
              maxWait = 10000
              maxAge = 10 * 60000
              timeBetweenEvictionRunsMillis = 5000
              minEvictableIdleTimeMillis = 60000
              validationQuery = "SELECT 1"
              validationQueryTimeout = 3
              validationInterval = 15000
              testOnBorrow = true
              testWhileIdle = true
              testOnReturn = false
              ignoreExceptionOnPreLoad = true
              // http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html#JDBC_interceptors
              jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
              defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED // safe default
              // controls for leaked connections
              abandonWhenPercentageFull = 100 // settings are active only when pool is full
              removeAbandonedTimeout = 120
              removeAbandoned = true
              // use JMX console to change this setting at runtime
              logAbandoned = false // causes stacktrace recording overhead, use only for debugging
           }
       }
   }
}
