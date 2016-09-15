<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="person.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="person.show.title" /></h1>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
      
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div>
        </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="person.uid.label" default="UID" /></th>
              <td><g:fieldValue bean="${personInstance}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="person.firstName.label" default="First Name" /></th>
              <td><g:fieldValue bean="${personInstance}" field="firstName"/></td>
            </tr>
            <tr>
              <th><g:message code="person.lastName.label" default="Last Name" /></th>
              <td><g:fieldValue bean="${personInstance}" field="lastName"/></td>
            </tr>
            <tr>
              <th><g:message code="person.dob.label" default="Dob" /></th>
              <td><g:formatDate date="${personInstance?.dob}" type="date" /></td>
            </tr>
            <tr>
              <th><g:message code="person.sex.label" default="Sex" /></th>
              <td><g:fieldValue bean="${personInstance}" field="sex"/></td>
            </tr>
            <tr>
              <th><g:message code="person.idCode.label" default="Id Code" /></th>
              <td><g:fieldValue bean="${personInstance}" field="idCode"/></td>
            </tr>
            <tr>
              <th><g:message code="person.idType.label" default="Id Type" /></th>
              <td><g:fieldValue bean="${personInstance}" field="idType"/></td>
            </tr>
            <tr>
              <th><g:message code="person.role.label" default="Role" /></th>
              <td><g:fieldValue bean="${personInstance}" field="role"/></td>
            </tr>
            <tr>
              <th><g:message code="person.organization.label" default="Organization" /></th>
              <td><g:link controller="organization" action="show" id="${personInstance.organizationUid}"><g:fieldValue bean="${personInstance}" field="organizationUid"/></g:link></td>
            </tr>
          </tbody>
        </table>
        
        <div class="btn-toolbar" role="toolbar">
          <g:link action="edit" id="${personInstance?.uid.encodeAsHTML()}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
        </div>
      </div>
    </div>
  </body>
</html>
