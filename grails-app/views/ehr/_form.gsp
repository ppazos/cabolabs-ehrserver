<%@ page import="ehr.Ehr" %>



<div class="fieldcontain ${hasErrors(bean: ehrInstance, field: 'compositions', 'error')} ">
	<label for="compositions">
		<g:message code="ehr.compositions.label" default="Compositions" />
		
	</label>
	<g:select name="compositions" from="${support.identification.CompositionRef.list()}" multiple="multiple" optionKey="id" size="5" value="${ehrInstance?.compositions*.id}" class="many-to-many"/>
</div>

<div class="fieldcontain ${hasErrors(bean: ehrInstance, field: 'contributions', 'error')} ">
	<label for="contributions">
		<g:message code="ehr.contributions.label" default="Contributions" />
		
	</label>
	<g:select name="contributions" from="${support.identification.ContributionRef.list()}" multiple="multiple" optionKey="id" size="5" value="${ehrInstance?.contributions*.id}" class="many-to-many"/>
</div>

<div class="fieldcontain ${hasErrors(bean: ehrInstance, field: 'ehrId', 'error')} ">
	<label for="ehrId">
		<g:message code="ehr.ehrId.label" default="Ehr Id" />
		
	</label>
	<g:textField name="ehrId" value="${ehrInstance?.ehrId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: ehrInstance, field: 'subject', 'error')} required">
	<label for="subject">
		<g:message code="ehr.subject.label" default="Subject" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="subject" name="subject.id" from="${common.generic.PatientProxy.list()}" optionKey="id" required="" value="${ehrInstance?.subject?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: ehrInstance, field: 'systemId', 'error')} ">
	<label for="systemId">
		<g:message code="ehr.systemId.label" default="System Id" />
		
	</label>
	<g:textField name="systemId" value="${ehrInstance?.systemId}"/>
</div>

