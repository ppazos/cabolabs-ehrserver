<%@ page import="com.cabolabs.ehrserver.openehr.demographic.Person" %><%@ page import="com.cabolabs.security.Organization" %><%@ page import="com.cabolabs.ehrserver.identification.PersonIdType" %>

<div class="form-group ${hasErrors(bean: personInstance, field: 'firstName', 'has-error')}">
  <label class="control-label"><g:message code="person.firstName.label" default="First Name" /></label>
  <g:textField name="firstName" value="${personInstance?.firstName}" class="form-control" />
</div>
<div class="form-group ${hasErrors(bean: personInstance, field: 'lastName', 'has-error')}">
  <label class="control-label"><g:message code="person.lastName.label" default="Last Name" /></label>
  <g:textField name="lastName" value="${personInstance?.lastName}" class="form-control" />
</div>
<div class="form-group ${hasErrors(bean: personInstance, field: 'dob', 'has-error')} required">
  <label class="control-label"><g:message code="person.dob.label" default="Dob" /><span class="required-indicator">*</span></label>
  <div>
    <g:datePicker name="dob" precision="day" value="${personInstance?.dob}" />
  </div>
</div>

<div class="form-group ${hasErrors(bean: personInstance, field: 'sex', 'has-error')}">
  <label class="control-label"><g:message code="person.sex.label" default="Sex" /></label>
  <g:select name="sex" from="${personInstance.constraints.sex.inList}" value="${personInstance?.sex}" valueMessagePrefix="person.sex" noSelection="['': '']" class="form-control" />
</div>

<div class="form-group ${hasErrors(bean: personInstance, field: 'idCode', 'has-error')}">
  <label class="control-label"><g:message code="person.idCode.label" default="Id Code" /></label>
  <g:textField name="idCode" value="${personInstance?.idCode}" class="form-control" />
</div>
<div class="form-group ${hasErrors(bean: personInstance, field: 'idType', 'has-error')}">
  <label class="control-label"><g:message code="person.idType.label" default="Id Type" /></label>
  <g:select name="idType" from="${PersonIdType.list()}" optionKey="code" optionValue="name" value="${personInstance?.idType}" valueMessagePrefix="person.idType" noSelection="['': '']" class="form-control" />
</div>

<div class="form-group ${hasErrors(bean: personInstance, field: 'role', 'has-error')}">
  <label class="control-label"><g:message code="person.role.label" default="Role" /></label>
  <g:select name="role" from="${personInstance.constraints.role.inList}" value="${personInstance?.role}" valueMessagePrefix="person.role" noSelection="['': '']"  class="form-control"/>
</div>

<div class="form-group ${hasErrors(bean: personInstance, field: 'organizationUid', 'has-error')}">
  <label class="control-label"><g:message code="person.organization.label" default="Organization" /></label>
  <sec:ifAnyGranted roles="ROLE_ADMIN">
    <g:select name="organizationUid" from="${Organization.list()}"
              optionKey="uid" optionValue="name" value="${personInstance.organizationUid}" class="form-control" />
  </sec:ifAnyGranted>
  <sec:ifNotGranted roles="ROLE_ADMIN">
    <g:selectWithCurrentUserOrganizations name="organizationUid" value="${personInstance.organizationUid}" class="form-control" />
  </sec:ifNotGranted>
</div>
<script type='text/javascript'>
 (function() {
   // FIX because datePicker doesnt set the class attr in the generated selects
   $('select[name=dob_day]').addClass('form-control').css({'width':'15%', 'display':'inline-block'});
   $('select[name=dob_month]').addClass('form-control').css({'width':'55%', 'display':'inline-block'});
   $('select[name=dob_year]').addClass('form-control').css({'width':'22%', 'display':'inline-block'});
 })();
</script>
