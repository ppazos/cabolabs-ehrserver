#CaboLabs EHRServer

## Service-oriented clinical data repository for shared EHRs/EMRs/PHRs based on the [openEHR standard](http://openehr.org).

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
/rest/ehrList(String format:XML|JSON, int max, int offset)

### Get patients (this service will be obsolete when we move patient data to a specific demographic repo)
/rest/patientList(String format:XML|JSON, int max, int offset)

### Get EHR for a subject
/rest/ehrForSubject(String subjectUid, String format:XML|JSON)

### Get EHR by uid
/rest/ehrGet(String ehrUid, String format:XML|JSON)

### Get all compositions for a patient
/rest/findCompositions(String ehrId)

### Get query definitions
/rest/queryList(String format:XML|JSON, int max, int offset)

### Query execution by uid
/rest/query(String queryUid, String ehrId)

### Commits a set of clinical documents to the EHR
/rest/commit(String ehrUid, Collection<Composition> versions, String auditSystemId, String auditCommitter)

#### Rules for VERSIONs

* The XML should be valid against the XSD (see /xsd folder in project).
* version.uid should have this format: versioned_object_id::creating_system_id::version_tree_id.
* remember to use the archetype_id in the archetype_node_id attribute, of all the LOCATABLE elements
  inside version.data, when the node_id is 'at0000' (root node).
* for the encoding element in ENTRIES use 'Unicode' as the terminology and 'UTF-8' as the code_string,
  and be sure that the XML content is encoded with UTF-8.
* version.commit_audit.time_committed should be set by the client but will be overriden by the server
  to be compliant with this rule:
   * The time_committed attribute in both the Contribution and Version audits
     should reflect the time of committal to an EHR server, i.e. the time of
     availability to other users in the same system. It should therefore be
     computed on the server in implementations where the data are created
     in a separate client context.

#### Rules for CONTRIBUTIONs

* The parameters _auditSystemId_, _auditTimeCommitted_ and _auditCommitter_ are used to create the CONTRIBUTION for each commit.
  To be compliant with the openEHR specs, the client system should use that data to create the VERSION.commit_audit structure. So
  this rule is met:
   * "CONTRIBUTION.audit captures to the time, place and committer of the committal act; these three attributes (system_id,
     committer, time_committed of AUDIT_DETAILS) should be copied into the corresponding attributes of the commit_audit of each VERSION included in the CONTRIBUTION..."


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


### EHRServer workflows to be supported by clients

#### WF1. Commit

#### WF2. Query

#### WF3. Checkout and Commit (versioned clinical documents)


