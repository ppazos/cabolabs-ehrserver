<%@ page import="com.cabolabs.security.Role" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="create">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="default.new.label" args="[entityName]" />
            </button>
          </g:link>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="default.list.label" args="[entityName]" /></h1>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12">
	     <g:if test="${flash.message}">
	       <div class="message" role="status">${flash.message}</div>
	     </g:if>
	     <div class="table-responsive">
	        <table class="table table-striped table-bordered table-hover">
		       <thead>
		         <tr>
		           <g:sortableColumn property="authority" title="${message(code: 'role.authority.label', default: 'Authority')}" />
		         </tr>
		       </thead>
		       <tbody>
		         <g:each in="${roleInstanceList}" status="i" var="roleInstance">
		           <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		             <td><g:link action="show" id="${roleInstance.id}">${fieldValue(bean: roleInstance, field: "authority")}</g:link></td>
		           </tr>
		         </g:each>
		       </tbody>
		     </table>
		  </div>
	     <g:paginator total="${roleInstanceCount}" />
      </div>
    </div>
  </body>
</html>
