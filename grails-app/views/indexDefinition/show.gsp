<%@ page import="ehr.clinical_documents.IndexDefinition" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'indexDefinition.label', default: 'IndexDefinition')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
	    <div class="nav" role="navigation">
	      <ul>
	        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
	      </ul>
	    </div>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
	      <h1><g:message code="indexDefinition.show.title" /></h1>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
	     <g:if test="${flash.message}">
	        <div class="message" role="status">${flash.message}</div>
	     </g:if>

	     <div class="control-group">
            <label class="control-label"><g:message code="indexDefinition.templateId.label" default="Template Id" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${indexDefinitionInstance}" field="templateId"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><g:message code="indexDefinition.path.label" default="Path" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${indexDefinitionInstance}" field="path"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><g:message code="indexDefinition.rmTypeName.label" default="Type Name" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${indexDefinitionInstance}" field="rmTypeName"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><g:message code="indexDefinition.archetypeId.label" default="Archetype Id" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${indexDefinitionInstance}" field="archetypeId"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><g:message code="indexDefinition.archetypePath.label" default="Archetype Path" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${indexDefinitionInstance}" field="archetypePath"/></p>
            </div>
        </div>
	     <div class="control-group">
            <label class="control-label"><g:message code="indexDefinition.name.label" default="Name" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${indexDefinitionInstance}" field="name"/></p>
            </div>
        </div>
      </div>
    </div>
  </body>
</html>