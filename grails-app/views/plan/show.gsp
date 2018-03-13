<%@ page import="com.cabolabs.ehrserver.account.Plan" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="admin">
    <title><g:message code="plan.show.title" /></title>
	</head>
	<body>
		<div class="row">
      <div class="col-lg-12">
      <h1><g:message code="plan.show.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
	     <g:if test="${flash.message}">
	       <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>

				<table class="table">
					<tbody>
						<tr>
							<th><g:message code="plan.name.label" default="Name" /></th>
							<td>${planInstance?.name}</td>
						</tr>
						<tr>
							<th><g:message code="plan.period.label" default="Period" /></th>
							<td><g:message code="plan.period.${planInstance.period}" /></td>
						</tr>
						<tr>
							<th><g:message code="plan.max_api_tokens_per_organization.label" default="Max API tokens per organization" /></th>
							<td>${planInstance?.max_api_tokens_per_organization}</td>
						</tr>
						<tr>
							<th><g:message code="plan.max_organizations.label" default="Max organizations" /></th>
							<td>${planInstance?.max_organizations}</td>
						</tr>
						<tr>
							<th><g:message code="plan.max_opts_per_organization.label" default="Max OPTs per organizations" /></th>
							<td>${planInstance?.max_opts_per_organization}</td>
						</tr>
						<tr>
							<th><g:message code="plan.repo_total_size_in_kb.label" default="Repository total size in KB" /></th>
							<td>${planInstance?.repo_total_size_in_kb}</td>
						</tr>
					</tbody>
				</table>

				<div class="btn-toolbar" role="toolbar">

					<g:form url="[resource:planInstance, action:'delete']" method="DELETE">
						<fieldset class="buttons">
							<g:link class="edit" action="edit" resource="${planInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
							<%--
							<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
							--%>
						</fieldset>
					</g:form>
			  </div>
			</div>
		</div>
	</body>
</html>
