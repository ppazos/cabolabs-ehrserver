<%@ page import="com.cabolabs.ehrserver.account.Account" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="account.index.title" /></title>
  </head>
  <body>

    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="account.index.title" /></h1>
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
               <th><g:message code="account.attr.companyName" default="Company name" /></th>
               <th><g:message code="account.attr.contact" default="Contact" /></th>
               <g:sortableColumn property="enabled" title="${message(code: 'account.attr.enabled', default: 'Enabled')}" />
             </tr>
            </thead>
            <tbody>
              <g:each in="${accounts}" status="i" var="accountInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td><g:link action="show" id="${accountInstance.id}">${accountInstance.companyName}</g:link></td>
                  <td>${accountInstance.contact}</td>
                  <td><g:formatBoolean boolean="${accountInstance.enabled}" /></td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
      <%--
      <div class="pagination">
        <g:paginate total="${accountInstanceCount ?: 0}" />
      </div>
      --%>
      </div>
    </div>
  </body>
</html>
