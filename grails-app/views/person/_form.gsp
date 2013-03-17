<%@ page import="demographic.Person" %>



<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'sex', 'error')} ">
	<label for="sex">
		<g:message code="person.sex.label" default="Sex" />
		
	</label>
	<g:select name="sex" from="${personInstance.constraints.sex.inList}" value="${personInstance?.sex}" valueMessagePrefix="person.sex" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'idCode', 'error')} ">
	<label for="idCode">
		<g:message code="person.idCode.label" default="Id Code" />
		
	</label>
	<g:textField name="idCode" value="${personInstance?.idCode}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'idType', 'error')} ">
	<label for="idType">
		<g:message code="person.idType.label" default="Id Type" />
		
	</label>
	<g:select name="idType" from="${personInstance.constraints.idType.inList}" value="${personInstance?.idType}" valueMessagePrefix="person.idType" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'role', 'error')} ">
	<label for="role">
		<g:message code="person.role.label" default="Role" />
		
	</label>
	<g:select name="role" from="${personInstance.constraints.role.inList}" value="${personInstance?.role}" valueMessagePrefix="person.role" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'dob', 'error')} required">
	<label for="dob">
		<g:message code="person.dob.label" default="Dob" />
		<span class="required-indicator">*</span>
	</label>
	<g:datePicker name="dob" precision="day"  value="${personInstance?.dob}"  />
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'firstName', 'error')} ">
	<label for="firstName">
		<g:message code="person.firstName.label" default="First Name" />
		
	</label>
	<g:textField name="firstName" value="${personInstance?.firstName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'lastName', 'error')} ">
	<label for="lastName">
		<g:message code="person.lastName.label" default="Last Name" />
		
	</label>
	<g:textField name="lastName" value="${personInstance?.lastName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'uid', 'error')} ">
	<label for="uid">
		<g:message code="person.uid.label" default="Uid" />
		
	</label>
	<g:textField name="uid" value="${personInstance?.uid}"/>
</div>

