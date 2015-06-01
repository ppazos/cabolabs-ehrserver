<%@ page import="query.Query" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <!-- highlight xml and json -->
    <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:javascript src="highlight.pack.js" />
    <!-- xmlToString -->
    <asset:javascript src="xml_utils.js" />
  </head>
  <body>
    <a href="#show-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list">Consultas</g:link></li>
      </ul>
    </div>
    <div id="show-query" class="content scaffold-show" role="main">
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <ol class="property-list query">
      
        <g:if test="${queryInstance?.name}">
        <li class="fieldcontain">
          <span id="name-label" class="property-label"><g:message code="query.uid.label" default="UID" /></span>
          <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${queryInstance}" field="uid"/></span>
        </li>
        </g:if>
      
        <g:if test="${queryInstance?.name}">
        <li class="fieldcontain">
          <span id="name-label" class="property-label"><g:message code="query.name.label" default="Name" /></span>
          <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${queryInstance}" field="name"/></span>
        </li>
        </g:if>
      
        <g:if test="${queryInstance?.group}">
        <li class="fieldcontain">
          <span id="group-label" class="property-label"><g:message code="query.group.label" default="Group" /></span>
          <span class="property-value" aria-labelledby="group-label"><g:fieldValue bean="${queryInstance}" field="group"/></span>
        </li>
        </g:if>
        
        <%-- No used for now
        <g:if test="${queryInstance?.qarchetypeId}">
        <li class="fieldcontain">
          <span id="qarchetypeId-label" class="property-label"><g:message code="query.qarchetypeId.label" default="Qarchetype Id" /></span>
          <span class="property-value" aria-labelledby="qarchetypeId-label"><g:fieldValue bean="${queryInstance}" field="qarchetypeId"/></span>
        </li>
        </g:if>
        --%>
        
        <g:if test="${queryInstance?.format}">
        <li class="fieldcontain">
          <span id="format-label" class="property-label"><g:message code="query.format.label" default="Format" /></span>
          <span class="property-value" aria-labelledby="format-label"><g:fieldValue bean="${queryInstance}" field="format"/></span>
        </li>
        </g:if>
      
        <g:if test="${queryInstance?.type}">
        <li class="fieldcontain">
          <span id="type-label" class="property-label"><g:message code="query.type.label" default="Type" /></span>
          <span class="property-value" aria-labelledby="type-label"><g:fieldValue bean="${queryInstance}" field="type"/></span>
        </li>
        </g:if>
      
        <g:if test="${queryInstance?.select}">
        <li class="fieldcontain">
          <span id="select-label" class="property-label"><g:message code="query.select.label" default="Select" /></span>
          
          <table>
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
        </li>
        </g:if>
      
        <g:if test="${queryInstance?.where}">
        <li class="fieldcontain">
          <span id="where-label" class="property-label"><g:message code="query.where.label" default="Where" /></span>
          
          <table>
            <tr>
              <th>archetypeId</th>
              <th>path</th>
              <th>operand</th>
              <th>value</th>
            </tr>
            <g:each in="${queryInstance.where}" var="w">
              <!-- <span class="property-value" aria-labelledby="where-label"><g:link controller="dataCriteria" action="show" id="${w.id}">${w?.encodeAsHTML()}</g:link></span> -->
              <tr>
                <td>${w.archetypeId}</td>
                <td>${w.path}</td>
                <td>${w.operand}</td>
                <td>${w.value}</td>
              </tr>
            </g:each>
          </table>
        </li>
        </g:if>
      </ol>
      
      <div class="row">
        <div class="col-md-6">
	       Query as XML:
	       <pre><code id="xml"></code></pre>
	     </div>
	     <div class="col-md-6">
	       Query as JSON:
	       <pre><code id="json"></code></pre>
	     </div>
	   </div>
      
      <g:form>
        <fieldset class="buttons">
          <g:hiddenField name="id" value="${queryInstance?.id}" />
          <g:link class="list" action="execute" params="[uid:queryInstance?.uid]"><g:message code="query.execute.action.execute" /></g:link>
          <g:link class="edit" action="edit" params="[id:queryInstance?.id]"><g:message code="query.execute.action.edit" /></g:link>
          <g:link class="delete" action="delete" params="[id:queryInstance?.id]" onclick="return confirm('${message(code:'query.execute.action.deleteConfirmation')}');"><g:message code="query.execute.action.delete" /></g:link>
        </fieldset>
      </g:form>
      
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