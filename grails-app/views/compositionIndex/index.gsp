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
     <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:javascript src="highlight.pack.js" />
     <asset:javascript src="xml_utils.js" />
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
          <g:link class="list" controller="dataValueIndex" action="index">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-database fa-fw" aria-hidden="true"></span> <g:message code="desktop.data" />
            </button></g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="alert alert-info" role="alert">${flash.message}</div>
	      </g:if>
	      
	      <g:render template="/compositionIndex/listTable"/>
	      
	      <g:paginator total="${total}" args="${params}" />
      </div>
    </div>
  </body>
</html>
