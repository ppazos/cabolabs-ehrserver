<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.optItems.title" /></title>
  </head>
  <body>    
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="opt.optItems.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link class="list" controller="operationalTemplate" action="list">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-list-alt fa-fw" aria-hidden="true"></span> <g:message code="opt.list.title" />
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
		            <g:sortableColumn property="templateId" title="${message(code: 'template.templateId.label', default: 'templateId')}" params="[uid: templateInstance.uid]" />
		            <g:sortableColumn property="path" title="${message(code: 'template.path.label', default: 'path')}" params="[uid: templateInstance.uid]" />
		            <g:sortableColumn property="rmTypeName" title="${message(code: 'template.rmTypeName.label', default: 'rmTypeName')}" params="[uid: templateInstance.uid]" />
		            <g:sortableColumn property="name" title="${message(code: 'template.name.label', default: 'name')}" params="[uid: templateInstance.uid]" />
		            <g:sortableColumn property="terminologyRef" title="${message(code: 'template.terminologyRef.label', default: 'terminologyRef')}" params="[uid: templateInstance.uid]" />
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${items}" status="i" var="item">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td>${fieldValue(bean: item, field: "templateId")}</td>
			            <td>${fieldValue(bean: item, field: "path")}</td>
			            <td>${fieldValue(bean: item, field: "rmTypeName")}</td>
			            <td>${fieldValue(bean: item, field: "name")}</td>
			            <td>${fieldValue(bean: item, field: "terminologyRef")}</td>
			          </tr>
			        </g:each>
		        </tbody>
		      </table>
		  </div>
      </div>
    </div>
  </body>
</html>