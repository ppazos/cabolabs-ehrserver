<%@ page import="com.cabolabs.ehrserver.reporting.ActivityLog" %>
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
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
            <thead>
             <tr>
               <g:sortableColumn property="username" mapping="logs" title="${message(code: 'activityLog.username.label', default: 'Username')}" />
               <g:sortableColumn property="objectId" mapping="logs" title="${message(code: 'activityLog.objectId.label', default: 'Object Id')}" />
               <g:sortableColumn property="action" mapping="logs" title="${message(code: 'activityLog.action.label', default: 'Action')}" />
               <g:sortableColumn property="clientIp" mapping="logs" title="${message(code: 'activityLog.clientIp.label', default: 'Client Ip')}" />
               <g:sortableColumn property="timestamp" mapping="logs" title="${message(code: 'activityLog.timestamp.label', default: 'Timestamp')}" />
             </tr>
            </thead>
            <tbody>
            <g:each in="${activityLogInstanceList}" status="i" var="activityLogInstance">
             <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
               <td><g:link controller="logs" action="show" id="${activityLogInstance.id}">${fieldValue(bean: activityLogInstance, field: "username")}</g:link></td>
               <td>${fieldValue(bean: activityLogInstance, field: "objectId")}</td>
               <td>${fieldValue(bean: activityLogInstance, field: "action")}</td>
               <td>${fieldValue(bean: activityLogInstance, field: "clientIp")}</td>
               <td><g:formatDate date="${activityLogInstance.timestamp}" /></td>
             </tr>
            </g:each>
            </tbody>
          </table>
        </div>
        <g:paginator total="${activityLogInstanceCount}" args="${params}" />
      </div>
    </div>
  </body>
</html>
