package com.cabolabs.ehrserver.sync

import grails.transaction.Transactional
import com.cabolabs.ehrserver.parsers.JsonService
import grails.util.Holders
import groovy.json.*
import com.cabolabs.ehrserver.parsers.JsonService

import com.cabolabs.ehrserver.openehr.common.change_control.*
import com.cabolabs.ehrserver.openehr.common.generic.*

@Transactional
class SyncMarshallersService {

   def config = Holders.config.app

   def jsonService

   def toJSON(List l, JsonBuilder jb)
   {
      jb( l.collect{ toJSON(it, jb) } )
   }

   def toJSON(Contribution c, JsonBuilder jb)
   {
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
      jb {
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
}
