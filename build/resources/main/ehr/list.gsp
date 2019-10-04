<%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="ehr.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="ehr.list.title" /></h1>
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

           <g:form class="form filter" action="list">
             <input type="hidden" name="sort" value="${params.sort}" />
             <input type="hidden" name="order" value="${params.order}" />
             <div class="form-group">
               <label for="ipt_uid"><g:message code="filters.uid" /></label>
               <input type="text" class="form-control" name="uid" id="ipt_uid" placeholder="11111111-1111-1111-1111-111111111111" value="${params?.uid}" />
             </div>
             <div class="form-group">
               <label for="ipt_orguid"><g:message code="filters.organizationUid" /></label>
               <input type="text" class="form-control" name="organizationUid" id="ipt_orguid" placeholder="11111111-1111-1111-1111-111111111111" value="${params?.organizationUid}" />
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
                <g:sortableColumn property="uid" title="${message(code: 'ehr.list.attr.uid')}" params="${params}" />
                <g:sortableColumn property="dateCreated" title="${message(code: 'ehr.list.attr.dateCreated')}" params="[uid: params.uid]" />
                <th><g:message code="ehr.list.attr.subject" /></th>
                <th><g:message code="ehr.list.attr.organization" /></th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${list}" status="i" var="ehrInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td><g:link action="show" params="[uid:ehrInstance.uid]">${fieldValue(bean: ehrInstance, field: "uid")}</g:link></td>
                  <td>${fieldValue(bean: ehrInstance, field: "dateCreated")}</td>
                  <td>${ehrInstance.subject.value}</td>
                  <td><g:link controller="organization" action="show" params="[uid:ehrInstance.organizationUid]">${fieldValue(bean: ehrInstance, field: "organizationUid")}</g:link></td>
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
