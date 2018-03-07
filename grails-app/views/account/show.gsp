<%@ page import="com.cabolabs.ehrserver.account.Account" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="account.show.title" /></title>
    <style>
      th:first-child { width: 30% ;}
      .help-block{ font-size: 0.8em; }
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

        var org_count = Object.keys(json.usage).length;
        var i = 0;
        for (org_uid in json.usage)
        {
          percent = precisionRound( json.usage[org_uid] * 100 / json.max_repo_size, 1);

          $('#stats-storage-'+ org_uid).text(percent +'%').append(' <i class="fa fa-database" aria-hidden="true" title="${message(code:'account.stats.repo_usage')}"></i>');

          i++;
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
