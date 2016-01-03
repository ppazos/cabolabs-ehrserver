<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'versionedComposition.label', default: 'VersionedComposition')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
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
                <g:sortableColumn property="ehrUid" title="${message(code: 'versionedComposition.ehrUid.label', default: 'Ehr Uid')}" />
                <g:sortableColumn property="isPersistent" title="${message(code: 'versionedComposition.isPersistent.label', default: 'Is Persistent')}" />
                <g:sortableColumn property="timeCreated" title="${message(code: 'versionedComposition.timeCreated.label', default: 'Time Created')}" />
                <g:sortableColumn property="uid" title="${message(code: 'versionedComposition.uid.label', default: 'Uid')}" />
              </tr>
            </thead>
            <tbody>
              <g:each in="${versionedCompositionInstanceList}" status="i" var="versionedCompositionInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td><g:link action="show" params="[uid: versionedCompositionInstance.uid]">${fieldValue(bean: versionedCompositionInstance, field: "ehrUid")}</g:link></td>
                  <td><g:formatBoolean boolean="${versionedCompositionInstance.isPersistent}" /></td>
                  <td><g:formatDate date="${versionedCompositionInstance.timeCreated}" /></td>
                  <td>${fieldValue(bean: versionedCompositionInstance, field: "uid")}</td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
        <g:paginator total="${versionedCompositionInstanceCount}" />
      </div>
    </div>
  </body>
</html>
