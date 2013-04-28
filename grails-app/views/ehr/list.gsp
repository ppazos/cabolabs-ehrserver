
<%@ page import="ehr.Ehr" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'ehr.label', default: 'Ehr')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-ehr" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
<<<<<<< HEAD
				<!--
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			   -->
=======
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
			</ul>
		</div>
		<div id="list-ehr" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="dateCreated" title="${message(code: 'ehr.dateCreated.label', default: 'Date Created')}" />
					
						<g:sortableColumn property="ehrId" title="${message(code: 'ehr.ehrId.label', default: 'Ehr Id')}" />
					
						<th><g:message code="ehr.subject.label" default="Subject" /></th>
					
						<g:sortableColumn property="systemId" title="${message(code: 'ehr.systemId.label', default: 'System Id')}" />
					
					</tr>
				</thead>
				<tbody>
<<<<<<< HEAD
				<g:each in="${list}" status="i" var="ehrInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="showEhr" params="[patientUID:ehrInstance.subject.value]">${fieldValue(bean: ehrInstance, field: "dateCreated")}</g:link></td>
=======
				<g:each in="${ehrInstanceList}" status="i" var="ehrInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${ehrInstance.id}">${fieldValue(bean: ehrInstance, field: "dateCreated")}</g:link></td>
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
					
						<td>${fieldValue(bean: ehrInstance, field: "ehrId")}</td>
					
						<td>${fieldValue(bean: ehrInstance, field: "subject")}</td>
					
						<td>${fieldValue(bean: ehrInstance, field: "systemId")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
<<<<<<< HEAD
				<g:paginate total="${total}" />
=======
				<g:paginate total="${ehrInstanceTotal}" />
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
			</div>
		</div>
	</body>
</html>
