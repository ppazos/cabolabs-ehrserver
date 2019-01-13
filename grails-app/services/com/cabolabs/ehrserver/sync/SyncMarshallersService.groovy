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

package com.cabolabs.ehrserver.sync

import grails.transaction.Transactional
import com.cabolabs.ehrserver.parsers.JsonService
import grails.util.Holders
import groovy.json.*

import com.cabolabs.ehrserver.openehr.ehr.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*
import com.cabolabs.ehrserver.account.*
import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.query.datatypes.*
import com.cabolabs.security.*
import com.cabolabs.ehrserver.ehr.clinical_documents.*
import com.cabolabs.ehrserver.openehr.directory.*

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

/*
 * usage:
 * ------
 * def model = ...                          // get data from database
 * def jb = new JsonBuilder()               // builder needs to be created externally
 * syncMarshallersService.toJSON(model, jb) // polymorphic method, depends on the model class
 * jb.toString()                            // get JSON string
 */
@Transactional
class SyncMarshallersService {

   def config = Holders.config.app

   def jsonService

   /*
    * Converts any list to JSON, the items are converted by the correspondent method.
    * Note: the default organization and the admin role are not synced since those should exist by default in the remote
    */
   def toJSON(List l, JsonBuilder jb)
   {
      assert jb // just in case we pass null on jb
      if (l == null || l.size() == 0)
      {
         jb([])
      }
      else
      {
         // do not sync admin
         if (l[0] instanceof UserRole)
         {
            def adm_user_role = l.find { it.user.username == 'admin' }
            l.remove(adm_user_role)
         }

         // do not sync default org
         if (l[0] instanceof Organization)
         {
            def default_org = l.find { it.id == 1 } // TODO: we need to put a key name to the default org instead of using the id here
            l.remove(default_org)
         }

         jb( l.collect{ toJSON(GrailsHibernateUtil.unwrapIfProxy(it), jb) } )
      }
   }

   def toJSON(Contribution c, JsonBuilder jb)
   {
      assert jb
      def _commit = Commit.findByContributionUid(c.uid)

      String ext = '.xml'
      if (_commit.contentType.contains('json')) ext = '.json'

      def file = new File(config.commit_logs.withTrailSeparator() +
                          c.organizationUid.withTrailSeparator() +
                          _commit.fileUid + ext)

      def parsed
      if (ext == '.xml')
      {
         def json = jsonService.xml2JsonV2(file.text) //xmlToJson(file.text)
         def jsonSlurper = new JsonSlurper()
         parsed = jsonSlurper.parseText(json)

         // test
         //println jsonService.json2XmlV2(json)
      }
      else
      {
         // files is already JSON
         def jsonSlurper = new JsonSlurper()
         parsed = jsonSlurper.parseText(file.text)
      }

      jb.contribution {
         uid c.uid
         organizationUid c.organizationUid
         ehrUid c.ehr.uid

         audit( toJSON(c.audit, jb) )

         commit parsed // this way of injecting the commit json works!
      }
   }

   def toJSON(AuditDetails a, JsonBuilder jb)
   {
      assert jb
      jb { // doesnt add a name, it is added by the parent method
         time_committed a.timeCommitted
         committer {
            namespace a.committer.namespace
            type a.committer.type
            value a.committer.value
            name a.committer.name
         }
         system_id a.systemId
      }
   }

   def toJSON(Ehr e, JsonBuilder jb)
   {
      assert jb
      jb.ehr {
         uid e.uid
         dateCreated e.dateCreated
         subjectUid e.subject.value
         systemId e.systemId
         organizationUid e.organizationUid
         deleted e.deleted
         master e.master
      }
   }

   def toJSON(Account a, JsonBuilder jb)
   {
      assert jb
      jb.account {
         uid a.uid
         companyName a.companyName
         enabled a.enabled
         contact (toJSON(a.contact, jb)) // User
         master a.master
         organizations (toJSON(a.organizations, jb)) // List<Organization>
         plans(toJSON(a.allPlans, jb))
      }
   }
   def toJSON(PlanAssociation pa, JsonBuilder jb)
   {
      jb.plan_association {
         from  pa.from
         to    pa.to
         state pa.state
         plan(toJSON(pa.plan, jb))
      }
   }
   def toJSON(Plan p, JsonBuilder jb)
   {
      jb { // the name is set by plan association marshaller
         name                            p.name
         period                          p.period
         repo_total_size_in_kb           p.repo_total_size_in_kb
         max_opts_per_organization       p.max_opts_per_organization
         max_organizations               p.max_organizations
         max_api_tokens_per_organization p.max_api_tokens_per_organization
      }
   }
   def toJSON(User u, JsonBuilder jb)
   {
      assert jb
      jb { // doesnt add a name, it is added by the parent method
         uid(u.uid)
         username(u.username)
         password(u.password) // hashed with salt
         email(u.email)
         isVirtual(u.isVirtual) // virtual for api keys
         enabled(u.enabled)
         accountExpired(u.accountExpired)
         accountLocked(u.accountLocked)
         passwordExpired(u.passwordExpired)
      }
   }
   def toJSON(Organization o, JsonBuilder jb)
   {
      assert jb
      def usr = UserRole.findAllByOrganization(o)

      jb { // doesnt add a name, it is added by the parent method
         uid(o.uid)
         name(o.name)
         number(o.number)
         user_roles (toJSON(usr, jb)) // List<UserRole>
      }
   }
   def toJSON(UserRole ur, JsonBuilder jb)
   {
      assert jb
      jb { // doesnt add a name, it is added by the parent method
         user(toJSON(ur.user, jb)) // User
         role(toJSON(ur.role, jb)) // Role
         organizationUid(ur.organization.uid)
      }
   }
   def toJSON(Role r, JsonBuilder jb)
   {
      assert jb
      jb { // doesnt add a name, it is added by the parent method
         authority(r.authority)
      }
   }

