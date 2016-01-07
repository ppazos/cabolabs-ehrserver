<%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'compositionIndex.label', default: 'CompositionIndex')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="list">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-list-alt fa-fw" aria-hidden="true"></span> <g:message code="default.list.label" args="[entityName]" />
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
	       <label class="control-label"><g:message code="compositionIndex.category.label" default="Category" /></label>
	       <div class="controls">
	         <p class="form-control-static"><g:fieldValue bean="${compositionIndexInstance}" field="category"/></p>
	       </div>
	     </div>
	     <div class="control-group">
          <label class="control-label"><g:message code="compositionIndex.startTime.label" default="Start Time" /></label>
          <div class="controls">
            <p class="form-control-static"><g:formatDate date="${compositionIndexInstance?.startTime}" /></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="compositionIndex.archetypeId.label" default="Archetype ID" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${compositionIndexInstance}" field="archetypeId"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="compositionIndex.ehrUid.label" default="EHR UID" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${compositionIndexInstance}" field="ehrUid"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="compositionIndex.uid.label" default="Composition UID" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${compositionIndexInstance}" field="uid"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="compositionIndex.subjectId.label" default="Patient UID" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${compositionIndexInstance}" field="subjectId"/></p>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>
