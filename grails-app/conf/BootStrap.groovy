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

import com.cabolabs.ehrserver.openehr.common.generic.PatientProxy
import grails.util.Holders

import com.cabolabs.security.RequestMap
import com.cabolabs.security.User
import com.cabolabs.security.Role
import com.cabolabs.security.UserRole
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import com.cabolabs.openehr.opt.manager.OptManager // load opts
import com.cabolabs.ehrserver.api.structures.PaginatedResults
import com.cabolabs.ehrserver.account.*
import grails.converters.*
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.web.converters.marshaller.NameAwareMarshaller
import com.cabolabs.ehrserver.ResourceService
import com.cabolabs.ehrserver.notification.*
import grails.util.Environment
import com.cabolabs.ehrserver.conf.ConfigurationItem
import com.cabolabs.ehrserver.ehr.*

class BootStrap {

   private static String PS = System.getProperty("file.separator")
   
   def mailService
   def grailsApplication
   def resourceService
   def configurationService
   
   
   def defaultConfigurationItems()
   {
      def conf = [
         new ConfigurationItem(key:'ehrserver.instance.id', value:'9cbabb12-c4ae-421c-868c-a6898520b983', type:'string', blank:false, description:'EHRServer running instance ID'),
         new ConfigurationItem(key:'ehrserver.console.lists.max_items', value:'20', type:'number', blank:false, description:'Max number of items on the lists views for the Web Console'),
         new ConfigurationItem(key:'ehrserver.security.passwords.min_length', value:'6', type:'number', blank:false, description:'Minimum password size used on password reset'),
         new ConfigurationItem(key:'ehrserver.security.password_token.expiration', value:'1440', type:'number', blank:false, description:'Number of minutes after the password reset token expires')
      ]
      
      conf.each {
         if (ConfigurationItem.countByKey(it.key) == 0)
         {
            if (!it.save(flush: true))
            {
               log.warn(it.errors.toString()) 
            }
         }
      }
      
      configurationService.refresh()
   }
   
   def extendClasses()
   {
      // Used by query builder, all return String
      String.metaClass.asSQLValue = { operand ->
        //if (operand == 'contains') return "'%"+ delegate +"%'" // Contains is translated to LIKE, we need the %
        if (['contains', 'contains_like'].contains(operand)) return "'%"+ delegate +"%'" // Contains is translated to LIKE, we need the %
        return "'"+ delegate +"'"
      }
      Double.metaClass.asSQLValue = { operand ->
        return delegate.toString()
      }
      Integer.metaClass.asSQLValue = { operand ->
        return delegate.toString()
      }
      Long.metaClass.asSQLValue = { operand ->
        return delegate.toString()
      }
      Date.metaClass.asSQLValue = { operand ->
        def formatterDateDB = new java.text.SimpleDateFormat( Holders.config.app.l10n.db_datetime_format )
        return "'"+ formatterDateDB.format( delegate ) +"'" 
      }
      Boolean.metaClass.asSQLValue = { operand ->
        return delegate.toString()
      }
     
      String.metaClass.md5 = {
         return java.security.MessageDigest.getInstance("MD5").digest(delegate.bytes).encodeHex().toString()
      }
      
      // call String.randomNumeric(5)
      String.metaClass.static.randomNumeric = { digits ->
        def alphabet = ['0','1','2','3','4','5','6','7','8','9']
        new Random().with {
          (1..digits).collect { alphabet[ nextInt( alphabet.size() ) ] }.join()
        }
      }
      
      String.metaClass.static.random = { n ->
        def alphabet = 'a'..'z'
        new Random().with {
          (1..n).collect { alphabet[ nextInt( alphabet.size() ) ] }.join()
        }
      }

      String.metaClass.static.uuid = { ->
         java.util.UUID.randomUUID() as String
      }
      
      // adds trailing path separator to a file path if it doesnt have it
      String.metaClass.withTrailSeparator = {
         def PS = System.getProperty("file.separator")
         if (!delegate.endsWith(PS)) delegate += PS
         return delegate
      }
   }
   
