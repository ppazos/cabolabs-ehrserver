package com.cabolabs.ehrserver.sync

import grails.transaction.Transactional
import com.cabolabs.ehrserver.parsers.JsonService
import grails.util.Holders
import groovy.json.*

import com.cabolabs.ehrserver.openehr.ehr.*
import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*
import com.cabolabs.ehrserver.account.*
import com.cabolabs.security.*

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
    */
   def toJSON(List l, JsonBuilder jb)
   {
      assert jb // just in case we pass null on jb
      jb( l.collect{ toJSON(GrailsHibernateUtil.unwrapIfProxy(it), jb) } )
   }

   def toJSON(Contribution c, JsonBuilder jb)
   {
      assert jb
      def _commit = Commit.findByContributionUid(c.uid)
      def file = new File(config.commit_logs.withTrailSeparator() +
                          c.organizationUid.withTrailSeparator() +
                          _commit.fileUid + '.xml')
      def json = jsonService.xmlToJson(file.text)
      def jsonSlurper = new JsonSlurper()
      def parsed = jsonSlurper.parseText(json)

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
         companyName a.companyName
         enabled a.enabled
         contact (toJSON(a.contact, jb)) // User
         master a.master
         organizations (toJSON(a.organizations, jb)) // List<Organization>
      }
   }
   def toJSON(User u, JsonBuilder jb)
   {
      assert jb
      jb { // doesnt add a name, it is added by the parent method
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
      }
   }
   def toJSON(Role r, JsonBuilder jb)
   {
      assert jb
      jb { // doesnt add a name, it is added by the parent method
         authority(r.authority)
      }
   }
}
