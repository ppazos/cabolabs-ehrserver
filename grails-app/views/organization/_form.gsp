<%@ page import="com.cabolabs.security.Organization" %>



<div class="fieldcontain ${hasErrors(bean: organizationInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="organization.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${organizationInstance?.name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: organizationInstance, field: 'pin', 'error')} required">
	<label for="pin">
		<g:message code="organization.pin.label" default="Pin" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="pin" required="" value="${organizationInstance?.pin}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: organizationInstance, field: 'uid', 'error')} required">
	<label for="uid">
		<g:message code="organization.uid.label" default="Uid" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="uid" required="" value="${organizationInstance?.uid}"/>

</div>

