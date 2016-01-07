<%@ page import="com.cabolabs.ehrserver.query.Query" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
    <title><g:message code="query.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="create">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="query.create.title" />
            </button>
          </g:link>
        </div>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
	      <h1><g:message code="query.list.title" /></h1>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="message" role="status"><g:message code="${flash.message}" args="${flash.args}" /></div>
	      </g:if>
	      
         <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
		        <thead>
		          <tr>
		            <g:sortableColumn property="name" title="${message(code: 'query.name.label', default: 'Name')}" />
		            <g:sortableColumn property="group" title="${message(code: 'query.group.label', default: 'Group')}" />
		            <g:sortableColumn property="format" title="${message(code: 'query.format.label', default: 'Format')}" />
		            <g:sortableColumn property="type" title="${message(code: 'query.type.label', default: 'Type')}" />
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${queryInstanceList}" status="i" var="queryInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td><g:link action="show" id="${queryInstance.id}">${fieldValue(bean: queryInstance, field: "name")}</g:link></td>
			            <td>${fieldValue(bean: queryInstance, field: "group")}</td>
			            <td>${fieldValue(bean: queryInstance, field: "format")}</td>
			            <td>${fieldValue(bean: queryInstance, field: "type")}</td>
			          </tr>
			        </g:each>
		        </tbody>
		      </table>
		   </div>
	      <g:paginator total="${queryInstanceTotal}" />
      </div>
    </div>
  </body>
</html>