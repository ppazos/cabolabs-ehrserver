<%@ page import="com.cabolabs.security.User" %><%@ page import="com.cabolabs.security.Role" %><%@ page import="com.cabolabs.security.Organization" %>

<input type="hidden" name="type" value="${params.type}" />

<div class="form-group ${hasErrors(bean: userInstance, field: 'username', 'error')} required">
	<label for="username"><g:message code="user.username.label" default="Username" /><span class="required-indicator">*</span></label>
	<g:textField name="username" required="" value="${userInstance?.username}" class="form-control" />
</div>

<div class="form-group ${hasErrors(bean: userInstance, field: 'email', 'error')} required">
   <label for="email"><g:message code="user.email.label" default="Email" /><span class="required-indicator">*</span></label>
   <g:textField name="email" required="true" value="${userInstance?.email}" class="form-control"/>
</div>

<%--
<sec:ifLoggedIn><!-- new user from web console -->
  <div class="form-group ${hasErrors(bean: userInstance, field: 'organizations', 'error')} required">
    <label for="organizationUid"><g:message code="user.organizations.label" default="Organizations" /><span class="required-indicator">*</span></label>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
      <g:select name="organizationUid" from="${Organization.list()}"
                optionKey="uid" optionValue="${{it.name +' '+ it.uid}}" value="${userInstance?.organizations}"
                multiple="true" size="5" class="form-control" />
    </sec:ifAnyGranted>
    <sec:ifNotGranted roles="ROLE_ADMIN">
      <g:selectWithCurrentUserOrganizations name="organizationUid" value="${userInstance?.organizations}" multiple="true" class="form-control" />
    </sec:ifNotGranted>
    <span><g:message code="user.edit.canOnlyEditAssociatedOrgs" /></span>
  </div>
</sec:ifLoggedIn>
--%>
<sec:ifNotLoggedIn><!-- register -->
  <div class="form-group required">
	 <label for="org_name"><g:message code="user.register.organization_name" default="Organization Name" /><span class="required-indicator">*</span></label>
	 <g:textField name="org_name" value="${params.org_name}" required="true" class="form-control" />
  </div>
</sec:ifNotLoggedIn>

<sec:ifLoggedIn>
  <div class="form-group">
    <label for="role">
      <g:message code="user.roles.label" default="Roles" />
      <span class="required-indicator">*</span>
    </label>
    <div class="table-responsive">
      <table class="table table-striped table-bordered table-hover">
        <thead>
          <tr>
            <th><g:message code="user.organizations.label" default="Organizations" /> &gt;</th>
            <g:each in="${organizations}" var="org">
              <th>${org.name}</th>
            </g:each>
          </tr>
        </thead>
        <tbody>
          <g:each in="${roles}" status="i" var="role">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
              <th>${role.authority}</th>
              <g:each in="${organizations}" var="org">
                <td>
                  <input type="checkbox" name="${role.authority}" ${(userRoles?.find{ it.role == role && it.organization == org })?'checked="true"':''} value="${org.uid}" />
                </td>
              </g:each>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>
  </div>
  <%--
  <div class="form-group">
    <label for="role">
      <g:message code="user.roles.label" default="Roles" />
      <span class="required-indicator">*</span>
    </label>
    <g:selectWithRolesICanAssign name="role" value="${params?.role}" user_values="${userInstance?.authorities?.authority}" multiple="true" class="form-control" />
  </div>
  --%>
</sec:ifLoggedIn>

<sec:access expression="hasRole('ROLE_ADMIN')">
	<div class="form-group ${hasErrors(bean: userInstance, field: 'accountExpired', 'error')} ">
		<label for="accountExpired"><g:message code="user.accountExpired.label" default="Account Expired" /></label>
		<g:checkBox name="accountExpired" value="${userInstance?.accountExpired}" />
	</div>
	
	<div class="form-group ${hasErrors(bean: userInstance, field: 'accountLocked', 'error')} ">
		<label for="accountLocked"><g:message code="user.accountLocked.label" default="Account Locked" /></label>
		<g:checkBox name="accountLocked" value="${userInstance?.accountLocked}" />
	</div>
	
	<div class="form-group ${hasErrors(bean: userInstance, field: 'enabled', 'error')} ">
		<label for="enabled"><g:message code="user.enabled.label" default="Enabled" /></label>
		<g:checkBox name="enabled" value="${userInstance?.enabled}" />
	</div>
	
	<div class="form-group ${hasErrors(bean: userInstance, field: 'passwordExpired', 'error')} ">
		<label for="passwordExpired"><g:message code="user.passwordExpired.label" default="Password Expired" /></label>
		<g:checkBox name="passwordExpired" value="${userInstance?.passwordExpired}" />
	</div>
</sec:access>

<script type="text/javascript">
  $(document).ready(function() {

     $('input[type="checkbox"]').css({'display':'block'});

  });
</script>
