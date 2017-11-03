<%@ page import="com.cabolabs.ehrserver.query.EhrQuery" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="ehrquery.show.title" /></title>
    <asset:stylesheet src="pnotify.custom.min.css" />
    <asset:javascript src="pnotify.custom.min.js" />
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1 class="hidden_uid"><g:message code="ehrquery.show.title" /></h1>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-lg-12">
        
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="ehrquery.attr.name" /></th>
              <td>${ehrQueryInstance.name}</td>
            </tr>
             <tr>
              <th><g:message code="ehrquery.attr.description" /></th>
              <td>${ehrQueryInstance.description}</td>
            </tr>
            <tr>
              <th><g:message code="ehrquery.attr.queries" /></th>
              <td>
                <g:each in="${ehrQueryInstance.queries}" var="q">
                  <div class="property-value" aria-labelledby="queries-label">
                    <g:link controller="query" action="show" params="[uid: q.uid]">${q.name} (${q.uid})</g:link>
                  </div>
                </g:each>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div class="btn-toolbar" role="toolbar">
          <fieldset class="buttons">
            <g:link url="[action: 'execute', id: ehrQueryInstance.id]" elementId="execute"><button type="button" class="btn btn-default btn-md"><span class="fa fa-cog" aria-hidden="true"></span> <g:message code="default.button.execute.label" default="Execute" /></button></g:link>
            <g:link action="edit" id="${ehrQueryInstance.id}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
          </fieldset>
        </div>
        
      </div>
    </div>
    
    <script>
    $('#execute').on('click', function(e) {
    
      console.log(this.href);
      e.preventDefault();
      
      icon = $('span', this);
      icon.addClass('fa-spin');
      
      ehr_show_url = '${createLink(controller:"ehr", action:"show")}';
      
      new PNotify({
         title: '${g.message(code:"ehrquery.show.executing")}',
         text : '${g.message(code:"ehrquery.show.executing_text")}',
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
           
        //console.log(res);
        
        
        new PNotify({
         title: '${g.message(code:"ehrquery.show.executing_done")}',
         text : res.length +' ${g.message(code:"ehrquery.show.executing_result")}',
         type : 'info',
         styling: 'bootstrap3',
         history: false
        });
        
        
        $('#results').remove(); // previous results
        $('#page-wrapper').append('<div id="results"><table class="table"><tr><th>#</th><th>EHR UID</th></tr></table></div>');
        
        res.forEach(function(uid, index){
        
          $('table', '#results').append('<tr><td>'+ (index+1) +'</td><td><a href="'+ ehr_show_url +'?uid='+ uid +'">'+ uid +'</a></td></tr>');
        });
        
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
