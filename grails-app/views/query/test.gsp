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
      ehrId <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" /><br />
      <g:message code="query.test.from" /> <input type="text" name="fromDate" />
      <g:message code="query.test.to" /> <input type="text" name="toDate" /><br />
      
      <!-- FIXME: busco los arquetipos de composition en los indices porque
                  el EHRServer aun no tiene repositorio de arquetipos. Cuando lo
                  tenga, esta operacion deberia usar el ArchetypeManager. -->

      <!-- solo arquetipos de composition -->
      <g:message code="query.test.document_type" />
      <g:select name="qarchetypeId" size="4"
                from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }" />
    </div>
    
    <h2><g:message code="query.execute.results" /></h2>
    <a href="#" id="show_data"><g:message code="query.execute.showData" /></a>
    <div id="results" class="out"></div>
    <pre><code id="code"></code></pre>
    <div id="chartContainer"></div>
  </body>
</html>
