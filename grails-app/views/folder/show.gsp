<%@ page import="directory.Folder" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'folder.label', default: 'Folder')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#show-folder" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="show-folder" class="content scaffold-show" role="main">
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      <ol class="property-list folder">
      
        <g:if test="${folderInstance?.uid}">
            <li class="fieldcontain">
               <span id="uid-label" class="property-label"><g:message code="folder.uid.label" default="Uid" /></span>
               <span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${folderInstance}" field="uid"/></span>
            </li>
        </g:if>
        
        <g:if test="${folderInstance?.name}">
	        <li class="fieldcontain">
	          <span id="name-label" class="property-label"><g:message code="folder.name.label" default="Name" /></span>
	          <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${folderInstance}" field="name"/></span>
	        </li>
        </g:if>
        
        <g:if test="${folderInstance?.ehr}">
	        <li class="fieldcontain">
	          <span id="parent-label" class="property-label"><g:message code="folder.ehr.label" default="EHR" /></span>
	          <span class="property-value" aria-labelledby="parent-label"><g:link controller="ehr" action="show" id="${folderInstance?.ehr?.id}">${folderInstance?.ehr?.encodeAsHTML()}</g:link></span>
	        </li>
        </g:if>
        
        <g:if test="${folderInstance?.parent}">
	        <li class="fieldcontain">
	          <span id="parent-label" class="property-label"><g:message code="folder.parent.label" default="Parent" /></span>
	          <span class="property-value" aria-labelledby="parent-label"><g:link controller="folder" action="show" id="${folderInstance?.parent?.id}">${folderInstance?.parent?.encodeAsHTML()}</g:link></span>
	        </li>
        </g:if>
      
        <g:if test="${folderInstance?.folders}">
	        <li class="fieldcontain">
	          <span id="folders-label" class="property-label"><g:message code="folder.folders.label" default="Folders" /></span>
	          
	          <g:each in="${folderInstance.folders}" var="f">
	            <span class="property-value" aria-labelledby="folders-label"><g:link controller="folder" action="show" id="${f.id}">${f?.encodeAsHTML()}</g:link></span>
	          </g:each>
	        </li>
        </g:if>
      
        <g:if test="${folderInstance?.items}">
	        <li class="fieldcontain">
	          <span id="items-label" class="property-label"><g:message code="folder.items.label" default="Items" /></span>
	          <span class="property-value" aria-labelledby="items-label">
	            <g:each in="${folderInstance.items}" var="vouid">
	              Versioned Composition:
	              <g:link controller="versionedComposition" action="show" params="[uid: vouid]">${vouid}</g:link><br/>
	            </g:each>
	          </span>
	        </li>
        </g:if>    
      </ol>
      
      <g:form url="[resource:folderInstance, action:'delete']" method="DELETE">
        <fieldset class="buttons">
          <g:link class="edit" action="edit" resource="${folderInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
        </fieldset>
      </g:form>
    </div>
  </body>
</html>
