<%@ page import="com.cabolabs.security.Organization" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'organization.label', default: 'Organization')}" />
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
	             <g:sortableColumn property="name" title="${message(code: 'organization.name.label', default: 'Name')}" />
	             <g:sortableColumn property="number" title="${message(code: 'organization.number.label', default: 'Number')}" />
	             <g:sortableColumn property="uid" title="${message(code: 'organization.uid.label', default: 'Uid')}" />
	           </tr>
		      </thead>
		      <tbody>
		        <g:each in="${organizationInstanceList}" status="i" var="organizationInstance">
		          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		            <td><g:link action="show" id="${organizationInstance.id}">${fieldValue(bean: organizationInstance, field: "name")}</g:link></td>
		            <td>${fieldValue(bean: organizationInstance, field: "number")}</td>
		            <td>${fieldValue(bean: organizationInstance, field: "uid")}</td>
		          </tr>
		        </g:each>
	         </tbody>
	       </table>
	     </div>
	     <g:paginator total="${total}" />
	   </div>
	 </div>
  </body>
</html>
