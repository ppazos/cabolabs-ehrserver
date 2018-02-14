<%@ page import="com.cabolabs.ehrserver.account.Account" %>

<div class="form-group ${hasErrors(bean: account, field: 'enabled', 'error')} ">
  <label class="control-label" for="enabled">
    <g:message code="account.enabled.label" default="Enabled" />
  </label>
  <g:checkBox name="enabled" value="${account?.enabled}" />
</div>

<h2><g:message code="account.create.contact_user.title" /></h2>

<div class="form-group ${hasErrors(bean: account?.contact, field: 'username', 'error')} required">
  <label for="username"><g:message code="user.username.label" default="Username" /><span class="required-indicator">*</span></label>
  <g:textField name="username" required="" value="${account?.contact?.username}" class="form-control" />
</div>

<div class="form-group ${hasErrors(bean: account?.contact, field: 'email', 'error')} required">
  <label for="email"><g:message code="user.email.label" default="Email" /><span class="required-indicator">*</span></label>
  <g:textField name="email" required="true" value="${account?.contact?.email}" class="form-control"/>
</div>

<h2><g:message code="account.create.organization.title" /></h2>

<div class="form-group ${hasErrors(bean: organization, field: 'name', 'error')} required">
  <label for="organization"><g:message code="organization.attr.name" default="Organization name" /><span class="required-indicator">*</span></label>
  <g:textField name="organization" required="" value="${organization?.name}" class="form-control" />
</div>

<%--
<div class="form-group ${hasErrors(bean: accountInstance, field: 'organizations', 'error')} ">
  <label class="control-label" for="organizations">
    <g:message code="account.organizations.label" default="Organizations" />
  </label>
  
  <ul class="one-to-many">
    <g:each in="${accountInstance?.organizations?}" var="o">
      <li><g:link controller="organization" action="show" id="${o.id}">${o?.encodeAsHTML()}</g:link></li>
    </g:each>
    <li class="add">
      <g:link controller="organization" action="create" params="['account.id': accountInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'organization.label', default: 'Organization')])}</g:link>
    </li>
  </ul>

</div>
--%>
