<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'template.label', default: 'template')}" />
    <title><g:message code="template.list.title" /></title>
  </head>
  <body>
    <a href="#list-template" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="create" action="upload"><g:message code="default.upload.label" /></g:link></li>
      </ul>
    </div>
    <div id="list-template" class="content scaffold-list" role="main">
    
      <h1><g:message code="template.list.title" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <table>
        <thead>
          <tr>
            <g:sortableColumn property="templateId" title="${message(code: 'template.templateId.label', default: 'templateId')}" />
            <g:sortableColumn property="concept" title="${message(code: 'template.concept.label', default: 'concept')}" />
            <g:sortableColumn property="language" title="${message(code: 'template.language.label', default: 'language')}" />
            <g:sortableColumn property="uid" title="${message(code: 'template.uid.label', default: 'uid')}" />
            <th>
              Actions
            </th>
          </tr>
        </thead>
        <tbody>
        <g:each in="${opts}" status="i" var="templateInstance">
          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <td>
              <g:link action="show" id="${templateInstance.id}">${fieldValue(bean: templateInstance, field: "templateId")}</g:link>
            </td>
            <td>${fieldValue(bean: templateInstance, field: "concept")}</td>
            <td>${fieldValue(bean: templateInstance, field: "language")}</td>
            <td>${fieldValue(bean: templateInstance, field: "uid")}</td>
             <td>
               <%--
               <g:hasEhr patientUID="${templateInstance.uid}">
                 <g:link controller="ehr" action="showEhr" params="[patientUID: templateInstance.uid]">Show EHR</g:link>
               </g:hasEhr>
               <g:dontHasEhr patientUID="${templateInstance.uid}">
                 <g:link controller="ehr" action="createEhr" params="[patientUID: templateInstance.uid]">Create EHR</g:link>
               </g:dontHasEhr>
               --%>
             </td>
          </tr>
        </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <g:paginate total="${total}" />
      </div>
    </div>
  </body>
</html>