<%@ page import="ehr.clinical_documents.DataIndex" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'dataIndex.label', default: 'DataIndex')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#list-dataIndex" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <!--
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
        -->
        <li><g:link class="create" action="generate"><g:message code="dataIndex.generate.label" /></g:link></li>
        <li><g:link class="list" controller="compositionIndex" action="list"><g:message code="compositionIndex.list.title" /></g:link></li>
      </ul>
    </div>
    <div id="list-dataIndex" class="content scaffold-list" role="main">
    
      <h1><g:message code="dataIndex.list.title" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <table>
        <thead>
          <tr>
            <g:sortableColumn property="templateId" title="${message(code: 'dataIndex.templateId.label', default: 'Tempalte Id')}" />
            <g:sortableColumn property="path" title="${message(code: 'dataIndex.path.label', default: 'Path')}" />
            <g:sortableColumn property="name" title="${message(code: 'dataIndex.name.label', default: 'Name')}" />
            <g:sortableColumn property="rmTypeName" title="${message(code: 'dataIndex.rmTypeName.label', default: 'Rm Type Name')}" />
          </tr>
        </thead>
        <tbody>
        <g:each in="${dataIndexInstanceList}" status="i" var="dataIndexInstance">
          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <td><g:link action="show" id="${dataIndexInstance.id}">${fieldValue(bean: dataIndexInstance, field: "templateId")}</g:link></td>
            <td>${fieldValue(bean: dataIndexInstance, field: "path")}</td>
            <td>${fieldValue(bean: dataIndexInstance, field: "name")}</td>
            <td>${fieldValue(bean: dataIndexInstance, field: "rmTypeName")}</td>
          </tr>
        </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <g:paginate total="${dataIndexInstanceTotal}" />
      </div>
    </div>
  </body>
</html>