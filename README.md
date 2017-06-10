#CaboLabs EHRServer

[![Build Status](https://travis-ci.org/ppazos/cabolabs-ehrserver.svg?branch=master)](https://travis-ci.org/ppazos/cabolabs-ehrserver)


## Service-oriented clinical data repository for shared EHRs/EMRs/PHRs based on the [openEHR standard](http://openehr.org).

### [Latest documentation](http://cabolabs.com/en/projects)
### [EHRServer SaaS](https://cloudehrserver.com/)

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


### Use cases:

* Centralize clinical data storage from many apps into an integrated patient EHR
* Backend for end-user applications (Web, Mobile, Desktop)
* Data querying for visualization
* Data querying for analysis (Clinical desicion support, clinical dashboards, clinical management, research, etc.)


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
