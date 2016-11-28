<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="template.show.title" /></title>
    <!-- highlight xml and json -->
    <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:javascript src="highlight.pack.js" />
    <!-- xmlToString -->
    <asset:javascript src="xml_utils.js" />
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="template.show.title" /></h1>
        
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div>
        </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="template.templateId.label" /></th>
              <td><g:fieldValue bean="${opt}" field="templateId"/></td>
            </tr>
            <tr>
              <th><g:message code="template.concept.label" /></th>
              <td><g:fieldValue bean="${opt}" field="concept"/></td>
            </tr>
            <tr>
              <th><g:message code="template.language.label" /></th>
              <td><g:fieldValue bean="${opt}" field="language"/></td>
            </tr>
            <tr>
              <th><g:message code="template.uid.label" /></th>
              <td><g:fieldValue bean="${opt}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="template.archetypeId.label" /></th>
              <td><g:fieldValue bean="${opt}" field="archetypeId"/></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <div class="row">
      <div class="col-md-12">
        <g:if test="${!opt.isPublic}">
          <g:link controller="resource" action="shareOpt" params="[uid:opt?.uid]"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="opt.show.action.share" /></button></g:link>
        </g:if>
      </div>
    </div>
    
    <div class="row">
      <div class="col-md-12">
        <g:message code="common.format.xml" />
        <pre><code id="xml"></code></pre>
      </div>
    </div>
    
    <script type="text/javascript">
       $('#xml').addClass('xml');
       // The first replace removes the new lines and empty spaces of indentation
       // The second escapes single quotes that might appear in the text of the XML that breaks the javascript
       $('#xml').text(formatXml( '${opt_xml.normalize().replaceAll(/\n(\s)*/,'').replaceAll("'", "\\\\'")}' ));
       $('#xml').each(function(i, e) { hljs.highlightBlock(e); });
    </script>
  </body>
</html>
