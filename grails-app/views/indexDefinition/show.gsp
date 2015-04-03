<%@ page import="ehr.clinical_documents.IndexDefinition" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'indexDefinition.label', default: 'IndexDefinition')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#show-indexDefinition" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <!--
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
        -->
      </ul>
    </div>
    
    <div id="show-indexDefinition" class="content scaffold-show" role="main">
    
      <h1><g:message code="indexDefinition.show.title" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <ol class="property-list indexDefinition">
      
        <g:if test="${indexDefinitionInstance?.templateId}">
        <li class="fieldcontain">
          <span id="templateId-label" class="property-label"><g:message code="indexDefinition.templateId.label" default="Template Id" /></span>
          <span class="property-value" aria-labelledby="templateId-label"><g:fieldValue bean="${indexDefinitionInstance}" field="templateId"/></span>
        </li>
        </g:if>
      
        <g:if test="${indexDefinitionInstance?.path}">
        <li class="fieldcontain">
          <span id="path-label" class="property-label"><g:message code="indexDefinition.path.label" default="Path" /></span>
          <span class="property-value" aria-labelledby="path-label"><g:fieldValue bean="${indexDefinitionInstance}" field="path"/></span>
        </li>
        </g:if>
      
        <g:if test="${indexDefinitionInstance?.rmTypeName}">
        <li class="fieldcontain">
          <span id="rmTypeName-label" class="property-label"><g:message code="indexDefinition.rmTypeName.label" default="Type Name" /></span>
          <span class="property-value" aria-labelledby="rmTypeName-label"><g:fieldValue bean="${indexDefinitionInstance}" field="rmTypeName"/></span>
        </li>
        </g:if>
        
        <g:if test="${indexDefinitionInstance?.name}">
        <li class="fieldcontain">
          <span id="name-label" class="property-label"><g:message code="indexDefinition.name.label" default="Name" /></span>
          <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${indexDefinitionInstance}" field="name"/></span>
        </li>
        </g:if>
      </ol>
    </div>
  </body>
</html>