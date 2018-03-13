<%@ page import="com.cabolabs.ehrserver.account.Plan" %>

<div class="form-group  ${hasErrors(bean: planInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="plan.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" value="${planInstance.name}" required="" class="form-control"/>
</div>

<div class="form-group  ${hasErrors(bean: planInstance, field: 'period', 'error')} required">
	<label for="period">
		<g:message code="plan.period.label" default="Period" />
		<span class="required-indicator">*</span>
	</label>
	<g:select name="period" from="${planInstance.constraints.period.inList}" required="" value="${fieldValue(bean: planInstance, field: 'period')}" valueMessagePrefix="plan.period" class="form-control"/>
</div>

<div class="form-group  ${hasErrors(bean: planInstance, field: 'max_api_tokens_per_organization', 'error')} required">
	<label for="max_api_tokens_per_organization">
		<g:message code="plan.max_api_tokens_per_organization.label" default="Max API tokens per organization" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="max_api_tokens_per_organization" type="number" value="${planInstance.max_api_tokens_per_organization}" required="" class="form-control"/>
</div>

<div class="form-group  ${hasErrors(bean: planInstance, field: 'max_organizations', 'error')} required">
	<label for="max_organizations">
		<g:message code="plan.max_organizations.label" default="Max organizations" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="max_organizations" type="number" value="${planInstance.max_organizations}" required="" class="form-control"/>
</div>

<div class="form-group  ${hasErrors(bean: planInstance, field: 'max_opts_per_organization', 'error')} required">
	<label for="max_opts_per_organization">
		<g:message code="plan.max_opts_per_organization.label" default="Max OPTs per organizations" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="max_opts_per_organization" type="number" required="" value="${planInstance?.max_opts_per_organization}" class="form-control"/>
</div>

<div class="form-group  ${hasErrors(bean: planInstance, field: 'repo_total_size_in_kb', 'error')} required">
	<label for="repo_total_size_in_kb">
		<g:message code="plan.repo_total_size_in_kb.label" default="Repository total size in KB" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="repo_total_size_in_kb" type="number" value="${planInstance.repo_total_size_in_kb}" required="" class="form-control"/>
</div>
