<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.opt_man_list.title" /></title>
    <style>
    .btn-toolbar .btn-group { /* fix for confirmation buttons style */
      float: none;
    }
      .align-label {
        padding: .375em .75em; /* fixes vertical alignment of label for inline form */
      }
      .results label {
        padding-top: 5px;
        padding-bottom: 5px;
      }
      .results div {
        padding-left: 10px;
        padding-top: 5px;
        padding-bottom: 5px;
      }
      .COMPOSITION {
        background-color: #fabe6e;
      }
      .EVENT_CONTEXT, .HISTORY {
        background-color: #f69c6b;
      }
      .SECTION, .EVENT, .POINT_EVENT, .INTERVAL_EVENT {
        background-color: #ffdc4f;
      }
      .OBSERVATION, .EVALUATION, .INSTRUCTION, .ACTION, .ADMIN_ENTRY {
        background-color: #cdeefa;
      }
      .ITEM_TREE, .ITEM_LIST, .ITEM_TABLE, .ITEM_SINGLE {
        background-color: #dbe7f3;
      }
      .CLUSTER {
        background-color: #eb839a;
      }
      .ELEMENT {
        background-color: #c58fc0;
      }
      .DV_TEXT, .DV_CODED_TEXT, .DV_DATE, .DV_DATE_TIME, .DV_QUANTITY {
        background-color: #a6d585;
      }
      .fa-stack {
        margin-right: 5px;
        font-size: 0.6em;
        vertical-align: top;
      }
      .results h3 {
        margin-top: 0;
      }
      .results h3 i {
        margin-left: 5px;
      }
      .results h3 > i.fa-caret-up,
      .results h3.folded > i.fa-caret-down {
        display: none;
      }
      .results h3 > i.fa-caret-down,
      .results h3.folded > i.fa-caret-up {
        display: inline;
      }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="opt.opt_man_list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-md-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="generate">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-refresh" aria-hidden="true"></span> <g:message code="operationalTemplate.generate.label" />
            </button></g:link>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
        <h2><g:message code="opt.opt_man_list.forOrganization" /></h2>
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

    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">

            <g:each in="${optMap}" status="i" var="optId_opt">
              <tr>
                <td>${optId_opt.key}
                </td>
                <td class="results">
                  <g:displayOPTTree opt="${optId_opt.value}" />
                </td>
              </tr>
            </g:each>

          </table>
        </div>
      </div>
    </div>
    <script>
    $('.row h3').on('click', function(){
      it = $(this);
      if (!it.hasClass('folded'))
      {
        it.parent().siblings().hide();
        it.addClass('folded');
      }
      else
      {
        it.parent().siblings().show();
        it.removeClass('folded');
      }
    });
    </script>
  </body>
</html>
