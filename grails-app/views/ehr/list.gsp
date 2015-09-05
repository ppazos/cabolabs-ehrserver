<%@ page import="ehr.Ehr" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="admin">
		<g:set var="entityName" value="${message(code: 'ehr.label', default: 'Ehr')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div id="list-ehr" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
						<g:sortableColumn property="ehrId" title="${message(code: 'ehr.ehrId.label', default: 'Ehr Id')}" />
					   <g:sortableColumn property="dateCreated" title="${message(code: 'ehr.dateCreated.label', default: 'Date Created')}" />
						<th><g:message code="ehr.subject.label" default="Subject" /></th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${list}" status="i" var="ehrInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					   <td><g:link action="showEhr" params="[patientUID:ehrInstance.subject.value]">${fieldValue(bean: ehrInstance, field: "ehrId")}</g:link></td>
						<td>${fieldValue(bean: ehrInstance, field: "dateCreated")}</td>
						<td><g:link controller="person" action="show" params="[uid:ehrInstance.subject.value]">${ehrInstance.subject.value}</g:link></td>
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${total}" />
			</div>
		</div>
	</body>
</html>
