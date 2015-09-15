<%@ page import="com.cabolabs.security.User" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
    <title><g:message code="user.registerOk.label" args="[entityName]" /></title>
  </head>
  <body>
    <div id="show-user" class="content scaffold-show" role="main">
      <h1><g:message code="user.registerOk.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      <g:message code="user.registerOk.text" />
    </div>
  </body>
</html>
