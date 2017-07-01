#CaboLabs EHRServer

[![Build Status](https://travis-ci.org/ppazos/cabolabs-ehrserver.svg?branch=master)](https://travis-ci.org/ppazos/cabolabs-ehrserver)

## Clinical Data Management and Sharing Platform based on the [openEHR standard](http://openehr.org).

### [Latest documentation](http://cabolabs.com/en/projects)


### Main features:

* Service Oriented / REST API
* Open Source
* Supports XML and JSON formats
* Compliant with the openEHR standard
* Full audit access for traceability
* Versioned clinical records
* Data query creation interface
* Supports any structure of clinical document
* Multitenancy


### Install, Configure, Run locally:

1. curl -s get.sdkman.io | bash
2. source "$HOME/.sdkman/bin/sdkman-init.sh"
3. sdk install grails 2.5.5
4. set version by default: Y
5. grails -version
6. install [MySQL](https://dev.mysql.com/downloads/mysql/)
7. copy the default root password
8. cd /usr/local/mysql/bin
9. ./mysql -u root -p
10. enter default root password
11. ALTER USER 'root'@'localhost' IDENTIFIED BY 'NEW-ROOT-PASSWORD';
12. cd ehrserver/grails-app/conf
13. nano DataSource.groovy
14. change development password to NEW-ROOT-PASSWORD
15. save
16. mysql
17. CREATE TABLE ehrserver;
18. exit
19. cd ehrserver
20. grails run-app
21. go to localhost:8090/ehr
22. login with admin/admin/123456


### SaaS

Support us at [CloudEHRServer](https://cloudehrserver.com/)


### Use cases:

* Centralize clinical data storage from many apps into an integrated patient EHR
* Backend for end-user applications (Web, Mobile, Desktop)
* Data querying for visualization
* Data querying for analysis (Clinical desicion support, clinical dashboards, clinical management, research, etc.)
* [More](https://cloudehrserver.com/learn)


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
