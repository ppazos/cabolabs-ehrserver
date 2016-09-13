<%@ page import="com.cabolabs.security.Organization" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="organization.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="organization.show.title" /></h1>
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
              <th><g:message code="organization.name.label" default="Name" /></th>
              <td><g:fieldValue bean="${organizationInstance}" field="name"/></td>
            </tr>
            <tr>
              <th><g:message code="organization.number.label" default="Number" /></th>
              <td><g:fieldValue bean="${organizationInstance}" field="number"/></td>
            </tr>
            <tr>
              <th><g:message code="organization.uid.label" default="UID" /></th>
              <td><g:fieldValue bean="${organizationInstance}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="organization.ehrs.label" default="EHRS" /></th>
              <td><g:link controller="ehr" action="list" params="[organizationUid:organizationInstance.uid]"><g:message code="common.action.display" /></g:link></td>
            </tr>
          </tbody>
        </table>
        
        <div class="btn-toolbar" role="toolbar">
          <g:form url="[resource:organizationInstance, action:'delete']" method="DELETE">
            <fieldset class="buttons">
              <g:link action="edit" id="${organizationInstance.uid}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
              <%--
              <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
              --%>
            </fieldset>
          </g:form>
        </div>
	   </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
        <h2><g:message code="organization.show.stats" /></h2>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12">
        <g:include controller="stats" action="organization"
                   params="[uid: organizationInstance.uid]" />
      </div>
    </div>
  </body>
</html>
