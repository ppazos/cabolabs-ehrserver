<%@ page import="com.cabolabs.security.User" %><%@ page import="com.cabolabs.security.Role" %><%@ page import="com.cabolabs.security.Organization" %>

<input type="hidden" name="type" value="${params.type}" />

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'username', 'error')} required">
	<label for="username">
		<g:message code="user.username.label" default="Username" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="username" required="" value="${userInstance?.username}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'email', 'error')} required">
   <label for="email">
      <g:message code="user.email.label" default="Email" />
      <span class="required-indicator">*</span>
   </label>
   <g:textField name="email" required="" value="${userInstance?.email}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'organizations', 'error')} required">
   <label for="organizations">
      <g:message code="user.organizations.label" default="Organizations" />
      <span class="required-indicator">*</span>
   </label>
   <br/>
   <sec:ifAnyGranted roles="ROLE_ADMIN">
     <g:select name="organizations" from="${Organization.list()}"
               optionKey="uid" optionValue="name" value="${userInstance?.organizationObjects}"
               multiple="true" size="5" />
   </sec:ifAnyGranted>
   <sec:ifNotGranted roles="ROLE_ADMIN">
     <g:selectWithCurrentUserOrganizations name="organizations" value="${userInstance?.organizations}" multiple="true" />
   </sec:ifNotGranted>
</div>

<div>
  <label for="role">
    <g:message code="user.roles.label" default="Roles" />
    <span class="required-indicator">*</span>
  </label>
  <g:selectWithRolesICanAssign name="role" value="${userInstance?.authorities}" multiple="true" />
</div>

<sec:access expression="hasRole('ROLE_ADMIN')">
	<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'accountExpired', 'error')} ">
		<label for="accountExpired">
			<g:message code="user.accountExpired.label" default="Account Expired" />
		</label>
		<g:checkBox name="accountExpired" value="${userInstance?.accountExpired}" />
	</div>
	
	<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'accountLocked', 'error')} ">
		<label for="accountLocked">
			<g:message code="user.accountLocked.label" default="Account Locked" />
		</label>
		<g:checkBox name="accountLocked" value="${userInstance?.accountLocked}" />
	</div>
	
	<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'enabled', 'error')} ">
		<label for="enabled">
			<g:message code="user.enabled.label" default="Enabled" />
		</label>
		<g:checkBox name="enabled" value="${userInstance?.enabled}" />
	</div>
	
	<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'passwordExpired', 'error')} ">
		<label for="passwordExpired">
			<g:message code="user.passwordExpired.label" default="Password Expired" />
		</label>
		<g:checkBox name="passwordExpired" value="${userInstance?.passwordExpired}" />
	</div>
</sec:access>
