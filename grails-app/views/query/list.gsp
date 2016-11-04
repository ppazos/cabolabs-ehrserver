<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="query.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
	      <h1><g:message code="query.list.title" /></h1>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-md-8">
        <g:form class="form-inline" action="list">
          <input type="hidden" name="sort" value="${params.sort}" />
          <input type="hidden" name="order" value="${params.order}" />
          <div class="form-group">
            <label for="ipt_name"><g:message code="query.show.name.attr" /></label>
            <input type="text" class="form-control" name="name" id="ipt_name" value="${params?.name}" />
          </div>
          <button type="submit" class="btn btn-default"><g:message code="common.action.filter" /></button>
        </g:form>
      </div>
      <div class="col-md-4">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="create">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="common.action.create" />
            </button>
          </g:link>
        </div>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="message" role="status"><g:message code="${flash.message}" args="${flash.args}" /></div>
	      </g:if>
	      
         <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
		        <thead>
		          <tr>
		            <g:sortableColumn property="name" title="${message(code: 'query.show.name.attr', default: 'Name')}" />
		            <g:sortableColumn property="group" title="${message(code: 'query.show.group.attr', default: 'Group')}" />
		            <g:sortableColumn property="format" title="${message(code: 'query.show.format.attr', default: 'Format')}" />
		            <g:sortableColumn property="type" title="${message(code: 'query.show.type.attr', default: 'Type')}" />
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${queryInstanceList}" status="i" var="queryInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td><g:link action="show" params="[uid: queryInstance.uid]">${fieldValue(bean: queryInstance, field: "name")}</g:link></td>
			            <td>${fieldValue(bean: queryInstance, field: "group")}</td>
			            <td>${fieldValue(bean: queryInstance, field: "format")}</td>
			            <td>${fieldValue(bean: queryInstance, field: "type")}</td>
			          </tr>
			        </g:each>
		        </tbody>
		      </table>
		   </div>
	      <g:paginator total="${queryInstanceTotal}" args="${params}" />
      </div>
    </div>
  </body>
</html>
