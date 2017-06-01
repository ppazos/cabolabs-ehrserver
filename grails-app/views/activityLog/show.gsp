<%@ page import="com.cabolabs.ehrserver.reporting.ActivityLog" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'activityLog.label', default: 'ActivityLog')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#show-activityLog" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="show-activityLog" class="content scaffold-show" role="main">
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
        <div class="alert alert-info" role="alert">${flash.message}</div>
      </g:if>
      <ol class="property-list activityLog">
      
        <g:if test="${activityLogInstance?.username}">
        <li class="fieldcontain">
          <span id="username-label" class="property-label"><g:message code="activityLog.username.label" default="Username" /></span>
          <span class="property-value" aria-labelledby="username-label"><g:fieldValue bean="${activityLogInstance}" field="username"/></span>
        </li>
        </g:if>
      
        <g:if test="${activityLogInstance?.objectId}">
        <li class="fieldcontain">
          <span id="objectId-label" class="property-label"><g:message code="activityLog.objectId.label" default="Object Id" /></span>
          <span class="property-value" aria-labelledby="objectId-label"><g:fieldValue bean="${activityLogInstance}" field="objectId"/></span>
        </li>
        </g:if>
      
        <g:if test="${activityLogInstance?.action}">
        <li class="fieldcontain">
          <span id="action-label" class="property-label"><g:message code="activityLog.action.label" default="Action" /></span>
          <span class="property-value" aria-labelledby="action-label"><g:fieldValue bean="${activityLogInstance}" field="action"/></span>
        </li>
        </g:if>
      
        <g:if test="${activityLogInstance?.clientIp}">
        <li class="fieldcontain">
          <span id="clientIp-label" class="property-label"><g:message code="activityLog.clientIp.label" default="Client Ip" /></span>
          <span class="property-value" aria-labelledby="clientIp-label"><g:fieldValue bean="${activityLogInstance}" field="clientIp"/></span>
        </li>
        </g:if>
      
        <g:if test="${activityLogInstance?.timestamp}">
        <li class="fieldcontain">
          <span id="timestamp-label" class="property-label"><g:message code="activityLog.timestamp.label" default="Timestamp" /></span>
          <span class="property-value" aria-labelledby="timestamp-label"><g:formatDate date="${activityLogInstance?.timestamp}" /></span>
        </li>
        </g:if>
      
      </ol>
      <g:form url="[resource:activityLogInstance, action:'delete']" method="DELETE">
        <fieldset class="buttons">
          <g:link class="edit" action="edit" resource="${activityLogInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
        </fieldset>
      </g:form>
    </div>
  </body>
</html>
