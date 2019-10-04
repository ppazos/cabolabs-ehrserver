<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="versionedComposition.list.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="versionedComposition.list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <button type="button" class="btn btn-default btn-md filter" data-toggle="collapse" href="#collapse-filter">
            <span class="fa fa-filter" aria-hidden="true"></span>
          </button>
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
                <label for="ipt_ehr"><g:message code="versionedComposition.attr.ehr" /></label>
                <input type="text" class="form-control" name="ehdUid" id="ipt_ehr" placeholder="11111111-1111-1111-1111-111111111111" value="${params?.ehdUid}" />
              </div>
              <div class="form-group">
                <label for="organizationUid"><g:message code="entity.organization" /></label>
                <g:select name="organizationUid" from="${organizations}"
				              optionKey="uid" optionValue="name"
				              noSelection="${['':message(code:'defaut.select.selectOne')]}"
                          value="${params?.organizationUid ?: ''}" class="form-control" />
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
                <g:sortableColumn property="uid" title="${message(code:'versionedComposition.attr.uid')}" params="${params}" />
                <th>${message(code: 'versionedComposition.attr.ehr')}</th>
                <g:sortableColumn property="isPersistent" title="${message(code:'versionedComposition.attr.isPersistent')}" params="${params}" />
                <g:sortableColumn property="timeCreated" title="${message(code:'versionedComposition.attr.timeCreated')}" params="${params}" />
                <th>${message(code: 'composition.attr.templateId')}</th>
                <th>${message(code: 'composition.attr.archetypeId')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${versionedCompositionInstanceList}" status="i" var="versionedCompositionInstance">
                <g:set var="compo" value="${versionedCompositionInstance.latestVersion.data}" />
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td><g:link action="show" params="[uid: versionedCompositionInstance.uid]">${fieldValue(bean: versionedCompositionInstance, field: "uid")}</g:link></td>
                  <td><g:link controller="ehr" action="show" params="[uid: versionedCompositionInstance.ehr.uid]">${versionedCompositionInstance.ehr.uid}</g:link></td>
                  <td><g:formatBoolean boolean="${versionedCompositionInstance.isPersistent}" /></td>
                  <td><g:formatDate date="${versionedCompositionInstance.timeCreated}" /></td>
                  <td>${compo.templateId}</td>
                  <td>${compo.archetypeId}</td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
        <g:paginator total="${versionedCompositionInstanceCount}" args="${params}" />
      </div>
    </div>
  </body>
</html>
