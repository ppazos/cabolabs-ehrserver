<%@ page import="com.cabolabs.ehrserver.reporting.ActivityLog" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'activityLog.label', default: 'ActivityLog')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#list-activityLog" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="list-activityLog" class="content scaffold-list" role="main">
      <h1><g:message code="default.list.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
        <div class="alert alert-info" role="alert">${flash.message}</div>
      </g:if>
      <table>
        <thead>
          <tr>
            <g:sortableColumn property="username" title="${message(code: 'activityLog.username.label', default: 'Username')}" />
            <g:sortableColumn property="objectId" title="${message(code: 'activityLog.objectId.label', default: 'Object Id')}" />
            <g:sortableColumn property="action" title="${message(code: 'activityLog.action.label', default: 'Action')}" />
            <g:sortableColumn property="clientIp" title="${message(code: 'activityLog.clientIp.label', default: 'Client Ip')}" />
            <g:sortableColumn property="timestamp" title="${message(code: 'activityLog.timestamp.label', default: 'Timestamp')}" />
          </tr>
        </thead>
        <tbody>
        <g:each in="${activityLogInstanceList}" status="i" var="activityLogInstance">
          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <td><g:link action="show" id="${activityLogInstance.id}">${fieldValue(bean: activityLogInstance, field: "username")}</g:link></td>
            <td>${fieldValue(bean: activityLogInstance, field: "objectId")}</td>
            <td>${fieldValue(bean: activityLogInstance, field: "action")}</td>
            <td>${fieldValue(bean: activityLogInstance, field: "clientIp")}</td>
            <td><g:formatDate date="${activityLogInstance.timestamp}" /></td>
          </tr>
        </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <g:paginate total="${activityLogInstanceCount ?: 0}" />
      </div>
    </div>
  </body>
</html>
