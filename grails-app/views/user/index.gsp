<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="user.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="user.list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-md-6">
        <g:form class="form-inline" action="index">
          <input type="hidden" name="sort" value="${params.sort}" />
          <input type="hidden" name="order" value="${params.order}" />
          <div class="form-group">
            <label for="ipt_un"><g:message code="user.attr.username" /></label>
            <input type="text" class="form-control" name="username" id="ipt_un" value="${params?.username}" />
          </div>
          <button type="submit" class="btn btn-default"><g:message code="common.action.filter" /></button>
        </g:form>
      </div>
      <div class="col-md-6">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="create">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="common.action.create" />
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
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
		      <thead>
	           <tr>
	             <g:sortableColumn property="username" title="${message(code: 'user.attr.username', default: 'Username')}" />
	             <g:sortableColumn property="accountExpired" title="${message(code: 'user.attr.account_expired', default: 'Account Expired')}" />
	             <g:sortableColumn property="accountLocked" title="${message(code: 'user.attr.account_locked', default: 'Account Locked')}" />
	             <g:sortableColumn property="enabled" title="${message(code: 'user.attr.enabled', default: 'Enabled')}" />
	             <g:sortableColumn property="passwordExpired" title="${message(code: 'user.attr.password_expired', default: 'Password Expired')}" />
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
	     <g:paginator total="${userInstanceCount}" args="${params}" />
      </div>
    </div>
  </body>
</html>
