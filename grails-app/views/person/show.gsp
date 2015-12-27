<%@ page import="com.cabolabs.ehrserver.openehr.demographic.Person" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div>
        </g:if>
        
        <div class="control-group">
          <label class="control-label"><g:message code="person.uid.label" default="UID" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="uid"/></p>
          </div>
        </div>
        
        <div class="control-group">
          <label class="control-label"><g:message code="person.firstName.label" default="First Name" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="firstName"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="person.lastName.label" default="Last Name" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="lastName"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="person.dob.label" default="Dob" /></label>
          <div class="controls">
            <p class="form-control-static"><g:formatDate date="${personInstance?.dob}" /></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="person.sex.label" default="Sex" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="sex"/></p>
          </div>
        </div>
        
        <div class="control-group">
          <label class="control-label"><g:message code="person.idCode.label" default="Id Code" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="idCode"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="person.idType.label" default="Id Type" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="idType"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="person.role.label" default="Role" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="role"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="person.organization.label" default="Organization" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personInstance}" field="organizationUid"/></p>
          </div>
        </div>
        
        <div class="btn-toolbar" role="toolbar">
          <g:link action="edit" id="${personInstance?.id}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
        </div>
      </div>
    </div>
  </body>
</html>
