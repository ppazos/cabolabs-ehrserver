<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:if test="${params.deleted}">
      <title><g:message code="opt.trash.title" /></title>
    </g:if>
    <g:else>
      <title><g:message code="opt.list.title" /></title>
    </g:else>
    <asset:javascript src="bootstrap-confirmation.min.js" />
    <style>
    .btn-toolbar .btn-group { /* fix for confirmation buttons style */
      float: none;
    }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <g:if test="${params.deleted}">
          <h1><g:message code="opt.trash.title" /></h1>
        </g:if>
        <g:else>
          <h1><g:message code="opt.list.title" /></h1>
        </g:else>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <g:if test="${params.deleted}">
          <g:link action="empty_trash" data-toggle="confirmation" data-title="Are you sure?" data-content="This action can't be undone!">
            <button type="button" class="btn btn-danger btn-md">
              <span class="fa fa-exclamation-triangle" aria-hidden="true"></span> <g:message code="operationalTemplate.empty_trash.label" />
            </button></g:link>
          </g:if>
          <g:else>
            <button type="button" class="btn btn-default btn-md filter" data-toggle="collapse" href="#collapse-filter">
              <span class="fa fa-filter" aria-hidden="true"></span>
            </button>

            <g:link action="generate">
              <button type="button" class="btn btn-default btn-md">
                <span class="fa fa-refresh" aria-hidden="true"></span> <g:message code="operationalTemplate.generate.label" />
              </button></g:link>

            <g:link action="upload" title="upload operational template">
              <button type="button" class="btn btn-primary btn-md">
                <span class="fa fa-upload" aria-hidden="true"></span>
              </button></g:link>

            <sec:ifAnyGranted roles="ROLE_ADMIN">
              <g:link action="opt_manager_status" title="upload operational template">
                <button type="button" class="btn btn-primary btn-md">
                  <span class="fa fa-eye" aria-hidden="true"></span> <g:message code="operationalTemplate.optManagerStatus.label" />
                </button></g:link>
            </sec:ifAnyGranted>

            <g:link action="trash">
              <button type="button" class="btn btn-default btn-md">
                <span class="fa fa-trash fa-fw" aria-hidden="true"></span> <g:message code="operationalTemplate.trash.label" />
              </button></g:link>
          </g:else>
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
                <label for="ipt_con"><g:message code="opt.list.filter.concept.label" /></label>
                <input type="text" class="form-control" name="concept" id="ipt_con" value="${params?.concept}" />
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
                <g:sortableColumn property="templateId" title="${message(code: 'template.templateId.label', default: 'templateId')}" />
                <g:sortableColumn property="concept" title="${message(code: 'template.concept.label', default: 'concept')}" />
                <g:sortableColumn property="language" title="${message(code: 'template.language.label', default: 'language')}" />
                <g:sortableColumn property="uid" title="${message(code: 'template.uid.label', default: 'uid')}" />
                <g:sortableColumn property="archetypeId" title="${message(code: 'template.archetypeId.label', default: 'root archetype')}" />
                <g:sortableColumn property="versionNumber" title="${message(code: 'template.versionNumber.label', default: 'version')}" />
                <th></th>
              </tr>
            </thead>
            <tbody>
              <g:set var="sameLangTemplates" value="${opts.findAll{ it.lang == session.lang }}" />
              <g:set var="otherLangTemplates" value="${opts.findAll{ it.lang != session.lang }}" />
              <tr><td colspan="7"><h3><g:message code="template.list.accessible_in_current_lang" /></h3></td></tr>
              <g:each in="${sameLangTemplates}" status="i" var="templateInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td>
                    <g:link action="show" params="[uid:templateInstance.uid]" id="${templateInstance.id}">${fieldValue(bean: templateInstance, field: "templateId")}</g:link>
                  </td>
                  <td>${fieldValue(bean: templateInstance, field: "concept")}</td>
                  <td>${fieldValue(bean: templateInstance, field: "language")}</td>
                  <td>${fieldValue(bean: templateInstance, field: "uid")}</td>
                  <td>${fieldValue(bean: templateInstance, field: "archetypeId")}</td>
                  <td>${templateInstance.versionNumber}</td>
                  <td>
                    <g:link action="items" params="[uid: templateInstance.uid]"><g:message code="template.list.template_indexes" /></g:link>
                    <br/>
                    <g:link action="archetypeItems" params="[uid: templateInstance.uid]"><g:message code="template.list.archetype_indexes" /></g:link>
                  </td>
                </tr>
              </g:each>
              <tr>
                <td colspan="7">
                  <h3><g:message code="template.list.not_accessible_in_current_lang" /></h3>
                  <p><g:message code="template.list.not_accessible_in_current_lang.description" /></p>
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
                  <td>${templateInstance.versionNumber}</td>
                  <td>
                    <g:link action="items" params="[uid: templateInstance.uid]"><g:message code="template.list.template_indexes" /></g:link>
                    <br/>
                    <g:link action="archetypeItems" params="[uid: templateInstance.uid]"><g:message code="template.list.archetype_indexes" /></g:link>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
        <g:paginator total="${total}" args="${params}" />
      </div>
    </div>

    <script type="text/javascript">
      $('[data-toggle=confirmation]').confirmation({
        rootSelector: '[data-toggle=confirmation]',
        placement: 'left',
        btnOkClass: 'btn btn-primary',
        btnCancelClass: 'btn btn-default'
      });
    </script>
  </body>
</html>
