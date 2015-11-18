<%@ page import="com.cabolabs.security.Organization" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'organization.label', default: 'Organization')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
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
          <label><g:message code="organization.name.label" default="Name" /></label>
	       <div class="control"><g:fieldValue bean="${organizationInstance}" field="name"/></div>
	     </div>
	     <div class="control-group">
          <label><g:message code="organization.number.label" default="Number" /></label>
	       <div class="control"><g:fieldValue bean="${organizationInstance}" field="number"/></div>
	     </div>
	     <div class="control-group">
          <label><g:message code="organization.uid.label" default="Uid" /></label>
	       <div class="control"><g:fieldValue bean="${organizationInstance}" field="uid"/></div>
	     </div>
	     <g:form url="[resource:organizationInstance, action:'delete']" method="DELETE">
	       <fieldset class="buttons">
	         <g:link action="edit" resource="${organizationInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
	         <%--
	          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
	         --%>
	       </fieldset>
	     </g:form>
	   </div>
    </div>
  </body>
</html>
