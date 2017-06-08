<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %><%@ page import="com.cabolabs.security.Role" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="user.show.title" /></title>
    <style>
      form {
        display: inline-block;
      }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
      <h1><g:message code="user.show.title" /></h1>
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
              <th><g:message code="user.attr.username" default="Username" /></th>
              <td><g:fieldValue bean="${userInstance}" field="username"/></td>
            </tr>
            <tr>
              <th><g:message code="user.attr.email" default="Email" /></th>
              <td><g:fieldValue bean="${userInstance}" field="email"/></td>
            </tr>
            <%--
            <tr>
              <th><g:message code="user.attr.organizations" default="Organizations" /></th>
              <td><g:select name="organizations" from="${userInstance.organizations}" optionValue="${{it.name +' ('+ it.uid +')'}}" optionKey="uid" size="5" class="form-control" disabled="disabled" /></td>
            </tr>
            --%>
            <tr>
              <th><g:message code="user.attr.organizations" default="Organizations" /></th>
              <td>
                <div class="table-responsive">
                  <table class="table table-striped table-bordered table-hover">
                    <thead>
                      <tr>
                        <th><g:message code="user.organizations.label" default="Organizations" /></th>
                        <g:each in="${Role.list()}" var="role">
                          <th>${role.authority}</th>
                        </g:each>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${roles}" status="i" var="roleOrg">
                        <g:set var="org" value="${roleOrg.key}" />
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                          <th>${org.name}</th>
                          <g:each in="${Role.list()}" var="role">
                            <td>
                              <input type="checkbox" name="${org.uid}" ${(userRoles?.find{ it.role == role && it.organization == org })?'checked="true"':''} value="${role.authority}" disabled="true" />
                            </td>
                          </g:each>
                        </tr>
                      </g:each>
                    
                      <%--
                      <g:each in="${roles}" status="i" var="role">
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                          <th>${role.authority}</th>
                          <g:each in="${organizations}" var="org">
                            <td>
                              <input type="checkbox" name="${role.authority}" ${(userRoles.find{ it.role == role && it.organization == org })?'checked="true"':''} value="${org.uid}" disabled="true" />
                            </td>
                          </g:each>
                        </tr>
                      </g:each>
                      --%>
                    </tbody>
                  </table>
                </div>
              </td>
            </tr>
            <tr>
              <th><g:message code="user.attr.account_expired" default="Account Expired" /></th>
              <td><g:formatBoolean boolean="${userInstance?.accountExpired}" /></td>
            </tr>
            <tr>
              <th><g:message code="user.attr.account_locked" default="Account Locked" /></th>
              <td><g:formatBoolean boolean="${userInstance?.accountLocked}" /></td>
            </tr>
            <tr>
              <th><g:message code="user.attr.enabled" default="Enabled" /></th>
              <td><g:formatBoolean boolean="${userInstance?.enabled}" /></td>
            </tr>
            <tr>
              <th><g:message code="user.attr.password_expired" default="Password Expired" /></th>
              <td><g:formatBoolean boolean="${userInstance?.passwordExpired}" /></td>
            </tr>
          </tbody>
        </table>
	     
	     <%-- if the user shown is admin, only can be edited if the logged user is admin (admins can edit any user)
	          if the user shown is not admin, only can be edited if the logged user is org admin
	    
	     <g:if test="${ SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') || (!userInstance.authoritiesContains('ROLE_ADMIN') && SpringSecurityUtils.ifAllGranted('ROLE_ORG_MANAGER')) || (userInstance.id == Long.valueOf(sec.loggedInUserInfo(field:'id').toString())) }">
		     --%>
		     <%--
		    <g:form url="[resource:userInstance, action:'delete']" method="DELETE">
		      <fieldset class="buttons">
		        <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
		      </fieldset>
		    </g:form>
		    --%>
		  <g:canEditUser userInstance="${userInstance}">
		    <g:link action="edit" resource="${userInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
	       <g:form action="resetPasswordRequest" id="${userInstance.id}" method="post">
	         <input type="hidden" name="email" value="${userInstance.email}" />
	         <button type="submit" class="btn btn-warning">${message(code: "springSecurity.reset.button")}</button>
	       </g:form>
	     </g:canEditUser>
	     
      </div>
    </div>
  </body>
</html>
