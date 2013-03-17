<%@ page import="ehr.clinical_documents.CompositionIndex" %>



<div class="fieldcontain ${hasErrors(bean: compositionIndexInstance, field: 'category', 'error')} ">
	<label for="category">
		<g:message code="compositionIndex.category.label" default="Category" />
		
	</label>
	<g:select name="category" from="${compositionIndexInstance.constraints.category.inList}" value="${compositionIndexInstance?.category}" valueMessagePrefix="compositionIndex.category" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: compositionIndexInstance, field: 'startTime', 'error')} ">
	<label for="startTime">
		<g:message code="compositionIndex.startTime.label" default="Start Time" />
		
	</label>
	<g:datePicker name="startTime" precision="day"  value="${compositionIndexInstance?.startTime}" default="none" noSelection="['': '']" />
</div>

<div class="fieldcontain ${hasErrors(bean: compositionIndexInstance, field: 'archetypeId', 'error')} ">
	<label for="archetypeId">
		<g:message code="compositionIndex.archetypeId.label" default="Archetype Id" />
		
	</label>
	<g:textField name="archetypeId" value="${compositionIndexInstance?.archetypeId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: compositionIndexInstance, field: 'ehrId', 'error')} ">
	<label for="ehrId">
		<g:message code="compositionIndex.ehrId.label" default="Ehr Id" />
		
	</label>
	<g:textField name="ehrId" value="${compositionIndexInstance?.ehrId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: compositionIndexInstance, field: 'subjectId', 'error')} ">
	<label for="subjectId">
		<g:message code="compositionIndex.subjectId.label" default="Subject Id" />
		
	</label>
	<g:textField name="subjectId" value="${compositionIndexInstance?.subjectId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: compositionIndexInstance, field: 'uid', 'error')} ">
	<label for="uid">
		<g:message code="compositionIndex.uid.label" default="Uid" />
		
	</label>
	<g:textField name="uid" value="${compositionIndexInstance?.uid}"/>
</div>

