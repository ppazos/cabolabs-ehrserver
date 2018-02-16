<%@ page import="com.cabolabs.ehrserver.account.Account" %><%@ page import="com.cabolabs.ehrserver.account.Plan" %>

<div class="form-group ${hasErrors(bean: account, field: 'enabled', 'error')} ">
  <label class="control-label" for="enabled">
    <g:message code="account.enabled.label" default="Enabled" />
  </label>
  <g:checkBox name="enabled" value="${account?.enabled}" />
</div>

<g:if test="${actionName=='create'||actionName=='save'}">

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
</g:if>

<g:if test="${actionName=='edit' || actionName=='update'}">
  <div class="form-group">
    <label for="plan_id"><g:message code="organization.edit.plan" default="Plan" /></label>
    <g:select from="${Plan.list()}" name="plan_id" optionKey="id" optionValue="name" class="form-control"></g:select>
  </div>
</g:if>
