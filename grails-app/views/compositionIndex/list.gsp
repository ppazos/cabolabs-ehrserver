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
        <li><g:link class="list" controller="indexDefinition" action="list"><g:message code="indexDefinition.list.title" /></g:link></li>
      </ul>
    </div>
    <div id="list-compositionIndex" class="content scaffold-list" role="main">
    
      <h1><g:message code="compositionIndex.list.title" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <g:render template="/compositionIndex/listTable"/>
      
      <div class="pagination">
        <g:paginate total="${compositionIndexInstanceTotal}" />
      </div>
    </div>
  </body>
</html>
