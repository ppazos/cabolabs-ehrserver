<html>
  <head>
    <%-- Modal, doesnt have layout, used from query/create to test queries before creation --%>
    <asset:javascript src="query_test_and_execution.js" />
  </head>
  <body>
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>
    
    <div id="query_test_composition">
      
      <h2><g:message code="query.test.search_documents" /></h2>

      <h3><g:message code="query.test.filters" /></h3>
      <table>
        <tr>
          <td><g:message code="query.test.retrieve_data" /></td>
          <td>
            <select name="retrieveData">
              <option value="false" selected="selected"><g:message code="query.test.no" /></option>
              <option value="true"><g:message code="query.test.yes" /></option>
            </select>
          </td>
        </tr>
      </table>
    </div><!-- test_by_composition -->

    <div id="query_test_datavalue">
      <h2><g:message code="query.test.search_data" /></h2>
      <h3><g:message code="query.test.filters" /></h3>
    </div><!-- query_test_datavalues -->
    
    <div id="query_test_common">
      <label>
        <span><g:message code="query.test.ehr_id" /></span>
        <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" />
      </label><br />
      <label>
        <span><g:message code="query.test.from" /></span>
        <input type="text" name="fromDate" />
      </label><br />
      <label>
        <span><g:message code="query.test.to" /></span>
        <input type="text" name="toDate" /><br />
      </label><br />
      <!-- FIXME: busco los arquetipos de composition en los indices porque
                  el EHRServer aun no tiene repositorio de arquetipos. Cuando lo
                  tenga, esta operacion deberia usar el ArchetypeManager. -->

      <!-- solo arquetipos de composition -->
      <label>
        <span><g:message code="query.test.document_type" /></span>
        <g:select name="qarchetypeId" size="4"
                  from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }" />
      </label>
    </div>
    
    <h2><g:message code="query.execute.results" /></h2>
    <a href="#" id="show_data"><g:message code="query.execute.showData" /></a>
    <div id="results" class="out"></div>
    <pre><code id="code"></code></pre>
    <div id="chartContainer"></div>
    
    <style>
      #query_test_common label {
        display: block;
      }
      #query_test_common span {
        display: inline-block;
        width: 22%;
        text-align: right;
        padding-right: 1em;
        vertical-align: top;
      }
    </style>
  </body>
</html>
