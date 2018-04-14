<%@ page import="com.cabolabs.ehrserver.reporting.ActivityLog" %><%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.Commit" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="activityLog.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="activityLog.list.title" /></h1>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
	       <div class="alert alert-info" role="alert">${flash.message}</div>
	     </g:if>

        <g:each in="${activityLogInstanceList}" var="e">
          <g:set var="sessionId" value="${e.key}" />
          <h2><g:message code="activityLog.attr.sessionId" /> ${sessionId}</h2>
          <div class="table-responsive">
            <table class="table table-striped table-bordered table-hover">
               <thead>
                <tr>
                  <g:sortableColumn property="timestamp" mapping="logs" title="${message(code: 'activityLog.timestamp.label', default: 'Timestamp')}" />
                  <g:sortableColumn property="username" mapping="logs" title="${message(code: 'activityLog.username.label', default: 'Username')}" />
                  <g:sortableColumn property="objectId" mapping="logs" title="${message(code: 'activityLog.objectId.label', default: 'Object Id')}" />
                  <g:sortableColumn property="action" mapping="logs" title="${message(code: 'activityLog.action.label', default: 'Action')}" />
                  <g:sortableColumn property="remoteAddr" mapping="logs" title="${message(code: 'activityLog.remoteAddr.label', default: 'Address')}" />
                </tr>
               </thead>
               <tbody>
                 <g:each in="${e.value}" status="i" var="activityLogInstance">
                   <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                     <td><g:link controller="logs" action="show" id="${activityLogInstance.id}"><g:formatDate date="${activityLogInstance.timestamp}" /></g:link></td>
                     <td>${fieldValue(bean: activityLogInstance, field: "username")}</td>
                     <td>${fieldValue(bean: activityLogInstance, field: "objectId")}</td>
                     <td>${fieldValue(bean: activityLogInstance, field: "action")}</td>
                     <td>${fieldValue(bean: activityLogInstance, field: "remoteAddr")}</td>
                   </tr>
                   <g:set var="commit" value="${Commit.findByActivityLog(activityLogInstance)}" />
                   <g:if test="${commit}">
                     <tr>
                       <td colspan="5" style="padding:0;">
                         <table class="table" style="margin:0;">
                           <tr>
                             <th>EHR</th>
                             <th>Contribution</th>
                             <th>Type</th>
                             <th>Language</th>
                             <th>Successful commit?</th>
                           </tr>
                           <tr>
                             <td>${commit.ehrUid}</td>
                             <td>${commit.contributionUid}</td>
                             <td>${commit.contentType}</td>
                             <td>${commit.locale}</td>
                             <td>${commit.success}</td>
                           </tr>
                         </table>
                       </td>
                     </tr>
                   </g:if>
                 </g:each>
               </tbody>
            </table>
          </div>
        </g:each>


        <g:paginator total="${activityLogInstanceCount}" args="${params}" />
      </div>
    </div>
  </body>
</html>
