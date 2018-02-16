<%@ page import="com.cabolabs.ehrserver.account.Account" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="account.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
      <h1><g:message code="account.show.title" /></h1>
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
              <th><g:message code="account.attr.contact" default="Contact" /></th>
              <td><g:link controller="user" action="show" id="${account?.contact?.id}">${account?.contact?.encodeAsHTML()}</g:link></td>
            </tr>
            <tr>
              <th><g:message code="account.attr.enabled" default="Enabled" /></th>
              <td><g:formatBoolean boolean="${account?.enabled}" /></td>
            </tr>
        </table>
        
        <div class="btn-toolbar" role="toolbar">
          <fieldset class="buttons">
            <g:link class="edit" action="edit" resource="${account}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
            <%-- TODO handle delete Account
            <g:link action="delete" onclick="return confirm('${message(code:'default.button.delete.confirm.message')}');" params="[id: account.id]"><button type="button" class="btn btn-default btn-md"><span class="fa fa-trash-o fa-fw" aria-hidden="true"></span> <g:message code="default.button.delete.label" /></button></g:link>
            --%>
          </fieldset>
        </div>
      </div>
    </div>
      
        <%-- TODO: show organizations
        <g:if test="${account?.organizations}">
        <ol>
        <li class="fieldcontain">
          <span id="organizations-label" class="property-label"><g:message code="account.organizations.label" default="Organizations" /></span>
          
            <g:each in="${account.organizations}" var="o">
            <span class="property-value" aria-labelledby="organizations-label"><g:link controller="organization" action="show" id="${o.id}">${o?.encodeAsHTML()}</g:link></span>
            </g:each>
          
        </li>
        </ol>
        </g:if>
        --%>

  </body>
</html>
