<%@ page import="com.cabolabs.ehrserver.account.Account" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="account.show.title" /></title>
    <style>
      th:first-child { width: 30% ;}
      .help-block{ font-size: 0.8em; }
      .state-2 {
        background-color: #4185F3 !important;
        color: #fff;
      }
      .overdue-date {
        background-color: #F38541 !important;
      }
      td.left {
        text-align: left !important;
      }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
      <h1><g:message code="account.show.title" /></h1>
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
              <th><g:message code="account.attr.companyName" default="Company name" /></th>
              <td>${account?.companyName}</td>
            </tr>
            <tr>
              <th><g:message code="account.attr.contact" default="Contact" /></th>
              <td><g:link controller="user" action="show" id="${account?.contact?.id}">${account?.contact?.encodeAsHTML()}</g:link></td>
            </tr>
            <tr>
              <th><g:message code="account.attr.enabled" default="Enabled" /></th>
              <td><g:formatBoolean boolean="${account?.enabled}" /></td>
            </tr>
            <tr>
              <th>
                <g:message code="account.stats.organizations" default="Organizations" />
                <g:if test="${plan_max_orgs}">
                  ${account.organizations.size()} / ${plan_max_orgs}
                </g:if>
              </th>
              <td>
                <div class="table-responsive">
                  <table class="table table-striped table-bordered table-hover table-org-roles">
                    <thead>
                      <tr>
                        <th><g:message code="organization.name.label" default="Organization" /></th>
                        <th><g:message code="stats.org.storageUsagePercentage" default="Storage usage %" /></th>
                        <th><g:message code="stats.org.loadedTemplates" default="Loaded templates" /></th>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${account.organizations}" var="org">
                        <tr>
                          <td><g:link controller="organization" action="show" params="[uid: org.uid]">${org.name}</g:link></td>
                          <td><span id="stats-storage-${org.uid}"></span></td>
                          <td><span id="stats-opts-${org.uid}"></span></td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div>
              </td>
            </tr>
            <tr>
              <th><g:message code="plan.index.title" default="Plans" /></th>
              <td>
                <div class="table-responsive">
                  <table class="table table-striped table-bordered table-hover table-org-roles">
                    <thead>
                      <tr>
                        <th><g:message code="plan_association.attr.state" default="State" /></th>
                        <th><g:message code="plan.name.label" default="Name" /></th>
                        <th><g:message code="plan_association.attr.from" default="From" /></th>
                        <th><g:message code="plan_association.attr.to" default="To" /></th>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${account.allPlans}" var="plan_assoc">
                        <tr class="state-${plan_assoc.state}">
                          <td><g:message code="plan_association.state.${plan_assoc.state}" /></td>
                          <td>${plan_assoc.plan.name}</td>
                          <td><g:formatDate date="${plan_assoc.from}" format="yyyy-MM-dd HH:mm"/></td>
                          <td ${(plan_assoc.state == 2 && plan_assoc.to < new Date().clearTime()) ? 'class="overdue-date"' : ''}>
                            <g:formatDate date="${plan_assoc.to}" format="yyyy-MM-dd HH:mm"/>
                          </td>
                        </tr>
                        <tr>
                          <td colspan="4" class="left">
                            <ul>
                              <li>
                                <g:message code="plan.max_api_tokens_per_organization.label" default="Max API tokens per organization" />
                                ${plan_assoc.plan.max_api_tokens_per_organization}
                              </li>
                              <li>
                                <g:message code="plan.max_organizations.label" default="Max organizations" />
                                ${plan_assoc.plan.max_organizations}
                              </li>
                              <li>
                                <g:message code="plan.max_opts_per_organization.label" default="Max OPTs per organizations" />
                                ${plan_assoc.plan.max_opts_per_organization}
                              </li>
                              <li>
                                <g:message code="plan.repo_total_size_in_kb.label" default="Repository total size in KB" />
                                ${plan_assoc.plan.repo_total_size_in_kb}
                              </li>
                            </ul>
                          </td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div>
              </td>
            </tr>


          </tbody>
        </table>

        <div class="btn-toolbar" role="toolbar">
          <fieldset class="buttons">
            <g:link class="edit" action="edit" resource="${account}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
            <%-- TODO handle delete Account
            <g:link action="delete" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}');" params="[id: account.id]"><button type="button" class="btn btn-default btn-md"><span class="fa fa-trash-o fa-fw" aria-hidden="true"></span> <g:message code="default.button.delete.label" /></button></g:link>
            --%>
          </fieldset>
        </div>
      </div>
    </div>

    <script type="text/javascript">
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/round
    function precisionRound(number, precision) {
      var factor = Math.pow(10, precision);
      return Math.round(number * factor) / factor;
    }

    $(document).ready(function() {
      $.ajax({
        url: '${createLink(controller:"stats", action:"accountRepoUsage", id:account.id)}',
        method: 'GET',
        dataType: 'json'
      })
      .done( function(json) {
        console.log('stats', json);

        for (org_uid in json.usage)
        {
          if (json.max_repo_size == 0) json.max_repo_size = 1; // avoid div by 0
          percent = precisionRound( json.usage[org_uid] * 100 / json.max_repo_size, 1);

          $('#stats-storage-'+ org_uid).text(percent +'%').append(' <i class="fa fa-database" aria-hidden="true" title="${message(code:'account.stats.repo_usage')}"></i>');
        }
      });

      $.ajax({
        url: '${createLink(controller:"stats", action:"accountTemplatesLoaded", id:account.id)}',
        method: 'GET',
        dataType: 'json'
      })
      .done( function(json) {
        console.log('stats', json);

        for (org_uid in json.usage)
        {
          $('#stats-opts-'+ org_uid).text(json.usage[org_uid] +' / '+ json.max_opts_per_org).append(' <i class="fa fa-cubes" aria-hidden="true" title="${message(code:'account.stats.uploaded_opts')}"></i>');
        }
      });
    });
    </script>
  </body>
</html>
