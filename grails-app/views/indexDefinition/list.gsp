<%@ page import="ehr.clinical_documents.IndexDefinition" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'indexDefinition.label', default: 'IndexDefinition')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="generate">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-list-alt fa-fw" aria-hidden="true"></span> <g:message code="indexDefinition.generate.label" />
            </button>
          </g:link>
          <g:link controller="compositionIndex" action="list">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-list-alt fa-fw" aria-hidden="true"></span> <g:message code="compositionIndex.list.title" />
            </button>
          </g:link>
        </div>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="indexDefinition.list.title" /></h1>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="message" role="status">${flash.message}</div>
	      </g:if>
	      
         <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
		        <thead>
		          <tr>
		            <g:sortableColumn property="templateId" title="${message(code: 'indexDefinition.templateId.label', default: 'Template Id')}" />
		            <g:sortableColumn property="path" title="${message(code: 'indexDefinition.path.label', default: 'Path')}" />
		            <g:sortableColumn property="name" title="${message(code: 'indexDefinition.name.label', default: 'Name')}" />
		            <g:sortableColumn property="rmTypeName" title="${message(code: 'indexDefinition.rmTypeName.label', default: 'Rm Type Name')}" />
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${indexDefinitionInstanceList}" status="i" var="indexDefinitionInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td><g:link action="show" id="${indexDefinitionInstance.id}">${fieldValue(bean: indexDefinitionInstance, field: "templateId")}</g:link></td>
			            <td>${fieldValue(bean: indexDefinitionInstance, field: "path")}</td>
			            <td>${fieldValue(bean: indexDefinitionInstance, field: "name")}</td>
			            <td>${fieldValue(bean: indexDefinitionInstance, field: "rmTypeName")}</td>
			          </tr>
			        </g:each>
		        </tbody>
           </table>
         </div>         
	     <g:paginator currentPage="${pageCurrent}" numberOfPages="5" urlPage="/ehr/indexDefinition/list/?offset" total="${indexDefinitionInstanceTotal}" numberItemsDisplay="12" />
      </div>
    </div>
  </body>
</html>
