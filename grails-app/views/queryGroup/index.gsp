<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="queryGroup.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="queryGroup.list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="createGroup">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus fa-fw" aria-hidden="true"></span>
            </button></g:link>
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
                <g:sortableColumn property="templateId" title="${message(code: 'queryGroup.attr.uid')}" />
                <g:sortableColumn property="concept" title="${message(code: 'queryGroup.attr.name')}" />
                <g:sortableColumn property="language" title="${message(code: 'queryGroup.attr.organizationUid')}" />
              </tr>
            </thead>
            <tbody>
              <g:each in="${groups}" status="i" var="queryGroupInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td>
                    <g:link action="show" params="[uid:queryGroupInstance.uid]" id="${queryGroupInstance.id}">${fieldValue(bean: queryGroupInstance, field: "uid")}</g:link>
                  </td>
                  <td>${fieldValue(bean: queryGroupInstance, field: "name")}</td>
                  <td>${fieldValue(bean: queryGroupInstance, field: "organizationUid")}</td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </body>
</html>
