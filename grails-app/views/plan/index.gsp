<%@ page import="com.cabolabs.ehrserver.account.Plan" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="admin">
    <title><g:message code="plan.index.title" /></title>
	</head>
	<body>
		<div class="row">
      <div class="col-lg-12">
        <h1><g:message code="plan.index.title" /></h1>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <button type="button" class="btn btn-default btn-md filter" data-toggle="collapse" href="#collapse-filter">
            <span class="fa fa-filter" aria-hidden="true"></span>
          </button>
          <g:link action="create">
            <button type="button" class="btn btn-primary btn-md">
              <span class="fa fa-plus" aria-hidden="true"></span>
            </button>
          </g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid">
			<div class="col-lg-12">
				<g:if test="${flash.message}">
					<div class="alert alert-info" role="alert">${flash.message}</div>
				</g:if>
				<div class="table-responsive">
					<table class="table table-striped table-bordered table-hover">
						<thead>
							<tr>
							  <g:sortableColumn property="name" title="${message(code: 'plan.name.label', default: 'Name')}" />
								<g:sortableColumn property="period" title="${message(code: 'plan.period.label', default: 'Period')}" />
								<g:sortableColumn property="max_api_tokens_per_organization" title="${message(code: 'plan.max_api_tokens_per_organization.label', default: 'Max API Tolens per organization')}" />
								<g:sortableColumn property="max_opts_per_organization" title="${message(code: 'plan.max_opts_per_organization.label', default: 'Max OPTs per organization')}" />
								<g:sortableColumn property="max_organizations" title="${message(code: 'plan.max_organizations.label', default: 'Max organizations')}" />
								<g:sortableColumn property="repo_total_size_in_kb" title="${message(code: 'plan.repo_total_size_in_kb.label', default: 'Repository total size in KB')}" />
							</tr>
						</thead>
						<tbody>
					    <g:each in="${planInstanceList}" status="i" var="planInstance">
								<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
									<td><g:link action="show" id="${planInstance.id}">${fieldValue(bean: planInstance, field: "name")}</g:link></td>
									<td><g:message code="plan.period.${planInstance.period}" /></td>
									<td>${fieldValue(bean: planInstance, field: "max_api_tokens_per_organization")}</td>
									<td>${fieldValue(bean: planInstance, field: "max_opts_per_organization")}</td>
									<td>${fieldValue(bean: planInstance, field: "max_organizations")}</td>
									<td>${fieldValue(bean: planInstance, field: "repo_total_size_in_kb")}</td>
								</tr>
							</g:each>
						</tbody>
					</table>
			  </div>
			</div>
		</div>
	</body>
</html>
