<%@ page import="com.cabolabs.security.User" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
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
        <h1><g:message code="default.list.label" args="[entityName]" /></h1>
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
	             <g:sortableColumn property="username" title="${message(code: 'user.username.label', default: 'Username')}" />
	             <g:sortableColumn property="accountExpired" title="${message(code: 'user.accountExpired.label', default: 'Account Expired')}" />
	             <g:sortableColumn property="accountLocked" title="${message(code: 'user.accountLocked.label', default: 'Account Locked')}" />
	             <g:sortableColumn property="enabled" title="${message(code: 'user.enabled.label', default: 'Enabled')}" />
	             <g:sortableColumn property="passwordExpired" title="${message(code: 'user.passwordExpired.label', default: 'Password Expired')}" />
	           </tr>
		      </thead>
		      <tbody>
		        <g:each in="${userInstanceList}" status="i" var="userInstance">
		          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		            <td><g:link action="show" id="${userInstance.id}">${fieldValue(bean: userInstance, field: "username")}</g:link></td>
		            <td><g:formatBoolean boolean="${userInstance.accountExpired}" /></td>
		            <td><g:formatBoolean boolean="${userInstance.accountLocked}" /></td>
		            <td><g:formatBoolean boolean="${userInstance.enabled}" /></td>
		            <td><g:formatBoolean boolean="${userInstance.passwordExpired}" /></td>
		          </tr>
		        </g:each>
	         </tbody>
	       </table>
	     </div>
	     <g:paginator total="${userInstanceCount}" />
      </div>
    </div>
  </body>
</html>
