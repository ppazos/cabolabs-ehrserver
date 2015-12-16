<%@ page import="com.cabolabs.ehrserver.identification.PersonIdType" %>

<div class="form-group ${hasErrors(bean: personIdTypeInstance, field: 'name', 'has-error')} required">
  <label class="control-label"><g:message code="personIdType.name.label" default="Name" /></label>
  <g:textField name="name" required="" value="${personIdTypeInstance?.name}" class="form-control" />
</div>

<div class="form-group ${hasErrors(bean: personIdTypeInstance, field: 'code', 'has-error')} required">
  <label class="control-label"><g:message code="personIdType.code.label" default="Code" /></label>
  <g:textField name="code" required="" value="${personIdTypeInstance?.code}" class="form-control" />
</div>
