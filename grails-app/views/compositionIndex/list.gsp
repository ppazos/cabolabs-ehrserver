<%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="compositionIndex.list.title" /></title>
    <style>
     .icon {
       width: 64px;
       border: none;
     }
     </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="compositionIndex.list.title" /></h1>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link class="list" controller="operationalTemplate" action="list">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-list-alt fa-fw" aria-hidden="true"></span> <g:message code="opt.list.title" />
            </button>
          </g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="message" role="status">${flash.message}</div>
	      </g:if>
	      
	      <g:render template="/compositionIndex/listTable"/>
	      
	      <g:paginator total="${total}" args="${params}" />
      </div>
    </div>
  </body>
</html>
