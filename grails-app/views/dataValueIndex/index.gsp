<%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.data.DataValueIndex" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="datavalueindex.index.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="datavalueindex.index.title" /></h1>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link class="list" controller="dataValueIndex" action="reindex">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-refresh fa-fw" aria-hidden="true"></span> <g:message code="datavalueindex.index.reindex" />
            </button></g:link>
          <g:link controller="compositionIndex" action="list">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-list-alt fa-fw" aria-hidden="true"></span> <g:message code="compositionIndex.list.title" />
            </button></g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="alert alert-info" role="alert">${flash.message}</div>
	      </g:if>
	      <g:render template="/dataValueIndex/listTable"/>
	      <g:paginator total="${total}" args="${params}" />
      </div>
    </div>
  </body>
</html>
