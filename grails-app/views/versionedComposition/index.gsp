
<%@ page import="common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'versionedComposition.label', default: 'VersionedComposition')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-versionedComposition" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-versionedComposition" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="ehrUid" title="${message(code: 'versionedComposition.ehrUid.label', default: 'Ehr Uid')}" />
					
						<g:sortableColumn property="isPersistent" title="${message(code: 'versionedComposition.isPersistent.label', default: 'Is Persistent')}" />
					
						<g:sortableColumn property="timeCreated" title="${message(code: 'versionedComposition.timeCreated.label', default: 'Time Created')}" />
					
						<g:sortableColumn property="uid" title="${message(code: 'versionedComposition.uid.label', default: 'Uid')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${versionedCompositionInstanceList}" status="i" var="versionedCompositionInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${versionedCompositionInstance.id}">${fieldValue(bean: versionedCompositionInstance, field: "ehrUid")}</g:link></td>
					
						<td><g:formatBoolean boolean="${versionedCompositionInstance.isPersistent}" /></td>
					
						<td><g:formatDate date="${versionedCompositionInstance.timeCreated}" /></td>
					
						<td>${fieldValue(bean: versionedCompositionInstance, field: "uid")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${versionedCompositionInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
