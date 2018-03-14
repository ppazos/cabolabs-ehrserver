<%@ page import="com.cabolabs.security.Organization" %>
<html>
  <head>
    <style>
      #query_test_composition label, #query_test_common label {
        display: block;
      }
      #query_test_composition span, #query_test_common span {
        display: inline-block;
        width: 15%;
        text-align: right;
        padding-right: 1em;
        vertical-align: top;
      }
    </style>
  </head>
  <body>
    <g:if test="${flash.message}">
      <div class="alert alert-info" role="alert">${flash.message}</div>
    </g:if>

    <div id="query_test_composition">
      <h2><g:message code="query.test.search_documents" /></h2>
      <h3><g:message code="query.test.filters" /></h3>

      <div class="table-responsive">
        <table class="table table-striped table-bordered table-hover">
		    <tr>
		      <td>
              <g:message code="query.test.retrieve_data" />
            </td>
            <td>
              <select name="retrieveData" class="form-control input-sm">
                <option value="false" selected="selected"><g:message code="query.test.no" /></option>
                <option value="true"><g:message code="query.test.yes" /></option>
              </select>
            </td>
          </tr>
        </table>
      </div>
    </div><!-- test_by_composition -->

    <div id="query_test_datavalue">
      <h2><g:message code="query.test.search_data" /></h2>
      <h3><g:message code="query.test.filters" /></h3>
    </div><!-- query_test_datavalues -->

    <div id="query_test_common">
      <div class="table-responsive">
        <table class="table table-striped table-bordered table-hover">
		    <tr>
		      <td>
              <g:message code="query.test.ehr_id" />
            </td>
            <td>
              <g:select name="qehrId" from="${ehrs}" optionKey="uid" size="4" noSelection="${['':message(code:'defaut.select.selectOne')]}" class="form-control withsize" />
            </td>
          </tr>
          <tr>
		      <td>
              <g:message code="query.test.from" />
            </td>
            <td>
              <input type="text" name="fromDate" class="form-control input-sm" />
            </td>
          </tr>
          <tr>
		      <td>
              <g:message code="query.test.to" />
            </td>
            <td>
              <input type="text" name="toDate" class="form-control input-sm" />
            </td>
          </tr>
          <tr>
		      <td>
              <g:message code="query.test.composerUid" />
            </td>
            <td>
              <g:select from="${composerUids}" name="composerUid" class="form-control" noSelection="${['':message(code:'defaut.select.selectOne')]}" />
            </td>
          </tr>
          <tr>
		      <td>
              <g:message code="query.test.composerName" />
            </td>
            <td>
              <input type="text" name="composerName" class="form-control" />
            </td>
          </tr>
          <%--
          <tr>
            <td></td>
            <td>
              <sec:ifAnyGranted roles="ROLE_ADMIN">
                <g:select name="organizationUid" from="${Organization.list()}"
                          optionKey="uid" optionValue="name" class="form-control input-sm" />
              </sec:ifAnyGranted>
              <sec:ifNotGranted roles="ROLE_ADMIN">
                <g:selectWithCurrentUserOrganizations name="organizationUid" class="form-control input-sm" />
              </sec:ifNotGranted>
            </td>
          </tr>
          --%>
        </table>
      </div>
    </div>

    <div class="btn-toolbar" role="toolbar">
      <a href="javascript:void(0);" onclick="javascript:ajax_submit_test_or_save('test');" id="test_query">
        <button type="button" class="btn btn-default btn-md">
          <span class="fa fa-play fa-fw" aria-hidden="true"></span> <g:message code="default.button.execute.label" default="Execute" />
        </button>
      </a>
    </div>

    <h2><g:message code="query.execute.results" /></h2>
    <a href="#" id="show_data"><g:message code="query.execute.showData" /></a>
    <div id="results" class="out"></div>
    <pre><code id="code"></code></pre>
    <div id="chartContainer"></div>
  </body>
</html>
