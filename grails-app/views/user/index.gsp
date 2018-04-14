<%@ page import="com.cabolabs.security.Organization" %>
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
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <button type="button" class="btn btn-default btn-md filter" data-toggle="collapse" href="#collapse-filter">
            <span class="fa fa-filter" aria-hidden="true"></span>
          </button>
          <g:link action="create">
            <button type="button" class="btn btn-primary btn-md">
              <span class="fa fa-plus" aria-hidden="true"></span>
            </button>
          </g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid collapse" id="collapse-filter">
      <div class="col-md-12">
        <div class="panel panel-default">
          <div class="panel-body">
            <g:form class="form filter" action="index">
              <input type="hidden" name="sort" value="${params.sort}" />
              <input type="hidden" name="order" value="${params.order}" />
              <div class="form-group">
                <label for="ipt_un"><g:message code="user.attr.username" /></label>
                <input type="text" class="form-control" name="username" id="ipt_un" value="${params?.username}" />
              </div>
              <sec:ifAnyGranted roles="ROLE_ADMIN">
              <div class="form-group">
                <label for="organizationUid"><g:message code="entity.organization" /></label>
                <g:select name="organizationUid" from="${Organization.list()}"
				              optionKey="uid" optionValue="name"
				              noSelection="${['':message(code:'defaut.select.selectOne')]}"
                          value="${params?.organizationUid ?: ''}" class="form-control" />
              </div>
              </sec:ifAnyGranted>
              <div class="btn-toolbar" role="toolbar">
                <button type="submit" name="filter" class="btn btn-primary"><span class="fa fa-share" aria-hidden="true"></span></button>
                <button type="reset" id="filter-reset" class="btn btn-default"><span class="fa fa-trash " aria-hidden="true"></span></button>
              </div>
            </g:form>
          </div>
        </div>
      </div>
    </div>
    <script>
    // avoids waiting to load the whole page to show the filters, that makes the page do an unwanted jump.
    if (${params.containsKey('filter')})
    {
      $("#collapse-filter").addClass('in');
      $(".btn.filter").toggleClass( "btn-primary" );
    }
    </script>

    <div class="row row-grid">
      <div class="col-lg-12">
	     <g:if test="${flash.message}">
	       <div class="alert alert-info" role="alert">${flash.message}</div>
	     </g:if>

        <g:each in="${userInstanceList.groupBy{ it.organization }}" var="orgUserRoles">
          <h2>${orgUserRoles.key.name}</h2>
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
                <g:each in="${orgUserRoles.value}" status="i" var="userRole">
                  <g:set var="userInstance" value="${userRole.user}" />
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
        </g:each>
	     <g:paginator total="${userInstanceCount}" args="${params}" />
      </div>
    </div>
  </body>
</html>
