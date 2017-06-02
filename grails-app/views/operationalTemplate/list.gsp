<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="opt.list.title" /></h1>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-md-5">
        <g:form class="form-inline" action="list">
          <input type="hidden" name="sort" value="${params.sort}" />
          <input type="hidden" name="order" value="${params.order}" />
          <div class="form-group">
            <label for="ipt_con"><g:message code="opt.list.filter.concept.label" /></label>
            <input type="text" class="form-control" name="concept" id="ipt_con" value="${params?.concept}" />
          </div>
          <button type="submit" class="btn btn-default"><g:message code="common.action.filter" /></button>
        </g:form>
      </div>
      <div class="col-md-7">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="upload">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-upload" aria-hidden="true"></span>
            </button>
          </g:link>
          <g:link action="generate">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-refresh fa-fw" aria-hidden="true"></span> <g:message code="operationalTemplate.generate.label" />
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
    
    <div class="row row-grid">
      <div class="col-lg-12">
	     <g:if test="${flash.message}">
	       <div class="alert alert-info" role="alert">${flash.message}</div>
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
                <g:sortableColumn property="isPublic" title="${message(code: 'template.isPublic.label', default: 'is public')}" />
		          <th></th>
		        </tr>
		      </thead>
		      <tbody>
		        <g:set var="sameLangTemplates" value="${opts.findAll{ it.lang == session.lang }}" />
		        <g:set var="otherLangTemplates" value="${opts.findAll{ it.lang != session.lang }}" />
		        
		        <tr><td colspan="7"><h3>Templates accessible with your current language</h3></td></tr>
			     <g:each in="${sameLangTemplates}" status="i" var="templateInstance">
			       <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		            <td>
		              <g:link action="show" params="[uid:templateInstance.uid]" title="Ver XML ${templateInstance.concept}" id="${templateInstance.id}">${fieldValue(bean: templateInstance, field: "templateId")}</g:link>
		            </td>
		            <td>${fieldValue(bean: templateInstance, field: "concept")}</td>
		            <td>${fieldValue(bean: templateInstance, field: "language")}</td>
		            <td>${fieldValue(bean: templateInstance, field: "uid")}</td>
		            <td>${fieldValue(bean: templateInstance, field: "archetypeId")}</td>
                  <td>${templateInstance.isPublic.toString()}</td>
		            <td>
		              <g:link action="items" params="[uid: templateInstance.uid]">Template Indexes</g:link>
		              <br/>
		              <g:link action="archetypeItems" params="[uid: templateInstance.uid]">Archetype Indexes</g:link>
		            </td>
			       </tr>
			     </g:each>
			     
			     <tr>
			       <td colspan="7">
			         <h3>Templates not accessible with your current language</h3>
			         <p>These templates won't be shown on the Query Builder. You should load templates with the same
			         language as your that you will use, change your language from the login screen, or share the
			         templates with organizations that other languages are used.</p>
			       </td>
			     </tr>
			     <g:each in="${otherLangTemplates}" status="i" var="templateInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td>
                    <g:link action="show" params="[uid:templateInstance.uid]" title="Ver XML ${templateInstance.concept}" id="${templateInstance.id}">${fieldValue(bean: templateInstance, field: "templateId")}</g:link>
                  </td>
                  <td>${fieldValue(bean: templateInstance, field: "concept")}</td>
                  <td>${fieldValue(bean: templateInstance, field: "language")}</td>
                  <td>${fieldValue(bean: templateInstance, field: "uid")}</td>
                  <td>${fieldValue(bean: templateInstance, field: "archetypeId")}</td>
                  <td>${templateInstance.isPublic.toString()}</td>
                  <td>
                    <g:link action="items" params="[uid: templateInstance.uid]">Template Indexes</g:link>
                    <br/>
                    <g:link action="archetypeItems" params="[uid: templateInstance.uid]">Archetype Indexes</g:link>
                  </td>
                </tr>
              </g:each>
		      </tbody>
		    </table>
		  </div>
	     <g:paginator total="${total}" args="${params}" />
      </div>
    </div>
  </body>
</html>
