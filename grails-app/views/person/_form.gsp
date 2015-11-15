<%@ page import="demographic.Person" %><%@ page import="com.cabolabs.security.Organization" %>

<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'firstName', 'has-error')}">
  <label class="control-label"><g:message code="person.firstName.label" default="First Name" /></label>
  <div class="controls">
    <p class="form-control-static"><g:textField name="firstName" value="${personInstance?.firstName}"/></p>
  </div>
</div>
<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'lastName', 'has-error')}">
  <label class="control-label"><g:message code="person.lastName.label" default="Last Name" /></label>
  <div class="controls">
    <p class="form-control-static"><g:textField name="lastName" value="${personInstance?.lastName}"/></p>
  </div>
</div>
<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'dob', 'has-error')} required">
  <label class="control-label"><g:message code="person.dob.label" default="Dob" /><span class="required-indicator">*</span></label>
  <div class="controls">
    <p class="form-control-static"><g:datePicker name="dob" precision="day"  value="${personInstance?.dob}" /></p>
  </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'sex', 'has-error')}">
  <label class="control-label"><g:message code="person.sex.label" default="Sex" /></label>
  <div class="controls">
    <p class="form-control-static"><g:select name="sex" from="${personInstance.constraints.sex.inList}" value="${personInstance?.sex}" valueMessagePrefix="person.sex" noSelection="['': '']"/></p>
  </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'idCode', 'has-error')}">
  <label class="control-label"><g:message code="person.idCode.label" default="Id Code" /></label>
  <div class="controls">
    <p class="form-control-static"><g:textField name="idCode" value="${personInstance?.idCode}"/></p>
  </div>
</div>
<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'idType', 'has-error')}">
  <label class="control-label"><g:message code="person.idType.label" default="Id Type" /></label>
  <div class="controls">
    <p class="form-control-static"><g:select name="idType" from="${personInstance.constraints.idType.inList}" value="${personInstance?.idType}" valueMessagePrefix="person.idType" noSelection="['': '']"/></p>
  </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'role', 'has-error')}">
  <label class="control-label"><g:message code="person.role.label" default="Role" /></label>
  <div class="controls">
    <p class="form-control-static"><g:select name="role" from="${personInstance.constraints.role.inList}" value="${personInstance?.role}" valueMessagePrefix="person.role" noSelection="['': '']"/></p>
  </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: personInstance, field: 'organizationUid', 'has-error')}">
  <label class="control-label"><g:message code="person.organization.label" default="Organization" /></label>
  <div class="controls">
    <p class="form-control-static">
      <sec:ifAnyGranted roles="ROLE_ADMIN">
        <g:select name="organizationUid" from="${Organization.list()}"
                  optionKey="uid" optionValue="name" value="${personInstance.organizationUid}" />
      </sec:ifAnyGranted>
      <sec:ifNotGranted roles="ROLE_ADMIN">
        <g:selectWithCurrentUserOrganiations name="organizationUid" value="${personInstance.organizationUid}" />
      </sec:ifNotGranted>
    </p>
  </div>
</div>
