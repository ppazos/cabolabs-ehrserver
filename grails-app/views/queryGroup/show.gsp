<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="queryGroup.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1 class="hidden_uid"><g:message code="queryGroup.show.title" /></h1>
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
              <th><g:message code="queryGroup.attr.uid" /></th>
              <td>${queryGroupInstance.uid}</td>
            </tr>
            <tr>
              <th><g:message code="queryGroup.attr.name" /></th>
              <td>${queryGroupInstance.name}</td>
            </tr>
             <tr>
              <th><g:message code="queryGroup.attr.organizationUid" /></th>
              <td>${queryGroupInstance.organizationUid}</td>
            </tr>
          </tbody>
        </table>
        
        <div class="btn-toolbar" role="toolbar">
          <fieldset class="buttons">
            <g:link action="editGroup" params="[uid:queryGroupInstance.uid]"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
          </fieldset>
        </div>
        
      </div>
    </div>
  </body>
</html>
