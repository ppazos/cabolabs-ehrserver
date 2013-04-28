<%@ page import="query.Query" %>



<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="query.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${queryInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'group', 'error')} ">
	<label for="group">
		<g:message code="query.group.label" default="Group" />
		
	</label>
	<g:select name="group" from="${queryInstance.constraints.group.inList}" value="${queryInstance?.group}" valueMessagePrefix="query.group" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'qarchetypeId', 'error')} ">
	<label for="qarchetypeId">
		<g:message code="query.qarchetypeId.label" default="Qarchetype Id" />
		
	</label>
	<g:textField name="qarchetypeId" value="${queryInstance?.qarchetypeId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'format', 'error')} ">
	<label for="format">
		<g:message code="query.format.label" default="Format" />
		
	</label>
	<g:select name="format" from="${queryInstance.constraints.format.inList}" value="${queryInstance?.format}" valueMessagePrefix="query.format" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'type', 'error')} ">
	<label for="type">
		<g:message code="query.type.label" default="Type" />
		
	</label>
	<g:select name="type" from="${queryInstance.constraints.type.inList}" value="${queryInstance?.type}" valueMessagePrefix="query.type" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'select', 'error')} ">
	<label for="select">
		<g:message code="query.select.label" default="Select" />
		
	</label>
	<g:select name="select" from="${query.DataGet.list()}" multiple="multiple" optionKey="id" size="5" value="${queryInstance?.select*.id}" class="many-to-many"/>
</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'where', 'error')} ">
	<label for="where">
		<g:message code="query.where.label" default="Where" />
		
	</label>
	<g:select name="where" from="${query.DataCriteria.list()}" multiple="multiple" optionKey="id" size="5" value="${queryInstance?.where*.id}" class="many-to-many"/>
</div>

