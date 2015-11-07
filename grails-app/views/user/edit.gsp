<%@ page import="com.cabolabs.security.User" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
    <title><g:message code="default.edit.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${userInstance}">
          <ul class="errors" role="alert">
            <g:eachError bean="${userInstance}" var="error">
              <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
            </g:eachError>
          </ul>
        </g:hasErrors>
        <g:form url="[resource:userInstance, action:'update']" method="PUT" >
          <g:hiddenField name="version" value="${userInstance?.version}" />
          <fieldset class="form">
            <g:render template="form"/>
          </fieldset>
          <fieldset class="buttons">
            <g:actionSubmit class="save btn btn-default btn-md" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
          </fieldset>
        </g:form>
      </div>
    </div>
  </body>
</html>
