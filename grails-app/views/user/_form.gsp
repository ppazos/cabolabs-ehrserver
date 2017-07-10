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

<sec:ifNotLoggedIn><%-- register --%>
  <div class="form-group required">
   <label for="org_name"><g:message code="user.register.organization_name" default="Organization Name" /><span class="required-indicator">*</span></label>
   <g:textField name="org_name" value="${params.org_name}" required="true" class="form-control" />
  </div>
</sec:ifNotLoggedIn>

<sec:ifLoggedIn><%-- edit --%>
  <div class="form-group">
    <label for="role">
      <g:message code="user.roles.label" default="Roles" />
      <span class="required-indicator">*</span>
    </label>
    <div class="table-responsive">
      <table class="table table-striped table-bordered table-hover table-org-roles">
        <thead>
          <tr>
            <th><g:message code="user.organizations.label" default="Organizations" /></th>
            <g:each in="${Role.list()}" var="role">
              <sec:ifNotGranted roles="ROLE_ADMIN"><%-- dont show admin if user is not admin --%>
                <g:if test="${role.authority != 'ROLE_ADMIN'}">
                  <th>${role.authority}</th>
                </g:if>
              </sec:ifNotGranted>
              <sec:ifAnyGranted roles="ROLE_ADMIN">
                <th>${role.authority}</th>
              </sec:ifAnyGranted>
            </g:each>
          </tr>
        </thead>
        <tbody>
          <g:each in="${roles}" status="i" var="roleOrg">
            <g:set var="org" value="${roleOrg.key}" />
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
              <td>${org.name}</td>
              <g:each in="${Role.list()}" var="role">
                <sec:ifNotGranted roles="ROLE_ADMIN"><%-- dont show admin if user is not admin --%>
                  <g:if test="${role.authority != 'ROLE_ADMIN'}">
                    <td>
                      <g:if test="${(roleOrg.value.contains(role))}"><%-- dont show if I cant assing the role --%>
                        <input type="checkbox" name="${org.uid}" ${(userRoles?.find{ it.role == role && it.organization == org })?'checked="true"':''} value="${role.authority}" />
                      </g:if>
                    </td>
                  </g:if>
                </sec:ifNotGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                  <td>
                    <g:if test="${(roleOrg.value.contains(role))}"><%-- dont show if I cant assing the role --%>
                      <input type="checkbox" name="${org.uid}" ${(userRoles?.find{ it.role == role && it.organization == org })?'checked="true"':''} value="${role.authority}" />
                    </g:if>
                  </td>
                </sec:ifAnyGranted>
              </g:each>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>
  </div>
</sec:ifLoggedIn>

<sec:access expression="hasRole('ROLE_ADMIN')">
  <div class="form-group ${hasErrors(bean: userInstance, field: 'accountExpired', 'error')} ">
    <g:checkBox name="accountExpired" value="${userInstance?.accountExpired}" />
    <label for="accountExpired"><g:message code="user.accountExpired.label" default="Account Expired" /></label>
  </div>
  
  <div class="form-group ${hasErrors(bean: userInstance, field: 'accountLocked', 'error')} ">
    <g:checkBox name="accountLocked" value="${userInstance?.accountLocked}" />
    <label for="accountLocked"><g:message code="user.accountLocked.label" default="Account Locked" /></label>
  </div>
  
  <div class="form-group ${hasErrors(bean: userInstance, field: 'enabled', 'error')} ">
    <g:checkBox name="enabled" value="${userInstance?.enabled}" />
    <label for="enabled"><g:message code="user.enabled.label" default="Enabled" /></label>
  </div>
  
  <div class="form-group ${hasErrors(bean: userInstance, field: 'passwordExpired', 'error')} ">
    <g:checkBox name="passwordExpired" value="${userInstance?.passwordExpired}" />
    <label for="passwordExpired"><g:message code="user.passwordExpired.label" default="Password Expired" /></label>
  </div>
</sec:access>
