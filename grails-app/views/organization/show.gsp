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
	     <div id="show-organization" class="content scaffold-show" role="main">
	      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
	      <g:if test="${flash.message}">
	        <div class="message" role="status">${flash.message}</div>
	      </g:if>
	      <ol class="property-list organization">
	      
	        <g:if test="${organizationInstance?.name}">
	        <li class="fieldcontain">
	          <span id="name-label" class="property-label"><g:message code="organization.name.label" default="Name" /></span>
	          <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${organizationInstance}" field="name"/></span>
	        </li>
	        </g:if>
	      
	        <g:if test="${organizationInstance?.number}">
	        <li class="fieldcontain">
	          <span id="pin-label" class="property-label"><g:message code="organization.number.label" default="Number" /></span>
	          <span class="property-value" aria-labelledby="number-label"><g:fieldValue bean="${organizationInstance}" field="number"/></span>
	        </li>
	        </g:if>
	      
	        <g:if test="${organizationInstance?.uid}">
	        <li class="fieldcontain">
	          <span id="uid-label" class="property-label"><g:message code="organization.uid.label" default="Uid" /></span>
	          <span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${organizationInstance}" field="uid"/></span>
	        </li>
	        </g:if>
	      </ol>
	      <g:form url="[resource:organizationInstance, action:'delete']" method="DELETE">
	        <fieldset class="buttons">
	          <g:link class="edit" action="edit" resource="${organizationInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
	          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
	        </fieldset>
	      </g:form>
	     </div>
      </div>
    </div>
  </body>
</html>
