<<<<<<< HEAD
<%@ page import="ehr.clinical_documents.DataIndex" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'dataIndex.label', default: 'DataIndex')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#show-dataIndex" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <!--
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
        -->
      </ul>
    </div>
    
    <div id="show-dataIndex" class="content scaffold-show" role="main">
    
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <ol class="property-list dataIndex">
      
        <g:if test="${dataIndexInstance?.archetypeId}">
        <li class="fieldcontain">
          <span id="archetypeId-label" class="property-label"><g:message code="dataIndex.archetypeId.label" default="Archetype Id" /></span>
          
          <span class="property-value" aria-labelledby="archetypeId-label"><g:fieldValue bean="${dataIndexInstance}" field="archetypeId"/></span>
          
        </li>
        </g:if>
      
        <g:if test="${dataIndexInstance?.path}">
        <li class="fieldcontain">
          <span id="path-label" class="property-label"><g:message code="dataIndex.path.label" default="Path" /></span>
          
          <span class="property-value" aria-labelledby="path-label"><g:fieldValue bean="${dataIndexInstance}" field="path"/></span>
          
        </li>
        </g:if>
      
        <g:if test="${dataIndexInstance?.rmTypeName}">
        <li class="fieldcontain">
          <span id="rmTypeName-label" class="property-label"><g:message code="dataIndex.rmTypeName.label" default="Rm Type Name" /></span>
          
          <span class="property-value" aria-labelledby="rmTypeName-label"><g:fieldValue bean="${dataIndexInstance}" field="rmTypeName"/></span>
          
        </li>
        </g:if>
      </ol>
      
      <!--
      <g:form>
        <fieldset class="buttons">
          <g:hiddenField name="id" value="${dataIndexInstance?.id}" />
          <g:link class="edit" action="edit" id="${dataIndexInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
        </fieldset>
      </g:form>
      -->
    </div>
  </body>
</html>
=======

<%@ page import="ehr.clinical_documents.DataIndex" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'dataIndex.label', default: 'DataIndex')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-dataIndex" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-dataIndex" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list dataIndex">
			
				<g:if test="${dataIndexInstance?.archetypeId}">
				<li class="fieldcontain">
					<span id="archetypeId-label" class="property-label"><g:message code="dataIndex.archetypeId.label" default="Archetype Id" /></span>
					
						<span class="property-value" aria-labelledby="archetypeId-label"><g:fieldValue bean="${dataIndexInstance}" field="archetypeId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${dataIndexInstance?.path}">
				<li class="fieldcontain">
					<span id="path-label" class="property-label"><g:message code="dataIndex.path.label" default="Path" /></span>
					
						<span class="property-value" aria-labelledby="path-label"><g:fieldValue bean="${dataIndexInstance}" field="path"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${dataIndexInstance?.rmTypeName}">
				<li class="fieldcontain">
					<span id="rmTypeName-label" class="property-label"><g:message code="dataIndex.rmTypeName.label" default="Rm Type Name" /></span>
					
						<span class="property-value" aria-labelledby="rmTypeName-label"><g:fieldValue bean="${dataIndexInstance}" field="rmTypeName"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${dataIndexInstance?.id}" />
					<g:link class="edit" action="edit" id="${dataIndexInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
