#CaboLabs EHRServer

[![Build Status](https://travis-ci.org/ppazos/cabolabs-ehrserver.svg?branch=master)](https://travis-ci.org/ppazos/cabolabs-ehrserver)


## Service-oriented clinical data repository for shared EHRs/EMRs/PHRs based on the [openEHR standard](http://openehr.org).


### [New EHRServer v0.5 guide](http://cabolabs.com/software_resources/EHRServer_v0.5.pdf)



### Main Services:

* clinical data commit
* clinical data query (with aggregation and grouping capabilities)
* data synchronization (for backup and server clustering)


### Features:

* multiformat XML/JSON
* web services (REST and SOAP)
* versioned openEHR objects
* data indexing


### Main functionalities:

* Patient management
* EHR and contributions management
* Query building and testing
* Usage logs


## The project

### Intended audience

* Clinical Record Managers
* EHR/EMR/PHR System Admins
* Healthcare Informatics Trainers & Students


### Technologies

* [Grails Framework](http://grails.org)
* [Groovy](http://groovy.codehaus.org)
* [Java](http://docs.oracle.com/javase/specs)


### Out of scope

* EHR/EMR/PHR apps should be written separately as clients of the EHRServer, but use the provided data services to access and send data from/to the EHRServer.
* Data analysis, data aggregation, business intelligence and other data processing related applications should be written separately from the EHRServer, all those systems can get data from the EHRServer using the provided data services.


## REST API
### Get EHRs
/rest/ehrs(String format:xml|json, int max, int offset)

### Get patients (this service will be obsolete when we move patient data to a specific demographic repo)
/rest/patients(String format:xml|json, int max, int offset)

### Get one patient by uid
/rest/patients/{uid}(String format:xml|json, int max, int offset)

### Get EHR for a subject (patient)
/rest/ehrs/subjectUid/{subjectUid}(String format:xml|json)

### Get EHR by uid
/rest/ehrs/ehrUid/{ehrUid}(String format:xml|json)

### Get all compositions for a patient
/rest/findCompositions(String ehrId)

### Get query definitions
/rest/queries(String format:xml|json, int max, int offset)

### Get one query by it's uid
/rest/queries/{queryUid}

### Query execution by uid
/rest/queries/{queryUid}/execute(String organizationUid, String ehrId<optional>)

### Commits a set of clinical documents to the EHR
/rest/commit(String ehrUid, Version[] versions, String auditSystemId, String auditCommitter)

Versions should be committed in XML, following this XSD: https://github.com/ppazos/cabolabs-ehrserver/blob/master/xsd/Version.xsd


#### Rules for VERSIONs (commit, checkout and version control)

* The XML should be valid against the XSD (see /xsd folder in project).
* version.uid should have this format: versioned_object_id::creating_system_id::version_tree_id.
   * The 3 fields of the version.uid attribute should be set by the client.
   * When committing a new Version, the client should assign: a_generated_uid::client_system_id::1
   * The first commit of a Version will generate on the EHRServer a new VersionedObject that contains that Version.
   * The versionedObject.uid will be equal to the first part of the version.uid
   * When committing a change to an existing VersionedObject, the version.uid should be equal to the uid of the version that was checked out and to which the changes were apply.
   * When receiving a new Version for an existing VersionedObject, the EHRServer will update the versio_tree_id, generating uid::system::1, uid::system::2, uid::system::3, ... for each version.uid of the same VersionedObject.
* remember to use the archetype_id in the archetype_node_id attribute, of all the LOCATABLE elements
  inside version.data, when the node_id is 'at0000' (root node).
* for the encoding element in ENTRIES use 'Unicode' as the terminology and 'UTF-8' as the code_string,
  and be sure that the XML content is encoded with UTF-8.
* version.commit_audit.time_committed that is set by client apps will be overriden by the server
  to be compliant with this rule from the openEHR specs:
   * The time_committed attribute in both the Contribution and Version audits **should reflect the time
     of committal to an EHR server, i.e. the time of availability to other users** in the same system.
     It should therefore be computed on the server in implementations where the data are created
     in a separate client context.

#### Rules for CONTRIBUTIONs

* The parameters _auditSystemId_, _auditTimeCommitted_ and _auditCommitter_ are used to create the CONTRIBUTION for each commit.
  To be compliant with the openEHR specs, the client system should use that data to create the VERSION.commit_audit structure. So
  this rule is met:
   * "CONTRIBUTION.audit captures to the time, place and committer of the committal act; these three attributes (system_id,
     committer, time_committed of AUDIT_DETAILS) should be copied into the corresponding attributes of the commit_audit of each VERSION included in the CONTRIBUTION..."

     
     
### EHRServer workflows supported by clients

#### WF1. Commit

A client applicaton can commit one or more versions of different Compositions in one transaction.

#### WF2. Query

Query execution by UID. Queries are created by admins. In the near future we'll add an API to create and manage Queries from client applications. We'll also explore adding support to AQL queries.

#### WF3. Checkout and Commit (versioned clinical documents)

On checkout, the client will receive a copy of an existing version of a Composition, with the current UID. The client can modify, and commit, and the same UID should be used. Then the EHRServer will update the version number in the new Version UID. Then Queries will get data only from the latest Version of the existing Compositions. All the Versions of a Composition are grouped in a VersionedComposition object.



### Milestones and deliverables

#### M1. Basic services in XML and JSON and internal operations

* list and show of EHRs, Compositions, Queries and Contributions
* commit reception (just openEHR XML for now)
* data indexing operation
* openEHR archetype definition indexing


#### M2. GUI to manage internal data (list, show, create, edit, delete)

* EHR management
* Directory management
* Composition management
* Query management
* Contribution management
* Patient management (should be externalized to a demographic server)
* User management


#### M3. Advanced features (each one could be a milestone itself)

* Support rules for directory management (copying and moving compositions between folders)
* Composition versioning
* Persistent composition support
* Querying versioned objects
* Data syncrhonization between servers
* Support for AQL/EQL output (openEHR archetype-based query language)
