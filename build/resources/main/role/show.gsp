<%@ page import="com.cabolabs.security.Role" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="role.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="role.show.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
      
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="role.attr.authority" default="Authority" /></th>
              <td><g:fieldValue bean="${roleInstance}" field="authority"/></td>
            </tr>
          </tbody>
        </table>

        <g:if test="${!Role.coreRoles().contains(roleInstance.authority)}">
        
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
        </g:if>
      </div>
    </div>
  </body>
</html>
