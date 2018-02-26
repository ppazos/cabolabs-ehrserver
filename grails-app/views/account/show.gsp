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
              <th><g:message code="account.attr.contact" default="Contact" /></th>
              <td><g:link controller="user" action="show" id="${account?.contact?.id}">${account?.contact?.encodeAsHTML()}</g:link></td>
            </tr>
            <tr>
              <th><g:message code="account.attr.enabled" default="Enabled" /></th>
              <td><g:formatBoolean boolean="${account?.enabled}" /></td>
            </tr>
            <tr>
              <th>
                <g:message code="account.stats.repo_usage" default="Repository usage" />
                <span class="help-block"><g:message code="account.stats.lowUsageOrganizationsAreNotShown" /></span>
              </th>
              <td>
                <div id="account_stats"></div>
              </td>
            </tr>
            <tr>
              <th>
                <g:message code="account.stats.organizations" default="Organizations" />
                <g:if test="${plan_max_orgs}">
                  ${account.organizations.size()} / ${plan_max_orgs}
                </g:if>
              </th>
              <td>
                <ul>
                  <g:each in="${account.organizations}" var="org">
                    <li>${org.name}</li>
                  </g:each>
                </ul>
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

        var classes = ['success', 'info', 'warning', 'danger'];
        var bar = $('<div class="progress"></div>');
        var org_count = Object.keys(json.usage).length;
        var i = 0;
        for (org_name in json.usage)
        {
          percent = precisionRound( json.usage[org_name] * 100 / json.max_repo_size, 1);

          // do not display if usage is too low to show
          if (percent >= 5)
          {
            org_bar = '<div class="progress-bar progress-bar-'+ classes[i%org_count] +'" style="width: '+ percent +'%">'+ org_name +'</div>';
            bar.append( org_bar );
          }

          i++;
        }

        $('#account_stats').append(bar);
      });
    });
    </script>

        <%-- TODO: show organizations
        <g:if test="${account?.organizations}">
        <ol>
        <li class="fieldcontain">
          <span id="organizations-label" class="property-label"><g:message code="account.organizations.label" default="Organizations" /></span>

            <g:each in="${account.organizations}" var="o">
            <span class="property-value" aria-labelledby="organizations-label"><g:link controller="organization" action="show" id="${o.id}">${o?.encodeAsHTML()}</g:link></span>
            </g:each>

        </li>
        </ol>
        </g:if>
        --%>

  </body>
</html>
