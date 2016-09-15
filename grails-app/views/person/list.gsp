<%@ page import="com.cabolabs.ehrserver.openehr.demographic.Person" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><g:message code="person.list.title" /></title>
    <style>
     /* adjusts the filder input width */
     @media (min-width: 768px) {
      #ipt_fn, #ipt_ln, #ipt_ic {
       width: 100px;
      }
     }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="person.list.title" /></h1>
      </div>
    </div>
  
    <div class="row row-grid">
      <div class="col-md-8">
        <g:form class="form-inline" action="list">
          <input type="hidden" name="sort" value="${params.sort}" />
          <input type="hidden" name="order" value="${params.order}" />
          <div class="form-group">
            <label for="ipt_fn">First Name</label>
            <input type="text" class="form-control" name="firstName" id="ipt_fn" value="${params?.firstName}" />
          </div>
          <div class="form-group">
            <label for="ipt_ln">Last Name</label>
            <input type="text" class="form-control" name="lastName" id="ipt_ln" value="${params?.lastName}" />
          </div>
          <div class="form-group">
            <label for="ipt_ic">ID Code</label>
            <input type="text" class="form-control" name="idCode" id="ipt_ic" value="${params?.idCode}" />
          </div>
          <button type="submit" class="btn btn-default"><g:message code="common.action.filter" /></button>
        </g:form>
      </div>
      <div class="col-md-4">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="create">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="default.new.label" args="[entityName]" />
            </button>
          </g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
	     <g:if test="${flash.message}">
	       <div class="message" role="status">${flash.message}</div>
	     </g:if>
	     <%-- display validation errors for create ehr --%>
	     <g:if test="${ehr?.hasErrors}">
	       <g:renderErrors bean="${ehr}" as="list" />
		  </g:if>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
		        <thead>
		          <tr>
		            <g:sortableColumn property="firstName" title="${message(code: 'person.firstName.label', default: 'First Name')}" params="${params}" />
		            <g:sortableColumn property="lastName" title="${message(code: 'person.lastName.label', default: 'Last Name')}" params="${params}" />
		            <g:sortableColumn property="sex" title="${message(code: 'person.sex.label', default: 'Sex')}" params="${params}" />
		            <g:sortableColumn property="idCode" title="${message(code: 'person.idCode.label', default: 'Id Code')}" params="${params}" />
		            <g:sortableColumn property="idType" title="${message(code: 'person.idType.label', default: 'Id Type')}" params="${params}" />
		            <g:sortableColumn property="role" title="${message(code: 'person.role.label', default: 'Role')}" params="${params}" />
		            <g:sortableColumn property="dob" title="${message(code: 'person.dob.label', default: 'Dob')}" params="${params}" />
                  <g:sortableColumn property="organizationUid" title="${message(code: 'person.organization.label', default: 'Organization')}" params="${params}" />
		            <th>
		              Actions
		            </th>
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${personInstanceList}" status="i" var="personInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td><g:link action="show" id="${personInstance.uid.encodeAsHTML()}">${fieldValue(bean: personInstance, field: "firstName")}</g:link></td>
			            <td><g:link action="show" id="${personInstance.uid.encodeAsHTML()}">${fieldValue(bean: personInstance, field: "lastName")}</g:link></td>
			            <td>${fieldValue(bean: personInstance, field: "sex")}</td>
			            <td>${fieldValue(bean: personInstance, field: "idCode")}</td>
			            <td>${fieldValue(bean: personInstance, field: "idType")}</td>
			            <td>${fieldValue(bean: personInstance, field: "role")}</td>
			            <td><g:formatDate date="${personInstance.dob}" type="date" /></td>
                     <td><g:link controller="organization" action="show" id="${personInstance.organizationUid.encodeAsHTML()}">${fieldValue(bean: personInstance, field: "organizationUid")}</g:link></td>
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
	     <g:paginator total="${personInstanceTotal}" args="${params}" />
      </div>
    </div>
  </body>
</html>
