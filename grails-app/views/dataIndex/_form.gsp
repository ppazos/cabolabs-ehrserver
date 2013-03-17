<%@ page import="ehr.clinical_documents.DataIndex" %>



<div class="fieldcontain ${hasErrors(bean: dataIndexInstance, field: 'archetypeId', 'error')} ">
	<label for="archetypeId">
		<g:message code="dataIndex.archetypeId.label" default="Archetype Id" />
		
	</label>
	<g:textField name="archetypeId" value="${dataIndexInstance?.archetypeId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: dataIndexInstance, field: 'path', 'error')} ">
	<label for="path">
		<g:message code="dataIndex.path.label" default="Path" />
		
	</label>
	<g:textField name="path" value="${dataIndexInstance?.path}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: dataIndexInstance, field: 'rmTypeName', 'error')} ">
	<label for="rmTypeName">
		<g:message code="dataIndex.rmTypeName.label" default="Rm Type Name" />
		
	</label>
	<g:textField name="rmTypeName" value="${dataIndexInstance?.rmTypeName}"/>
</div>

