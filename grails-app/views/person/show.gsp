<%@ page import="demographic.Person" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link class="create" action="create">
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
	      <ol class="property-list person">
	      
	        <g:if test="${personInstance?.sex}">
	        <li class="fieldcontain">
	          <span id="sex-label" class="property-label"><g:message code="person.sex.label" default="Sex" /></span>
	          
	          <span class="property-value" aria-labelledby="sex-label"><g:fieldValue bean="${personInstance}" field="sex"/></span>
	          
	        </li>
	        </g:if>
	      
	        <g:if test="${personInstance?.idCode}">
	        <li class="fieldcontain">
	          <span id="idCode-label" class="property-label"><g:message code="person.idCode.label" default="Id Code" /></span>
	          
	          <span class="property-value" aria-labelledby="idCode-label"><g:fieldValue bean="${personInstance}" field="idCode"/></span>
	          
	        </li>
	        </g:if>
	      
	        <g:if test="${personInstance?.idType}">
	        <li class="fieldcontain">
	          <span id="idType-label" class="property-label"><g:message code="person.idType.label" default="Id Type" /></span>
	          
	          <span class="property-value" aria-labelledby="idType-label"><g:fieldValue bean="${personInstance}" field="idType"/></span>
	          
	        </li>
	        </g:if>
	      
	        <g:if test="${personInstance?.role}">
	        <li class="fieldcontain">
	          <span id="role-label" class="property-label"><g:message code="person.role.label" default="Role" /></span>
	          
	          <span class="property-value" aria-labelledby="role-label"><g:fieldValue bean="${personInstance}" field="role"/></span>
	          
	        </li>
	        </g:if>
	      
	        <g:if test="${personInstance?.dob}">
	        <li class="fieldcontain">
	          <span id="dob-label" class="property-label"><g:message code="person.dob.label" default="Dob" /></span>
	          
	          <span class="property-value" aria-labelledby="dob-label"><g:formatDate date="${personInstance?.dob}" /></span>
	          
	        </li>
	        </g:if>
	      
	        <g:if test="${personInstance?.firstName}">
	        <li class="fieldcontain">
	          <span id="firstName-label" class="property-label"><g:message code="person.firstName.label" default="First Name" /></span>
	          
	          <span class="property-value" aria-labelledby="firstName-label"><g:fieldValue bean="${personInstance}" field="firstName"/></span>
	          
	        </li>
	        </g:if>
	      
	        <g:if test="${personInstance?.lastName}">
	        <li class="fieldcontain">
	          <span id="lastName-label" class="property-label"><g:message code="person.lastName.label" default="Last Name" /></span>
	          
	          <span class="property-value" aria-labelledby="lastName-label"><g:fieldValue bean="${personInstance}" field="lastName"/></span>
	          
	        </li>
	        </g:if>
	      
	        <g:if test="${personInstance?.uid}">
	        <li class="fieldcontain">
	          <span id="uid-label" class="property-label"><g:message code="person.uid.label" default="Uid" /></span>
	          
	          <span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${personInstance}" field="uid"/></span>
	          
	        </li>
	        </g:if>
	      
	      </ol>
	      
	      
	      
         <div class="btn-toolbar" role="toolbar">
	        <g:link class="edit" action="edit" id="${personInstance?.id}">
	          <button type="button" class="btn btn-default btn-md">
	            <span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" />
	          </button>
	        </g:link>
	      </div>

      </div>
    </div>
  </body>
</html>
