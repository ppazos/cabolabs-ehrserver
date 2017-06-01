<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'versionedComposition.label', default: 'VersionedComposition')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <style>
     .icon {
       width: 64px;
       border: none;
     }
     </style>
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
          <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="versionedComposition.attr.uid" /></th>
              <td><g:fieldValue bean="${versionedCompositionInstance}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="versionedComposition.attr.ehr" /></th>
              <td><g:link controller="ehr" action="show" params="[uid: versionedCompositionInstance.ehr.uid]">${versionedCompositionInstance.ehr.uid}</g:link></td>
            </tr>
            <tr>
              <th><g:message code="versionedComposition.attr.isPersistent" /></th>
              <td><g:formatBoolean boolean="${versionedCompositionInstance?.isPersistent}" /></td>
            </tr>
            <tr>
              <th><g:message code="versionedComposition.attr.timeCreated" /></th>
              <td><g:formatDate date="${versionedCompositionInstance?.timeCreated}" /></td>
            </tr>
          </tbody>
        </table>

        <h2>Versions</h2>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
            <tr>
		        <th><g:message code="version.attr.uid" /></th>
		        <th><g:message code="composition.attr.startTime" /></th>
		        <th><g:message code="composition.attr.archetypeId" /></th>
		        <th><g:message code="audit.attr.changeType" /></th>
		        <th></th>
		      </tr>
            <g:each in="${versionedCompositionInstance.allVersions}" var="version">
              <%-- ${version.uid}<br/> --%><!-- TODO: version row template + diff -->
              <g:render template="../version/versionRow" model="[version:version]"/>
            </g:each>
          </table>
        </div>
      </div>
    </div>
  </body>
</html>
