
<%@ page import="ehr.clinical_documents.CompositionIndex" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'compositionIndex.label', default: 'CompositionIndex')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-compositionIndex" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-compositionIndex" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list compositionIndex">
			
				<g:if test="${compositionIndexInstance?.category}">
				<li class="fieldcontain">
					<span id="category-label" class="property-label"><g:message code="compositionIndex.category.label" default="Category" /></span>
					
						<span class="property-value" aria-labelledby="category-label"><g:fieldValue bean="${compositionIndexInstance}" field="category"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${compositionIndexInstance?.startTime}">
				<li class="fieldcontain">
					<span id="startTime-label" class="property-label"><g:message code="compositionIndex.startTime.label" default="Start Time" /></span>
					
						<span class="property-value" aria-labelledby="startTime-label"><g:formatDate date="${compositionIndexInstance?.startTime}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${compositionIndexInstance?.archetypeId}">
				<li class="fieldcontain">
					<span id="archetypeId-label" class="property-label"><g:message code="compositionIndex.archetypeId.label" default="Archetype Id" /></span>
					
						<span class="property-value" aria-labelledby="archetypeId-label"><g:fieldValue bean="${compositionIndexInstance}" field="archetypeId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${compositionIndexInstance?.ehrId}">
				<li class="fieldcontain">
					<span id="ehrId-label" class="property-label"><g:message code="compositionIndex.ehrId.label" default="Ehr Id" /></span>
					
						<span class="property-value" aria-labelledby="ehrId-label"><g:fieldValue bean="${compositionIndexInstance}" field="ehrId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${compositionIndexInstance?.subjectId}">
				<li class="fieldcontain">
					<span id="subjectId-label" class="property-label"><g:message code="compositionIndex.subjectId.label" default="Subject Id" /></span>
					
						<span class="property-value" aria-labelledby="subjectId-label"><g:fieldValue bean="${compositionIndexInstance}" field="subjectId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${compositionIndexInstance?.uid}">
				<li class="fieldcontain">
					<span id="uid-label" class="property-label"><g:message code="compositionIndex.uid.label" default="Uid" /></span>
					
						<span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${compositionIndexInstance}" field="uid"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${compositionIndexInstance?.id}" />
					<g:link class="edit" action="edit" id="${compositionIndexInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
