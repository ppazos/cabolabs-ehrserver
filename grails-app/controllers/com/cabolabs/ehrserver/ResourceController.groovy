package com.cabolabs.ehrserver

import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndexShare
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.query.QueryShare
import com.cabolabs.security.Organization

class ResourceController {
   
   def resourceService
   
   // TODO: verify permissions of the user over the query & organizations
   
   def shareQuery(String uid)
   {
      def query = Query.findByUid(uid)
      def shares = QueryShare.findAllByQuery(query)
      render view:'/query/share', model:[query: query, organizations: shares.organization]
   }
   
   def saveSharesQuery()
   {
      Query query = params.query
      
      if (query.isPublic)
      {
         flash.message = "Can't change the shares of a public query"
         redirect action:'shareQuery', params:[uid:query.uid]
         return
      }
      
      // delete all shares but the one that belongs to the current org
      resourceService.cleanSharesQueryBut(query, session.organization)
      
      // share with selected orgs
      def organization
      def orgUids = params.list('organizationUid')
      orgUids.each { organizationUid ->
         organization = Organization.findByUid(organizationUid)
         resourceService.shareQuery(query, organization)
      }
      
      flash.message = "Query shares were updated correctly"
      redirect action:'shareQuery', params:[uid:query.uid]
   }
   
   def shareOpt(String uid)
   {
      def opt = OperationalTemplateIndex.findByUid(uid)
      def shares = OperationalTemplateIndexShare.findAllByOpt(opt)
      render view:'/operationalTemplate/share', model:[opt: opt, organizations: shares.organization]
   }
   
   def saveSharesOpt()
   {
      OperationalTemplateIndex opt = params.opt
      
      if (opt.isPublic)
      {
         flash.message = "Can't change the shares of a public template"
         redirect action:'shareOpt', params:[uid:opt.uid]
         return
      }
      
      // delete all shares but the one that belongs to the current org
      resourceService.cleanSharesOptBut(opt, session.organization)
      
      // share with selected orgs
      def organization
      def orgUids = params.list('organizationUid')
      orgUids.each { organizationUid ->
         organization = Organization.findByUid(organizationUid)
         resourceService.shareOpt(opt, organization)
      }
      
      flash.message = "Template shares were updated correctly"
      redirect action:'shareOpt', params:[uid:opt.uid]
   }
}
