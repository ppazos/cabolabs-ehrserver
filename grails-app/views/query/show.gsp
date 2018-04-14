<%@ page import="com.cabolabs.security.Organization" %><%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem" %><!doctype html>
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
              <th><g:message code="query.show.uid.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.name.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="name"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.type.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="type"/></td>
            </tr>
            <tr>
              <th><g:message code="query.show.isPublic.attr" /></th>
              <td><g:fieldValue bean="${queryInstance}" field="isPublic"/></td>
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
                <th><g:message code="query.show.archetype_id.attr" /></th>
                <th><g:message code="query.show.path.attr" /></th>
                <th><g:message code="query.show.name.attr" /></th>
              </tr>
               <g:each in="${queryInstance.select}" var="s">
                 <!--
                 <span class="property-value" aria-labelledby="select-label">  <g:link controller="dataGet" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></span>
                 -->
                <tr>
                  <td>${s.archetypeId}</td>
                  <td>${s.path}</td>
                  <td>${ArchetypeIndexItem.findByArchetypeIdAndPath(s.archetypeId, s.path).name[session.lang]}</td>
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
                <th><g:message code="query.show.archetype_id.attr" /></th>
                <th><g:message code="query.show.path.attr" /></th>
                <th><g:message code="query.show.name.attr" /></th>
                <th><g:message code="query.show.criteria.attr" /></th>
              </tr>
              <g:each in="${queryInstance.where}" var="w">
                <!-- <span class="property-value" aria-labelledby="where-label"><g:link controller="dataCriteria" action="show" id="${w.id}">${w?.encodeAsHTML()}</g:link></span> -->
                <tr>
                  <td>${w.archetypeId}</td>
                  <td>${w.path}</td>
                  <td>${ArchetypeIndexItem.findByArchetypeIdAndPath(w.archetypeId, w.path).name[session.lang]}</td>
                  <td>${w.toGUI()}</td>
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
          <g:if test="${!queryInstance.isPublic || queryInstance.organizationUid == session.organization.uid}">
            <g:link action="edit" params="[uid:queryInstance?.uid]"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="query.execute.action.edit" /></button></g:link>
          </g:if>
          <g:else>
            <g:message code="query.show.cantEditQueryHelp" args="[Organization.findByUid(queryInstance.organizationUid).name]" />
          </g:else>

          <g:if test="${!queryInstance.isPublic}">
            <g:link controller="resource" action="shareQuery" params="[uid:queryInstance?.uid]"><button type="button" class="btn btn-default btn-md"><span class="fa fa-share fa-fw" aria-hidden="true"></span> <g:message code="query.execute.action.share" /></button></g:link>
          </g:if>

          <g:form method="DELETE" action="delete" style="display:inline">
            <input type="hidden" name="uid" value="${queryInstance.uid}" />
            <button class="btn btn-default btn-md" name="delete" onclick="return confirm('${message(code:'query.execute.action.deleteConfirmation')}');"><span class="fa fa-trash-o fa-fw" aria-hidden="true"></span> <g:message code="query.execute.action.delete" /></button>
          </g:form>
        </div>
      </div>
    </div>

    <script type="text/javascript">
      $.ajax({
         url: '${createLink(controller:"query", action:"export")}',
         data: {uid: '${queryInstance?.uid}', format: 'json'},
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
         data: {uid: '${queryInstance?.uid}', format: 'xml'},
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
