<%@ page import="com.cabolabs.ehrserver.notification.Notification" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="default.edit.label" /></title>
  </head>
  <body>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="edit-notification" class="content scaffold-edit" role="main">
      <h1><g:message code="default.edit.label" /></h1>
      <g:if test="${flash.message}">
        <div class="alert alert-info" role="alert">${flash.message}</div>
      </g:if>
      <g:hasErrors bean="${notificationInstance}">
      <ul class="errors" role="alert">
        <g:eachError bean="${notificationInstance}" var="error">
        <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
        </g:eachError>
      </ul>
      </g:hasErrors>
      <g:form url="[resource:notificationInstance, action:'update']" method="PUT" id="frm_notification">
        <g:hiddenField name="version" value="${notificationInstance?.version}" />
        <fieldset class="form">
          <g:render template="form"/>
        </fieldset>
        <fieldset class="buttons">
          <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
        </fieldset>
      </g:form>
    </div>
  </body>
</html>
