# CaboLabs EHRServer

[![Build Status](https://travis-ci.org/ppazos/cabolabs-ehrserver.svg?branch=master)](https://travis-ci.org/ppazos/cabolabs-ehrserver)

## Clinical Data Management and Sharing Platform compliant with the [openEHR standard](http://openehr.org).

The EHRServer is a generic clinical data backend system, that helps cutting development time, increase data quality and enable
interoperability for any kind of clinical or health related information system or app.

Can be used as the main [clinical data repository](https://cloudehrserver.com/learn/use_case_health_and_wellness_apps) of your system or app, as a 
[shared repository/integrated patient EHR](https://cloudehrserver.com/learn/use_case_shared_health_recods) between many systems, as a 
[secondary/backup](https://cloudehrserver.com/learn/use_case_backup_and_query_database) repository, as a 
[wearable/monitoring device backend](https://cloudehrserver.com/learn/use_case_monitoring_and_wearables), as a 
standardized data source for [datawareousing](https://cloudehrserver.com/learn/use_case_analytics_and_datawarehousing), as a 
[fast prototyping platform](https://cloudehrserver.com/learn/use_case_fast_prototyping_poc),
and as a micro-service platform with many applications like 
[clinical decision support](https://cloudehrserver.com/learn/use_case_clinical_decision_support), clinical dashboards, data visualization, reporting, research, etc.

[More](https://cloudehrserver.com/learn)
[Let me know if you are using the EHRServer!](pablo.pazos@cabolabs.com)

### Resources

[Latest documentation](https://cabolabs.com/en/projects)
[CloudEHRServer: EHRServer SaaS](https://cloudehrserver.com/)

### Main features

* openEHR compliant clinical data repository
* Administrative Web Console
* Simple but powerful REST API
* Supports XML and JSON
* Full audit access for traceability
* Versioned clinical records
* Data query creation via Web Console (no programming!)
* Supports any structure of clinical document (following the openEHR standard information model)
* Vendor Neutral Archive
* Multitenancy

## Install, Configure, Run locally

### Dependencies

1. curl -s get.sdkman.io | bash
2. source "$HOME/.sdkman/bin/sdkman-init.sh"
3. sdk install grails 2.5.5
4. set version by default: Y
5. grails -version

### Database

1. install [MySQL](https://dev.mysql.com/downloads/mysql/)
2. copy the default root password
3. cd /usr/local/mysql/bin
4. ./mysql -u root -p
5. enter default root password
6. ALTER USER 'root'@'localhost' IDENTIFIED BY 'NEW-ROOT-PASSWORD';
8. CREATE TABLE ehrserver;
9. exit

### EHRServer configuration

1. cd ehrserver/grails-app/conf
2. nano DataSource.groovy
3. change development password to NEW-ROOT-PASSWORD
4. save

### EHRServer run (dev environment)

1. cd ehrserver
2. grails run-app
3. open localhost:8090/ehr
4. login with admin/admin/123456






### Based on Open Source Technologies

* [Grails Framework](http://grails.org)
* [Groovy](http://groovy.codehaus.org)
* [Java](http://docs.oracle.com/javase/specs)
* [MySQL](http://dev.mysql.com/downloads/mysql/)


### Out of scope

* EHR/EMR/PHR apps should be written separately as clients of the EHRServer, but use the provided data services to access and send data from/to the EHRServer.
* Data analysis, data aggregation, business intelligence and other data processing related applications should be written separately from the EHRServer, all those systems can get data from the EHRServer using the provided data services.


## REST API

Latest documentation: https://docs.google.com/viewerng/viewer?url=http://cabolabs.com/software_resources/EHRServer_v0.9.5.pdf

     
### EHRServer workflows supported by clients

#### WF1. Commit

A client applicaton can commit one or more versions of different Compositions in one transaction.

#### WF2. Query

Query execution by UID. Queries are created by admins. In the near future we'll add an API to 
create and manage Queries from client applications. We'll also explore adding support to AQL queries.

#### WF3. Checkout and Commit (versioned clinical documents)

On checkout, the client will receive a copy of an existing version of a Composition, with the 
current UID. The client can modify, and commit, and the same UID should be used. Then the EHRServer 
will update the version number in the new Version UID. Then Queries will get data only from the latest 
Version of the existing Compositions. All the Versions of a Composition are grouped in a VersionedComposition object.



### Staging server

Want to try EHRServer? Just create an account here and help us testing! https://ehrserver-cabolabs2.rhcloud.com/
