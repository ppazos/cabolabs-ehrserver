<%@ page import="com.cabolabs.ehrserver.identification.PersonIdType" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'personIdType.label', default: 'PersonIdType')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
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
        <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      </div>
    </div>
      
    <div class="row">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
	       <div class="message" role="status">${flash.message}</div>
	     </g:if>
	     
        <div class="control-group">
          <label class="control-label"><g:message code="personIdTypeInstance.name.label" default="Name" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personIdTypeInstance}" field="name"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="personIdTypeInstance.code.label" default="Code" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${personIdTypeInstance}" field="code"/></p>
          </div>
        </div>

	     <div class="btn-toolbar" role="toolbar">
	       <g:link class="edit" action="edit" resource="${personIdTypeInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
	       <g:link action="delete" id="${personIdTypeInstance?.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
	         <button type="button" class="btn btn-default btn-md">
	           <span class="fa fa-minus-circle fa-fw" aria-hidden="true"></span> <g:message code="default.button.delete.label" default="Delete" />
	         </button>
	       </g:link>
	     </div>
	   </div>
	 </div>
  </body>
</html>
