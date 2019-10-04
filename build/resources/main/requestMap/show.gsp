<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="requestMap.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="requestMap.show.title" /></h1>
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
              <th><g:message code="requestMap.attr.url" default="URL" /></th>
              <td><g:fieldValue bean="${requestMapInstance}" field="url"/></td>
            </tr>
            <tr>
              <th><g:message code="requestMap.attr.configAttribute" default="Config Attribute" /></th>
              <td><g:fieldValue bean="${requestMapInstance}" field="configAttribute"/></td>
            </tr>
          </tbody>
        </table>

        <g:form url="[resource:requestMapInstance, action:'delete']" method="DELETE">
          <fieldset class="buttons">
            <div class="btn-toolbar" role="toolbar">
              <g:link action="edit" resource="${requestMapInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
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
