<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="query.show.title" /></title>
    <!-- highlight xml and json -->
    <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:javascript src="highlight.pack.js" />
    <!-- xmlToString -->
    <asset:javascript src="xml_utils.js" />
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="query.show.title" /></h1>
        
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div>
        </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="query.show.uid.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.name.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="name"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.group.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="group"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.format.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="format"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.type.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="type"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.template_id.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="templateId"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.criteria.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="criteriaLogic"/></td>
            </tr>
          </tbody>
        </table>

        <g:if test="${queryInstance?.select}">
          <h2><g:message code="query.select.label" default="Select" /></h2>
          <div class="table-responsive">
            <table class="table table-striped table-bordered table-hover">
              <tr>
                <th>archetypeId</th>
                <th>path</th>
              </tr>
               <g:each in="${queryInstance.select}" var="s">
                 <!--
                 <span class="property-value" aria-labelledby="select-label">  <g:link controller="dataGet" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></span>
                 -->
                <tr>
                  <td>${s.archetypeId}</td>
                  <td>${s.path}</td>
                </tr>
              </g:each>
            </table>
          </div>
        </g:if>
        <g:if test="${queryInstance?.where}">
	       <h2><g:message code="query.where.label" default="Where" /></h2>
          <div class="table-responsive">
            <table class="table table-striped table-bordered table-hover">
              <tr>
                <th>archetypeId</th>
                <th>path</th>
                <th>conditions</th>
              </tr>
              <g:each in="${queryInstance.where}" var="w">
                <!-- <span class="property-value" aria-labelledby="where-label"><g:link controller="dataCriteria" action="show" id="${w.id}">${w?.encodeAsHTML()}</g:link></span> -->
                <tr>
                  <td>${w.archetypeId}</td>
                  <td>${w.path}</td>
                  <td>${w.toSQL()}</td>
                </tr>
              </g:each>
            </table>
          </div>
        </g:if>
      </div>
    </div>
    
    <div class="row">
      <div class="col-md-6">
        <g:message code="query.show.query_xml.label" />
        <pre><code id="xml"></code></pre>
      </div>
      <div class="col-md-6">
        <g:message code="query.show.query_json.label" />
        <pre><code id="json"></code></pre>
      </div>
    </div>
     
    <div class="row">
      <div class="col-lg-12">
        <div class="btn-toolbar" role="toolbar">
          <g:link action="edit" params="[id:queryInstance?.id]"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="query.execute.action.edit" /></button></g:link>
          
          <g:form method="DELETE" action="delete" style="display:inline">
            <input type="hidden" name="id" value="${queryInstance.id}" />
            <g:submitButton class="btn btn-default btn-md" name="delete" value="${g.message(code:'query.execute.action.delete')}" onclick="return confirm('${message(code:'query.execute.action.deleteConfirmation')}');" />
          </g:form>
        </div>
      </div>
    </div>
    
    <script type="text/javascript">
      $.ajax({
         url: '${createLink(controller:"query", action:"export")}',
         data: {id: ${queryInstance?.id}, format: 'json'},
         success: function(data, textStatus) {
            console.log(data);
            $('#json').addClass('json');
            $('#json').text(JSON.stringify(data, undefined, 2));
            $('#json').each(function(i, e) { hljs.highlightBlock(e); });
         },
         error: function(XMLHttpRequest, textStatus, errorThrown) {
           
           console.log(textStatus, errorThrown);
         }
      });
      $.ajax({
          url: '${createLink(controller:"query", action:"export")}',
          data: {id: ${queryInstance?.id}, format: 'xml'},
          success: function(data, textStatus) {
             console.log(data);
             $('#xml').addClass('xml');
             $('#xml').text(formatXml( xmlToString(data) ));
             $('#xml').each(function(i, e) { hljs.highlightBlock(e); });
          },
          error: function(XMLHttpRequest, textStatus, errorThrown) {
            
            console.log(textStatus, errorThrown);
          }
        });
    </script>
  </body>
</html>
