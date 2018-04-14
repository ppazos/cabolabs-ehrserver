<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="folderTemplate.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="folderTemplate.list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
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
		          <g:sortableColumn property="name" title="${message(code: 'folderTemplate.attr.name')}" />
		          <g:sortableColumn property="description" title="${message(code: 'folderTemplate.attr.description')}" />
		        </tr>
		      </thead>
		      <tbody>
			     <g:each in="${list}" status="i" var="folderTemplate">
			       <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			         <td><g:link action="show" id="${folderTemplate.id}">${fieldValue(bean: folderTemplate, field: "name")}</g:link></td>
			         <td>${fieldValue(bean: folderTemplate, field: "description")}</td>
			       </tr>
			     </g:each>
		      </tbody>
		    </table>
        </div>
	     <g:paginator total="${total}" args="${params}" />
      </div>
    </div>
  </body>
</html>
