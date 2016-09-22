
<%@ page import="com.cabolabs.ehrserver.account.Plan" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'plan.label', default: 'Plan')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-plan" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-plan" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list plan">
			
				<g:if test="${planInstance?.period}">
				<li class="fieldcontain">
					<span id="period-label" class="property-label"><g:message code="plan.period.label" default="Period" /></span>
					
						<span class="property-value" aria-labelledby="period-label"><g:fieldValue bean="${planInstance}" field="period"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${planInstance?.maxDocuments}">
				<li class="fieldcontain">
					<span id="maxDocuments-label" class="property-label"><g:message code="plan.maxDocuments.label" default="Max Documents" /></span>
					
						<span class="property-value" aria-labelledby="maxDocuments-label"><g:fieldValue bean="${planInstance}" field="maxDocuments"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${planInstance?.maxTransactions}">
				<li class="fieldcontain">
					<span id="maxTransactions-label" class="property-label"><g:message code="plan.maxTransactions.label" default="Max Transactions" /></span>
					
						<span class="property-value" aria-labelledby="maxTransactions-label"><g:fieldValue bean="${planInstance}" field="maxTransactions"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${planInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="plan.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${planInstance}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${planInstance?.repositorySize}">
				<li class="fieldcontain">
					<span id="repositorySize-label" class="property-label"><g:message code="plan.repositorySize.label" default="Repository Size" /></span>
					
						<span class="property-value" aria-labelledby="repositorySize-label"><g:fieldValue bean="${planInstance}" field="repositorySize"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${planInstance?.totalRepositorySize}">
				<li class="fieldcontain">
					<span id="totalRepositorySize-label" class="property-label"><g:message code="plan.totalRepositorySize.label" default="Total Repository Size" /></span>
					
						<span class="property-value" aria-labelledby="totalRepositorySize-label"><g:fieldValue bean="${planInstance}" field="totalRepositorySize"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form url="[resource:planInstance, action:'delete']" method="DELETE">
				<fieldset class="buttons">
					<g:link class="edit" action="edit" resource="${planInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
