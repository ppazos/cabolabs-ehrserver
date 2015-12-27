<%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'ehr.label', default: 'Ehr')}" />
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
          <div class="message alert alert-warning" role="status">${flash.message}</div>
        </g:if>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
            <thead>
              <tr>
                <g:sortableColumn property="uid" title="${message(code: 'ehr.uid.label', default: 'UID')}" />
                <g:sortableColumn property="dateCreated" title="${message(code: 'ehr.dateCreated.label', default: 'Date Created')}" />
                <th><g:message code="ehr.subject.label" default="Subject" /></th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${list}" status="i" var="ehrInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td><g:link action="showEhr" params="[patientUID:ehrInstance.subject.value]">${fieldValue(bean: ehrInstance, field: "uid")}</g:link></td>
                  <td>${fieldValue(bean: ehrInstance, field: "dateCreated")}</td>
                  <td><g:link controller="person" action="show" params="[uid:ehrInstance.subject.value]">${ehrInstance.subject.value}</g:link></td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
        <g:paginator total="${total}" />
      </div>
    </div>
  </body>
</html>
