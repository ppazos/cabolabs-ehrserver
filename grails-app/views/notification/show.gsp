<%@ page import="com.cabolabs.ehrserver.notification.Notification" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="notification.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1 class="hidden_uid"><g:message code="notification.show.title" /></h1>
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
              <th><g:message code="notification.list.attr.name" /></th>
              <td><g:fieldValue bean="${notificationInstance}" field="name"/></td>
            </tr>
            <tr>
              <th><g:message code="notification.list.attr.language" /></th>
              <td>${notificationInstance.language}</td>
            </tr>
            <tr>
              <th><g:message code="notification.list.attr.text" /></th>
              <td><g:fieldValue bean="${notificationInstance}" field="text"/></td>
            </tr>
            <tr>
              <th><g:message code="notification.list.attr.forOrganization" /></th>
              <td>
                <g:if test="${notificationInstance.forOrganization}">
                  <g:link controller="organization" action="show" id="${notificationInstance.forOrganization}"><g:fieldValue bean="${notificationInstance}" field="forOrganization"/></g:link>
                </g:if>
              </td>
            </tr>
            <tr>
              <th><g:message code="notification.list.attr.forSection" /></th>
              <td>
                <g:if test="${notificationInstance.forSection}">
                  <g:fieldValue bean="${notificationInstance}" field="forSection"/>
                </g:if>
              </td>
            </tr>
            <tr>
              <th><g:message code="notification.list.attr.forUser" /></th>
              <td>
                <g:if test="${notificationInstance.forUser}">
                  <g:fieldValue bean="${notificationInstance}" field="forUser"/>
                </g:if>
              </td>
            </tr>
            <tr>
              <th><g:message code="notification.list.attr.dateCreated" /></th>
              <td><g:formatDate date="${notificationInstance?.dateCreated}" /></td>
            </tr>
          </tbody>
        </table>

        <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
             <thead>
               <tr>
                 <th>status</th>
                 <th>user</th>
               </tr>
             </thead>
             <tbody>
               <g:each in="${statuses}" var="status">
                 <tr>
                   <td>${status.status}</td>
                   <td>${status.user}</td>
                 </tr>
               </g:each>
             </tbody>
            </table>
         </div>
         
        <%--
        <g:form url="[resource:notificationInstance, action:'delete']" method="DELETE">
          <fieldset class="buttons">
            <g:link class="edit" action="edit" resource="${notificationInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
            <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
          </fieldset>
        </g:form>
        --%>
      </div>
    </div>
  </body>
</html>
