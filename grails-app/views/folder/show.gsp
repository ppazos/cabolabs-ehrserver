<%@ page import="com.cabolabs.ehrserver.openehr.directory.Folder" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'folder.label', default: 'Folder')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="create">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="default.new.label" args="[entityName]" />
            </button>
          </g:link>
        </div>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="message" role="status">${flash.message}</div>
	      </g:if>
	      
	      <div class="control-group">
            <label class="control-label"><g:message code="folder.uid.label" default="Uid" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${folderInstance}" field="uid"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><g:message code="folder.name.label" default="Name" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${folderInstance}" field="name"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><g:message code="folder.ehr.label" default="EHR" /></label>
            <div class="controls">
                <p class="form-control-static"><g:link controller="ehr" action="show" id="${folderInstance?.ehr?.id}">${folderInstance?.ehr?.encodeAsHTML()}</g:link></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><g:message code="folder.parent.label" default="Parent" /></label>
            <div class="controls">
                <p class="form-control-static"><g:link controller="folder" action="show" id="${folderInstance?.parent?.id}">${folderInstance?.parent?.name}</g:link></p>
            </div>
        </div>
        
        <g:if test="${folderInstance?.folders}">
          <label class="control-label"><g:message code="folder.folders.label" default="Folders" /></label>
          <g:each in="${folderInstance.folders}" var="f">
            <div class="control-group">
	            <div class="controls">
	                <p class="form-control-static"><g:link controller="folder" action="show" id="${f.id}">${f.name}</g:link></p>
	            </div>
	         </div>
          </g:each>
        </g:if>
      
        <g:if test="${folderInstance?.items}">
          <span id="items-label" class="property-label"><g:message code="folder.items.label" default="Items" /></span>
          <span class="property-value" aria-labelledby="items-label">
            <g:each in="${folderInstance.items}" var="vouid">
              <g:message code="folder.show.versionedComposition" />
              <g:link controller="versionedComposition" action="show" params="[uid: vouid]">${vouid}</g:link><br/>
            </g:each>
          </span>
        </g:if>
	     
	     <div class="btn-toolbar" role="toolbar">
          <g:link action="edit" id="${folderInstance?.id}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
          <g:link action="delete" id="${folderInstance?.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-minus-circle fa-fw" aria-hidden="true"></span> <g:message code="default.button.delete.label" default="Delete" />
            </button>
          </g:link>
        </div>
      </div>
    </div>
  </body>
</html>
