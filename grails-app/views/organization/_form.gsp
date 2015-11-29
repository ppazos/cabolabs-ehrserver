<%@ page import="com.cabolabs.security.Organization" %>

<div class="form-group ${hasErrors(bean: organizationInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="organization.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${organizationInstance?.name}" class="form-control" />
</div>
<%--
<div class="fieldcontain ${hasErrors(bean: organizationInstance, field: 'number', 'error')} required">
	<label for="number">
		<g:message code="organization.number.label" default="Number" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="number" required="" value="${organizationInstance?.number}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: organizationInstance, field: 'uid', 'error')} required">
	<label for="uid">
		<g:message code="organization.uid.label" default="Uid" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="uid" required="" value="${organizationInstance?.uid}"/>
</div>
--%>
