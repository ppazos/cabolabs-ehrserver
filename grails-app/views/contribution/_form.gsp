<%@ page import="common.change_control.Contribution" %>



<div class="fieldcontain ${hasErrors(bean: contributionInstance, field: 'audit', 'error')} required">
	<label for="audit">
		<g:message code="contribution.audit.label" default="Audit" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="audit" name="audit.id" from="${common.generic.AuditDetails.list()}" optionKey="id" required="" value="${contributionInstance?.audit?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: contributionInstance, field: 'uid', 'error')} ">
	<label for="uid">
		<g:message code="contribution.uid.label" default="Uid" />
		
	</label>
	<g:textField name="uid" value="${contributionInstance?.uid}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: contributionInstance, field: 'versions', 'error')} ">
	<label for="versions">
		<g:message code="contribution.versions.label" default="Versions" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${contributionInstance?.versions?}" var="v">
    <li><g:link controller="version" action="show" id="${v.id}">${v?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="version" action="create" params="['contribution.id': contributionInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'version.label', default: 'Version')])}</g:link>
</li>
</ul>

</div>

