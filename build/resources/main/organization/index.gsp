<%@ page import="com.cabolabs.security.Organization" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="organization.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="organization.list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <button type="button" class="btn btn-default btn-md filter" data-toggle="collapse" href="#collapse-filter">
            <span class="fa fa-filter" aria-hidden="true"></span>
          </button>
          <g:link action="create">
            <button type="button" class="btn btn-primary btn-md">
              <span class="fa fa-plus" aria-hidden="true"></span>
            </button>
          </g:link>
        </div>
      </div>
    </div>

    <div class="row row-grid collapse" id="collapse-filter">
      <div class="col-md-12">
        <div class="panel panel-default">
          <div class="panel-body">

            <g:form class="form filter" action="index">
              <input type="hidden" name="sort" value="${params.sort}" />
              <input type="hidden" name="order" value="${params.order}" />
              <div class="form-group">
                <label for="name">Name</label>
                <input type="text" class="form-control" name="name" id="name" value="${params?.name}" />
              </div>
              <div class="form-group">
                <label for="number">Number</label>
                <input type="text" class="form-control" name="number" id="number" value="${params?.number}" />
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
	       <div class="alert alert-info" role="alert">${flash.message}</div>
	     </g:if>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
		      <thead>
	           <tr>
	             <g:sortableColumn property="name" title="${message(code: 'organization.name.label', default: 'Name')}" />
	             <g:sortableColumn property="number" title="${message(code: 'organization.number.label', default: 'Number')}" />
	             <g:sortableColumn property="uid" title="${message(code: 'organization.uid.label', default: 'Uid')}" />
	           </tr>
		      </thead>
		      <tbody>
		        <g:each in="${organizationInstanceList}" status="i" var="organizationInstance">
		          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
		            <td><g:link action="show" id="${organizationInstance.uid}">${fieldValue(bean: organizationInstance, field: "name")}</g:link></td>
		            <td>${fieldValue(bean: organizationInstance, field: "number")}</td>
		            <td>${fieldValue(bean: organizationInstance, field: "uid")}</td>
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
