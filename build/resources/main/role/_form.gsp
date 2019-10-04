<%@ page import="com.cabolabs.security.Role" %>

<div class="form-group ${hasErrors(bean: roleInstance, field: 'authority', 'error')} required">
	<label for="authority">
		<g:message code="role.authority.label" default="Authority" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="authority" required="" value="${roleInstance?.authority}" class="form-control" />
</div>
