<%@ page import="com.cabolabs.ehrserver.account.Plan" %>



<div class="fieldcontain ${hasErrors(bean: planInstance, field: 'period', 'error')} required">
	<label for="period">
		<g:message code="plan.period.label" default="Period" />
		<span class="required-indicator">*</span>
	</label>
	<g:select name="period" from="${planInstance.constraints.period.inList}" required="" value="${fieldValue(bean: planInstance, field: 'period')}" valueMessagePrefix="plan.period"/>

</div>

<div class="fieldcontain ${hasErrors(bean: planInstance, field: 'maxDocuments', 'error')} required">
	<label for="maxDocuments">
		<g:message code="plan.maxDocuments.label" default="Max Documents" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="maxDocuments" type="number" value="${planInstance.maxDocuments}" required=""/>

</div>

<div class="fieldcontain ${hasErrors(bean: planInstance, field: 'maxTransactions', 'error')} required">
	<label for="maxTransactions">
		<g:message code="plan.maxTransactions.label" default="Max Transactions" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="maxTransactions" type="number" value="${planInstance.maxTransactions}" required=""/>

</div>

<div class="fieldcontain ${hasErrors(bean: planInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="plan.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${planInstance?.name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: planInstance, field: 'repositorySize', 'error')} required">
	<label for="repositorySize">
		<g:message code="plan.repositorySize.label" default="Repository Size" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="repositorySize" type="number" value="${planInstance.repositorySize}" required=""/>

</div>

<div class="fieldcontain ${hasErrors(bean: planInstance, field: 'totalRepositorySize', 'error')} required">
	<label for="totalRepositorySize">
		<g:message code="plan.totalRepositorySize.label" default="Total Repository Size" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="totalRepositorySize" type="number" value="${planInstance.totalRepositorySize}" required=""/>

</div>

