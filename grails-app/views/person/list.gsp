<%@ page import="demographic.Person" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-person" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-person" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					   <g:sortableColumn property="firstName" title="${message(code: 'person.firstName.label', default: 'First Name')}" />
						<g:sortableColumn property="sex" title="${message(code: 'person.sex.label', default: 'Sex')}" />
						<g:sortableColumn property="idCode" title="${message(code: 'person.idCode.label', default: 'Id Code')}" />
						<g:sortableColumn property="idType" title="${message(code: 'person.idType.label', default: 'Id Type')}" />
						<g:sortableColumn property="role" title="${message(code: 'person.role.label', default: 'Role')}" />
						<g:sortableColumn property="dob" title="${message(code: 'person.dob.label', default: 'Dob')}" />
					  <th>
					    Actions
					  </th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${personInstanceList}" status="i" var="personInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					   <td><g:link action="show" id="${personInstance.id}">${fieldValue(bean: personInstance, field: "firstName")} ${fieldValue(bean: personInstance, field: "lastName")}</g:link></td>
						<td>${fieldValue(bean: personInstance, field: "sex")}</td>
						<td>${fieldValue(bean: personInstance, field: "idCode")}</td>
						<td>${fieldValue(bean: personInstance, field: "idType")}</td>
						<td>${fieldValue(bean: personInstance, field: "role")}</td>
						<td><g:formatDate date="${personInstance.dob}" /></td>
					   <td>
					     <g:hasEhr patientUID="${personInstance.uid}">
                      <g:link controller="ehr" action="showEhr" params="[patientUID: personInstance.uid]">Show EHR</g:link>
                    </g:hasEhr>
					     <g:dontHasEhr patientUID="${personInstance.uid}">
					       <g:link controller="ehr" action="createEhr" params="[patientUID: personInstance.uid]">Create EHR</g:link>
					     </g:dontHasEhr>
					   </td>
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${personInstanceTotal}" />
			</div>
		</div>
	</body>
</html>