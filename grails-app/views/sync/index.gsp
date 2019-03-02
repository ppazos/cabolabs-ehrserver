<%@ page import="com.cabolabs.ehrserver.account.Account" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="sync.index.title" /></title>
  </head>
  <body>

    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="sync.index.title" /></h1>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
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
               <th><g:message code="sync.attr.systemId" /></th>
               <th><g:message code="sync.attr.token" /></th>
             </tr>
            </thead>
            <tbody>
              <g:each in="${keys}" status="i" var="key">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td>${key.systemId}</td>
                  <td>
                    <textarea width="100%" rows="5" class="form-control">${key.token}</textarea>
                  </td>
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

    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="sync.index.cluster" /></h1>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="createRemote">
            <button type="button" class="btn btn-primary btn-md">
              <span class="fa fa-plus" aria-hidden="true"></span>
            </button>
          </g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
            <thead>
             <tr>
               <th><g:message code="remote.attr.remoteServerName" /></th>
               <th><g:message code="remote.attr.isActive" /></th>
               <th></th>
             </tr>
            </thead>
            <tbody>
              <g:each in="${remotes}" status="i" var="remote">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td>${remote.remoteServerName}</td>
                  <td>${remote.isActive}</td>
                  <td class="text-right">
                    <g:link action="editRemote" id="${remote.id}">
                      <button type="button" class="btn btn-primary btn-md">
                        <span class="fa fa-pencil" aria-hidden="true"></span>
                      </button>
                    </g:link>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </body>
</html>
