<%@ page import="com.cabolabs.security.Role" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}" />
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
          <label><g:message code="role.authority.label" default="Authority" /></label>
          <div class="control"><g:fieldValue bean="${roleInstance}" field="authority"/></div>
        </div>
        <g:form url="[resource:roleInstance, action:'delete']" method="DELETE">
          <fieldset class="buttons">
            <div class="btn-toolbar" role="toolbar">
              <g:link action="edit" resource="${roleInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
            </div>
            <%--
            <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
            --%>
          </fieldset>
        </g:form>
      </div>
    </div>
  </body>
</html>