   def repoChecks()
   {
      // file system checks
      def commits_repo  = new File(Holders.config.app.commit_logs)
      def versions_repo = new File(Holders.config.app.version_repo)
      def opt_repo      = new File(Holders.config.app.opt_repo)
      
      if (!commits_repo.exists())
      {
         throw new FileNotFoundException("File ${commits_repo.absolutePath} doesn't exists")
      }
      if (!versions_repo.exists())
      {
         throw new FileNotFoundException("File ${versions_repo.absolutePath} doesn't exists")
      }
      if (!opt_repo.exists())
      {
         throw new FileNotFoundException("File ${opt_repo.absolutePath} doesn't exists")
      }
      // /file system checks
   }
   
   def registerMarshallers()
   {
     // This format is used by jstree to display the template in the web console
     JSON.registerObjectMarshaller(FolderTemplate) { ftpl ->
     
        def traverse // needs to be declared before defined to work recursively
        
        traverse = { item ->
           def res = [text: item.name, children: []]
           item.folders.each {
              res.children << traverse(it)
           }
           return res
        }
     
        def res = []
        ftpl.folders.each {
          res << traverse(it)
        }
     
        return res
     }
     
     JSON.registerObjectMarshaller(ArchetypeIndexItem) { aii ->
        return [archetypeId:    aii.archetypeId,
                path:           aii.path,
                rmTypeName:     aii.rmTypeName,
                name:           aii.name, // Map
                terminologyRef: aii.terminologyRef,
                parentOpts:     aii.parentOpts // List
               ]
     }
   
     JSON.registerObjectMarshaller(OperationalTemplateIndex) { opt ->
        return [templateId:  opt.templateId,
                concept:     opt.concept,
                language:    opt.language,
                uid:         opt.uid,
                externalUid: opt.externalUid,
                archetypeId: opt.archetypeId,
                archetypeConcept: opt.archetypeConcept,
                organizationUid:  opt.organizationUid,
                setID:       opt.setId,
                versionNumber: opt.versionNumber]
     }
     
     XML.registerObjectMarshaller(OperationalTemplateIndex) { opt, xml ->
        xml.build {
          templateId(opt.templateId)
          concept(opt.concept)
          language(opt.language)
          uid(opt.uid)
          externalUid(opt.externalUid)
          archetypeId(opt.archetypeId)
          archetypeConcept(opt.archetypeConcept)
          organizationUid(opt.organizationUid)
          setId(opt.setId)
          versionNumber(opt.versionNumber)
        }
     }
     


     // Marshallers
     JSON.registerObjectMarshaller(Date) {
        //println "JSON DATE MARSHAL"
        return it?.format(Holders.config.app.l10n.db_datetime_format)
     }
     
     // These for XML dont seem to work...
     XML.registerObjectMarshaller(Date) {
        //println "XML DATE MARSHAL"
        return it?.format(Holders.config.app.l10n.db_datetime_format)
     }
     
     JSON.registerObjectMarshaller(CompositionIndex) { composition ->
        return [uid: composition.uid,
                category: composition.category,
                startTime: composition.startTime,
                subjectId: composition.subjectId,
                ehrUid: composition.ehrUid,
                templateId: composition.templateId,
                archetypeId: composition.archetypeId,
                lastVersion: composition.lastVersion,
                organizationUid: composition.organizationUid,
                parent: composition.getParent().uid
               ]
     }
     
     XML.registerObjectMarshaller(CompositionIndex) { composition, xml ->
        xml.build {
          uid(composition.uid)
          category(composition.category)
          startTime(composition.startTime)
          subjectId(composition.subjectId)
          ehrUid(composition.ehrUid)
          templateId(composition.templateId)
          archetypeId(composition.archetypeId)
          lastVersion(composition.lastVersion)
          organizationUid(composition.organizationUid)
          parent(composition.getParent().uid)
        }
     }
     
     
     JSON.registerObjectMarshaller(DoctorProxy) { doctor ->
        return [namespace: doctor.namespace,
                type: doctor.type,
                value: doctor.value,
                name: doctor.name
               ]
     }
     
     JSON.registerObjectMarshaller(AuditDetails) { audit ->
        def a = [timeCommitted: audit.timeCommitted,
                 committer: audit.committer, // DoctorProxy
                 systemId: audit.systemId
                ]
        // audit for contributions have changeType null, so we avoid to add it here if it is null
        if (audit.changeType) a << [changeType: audit.changeType]
        return a
     }
     
     JSON.registerObjectMarshaller(Contribution) { contribution ->
        return [uid: contribution.uid,
                organizationUid: contribution.organizationUid,
                ehrUid: contribution.ehr.uid,
                versions: contribution.versions.uid, // list of uids
                audit: contribution.audit // AuditDetails
               ]
     }
     
     XML.registerObjectMarshaller(DoctorProxy) { doctor, xml ->
        xml.build {
          namespace(doctor.namespace)
          type(doctor.type)
          value(doctor.value)
          name(doctor.name)
        }
     }
     
     XML.registerObjectMarshaller(AuditDetails) { audit, xml ->
        xml.build {
          timeCommitted(audit.timeCommitted)
          committer(audit.committer) // DoctorProxy
          systemId(audit.systemId)
          if (audit.changeType) changeType(audit.changeType)
        }
     }
     
     XML.registerObjectMarshaller(Contribution) { contribution, xml ->
        xml.build {
          uid(contribution.uid)
          organizationUid(contribution.organizationUid)
          ehrUid(contribution.ehr.uid)
          /*
           * <versions>
           *  <string>8b68a18c-bcb1... </string>
           * </versions>
           */
          //versions(contribution.versions.uid) // list of uids
          /* doesnt work, see below!
          versions {
             contribution.versions.uid.each { _vuid ->
                uid(_vuid)
             }
          }
          */
          audit(contribution.audit) // AuditDetails
        }
        
        // works!
        // https://jwicz.wordpress.com/2011/07/11/grails-custom-xml-marshaller/
        // http://docs.grails.org/2.5.3/api/grails/converters/XML.html
         /*
          * <versions>
          *  <uid>8b68a18c-bcb1... </uid>
          * </versions>
          */
        xml.startNode 'versions'
        contribution.versions.uid.each { _vuid ->
           xml.startNode 'uid'
           xml.chars _vuid
           xml.end()
        }
        xml.end()
     }
     
     
     JSON.registerObjectMarshaller(Organization) { o ->
        return [uid: o.uid,
                name: o.name,
                number: o.number
               ]
     }
     
     XML.registerObjectMarshaller(Organization) { o, xml ->
        xml.build {
          uid(o.uid)
          name(o.name)
          number(o.number)
        }
     }
     
     JSON.registerObjectMarshaller(User) { u ->
        return [username: u.username,
                email: u.email,
                organizations: u.organizations
               ]
     }
     
     XML.registerObjectMarshaller(User) { u, xml ->
        xml.build {
          username(u.username)
          email(u.email)
          xml.startNode 'organizations'
          
             xml.convertAnother u.organizations
   
          xml.end()
        }
     }
     
     
     JSON.registerObjectMarshaller(Query) { q ->
        def j = [uid: q.uid,
                 name: q.name,
                 format: q.format,
                 type: q.type,
                 author: q.author
                ]
        
        if (q.type == 'composition')
        {
           j << [criteriaLogic: q.criteriaLogic]
           j << [templateId:    q.templateId]
           j << [criteria:      q.where.collect { [archetypeId: it.archetypeId, path: it.path, conditions: it.getCriteriaMap()] }]
        }
        else
        {
           j << [group:         q.group] // Group is only for datavalue
           j << [projections:   q.select.collect { [archetypeId: it.archetypeId, path: it.path, rmTypeName: it.rmTypeName] }]
        }
        
        return j
     }
     
     XML.registerObjectMarshaller(Query) { q, xml ->
        xml.build {
          uid(q.uid)
          name(q.name)
          format(q.format)
          type(q.type)
          author(q.author)
        }
        
        if (q.type == 'composition')
        {
           xml.startNode 'criteriaLogic'
              xml.chars (q.criteriaLogic ?: '')
           xml.end()
           xml.startNode 'templateId'
              xml.chars (q.templateId ?: '') // fails if null!
           xml.end()
           
           def criteriaMap
           def _value
           //q.where.each { criteria -> // with this the criteria clases are marshalled twice, it seems the each is returning the criteria instead of just processing the xml format creation.
           for (criteria in q.where) // works ok, so we need to avoid .each
           {
              criteriaMap = criteria.getCriteriaMap() // [attr: [operand: value]] value can be a list
              
              xml.startNode 'criteria'
                 xml.startNode 'archetypeId'
                    xml.chars criteria.archetypeId
                 xml.end()
                 xml.startNode 'path'
                    xml.chars criteria.path
                 xml.end()
                 xml.startNode 'conditions'
 
                    criteriaMap.each { attr, cond ->
                    
                       _value = cond.find{true}.value // can be a list, string, boolean, ...
                       
                       xml.startNode "$attr"
                          xml.startNode 'operand'
                             xml.chars cond.find{true}.key
                          xml.end()
                          
                          if (_value instanceof List)
                          {
                             xml.startNode 'list'
                                _value.each { val ->
                                   
                                   if (val instanceof Date)
                                   {
                                      // FIXME: should use the XML date marshaller
                                      xml.startNode 'item'
                                         xml.chars val.format(Holders.config.app.l10n.ext_datetime_utcformat_nof, TimeZone.getTimeZone("UTC"))
                                      xml.end()
                                   }
                                   else
                                   {
                                      xml.startNode 'item'
                                         xml.chars val.toString() // chars fails if type is Double or other non string
                                      xml.end()
                                   }
                                }
                             xml.end()
                          }
                          else
                          {
                             xml.startNode 'value'
                                xml.chars _value.toString() // chars fails if type is Double or other non string
                             xml.end()
                          }
                       xml.end()
                    }
                 xml.end()
              xml.end()
           }
        }
        else
        {
           xml.startNode 'group'
              xml.chars q.group
           xml.end()
           
           q.select.each { proj ->
              xml.startNode 'projection'
                xml.startNode 'archetypeId'
                  xml.chars proj.archetypeId
                xml.end()
                xml.startNode 'path'
                  xml.chars proj.path
                xml.end()
              xml.end()
           }
        }
     }
     

     JSON.registerObjectMarshaller(Ehr) { ehr ->
        return [uid: ehr.uid,
                dateCreated: ehr.dateCreated,
                subjectUid: ehr.subject.value,
                systemId: ehr.systemId,
                organizationUid: ehr.organizationUid
               ]
     }
     
     XML.registerObjectMarshaller(Ehr) { ehr, xml ->
        xml.build {
          uid(ehr.uid)
          dateCreated(ehr.dateCreated)
          subjectUid(ehr.subject.value)
          systemId(ehr.systemId)
          organizationUid(ehr.organizationUid)
        }
     }
     
     /*
     XML.registerObjectMarshaller(new NameAwareMarshaller() {
        @Override
        public boolean supports(java.lang.Object object) {
           return (object instanceof PaginatedResults)
        }

        @Override
        String getElementName(java.lang.Object o) {
           'result'
        }
     })
     */
     
     JSON.registerObjectMarshaller(PaginatedResults) { pres ->
        
        pres.update() // updates and checks pagination values
        
        def res = [:]
        
        if (pres.list)
           res["${pres.listName}"] = (pres.list ?: []) // prevents null on the json
        else
           res["${pres.listName}"] = (pres.map ?: [:])
        
        res.pagination = [
           'max': pres.max,
           'offset': pres.offset,
           nextOffset: pres.nextOffset, // TODO: verificar que si la cantidad actual es menor que max, el nextoffset debe ser igual al offset
           prevOffset: pres.prevOffset
        ]
        
        if (pres.timing != null) res.timing = pres.timing.toString() + ' ms'
           
        return res
     }
     
     XML.registerObjectMarshaller(PaginatedResults) { pres, xml ->
        
        pres.update() // updates and checks pagination values
        
        // Our list marshaller to customize the name
        xml.startNode pres.listName
           
           if (pres.list)
              xml.convertAnother (pres.list ?: []) // this works, generates "ehr" nodes
           else
              xml.convertAnother (pres.map ?: [:])
           
           /* doesnt generate the patient root, trying with ^
           pres.list.each { item ->
              xml.convertAnother item // marshaller fot the item type should be declared
           }
           */

        xml.end()
        
        xml.startNode 'pagination'
           xml.startNode 'max'
           xml.chars pres.max.toString() // integer fails for .chars
           xml.end()
           xml.startNode 'offset'
           xml.chars pres.offset.toString()
           xml.end()
           xml.startNode 'nextOffset'
           xml.chars pres.nextOffset.toString()
           xml.end()
           xml.startNode 'prevOffset'
           xml.chars pres.prevOffset.toString()
           xml.end()
        xml.end()
        
        // TODO: timing
     }
   }
   
