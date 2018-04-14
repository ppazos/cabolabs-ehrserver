<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="requestMap.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="requestMap.list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <div class="btn-toolbar" requestMap="toolbar">
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
	       <div class="alert alert-info" requestMap="alert">${flash.message}</div>
	     </g:if>
	     <div class="table-responsive">
	        <table class="table table-striped table-bordered table-hover">
		       <thead>
		         <tr>
		           <g:sortableColumn property="url" title="${message(code: 'requestMap.attr.url', default: 'URL')}" />
		           <g:sortableColumn property="configAttribute" title="${message(code: 'requestMap.attr.configAttribute', default: 'Config Attribute')}" />
		         </tr>
		       </thead>
		       <tbody>
		         <g:each in="${requestMapInstanceList}" status="i" var="requestMapInstance">
		           <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		             <td><g:link action="show" id="${requestMapInstance.id}">${fieldValue(bean: requestMapInstance, field: "url")}</g:link></td>
                   <td>${fieldValue(bean: requestMapInstance, field: "configAttribute")}</td>
		           </tr>
		         </g:each>
		       </tbody>
		     </table>
		  </div>
	     <g:paginator total="${requestMapInstanceCount}" args="${params}" />
      </div>
    </div>
  </body>
</html>
