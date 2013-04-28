
<%@ page import="common.change_control.Contribution" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'contribution.label', default: 'Contribution')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-contribution" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-contribution" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list contribution">
			
				<g:if test="${contributionInstance?.audit}">
				<li class="fieldcontain">
					<span id="audit-label" class="property-label"><g:message code="contribution.audit.label" default="Audit" /></span>
					
						<span class="property-value" aria-labelledby="audit-label"><g:link controller="auditDetails" action="show" id="${contributionInstance?.audit?.id}">${contributionInstance?.audit?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
				<g:if test="${contributionInstance?.uid}">
				<li class="fieldcontain">
					<span id="uid-label" class="property-label"><g:message code="contribution.uid.label" default="Uid" /></span>
					
						<span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${contributionInstance}" field="uid"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${contributionInstance?.versions}">
				<li class="fieldcontain">
					<span id="versions-label" class="property-label"><g:message code="contribution.versions.label" default="Versions" /></span>
					
						<g:each in="${contributionInstance.versions}" var="v">
						<span class="property-value" aria-labelledby="versions-label"><g:link controller="version" action="show" id="${v.id}">${v?.encodeAsHTML()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${contributionInstance?.id}" />
					<g:link class="edit" action="edit" id="${contributionInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
