# CaboLabs EHRServer

[![Build Status](https://travis-ci.org/ppazos/cabolabs-ehrserver.svg?branch=master)](https://travis-ci.org/ppazos/cabolabs-ehrserver)


## Clinical Data Management and Sharing Platform compliant with the [openEHR standard](http://openehr.org).

The EHRServer is a generic clinical data backend system, that helps cutting development time, increase data quality and enable
interoperability for any kind of clinical or health related information system or app.

Can be used as the main [clinical data repository](https://cloudehrserver.com/learn/use_case_health_and_wellness_apps) of your system or app, as a 
[shared repository/integrated patient EHR](https://cloudehrserver.com/learn/use_case_shared_health_recods) between many systems, as a 
[secondary/backup](https://cloudehrserver.com/learn/use_case_backup_and_query_database) repository, as a 
[wearable/monitoring device backend](https://cloudehrserver.com/learn/use_case_monitoring_and_wearables), as a 
standardized data source for [datawarehousing](https://cloudehrserver.com/learn/use_case_analytics_and_datawarehousing), as a 
[fast prototyping platform](https://cloudehrserver.com/learn/use_case_fast_prototyping_poc),
and as a micro-service platform with many applications like 
[clinical decision support](https://cloudehrserver.com/learn/use_case_clinical_decision_support), clinical dashboards, data visualization, reporting, research, etc.

[Let me know if you are using the EHRServer!](https://twitter.com/ppazos)


### Resources

* [Latest documentation](https://cabolabs.com/en/projects)
* [REST API documentation](https://docs.google.com/viewerng/viewer?url=http://cabolabs.com/software_resources/EHRServer_v1.0.pdf)
* [CloudEHRServer: EHRServer SaaS](https://cloudehrserver.com/)
* [More guides](https://cloudehrserver.com/learn)


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


### Based on Open Source Technologies

* [Grails Framework](http://grails.org)
* [Groovy](http://groovy.codehaus.org)
* [Java](http://docs.oracle.com/javase/specs)
* [MySQL](http://dev.mysql.com/downloads/mysql/)


### Test it

Want to try EHRServer? Just create an account here and help us testing!

* [Staging 1](https://cabolabs-ehrserver.rhcloud.com/)
* [Staging 2](https://ehrserver-cabolabs2.rhcloud.com/)



## Install, Configure, Run locally


### Dependencies

1. curl -s get.sdkman.io | bash
2. source "$HOME/.sdkman/bin/sdkman-init.sh"
3. sdk install grails 2.5.6
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



## What EHRServer is not?

* EHR/EMR/PHR apps should be written separately as clients of the EHRServer, but use the provided data services to access and send data from/to the EHRServer.
* Data analysis, data aggregation, business intelligence and other data processing related applications should be written separately from the EHRServer, all those systems can get data from the EHRServer using the provided data services.

