<%@ page import="com.cabolabs.ehrserver.notification.Notification" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="notifications.index.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="notifications.index.title" /></h1>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="create">
            <button type="button" class="btn btn-primary btn-md">
              <span class="fa fa-plus" aria-hidden="true"></span>
            </button>
          </g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="message alert alert-warning" role="status">${flash.message}</div>
        </g:if>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
		      <thead>
		        <tr>
		          <g:sortableColumn property="name" title="${message(code: 'notification.name.label', default: 'Name')}" />
		          <g:sortableColumn property="forSection" title="${message(code: 'notification.forSection.label', default: 'For Section')}" />
		          <g:sortableColumn property="forOrganization" title="${message(code: 'notification.forOrganization.label', default: 'For Organization')}" />
		          <g:sortableColumn property="forUser" title="${message(code: 'notification.forUser.label', default: 'For User')}" />
		          <g:sortableColumn property="dateCreated" title="${message(code: 'notification.dateCreated.label', default: 'Date Created')}" />
		          <g:sortableColumn property="language" title="${message(code: 'notification.language.label', default: 'Language')}" />
		        </tr>
		      </thead>
		      <tbody>
		        <g:each in="${notificationInstanceList}" status="i" var="notificationInstance">
		          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		            <td><g:link action="show" id="${notificationInstance.id}">${fieldValue(bean: notificationInstance, field: "name")}</g:link></td>
		            <td>${fieldValue(bean: notificationInstance, field: "forSection")}</td>
		            <td>${fieldValue(bean: notificationInstance, field: "forOrganization")}</td>
		            <td>${fieldValue(bean: notificationInstance, field: "forUser")}</td>
		            <td><g:formatDate date="${notificationInstance.dateCreated}" /></td>
		            <td>${fieldValue(bean: notificationInstance, field: "language")}</td>
		          </tr>
              </g:each>
            </tbody>
          </table>
        </div>
        <g:paginator total="${total}" args="${params}" />
      </div>
    </div>
  </body>
</html>
