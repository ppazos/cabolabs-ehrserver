package com.cabolabs.ehrserver

import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare
import com.cabolabs.security.Organization

import grails.transaction.Transactional

@Transactional
class ResourceService {

   def shareQuery(Query query, Organization organization)
   {
      if (QueryShare.countByQueryAndOrganization(query, organization) == 0)
      {
         def share = new QueryShare(query: query, organization: organization)
         share.save(failOnError: true)
      }
   }
   
   def cleanSharesQuery(Query query)
   {
      def shares = QueryShare.findAllByQuery(query)
      shares.each { share ->
         share?.delete(failOnError: true)
      }
   }
   
   def cleanSharesQueryBut(Query query, Organization organization)
   {
      def shares = QueryShare.findAllByQuery(query)
      shares.each { share ->
         if (share.organization.id != organization.id)
            share?.delete(failOnError: true)
      }
   }
}
