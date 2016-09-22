
<%@ page import="com.cabolabs.ehrserver.account.Plan" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'plan.label', default: 'Plan')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-plan" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-plan" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="period" title="${message(code: 'plan.period.label', default: 'Period')}" />
					
						<g:sortableColumn property="maxDocuments" title="${message(code: 'plan.maxDocuments.label', default: 'Max Documents')}" />
					
						<g:sortableColumn property="maxTransactions" title="${message(code: 'plan.maxTransactions.label', default: 'Max Transactions')}" />
					
						<g:sortableColumn property="name" title="${message(code: 'plan.name.label', default: 'Name')}" />
					
						<g:sortableColumn property="repositorySize" title="${message(code: 'plan.repositorySize.label', default: 'Repository Size')}" />
					
						<g:sortableColumn property="totalRepositorySize" title="${message(code: 'plan.totalRepositorySize.label', default: 'Total Repository Size')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${planInstanceList}" status="i" var="planInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${planInstance.id}">${fieldValue(bean: planInstance, field: "period")}</g:link></td>
					
						<td>${fieldValue(bean: planInstance, field: "maxDocuments")}</td>
					
						<td>${fieldValue(bean: planInstance, field: "maxTransactions")}</td>
					
						<td>${fieldValue(bean: planInstance, field: "name")}</td>
					
						<td>${fieldValue(bean: planInstance, field: "repositorySize")}</td>
					
						<td>${fieldValue(bean: planInstance, field: "totalRepositorySize")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${planInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
