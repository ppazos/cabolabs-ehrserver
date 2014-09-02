<%@ page import="query.Query" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<title><g:message code="query.list.title" /></title>
	</head>
	<body>
		<a href="#list-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="query.create.title" /></g:link></li>
			</ul>
		</div>
		<div id="list-query" class="content scaffold-list" role="main">
			<h1><g:message code="query.list.title" /></h1>
			<g:if test="${flash.message}">
			  <div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
						<g:sortableColumn property="name" title="${message(code: 'query.name.label', default: 'Name')}" />
					
						<g:sortableColumn property="group" title="${message(code: 'query.group.label', default: 'Group')}" />
					<%-- wont use this for now
						<g:sortableColumn property="qarchetypeId" title="${message(code: 'query.qarchetypeId.label', default: 'Qarchetype Id')}" />
					--%>
						<g:sortableColumn property="format" title="${message(code: 'query.format.label', default: 'Format')}" />
					
						<g:sortableColumn property="type" title="${message(code: 'query.type.label', default: 'Type')}" />
					</tr>
				</thead>
				<tbody>
				<g:each in="${queryInstanceList}" status="i" var="queryInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td><g:link action="show" id="${queryInstance.id}">${fieldValue(bean: queryInstance, field: "name")}</g:link></td>
					
						<td>${fieldValue(bean: queryInstance, field: "group")}</td>
					<%-- wont use this for now
						<td>${fieldValue(bean: queryInstance, field: "qarchetypeId")}</td>
					--%>
						<td>${fieldValue(bean: queryInstance, field: "format")}</td>
					
						<td>${fieldValue(bean: queryInstance, field: "type")}</td>
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${queryInstanceTotal}" />
			</div>
		</div>
	</body>
</html>