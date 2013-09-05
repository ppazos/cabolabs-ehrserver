<%@ page import="ehr.clinical_documents.CompositionIndex" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'compositionIndex.label', default: 'CompositionIndex')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
    <style>
     .icon {
       width: 64px;
       border: none;
     }
     </style>
  </head>
  <body>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" controller="dataIndex" action="list"><g:message code="dataIndex.list.title" /></g:link></li>
      </ul>
    </div>
    <div id="list-compositionIndex" class="content scaffold-list" role="main">
    
      <h1><g:message code="compositionIndex.list.title" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <table>
        <thead>
          <tr>
            <g:sortableColumn property="category" title="${message(code: 'compositionIndex.category.label', default: 'Category')}" />
            <g:sortableColumn property="startTime" title="${message(code: 'compositionIndex.startTime.label', default: 'Start Time')}" />
            <g:sortableColumn property="archetypeId" title="${message(code: 'compositionIndex.archetypeId.label', default: 'Archetype Id')}" />
            <g:sortableColumn property="ehrId" title="${message(code: 'compositionIndex.ehrId.label', default: 'Ehr')}" />
            <g:sortableColumn property="subjectId" title="${message(code: 'compositionIndex.subjectId.label', default: 'Subject')}" />
            <g:sortableColumn property="uid" title="${message(code: 'compositionIndex.uid.label', default: 'Uid')}" />
            <th></th>
          </tr>
        </thead>
        <tbody>
        <g:each in="${compositionIndexInstanceList}" status="i" var="compositionIndexInstance">
          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <td><g:link action="show" id="${compositionIndexInstance.id}">${fieldValue(bean: compositionIndexInstance, field: "category")}</g:link></td>
            <td><g:formatDate date="${compositionIndexInstance.startTime}" /></td>
            <td>${fieldValue(bean: compositionIndexInstance, field: "archetypeId")}</td>
            <td>${fieldValue(bean: compositionIndexInstance, field: "ehrId")}</td>
            <td>${fieldValue(bean: compositionIndexInstance, field: "subjectId")}</td>
            <td>${fieldValue(bean: compositionIndexInstance, field: "uid")}</td>
            <td>
              <g:link controller="ehr" action="showComposition" params="[uid:compositionIndexInstance.uid]" title="Ver XML ${compositionIndexInstance.uid}"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></g:link>
              <g:link controller="ehr" action="showCompositionUI" params="[uid:compositionIndexInstance.uid]" title="Ver Documento ${compositionIndexInstance.uid}"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></g:link>
            </td>
          </tr>
        </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <g:paginate total="${compositionIndexInstanceTotal}" />
      </div>
    </div>
  </body>
</html>
