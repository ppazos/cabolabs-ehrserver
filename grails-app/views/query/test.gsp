<html>
  <head>
    <%-- Modal, doesnt have layout, used from query/create to test queries before creation --%>
    <asset:javascript src="query_test_and_execution.js" />
    
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
      <div class="message" role="status">${flash.message}</div>
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
              <select name="retrieveData">
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
              <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" noSelection="${['':'Select One...']}" />
            </td>
          </tr>
          <tr>
		      <td>
              <g:message code="query.test.from" />
            </td>
            <td>
              <input type="text" name="fromDate" />
            </td>
          </tr>
          <tr>
		      <td>
              <g:message code="query.test.to" />
            </td>
            <td>
              <input type="text" name="toDate" />
            </td>
          </tr>
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
