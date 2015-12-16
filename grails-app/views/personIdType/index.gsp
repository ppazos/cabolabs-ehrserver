<%@ page import="com.cabolabs.ehrserver.identification.PersonIdType" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'personIdType.label', default: 'PersonIdType')}" />
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
		          <g:sortableColumn property="name" title="${message(code: 'personIdType.name.label', default: 'Name')}" />
		          <g:sortableColumn property="code" title="${message(code: 'personIdType.code.label', default: 'Code')}" />
		        </tr>
		      </thead>
		      <tbody>
		        <g:each in="${personIdTypeInstanceList}" status="i" var="personIdTypeInstance">
		          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		            <td>${personIdTypeInstance.name}</td>
		            <td>${personIdTypeInstance.code}</td>
		          </tr>
		        </g:each>
		      </tbody>
		    </table>
	     </div>
	     <g:paginator total="${personIdTypeInstanceCount}" />
	   </div>
	 </div>
  </body>
</html>
