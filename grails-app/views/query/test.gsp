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
      
      <h2>B&uacute;queda de documentos</h2>

      <h3>Filtros</h3>
      <table>
        <tr>
          <td>retrieve data?</td>
          <td>
            <select name="retrieveData">
              <option value="false" selected="selected">no</option>
              <option value="true">yes</option>
            </select>
          </td>
        </tr>
      </table>
    </div><!-- test_by_composition -->

    <div id="query_test_datavalue">
      <h2>B&uacute;queda de datos</h2>
      <h3>Filtros</h3>
    </div><!-- query_test_datavalues -->
    
    <div id="query_test_common">
      ehrId <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" /><br />
      from <input type="text" name="fromDate" />
      to <input type="text" name="toDate" /><br />
      
      <!-- FIXME: busco los arquetipos de composition en los indices porque
                  el EHRServer aun no tiene repositorio de arquetipos. Cuando lo
                  tenga, esta operacion deberia usar el ArchetypeManager. -->

      <!-- solo arquetipos de composition -->
      document type <g:select name="qarchetypeId" size="4"
                              from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }" />
    </div>
    
    <h2><g:message code="query.execute.results" /></h2>
    <a href="#" id="show_data"><g:message code="query.execute.showData" /></a>
    <div id="results" class="out"></div>
    <pre><code id="code"></code></pre>
    <div id="chartContainer"></div>
  </body>
</html>
