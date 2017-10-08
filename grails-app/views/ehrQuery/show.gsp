<%@ page import="com.cabolabs.ehrserver.query.EhrQuery" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="ehrquery.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1 class="hidden_uid"><g:message code="ehrquery.show.title" /></h1>
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
              <th><g:message code="ehrquery.attr.name" /></th>
              <td>${ehrQueryInstance.name}</td>
            </tr>
             <tr>
              <th><g:message code="ehrquery.attr.description" /></th>
              <td>${ehrQueryInstance.description}</td>
            </tr>
            <tr>
              <th><g:message code="ehrquery.attr.queries" /></th>
              <td>
                <g:each in="${ehrQueryInstance.queries}" var="q">
                  <span class="property-value" aria-labelledby="queries-label">
                    <g:link controller="query" action="show" params="[uid: q.uid]">${q?.encodeAsHTML()}</g:link>
                  </span>
                </g:each>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </body>
</html>
