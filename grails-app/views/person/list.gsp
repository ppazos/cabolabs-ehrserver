<%@ page import="com.cabolabs.ehrserver.openehr.demographic.Person" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><g:message code="person.list.title" /></title>
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
		  <h1><g:message code="person.list.title" /></h1>
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
		            <g:sortableColumn property="firstName" title="${message(code: 'person.firstName.label', default: 'First Name')}" />
		            <g:sortableColumn property="sex" title="${message(code: 'person.sex.label', default: 'Sex')}" />
		            <g:sortableColumn property="idCode" title="${message(code: 'person.idCode.label', default: 'Id Code')}" />
		            <g:sortableColumn property="idType" title="${message(code: 'person.idType.label', default: 'Id Type')}" />
		            <g:sortableColumn property="role" title="${message(code: 'person.role.label', default: 'Role')}" />
		            <g:sortableColumn property="dob" title="${message(code: 'person.dob.label', default: 'Dob')}" />
                  <g:sortableColumn property="organizationUid" title="${message(code: 'person.organization.label', default: 'Organization')}" />
		            <th>
		              Actions
		            </th>
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${personInstanceList}" status="i" var="personInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td><g:link action="show" id="${personInstance.id}">${fieldValue(bean: personInstance, field: "firstName")} ${fieldValue(bean: personInstance, field: "lastName")}</g:link></td>
			            <td>${fieldValue(bean: personInstance, field: "sex")}</td>
			            <td>${fieldValue(bean: personInstance, field: "idCode")}</td>
			            <td>${fieldValue(bean: personInstance, field: "idType")}</td>
			            <td>${fieldValue(bean: personInstance, field: "role")}</td>
			            <td><g:formatDate date="${personInstance.dob}" /></td>
                     <td>${fieldValue(bean: personInstance, field: "organizationUid")}</td>
			            <td>
			              <g:if test="${personInstance.role == 'pat'}">
			                 <g:hasEhr patientUID="${personInstance.uid}">
			                   <g:link controller="ehr" action="showEhr" params="[patientUID: personInstance.uid]">Show EHR</g:link>
			                 </g:hasEhr>
			                 <g:dontHasEhr patientUID="${personInstance.uid}">
			                   <g:link controller="ehr" action="createEhr" params="[patientUID: personInstance.uid]">Create EHR</g:link>
			                 </g:dontHasEhr>
			               </g:if>
			             </td>
			          </tr>
			        </g:each>
		        </tbody>
		      </table>
		  </div>
	     <g:paginator total="${personInstanceTotal}" />
      </div>
    </div>
  </body>
</html>