   def toJSON(Query q, JsonBuilder jb)
   {
      assert jb
      jb.query {
         uid q.uid
         name q.name
         type q.type
         isPublic q.isPublic
         format q.format
         templateId q.templateId
         organizationUid q.organizationUid
         group q.group
         isDeleted q.isDeleted
         master q.master
         lastUpdated q.lastUpdated
         isCount q.isCount

         if (q.queryGroup)
         {
            queryGroup {
               uid q.queryGroup.uid
               name q.queryGroup.name
               organizationUid q.queryGroup.organizationUid
            }
         }

         author(toJSON(q.author, jb)) // User
         select(toJSON(q.select, jb)) // List<DataGet>
         where(toJSON(q.where, jb)) // List<DataCriteriaExpression>
      }
   }
   def toJSON(DataGet dg, JsonBuilder jb)
   {
      assert jb
      jb {
         archetypeId dg.archetypeId
         path dg.path
         rmTypeName dg.rmTypeName
         allowAnyArchetypeVersion dg.allowAnyArchetypeVersion
      }
   }
   def toJSON(DataCriteriaExpression ce, JsonBuilder jb)
   {
      assert jb
      jb {
         left_assoc ce.left_assoc
         right_assoc ce.right_assoc
         criteria(toJSON(ce.criteria, jb))
         criteria_class ce.criteria.getClass().getSimpleName() // need to know which specific class is the criteria to parse on the slave
      }
   }
   def toJSON(DataCriteriaString dc, JsonBuilder jb)
   {
      assert jb
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         valueValue dc.valueValue
         valueOperand dc.valueOperand
         valueNegation dc.valueNegation
      }
   }
   def toJSON(DataCriteriaLOCATABLE_REF dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         locatable_ref_pathValue dc.locatable_ref_pathValue
         locatable_ref_pathOperand dc.locatable_ref_pathOperand
         locatable_ref_pathNegation dc.locatable_ref_pathNegation
      }
   }
   def toJSON(DataCriteriaDV_TEXT dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         valueValue dc.valueValue
         valueOperand dc.valueOperand
         valueNegation dc.valueNegation
      }
   }
   def toJSON(DataCriteriaDV_QUANTITY dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         magnitudeValue dc.magnitudeValue
         unitsValue dc.unitsValue
         magnitudeOperand dc.magnitudeOperand
         unitsOperand dc.unitsOperand
         magnitudeNegation dc.magnitudeNegation
         unitsNegation dc.unitsNegation
      }
   }
   def toJSON(DataCriteriaDV_PROPORTION dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         numeratorValue dc.numeratorValue
         denominatorValue dc.denominatorValue
         typeValue dc.typeValue
         numeratorOperand dc.numeratorOperand
         denominatorOperand dc.denominatorOperand
         typeOperand dc.typeOperand
         numeratorNegation dc.numeratorNegation
         denominatorNegation dc.denominatorNegation
         typeNegation dc.typeNegation
      }
   }
   def toJSON(DataCriteriaDV_PARSABLE dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         valueValue dc.valueValue
         valueOperand dc.valueOperand
         formalismValue dc.formalismValue
         formalismOperand dc.formalismOperand
         valueNegation dc.valueNegation
         formalismNegation dc.formalismNegation
      }
   }
   def toJSON(DataCriteriaDV_ORDINAL dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         valueValue dc.valueValue
         symbol_codeValue dc.symbol_codeValue
         symbol_terminology_idValue dc.symbol_terminology_idValue
         symbol_valueValue dc.symbol_valueValue
         valueOperand dc.valueOperand
         symbol_valueOperand dc.symbol_valueOperand
         symbol_codeOperand dc.symbol_codeOperand
         symbol_terminology_idOperand dc.symbol_terminology_idOperand
         valueNegation dc.valueNegation
         symbol_valueNegation dc.symbol_valueNegation
         symbol_codeNegation dc.symbol_codeNegation
         symbol_terminology_idNegation dc.symbol_terminology_idNegation
      }
   }
   def toJSON(DataCriteriaDV_MULTIMEDIA dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         mediaTypeValue dc.mediaTypeValue
         alternateTextValue dc.alternateTextValue
         sizeValue dc.sizeValue
         uriValue dc.uriValue
         mediaTypeOperand dc.mediaTypeOperand
         alternateTextOperand dc.alternateTextOperand
         sizeOperand dc.sizeOperand
         uriOperand dc.uriOperand
         mediaTypeNegation dc.mediaTypeNegation
         alternateTextNegation dc.alternateTextNegation
         sizeNegation dc.sizeNegation
         uriNegation dc.uriNegation
      }
   }
   def toJSON(DataCriteriaDV_IDENTIFIER dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         identifierValue dc.identifierValue
         typeValue dc.typeValue
         issuerValue dc.issuerValue
         assignerValue dc.assignerValue
         identifierOperand dc.identifierOperand
         typeOperand dc.typeOperand
         issuerOperand dc.issuerOperand
         assignerOperand dc.assignerOperand
      }
   }
   def toJSON(DataCriteriaDV_DURATION dc, JsonBuilder jb)
   {
      assert jb
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         magnitudeValue dc.magnitudeValue
         magnitudeOperand dc.magnitudeOperand
         magnitudeNegation dc.magnitudeNegation
      }
   }
   def toJSON(DataCriteriaDV_DATE dc, JsonBuilder jb)
   {
      assert jb
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         valueValue dc.valueValue
         valueOperand dc.valueOperand
         age_in_yearsValue dc.age_in_yearsValue
         age_in_yearsOperand dc.age_in_yearsOperand
         age_in_monthsValue dc.age_in_monthsValue
         age_in_monthsOperand dc.age_in_monthsOperand
         valueNegation dc.valueNegation
         age_in_yearsNegation dc.age_in_yearsNegation
         age_in_monthsNegation dc.age_in_monthsNegation
      }
   }
   def toJSON(DataCriteriaDV_DATE_TIME dc, JsonBuilder jb)
   {
      assert jb
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         valueValue dc.valueValue
         valueOperand dc.valueOperand
         age_in_yearsValue dc.age_in_yearsValue
         age_in_yearsOperand dc.age_in_yearsOperand
         age_in_monthsValue dc.age_in_monthsValue
         age_in_monthsOperand dc.age_in_monthsOperand
         valueNegation dc.valueNegation
         age_in_yearsNegation dc.age_in_yearsNegation
         age_in_monthsNegation dc.age_in_monthsNegation
      }
   }
   def toJSON(DataCriteriaDV_COUNT dc, JsonBuilder jb)
   {
      assert jb
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         magnitudeValue dc.magnitudeValue
         magnitudeOperand dc.magnitudeOperand
         magnitudeNegation dc.magnitudeNegation
      }
   }
   def toJSON(DataCriteriaDV_CODED_TEXT dc, JsonBuilder jb)
   {
      assert jb
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         codeValue dc.codeValue
         terminologyIdValue dc.terminologyIdValue
         valueValue dc.valueValue
         codeOperand dc.codeOperand
         terminologyIdOperand dc.terminologyIdOperand
         valueOperand dc.valueOperand
         codeNegation dc.codeNegation
         terminologyIdNegation dc.terminologyIdNegation
         valueNegation dc.valueNegation
      }
   }
   def toJSON(DataCriteriaDV_BOOLEAN dc, JsonBuilder jb)
   {
      jb {
         archetypeId dc.archetypeId
         path dc.path
         allowAnyArchetypeVersion dc.allowAnyArchetypeVersion
         rmTypeName dc.rmTypeName
         spec dc.spec
         alias dc.alias

         valueValue dc.valueValue
         valueOperand dc.valueOperand
      }
   }

   def toJSON(EhrQuery eq, JsonBuilder jb)
   {
      jb.ehrquery {
         uid eq.uid
         name eq.name
         description eq.description
         master eq.master
         queries(toJSON(eq.queries, jb))
      }
   }

   def toJSON(OperationalTemplateIndex o, JsonBuilder jb)
   {
      def file = new File(config.opt_repo.withTrailSeparator() +
                          o.organizationUid.withTrailSeparator() +
                          o.fileUid +".opt")

      /* will send the OPT as is in XML
      def json = jsonService.xmlToJson(file.text)
      def jsonSlurper = new JsonSlurper()
      def parsed = jsonSlurper.parseText(json)
      */

      jb.template {
         templateId o.templateId
         concept o.concept
         language o.language
         uid o.uid
         externalUid o.externalUid
         archetypeId o.archetypeId
         archetypeConcept o.archetypeConcept
         organizationUid o.organizationUid
         setId o.setId
         versionNumber o.versionNumber
         lastVersion o.lastVersion
         dateCreated o.dateCreated
         isActive o.isActive
         master o.master
         opt file.text //parsed // OPT File contents
      }
   }

   def toJSON(Folder f, JsonBuilder jb)
   {
      assert jb
      jb.folder {
         uid f.uid
         name f.name
         master f.master
         folders (toJSON(f.folders ?: [], jb)) // List<Folder>
         items f.items // List<String>
         organizationUid f.organizationUid
         if (f.ehr) // only root folders have ehr
         {
            ehrUid f.ehr.uid
         }
      }
   }
}