   def registerRequestMap()
   {
      // Permissions
      if (RequestMap.count() == 0)
      {
        for (String url in [
         '/', // redirects to login, see UrlMappings
         '/error', '/index', '/index.gsp', '/**/favicon.ico', '/shutdown',
         '/assets/**', '/**/js/**', '/**/css/**', '/**/images/**', '/**/fonts/**',
         '/login', '/login.*', '/login/*',
         '/logout', '/logout.*', '/logout/*',
         '/user/register', '/user/resetPassword', '/user/forgotPassword', '/user/registerOk',
         '/simpleCaptcha/**',
         '/j_spring_security_logout',
         '/api/**', // REST security is handled by stateless security plugin
         '/ehr/showCompositionUI', // will be added as a rest service via url mapping
         '/user/profile',
         '/mgt/**' // management api
        ])
        {
           // permitAll is an expression, not a role, he correspondent role is IS_AUTHENTICATED_ANONYMOUSLY, see table 2:
           // https://github.com/grails-plugins/grails-spring-security-core/blob/master/plugin/src/docs/requestMappings/expressions.adoc
           new RequestMap(url: url, configAttribute: 'IS_AUTHENTICATED_ANONYMOUSLY').save()
        }
       
        // sections        
        new RequestMap(url: '/notification/**',              configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        
        new RequestMap(url: '/app/**',                       configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
       
        new RequestMap(url: '/ehr/**',                       configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/versionedComposition/**',      configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/contribution/**',              configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/folder/**',                    configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/folderTemplate/**',            configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/query/**',                     configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/operationalTemplateIndexItem/**', configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/archetypeIndexItem/**',        configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/compositionIndex/**',          configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/operationalTemplate/**',       configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        
        new RequestMap(url: '/dataValueIndex/**',            configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/requestMap/**',                configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/account/**',                  configAttribute: 'ROLE_ADMIN').save()
        
        // the rest of the operations should be open and security is checked inside the action
        new RequestMap(url: '/user/index',                   configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/user/show/**',                 configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/user/edit/**',                 configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/user/update/**',               configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/user/create',                  configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/user/save',                    configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/user/delete',                  configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/user/resetPasswordRequest/**', configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        
        new RequestMap(url: '/role/**',                      configAttribute: 'ROLE_ADMIN').save()
        new RequestMap(url: '/organization/**',              configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        
        new RequestMap(url: '/messaging/**',                 configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        
        // share/unshare queries and opts between orgs
        new RequestMap(url: '/resource/**',                  configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        
        new RequestMap(url: '/stats/**',                     configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/logs/**',                      configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/ehrQuery/**',                  configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()

        new RequestMap(url: '/j_spring_security_switch_user', configAttribute: 'ROLE_SWITCH_USER,isFullyAuthenticated()').save()
        
        new RequestMap(url: '/rest/queryCompositions',       configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
        new RequestMap(url: '/rest/queryData',               configAttribute: 'ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER').save()
      }
   }
   
   def defaultAccount(User contact, List organizations)
   {
      def account = new Account(contact: contact, enabled: true)
      organizations.each { org ->
         account.addToOrganizations(org)
      }
      account.save(failOnError:true, flush:true)
   }
   
   def defaultOrganizations()
   {
      def organizations = []
      if (Organization.count() == 0)
      {
         println "Creating default organization"
        
         // Default organization
         organizations << new Organization(name: 'EHRServer', number: '123456', uid:'e9d13294-bce7-44e7-9635-8e906da0c914')
        
         /* the account will save the orgs
         organizations.each {
            it.save(failOnError:true, flush:true)
         }
         */
      }
      else organizations = Organization.list()
      
      return organizations
   }
   
   def createRoles()
   {
      if (Role.count() == 0 )
      {
         // Create roles
         def adminRole          = new Role(authority: Role.AD).save(failOnError: true, flush: true)
         def orgManagerRole     = new Role(authority: Role.OM).save(failOnError: true, flush: true)
         def accountManagerRole = new Role(authority: Role.AM).save(failOnError: true, flush: true)
         def userRole           = new Role(authority: Role.US).save(failOnError: true, flush: true)
      }
   }
   
   def generateTemplateIndexes()
   {
      // for the default organization
      def org = Organization.get(1)
      
      // Always regenerate indexes in deploy
      if (OperationalTemplateIndex.count() == 0)
      {
         println "Indexing Operational Templates"
        
         def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
         
         ti.setupBaseOpts( org )
         ti.indexAll( org ) // also shares with all existing orgs if there are no shares
      }

      // OPT loading
      // This is done to set the OPT repo internally, further uses will not pass the repo path.
      def optMan = OptManager.getInstance( Holders.config.app.opt_repo.withTrailSeparator() )
      
      // OPTs are loaded into the manager in the login, after we know the org of the current user
   }
   
   def sampleFolderTemplates()
   {
      if (FolderTemplate.count() == 0)
      {
         println "Creating sample folder templates"
         def org = Organization.get(1)
         def ftpls = [
            new FolderTemplate(name:'openEHR', description:'Structure suggested on the openEHR specs', organizationUid: org.uid, folders: [
               new FolderTemplateItem(name:'subject'),
               new FolderTemplateItem(name:'persistent'),
               new FolderTemplateItem(name:'event'),
               new FolderTemplateItem(name:'episode X')
            ])
         ]
         
         ftpls.each {
            it.save(failOnError: true)
         }
      }
   }
   
   def init = { servletContext ->
   
      def working_folder = new File('.')
      log.info ("Working folder: "+ working_folder.absolutePath)
      
      //****** SETUP *******
      
      // Define server timezone
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
      
      repoChecks()
      extendClasses()
      registerMarshallers()
      
      if (Environment.current != Environment.TEST)
      {
         defaultConfigurationItems()
      }
      
      // --------------------------------------------------------------------
     
      //****** SECURITY *******
     
      // Register custom auth filter
      // ref: https://objectpartners.com/2013/07/11/custom-authentication-with-the-grails-spring-security-core-plugin/
      // See 'authFilter' in grails-app/conf/spring/resources.groovy
      // ref: http://grails-plugins.github.io/grails-spring-security-core/guide/filters.html
      SpringSecurityUtils.clientRegisterFilter('authFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)

      registerRequestMap()
      // --------------------------------------------------------------------
     
     
      // Do not create data if testing, tests will create their own data.
      if (Environment.current != Environment.TEST)
      {
         // doesnt save the orgs!
         def organizations = defaultOrganizations()
         

         createRoles()
        
        
         def accManUser // used below to create the Account
         def adminUser, orgManUser, user
         if (User.count() == 0)
         {
           println "Creating default users"
           
           adminUser = new User(username: 'admin', email: 'pablo.pazos@cabolabs.com', password: 'admin', enabled: true)
           adminUser.save(failOnError: true,  flush: true)
           
           accManUser = new User(username: 'accman', email: 'pablo.swp+accman@gmail.com', password: 'accman', enabled: true)
           accManUser.save(failOnError: true,  flush: true)
           
           orgManUser = new User(username: 'orgman', email: 'pablo.swp+orgman@gmail.com', password: 'orgman', enabled: true)
           orgManUser.save(failOnError: true,  flush: true)
           
           user = new User(username: 'user', email: 'pablo.swp+user@gmail.com', password: 'user', enabled: true)
           user.save(failOnError: true,  flush: true)
         }
         else
         {
            accManUser = User.allForRole(Role.AM).get(0)
            assert accManUser != null
         }
         
         
         // saves the organizations!
         def account = defaultAccount(accManUser, organizations)
         
        
         
         // Assign Roles for Users under Org 0, needs the org to be saved
         if (UserRole.count() == 0)
         {
            // Associate roles
            UserRole.create( adminUser,  (Role.findByAuthority(Role.AD)), organizations[0], true )
            UserRole.create( accManUser, (Role.findByAuthority(Role.AM)), organizations[0], true )
            UserRole.create( orgManUser, (Role.findByAuthority(Role.OM)), organizations[0], true )
            UserRole.create( user,       (Role.findByAuthority(Role.US)), organizations[0], true )
         }
         
        
        
                  
         // test, needs orgs to be saved
         sampleFolderTemplates()
         
         
         generateTemplateIndexes()
         
         
         // Sample EHRs for testing purposes
         if (Ehr.count() == 0)
         {
            println "Creating sample EHRs"
            def ehr_subject_uids = [
              '11111111-1111-1111-1111-111111111111',
              '22222222-1111-1111-1111-111111111111',
              '33333333-1111-1111-1111-111111111111',
              '44444444-1111-1111-1111-111111111111',
              '55555555-1111-1111-1111-111111111111'
            ]
           
            def ehr
            def c = Organization.count()
           
            ehr_subject_uids.eachWithIndex { uid, i ->
              ehr = new Ehr(
                 uid: uid, // the ehr id is the same as the patient just to simplify testing
                 subject: new PatientProxy(
                    value: uid
                 ),
                 organizationUid: Organization.get(i % c + 1).uid
              )
            
              if (!ehr.save()) println ehr.errors
            }
         }
         
         
         // Create plans
         def p1
         if (Plan.count() == 0)
         {
            // Create plans
            p1 = new Plan(
              name:                      "Research / Training",
              max_organizations:         1,
              max_opts_per_organization: 5,
              repo_total_size:           2.5*1024*1024, // 2.5 GB in KB
              period:                    Plan.periods.MONTHLY
            )
           
            p1.save(failOnError: true)
         }
         else
         {
            p1 = Plan.get(1)
         }
        
        
         // Associate free plans by default
         def accounts = Account.list()
         accounts.each { acct ->
            if (!PlanAssociation.findByAccount(acct))
            {
               p1.associate(acct, new Date())
            }
         }
         
         println 'User.allForAccount '+ User.allForAccount(accounts[0])
         println 'user.account '+ User.get(1).account
         
      } // not TEST ENV
     
     
      // ============================================================
      // migration for latest changes
      /*
      def versionsss = Version.list()
      def version_file, commit_file
      versionsss.each {
         if (!it.fileUid)
         {
            it.fileUid = java.util.UUID.randomUUID() as String
            if (!it.save())
            {
               println it.errors
            }
            else
            {
               // update the version file names
               version_file = new File(Holders.config.app.version_repo + it.uid.replaceAll('::', '_') +'.xml')
               if (version_file.exists())
               {
                  version_file.renameTo( Holders.config.app.version_repo + it.fileUid +'.xml' )
               }
               else
                  println "file doesnt exists "+ version_file.path
            }
         }
      }
      
      
      def commitsss = Commit.list()
      commitsss.each {
         if (!it.fileUid)
         {
            it.fileUid = java.util.UUID.randomUUID() as String
            if (!it.save())
            {
               println it.errors
            }
            else
            {
               commit_file = new File(Holders.config.app.commit_logs + it.id.toString() +'.xml')
               if (commit_file.exists())
               {
                  commit_file.renameTo( Holders.config.app.commit_logs + it.fileUid +'.xml' )
               }
               else
                  println "file doesnt exists "+ commit_file.path
            }
         }
      }
      */
      /* needed for 0.9 - 0.9.5
      // Fill rm_type_name for data_value_index for old indexes
      def aii
      com.cabolabs.ehrserver.ehr.clinical_documents.data.DataValueIndex.list().each { dvi ->
         println dvi.archetypeId+" "+dvi.archetypePath
         aii = ArchetypeIndexItem.findByArchetypeIdAndPath(dvi.archetypeId, dvi.archetypePath)
         println "aii " + aii
         if (aii)
         {
         dvi.rmTypeName = aii.rmTypeName
         dvi.save()
         }
         else
         {
            println "not aii!!!"
         }
      }
      */
     
      /*
      // Test notifications
      def notifs = [
         new Notification(name:'notif 1', language:'en', text:'Look at me!'),
         new Notification(name:'notif 2', language:'en', text:'Look at me!', forSection:'ehr'),
         new Notification(name:'notif 3', language:'en', text:'Look at me!', forOrganization:Organization.get(1).uid),
         new Notification(name:'notif 4', language:'en', text:'Look at me!', forUser:1),
         new Notification(name:'notif 5', language:'en', text:'Look at me!', forSection:'query', forOrganization:Organization.get(1).uid),
         new Notification(name:'notif 6', language:'en', text:'Look at me!', forSection:'query', forOrganization:Organization.get(1).uid, forUser:1),
        
         new Notification(name:'notif 7', language:'es', text:'mirame!'),
         new Notification(name:'notif 8', language:'es', text:'mirame', forSection:'ehr'),
         new Notification(name:'notif 9', language:'es', text:'mirame!', forOrganization:Organization.get(1).uid),
         new Notification(name:'notif 10', language:'es', text:'mirame!', forUser:1)
      ]
      
      def statuses = []
      notifs.each { notif ->
         if (!notif.forUser)
         {
            User.list().each { user ->
               statuses << new NotificationStatus(user:user, notification:notif)
            }
         }
         else
         {
            statuses << new NotificationStatus(user:User.get(notif.forUser), notification:notif)
         }
        
         notif.save(failOnError: true)
      }
     
      statuses.each { status ->
         status.save(failOnError: true)
      }
      */
      
      /*
      com.cabolabs.ehrserver.ehr.clinical_documents.data.DataValueIndex.list().each {
         it.delete()
      }
      CompositionIndex.list().each {
         it.dataIndexed = false
         it.save()
      }
      */
      
      /*
      QueryShare.list().each {
         it.delete()
      }
      Query.list().each {
         it.delete()
      }
      DataGet.list().each {
         it.delete()
      }
      DataCriteria.list().each {
         it.delete()
      }
      */
   }
   
   def destroy = {
   }
}
