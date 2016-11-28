package com.cabolabs.ehrserver

import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexShare
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare
import com.cabolabs.security.Organization

import grails.transaction.Transactional

@Transactional
class ResourceService {

   /**
    * Creates a share of the query with the organization.
    * @param query
    * @param organization
    * @return
    */
   def shareQuery(Query query, Organization organization)
   {
      if (QueryShare.countByQueryAndOrganization(query, organization) == 0)
      {
         def share = new QueryShare(query: query, organization: organization)
         share.save(failOnError: true)
      }
   }
   
   /**
    * Clean all the shares of a query.
    * @param query
    * @return
    */
   def cleanSharesQuery(Query query)
   {
      def shares = QueryShare.findAllByQuery(query)
      shares.each { share ->
         share?.delete(failOnError: true)
      }
   }
   
   /**
    * Cleans all the shares of a query, except for one organization.
    * @param query
    * @param organization
    * @return
    */
   def cleanSharesQueryBut(Query query, Organization organization)
   {
      def shares = QueryShare.findAllByQuery(query)
      shares.each { share ->
         if (share.organization.id != organization.id)
            share?.delete(failOnError: true)
      }
   }
   
   /**
    * Creates a share of the opt with the organization.
    * @param opt
    * @param organization
    * @return
    */
   def shareOpt(OperationalTemplateIndex opt, Organization organization)
   {
      if (OperationalTemplateIndexShare.countByOptAndOrganization(opt, organization) == 0)
      {
         def share = new OperationalTemplateIndexShare(opt: opt, organization: organization)
         share.save(failOnError: true)
      }
   }
   
   /**
    * Clean all the shares of an opt.
    * @param opt
    * @return
    */
   def cleanSharesOpt(OperationalTemplateIndex opt)
   {
      def shares = OperationalTemplateIndexShare.findAllByOpt(opt)
      shares.each { share ->
         share?.delete(failOnError: true)
      }
   }
   
   /**
    * Cleans all the shares of an opt, except for one organization.
    * @param opt
    * @param organization
    * @return
    */
   def cleanSharesOptBut(OperationalTemplateIndex opt, Organization organization)
   {
      def shares = OperationalTemplateIndexShare.findAllByOpt(opt)
      shares.each { share ->
         if (share.organization.id != organization.id)
            share?.delete(failOnError: true)
      }
   }
}
