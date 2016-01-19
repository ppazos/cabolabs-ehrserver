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
           dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''

//           logSql = true
           
           // ===========================================================
           // Config for OpenShift ======================================
           
           String host = "localhost"
           String port = 3306
           String dbName = "ehrserver"
           
           url = "jdbc:mysql://$host:$port/$dbName" // ?useTimezone=true&serverTimezone=UTC
           
           username = 'root'
           password = ''
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
           
           // ===========================================================
           // Config for OpenShift ======================================
           
           String host = "localhost"
           String port = 3306
           String dbName = "ehrserver"
           
           url = "jdbc:mysql://$host:$port/$dbName"
           
           username = 'root'
           password = ''
           */

            dbCreate = "update"
            
            // ===========================================================
            // Config for OpenShift ======================================
            
            String host = System.getenv('OPENSHIFT_MYSQL_DB_HOST')
            String port = System.getenv('OPENSHIFT_MYSQL_DB_PORT')
            String dbName = System.getenv('OPENSHIFT_APP_NAME')
            
            url = "jdbc:mysql://$host:$port/$dbName" // ?useTimezone=true&serverTimezone=UTC
            
            username = System.getenv('OPENSHIFT_MYSQL_DB_USERNAME')
            password = System.getenv('OPENSHIFT_MYSQL_DB_PASSWORD')
            
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
