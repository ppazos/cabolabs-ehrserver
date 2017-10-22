<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="query.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
	      <h1><g:message code="query.list.title" /></h1>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <button type="button" class="btn btn-default btn-md filter" data-toggle="collapse" href="#collapse-filter">
            <span class="fa fa-filter fa-fw" aria-hidden="true"></span>
          </button>
          
          <g:link action="groups" title="groups">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-sitemap fa-fw" aria-hidden="true"></span>
            </button></g:link>
          
          <g:link action="create">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus fa-fw" aria-hidden="true"></span>
            </button></g:link>
        </div>
      </div>
    </div>
    
    <div class="row row-grid collapse" id="collapse-filter">
      <div class="col-md-12">
        <div class="panel panel-default">
          <div class="panel-body">

            <g:form class="form filter" action="list">
              <input type="hidden" name="sort" value="${params.sort}" />
              <input type="hidden" name="order" value="${params.order}" />
              <div class="form-group">
                <label for="ipt_name"><g:message code="query.show.name.attr" /></label>
                <input type="text" class="form-control" name="name" id="ipt_name" value="${params?.name}" />
              </div>
              <div class="btn-toolbar" role="toolbar">
                <button type="submit" name="filter" class="btn btn-primary"><span class="fa fa-share" aria-hidden="true"></span></button>
                <button type="reset" id="filter-reset" class="btn btn-default"><span class="fa fa-trash " aria-hidden="true"></span></button>
              </div>
            </g:form>

          </div>
        </div>
      </div>
    </div>
    <script>
    // avoids waiting to load the whole page to show the filters, that makes the page do an unwanted jump. 
    if (${params.containsKey('filter')})
    {
      $("#collapse-filter").addClass('in');
      $(".btn.filter").toggleClass( "btn-primary" );
    }
    </script>
    
    <div class="row row-grid">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="alert alert-info" role="alert"><g:message code="${flash.message}" args="${flash.args}" /></div>
	      </g:if>
	      
         <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
		        <thead>
		          <tr>
		            <g:sortableColumn property="name" title="${message(code: 'query.show.name.attr', default: 'Name')}" />
		            <g:sortableColumn property="group" title="${message(code: 'query.show.group.attr', default: 'Group')}" />
		            <g:sortableColumn property="format" title="${message(code: 'query.show.format.attr', default: 'Format')}" />
		            <g:sortableColumn property="type" title="${message(code: 'query.show.type.attr', default: 'Type')}" />
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${queryInstanceList}" status="i" var="queryInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td><g:link action="show" params="[uid: queryInstance.uid]">${fieldValue(bean: queryInstance, field: "name")}</g:link></td>
			            <td>${fieldValue(bean: queryInstance, field: "group")}</td>
			            <td>${fieldValue(bean: queryInstance, field: "format")}</td>
			            <td>${fieldValue(bean: queryInstance, field: "type")}</td>
			          </tr>
			        </g:each>
		        </tbody>
		      </table>
		   </div>
	      <g:paginator total="${queryInstanceTotal}" args="${params}" />
      </div>
    </div>
  </body>
</html>
