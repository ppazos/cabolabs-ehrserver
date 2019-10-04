<%@ page import="com.cabolabs.security.Organization" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'organization.label', default: 'Organization')}" />
    <title><g:message code="default.edit.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${organizationInstance}">
          <ul class="errors" role="alert">
            <g:eachError bean="${organizationInstance}" var="error">
              <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
            </g:eachError>
          </ul>
        </g:hasErrors>
        <g:form action="update" id="${organizationInstance.uid}" method="PUT" >
          <g:hiddenField name="version" value="${organizationInstance?.version}" />
          <fieldset class="form">
            <g:render template="form"/>
            <div class="btn-toolbar" role="toolbar">
              <g:actionSubmit class="save btn btn-success btn-md" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
            </div>
          </fieldset>
        </g:form>
      </div>
    </div>
  </body>
</html>
