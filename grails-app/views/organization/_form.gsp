<%@ page import="com.cabolabs.security.Organization" %>

<div class="form-group ${hasErrors(bean: organizationInstance, field: 'name', 'error')} required">
  <label for="name">
    <g:message code="organization.name.label" default="Name" />
    <span class="required-indicator">*</span>
  </label>
  <g:textField name="name" required="" value="${organizationInstance?.name}" class="form-control" />
</div>

<sec:ifAnyGranted roles="ROLE_ADMIN">
  <div class="form-group">
    <label for="name">
      <g:message code="organization.associated.label" default="Associate to me" />
    </label>
    <g:checkBox name="assign" value="${true}" class="checkbox" checked="false" />
  </div>
  <g:if test="${actionName == 'create' || actionName == 'save'}">
  <div class="form-group">
    <label for="accounts">
      <g:message code="organization.accounts.label" default="Accounts" />
    </label>
    <g:select from="${accounts}" name="account_id" optionKey="id" optionValue="companyName" class="form-control"></g:select>
  </div>
  </g:if>
</sec:ifAnyGranted>
