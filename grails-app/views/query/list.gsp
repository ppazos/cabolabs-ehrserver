<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="query.list.title" /></title>
    <style>
    .count-results {
     display: none;
    }
    </style>
    <asset:stylesheet src="pnotify.custom.min.css" />
    <asset:javascript src="pnotify.custom.min.js" />
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
            <span class="fa fa-filter" aria-hidden="true"></span>
          </button>

          <g:link action="groups" title="groups">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-sitemap" aria-hidden="true"></span>
            </button></g:link>

          <g:link action="create">
            <button type="button" class="btn btn-primary btn-md">
              <span class="fa fa-plus" aria-hidden="true"></span>
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
	     <g:each in="${queryInstanceList}" var="groupQueries">
          <g:if test="${groupQueries.key == null}">
            <h2><g:message code="query.list.noGroup" /></h2>
          </g:if>
          <g:else>
            <h2>${groupQueries.key.name}</h2>
          </g:else>
          <div>
            <div class="table-responsive">
              <table class="table table-striped table-bordered table-hover">
                <thead>
                  <tr>
                    <g:sortableColumn property="name" title="${message(code: 'query.show.name.attr', default: 'Name')}" />
                    <g:sortableColumn property="group" title="${message(code: 'query.show.group.attr', default: 'Group')}" />
                    <g:sortableColumn property="format" title="${message(code: 'query.show.format.attr', default: 'Format')}" />
                    <g:sortableColumn property="type" title="${message(code: 'query.show.type.attr', default: 'Type')}" />
                    <th class="count-results"></th>
                  </tr>
                </thead>
                <tbody>
                  <g:each in="${groupQueries.value}" status="i" var="queryInstance">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                      <td><g:link action="show" params="[uid: queryInstance.uid]">${fieldValue(bean: queryInstance, field: "name")}</g:link></td>
                      <td>${fieldValue(bean: queryInstance, field: "group")}</td>
                      <td>${fieldValue(bean: queryInstance, field: "format")}</td>
                      <td>${fieldValue(bean: queryInstance, field: "type")}</td>
                      <td id="${queryInstance.uid}" class="count-results"></td>
                    </tr>
                  </g:each>
                </tbody>
              </table>
            </div>
            <g:if test="${groupQueries.key != null}">
              <div class="btn-toolbar" role="toolbar">
                <fieldset class="buttons">
                  <g:link url="[action: 'executeCountGroup', params: [uid: groupQueries.key.uid]]" class="execute_count"><button type="button" class="btn btn-default btn-md"><span class="fa fa-cog" aria-hidden="true"></span> <g:message code="default.button.execute_count.label" /></button></g:link>
                </fieldset>
              </div>
            </g:if>
          </div>
          <hr/>
        </g:each>
	     <g:paginator total="${queryInstanceTotal}" args="${params}" />
      </div>
    </div>

    <script>
    $('.execute_count').on('click', function(e) {

      button = $(this);

      e.preventDefault();

      icon = $('span', this);
      icon.addClass('fa-spin');

      new PNotify({
         title: '${g.message(code:"query.list.executing_count")}',
         text : '${g.message(code:"query.list.executing_count_text")}',
         type : 'info',
         styling: 'bootstrap3',
         history: false
      });

      $.ajax({
        method: 'GET',
        url: this.href,
        dataType: 'json'
      })
      .done(function( res ) {

        //console.log(res); // [queryUid: #ehrs, queryUid: #ehrs, ...]

        new PNotify({
         title: '${g.message(code:"query.list.executing_count_done")}',
         text : '${g.message(code:"query.list.executing_count_result")}',
         type : 'info',
         styling: 'bootstrap3',
         history: false
        });

        for (queryUid in res)
        {
          // result cotainer
          $('#'+queryUid).text(res[queryUid]);
        };

        table_container = button.closest('.btn-toolbar').siblings('.table-responsive');

        $('.count-results', table_container).show();

        icon.removeClass('fa-spin');
      })
      .fail(function(resp,status,status_msg) {

        console.log(resp);

        icon.removeClass('fa-spin');
      });
    });
    </script>
  </body>
</html>
