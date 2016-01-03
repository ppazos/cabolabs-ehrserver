<%@ page import="com.cabolabs.security.User" %><%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
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
	       <label><g:message code="user.username.label" default="Username" /></label>
	       <div><g:fieldValue bean="${userInstance}" field="username"/></div>
	     </div>
	     <div class="control-group">
          <label><g:message code="user.email.label" default="Email" /></label>
          <div><g:fieldValue bean="${userInstance}" field="email"/></div>
        </div>
	     <div class="control-group">
	       <label><g:message code="user.organizations.label" default="Organizations" /></label>
	       <div>
	         <g:select name="organizations" from="${userInstance.organizations}" optionValue="${{it.name +' ('+ it.uid +')'}}" optionKey="uid" size="5" />
	       </div>
	     </div>
	     <div class="control-group">
	       <label><g:message code="user.accountExpired.label" default="Account Expired" /></label>
	       <div><g:formatBoolean boolean="${userInstance?.accountExpired}" /></div>
	     </div>
	     <div class="control-group">
	       <label><g:message code="user.accountLocked.label" default="Account Locked" /></label>
	       <div><g:formatBoolean boolean="${userInstance?.accountLocked}" /></div>
	     </div>
	     <div class="control-group">
	       <label><g:message code="user.enabled.label" default="Enabled" /></label>
	       <div><g:formatBoolean boolean="${userInstance?.enabled}" /></div>
	     </div>
	     <div class="control-group">
	       <label><g:message code="user.passwordExpired.label" default="Password Expired" /></label>
	       <div><g:formatBoolean boolean="${userInstance?.passwordExpired}" /></div>
	     </div>
	     
	     <%-- if the user shown is admin, only can be edited if the logged user is admin (admins can edit any user)
	          if the user shown is not admin, only can be edited if the logged user is org admin
	     --%>
	     <g:if test="${ SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') || (!userInstance.authoritiesContains('ROLE_ADMIN') && SpringSecurityUtils.ifAllGranted('ROLE_ORG_MANAGER')) || (userInstance.id == Long.valueOf(sec.loggedInUserInfo(field:'id').toString())) }">
		     <g:form url="[resource:userInstance, action:'delete']" method="DELETE">
		       <fieldset class="buttons">
		         <g:link action="edit" resource="${userInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
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
