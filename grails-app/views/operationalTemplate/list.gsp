<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="upload">
            <button type="button" class="btn btn-default btn-md">
              <span class="glyphicon glyphicon-upload" aria-hidden="true"></span> <g:message code="opt.upload.label" />
            </button>
          </g:link>
          
          
          <g:link action="generate">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-list-alt fa-fw" aria-hidden="true"></span> <g:message code="operationalTemplate.generate.label" />
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
        <h1><g:message code="opt.list.title" /></h1>
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
		            <g:sortableColumn property="templateId" title="${message(code: 'template.templateId.label', default: 'templateId')}" />
		            <g:sortableColumn property="concept" title="${message(code: 'template.concept.label', default: 'concept')}" />
		            <g:sortableColumn property="language" title="${message(code: 'template.language.label', default: 'language')}" />
		            <g:sortableColumn property="uid" title="${message(code: 'template.uid.label', default: 'uid')}" />
		            <g:sortableColumn property="archetypeId" title="${message(code: 'template.archetypeId.label', default: 'root archetype')}" />
		            <th>
		              Actions
		            </th>
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${opts}" status="i" var="templateInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td>
			              <g:link action="show" params="[concept:templateInstance.concept]" title="Ver XML ${templateInstance.concept}" id="${templateInstance.id}">${fieldValue(bean: templateInstance, field: "templateId")}</g:link>
			            </td>
			            <td>${fieldValue(bean: templateInstance, field: "concept")}</td>
			            <td>${fieldValue(bean: templateInstance, field: "language")}</td>
			            <td>${fieldValue(bean: templateInstance, field: "uid")}</td>
			            <td>${fieldValue(bean: templateInstance, field: "archetypeId")}</td>
			            <td>
			              <g:link action="items" params="[uid: templateInstance.uid]">Template Indexes</g:link>
			              <br/>
			              <g:link action="archetypeItems" params="[uid: templateInstance.uid]">Archetype Indexes</g:link>
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
		  </div>
	     <g:paginator total="${total}" />
      </div>
    </div>
  </body>
</html>