<%@ page import="com.cabolabs.security.Organization" %><%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem" %><%@ page import="com.cabolabs.util.QueryUtils" %><!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="query.show.title" /></title>
    <!-- highlight xml and json -->
    <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:javascript src="highlight.pack.js" />
    <!-- xmlToString -->
    <asset:javascript src="xml_utils.js" />
    <style>
      /* displays criteria_builder GUI of ul/li as a tree */
      #criteria_builder, #criteria_builder ul, #criteria_builder li {
        position: relative;
      }
      #criteria_builder li {
        padding-bottom: 10px;
      }
      #criteria_builder ul {
        list-style: none;
        padding-left: 32px;
      }
      #criteria_builder li::before, #criteria_builder li::after {
        content: "";
        position: absolute;
        left: -20px;
      }
      #criteria_builder li::before {
        border-top: 1px solid #000;
        top: 9px;
        width: 15px;
        height: 0;
      }
      #criteria_builder li::after {
        border-left: 1px solid #000;
        height: 100%;
        width: 0px;
        top: 2px;
      }
      #criteria_builder ul > li:last-child::after {
        height: 8px;
      }
      #criteria_builder table {
        margin: 0;
      }
      /* removes the tree from the root nodes */
      #criteria_builder > ul > li::before, #criteria_builder > ul > li::after {
        border: 0;
      }
    </style>
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
              <td>${queryInstance.name[session.lang]}</td>
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
                  <td>
                    <%-- finishes with version or is for any version? --%>
                    <g:if test="${s.archetypeId.matches(/.*v(\d)*$/)}">
                      ${s.archetypeId}
                    </g:if>
                    <g:else>
                      ${s.archetypeId}.*
                    </g:else>
                  </td>
                  <td>${s.path}</td>
                  <td>
                    <g:if test="${s.archetypeId.matches(/.*v(\d)*$/)}">
                      ${ArchetypeIndexItem.findByArchetypeIdAndPath(s.archetypeId, s.path).name[session.lang]}
                    </g:if>
                    <g:else>
                      ${ArchetypeIndexItem.findByArchetypeIdLikeAndPath(s.archetypeId+'%', s.path).name[session.lang]}
                    </g:else>
                  </td>
                </tr>
              </g:each>
            </table>
          </div>
        </g:if>
        <g:if test="${queryInstance?.where}">
	        <h2><g:message code="query.where.label" default="Where" /></h2>
          <g:query_criteria query="${queryInstance}" />
        </g:if>
      </div>
    </div>

<%--
def tree = QueryUtils.getCriteriaTree(queryInstance)
//println tree
println "EXPRESSION: " + QueryUtils.getStringExpression(tree)
println "FULL WHERE: "+ QueryUtils.getFullCriteriaExpressionToSQL(tree)
--%>

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
            <g:link action="edit" id="${queryInstance?.uid}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="query.execute.action.edit" /></button></g:link>
          </g:if>
          <g:else>
            <g:message code="query.show.cantEditQueryHelp" args="[Organization.findByUid(queryInstance.organizationUid).name]" />
          </g:else>

          <g:if test="${!queryInstance.isPublic}">
            <g:link controller="resource" action="shareQuery" id="${queryInstance?.uid}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-share fa-fw" aria-hidden="true"></span> <g:message code="query.execute.action.share" /></button></g:link>
          </g:if>

          <g:form method="DELETE" action="delete" id="${queryInstance.uid}" style="display:inline">
            <!-- <input type="hidden" name="uid" value="${queryInstance.uid}" /> -->
            <button class="btn btn-default btn-md" name="delete" onclick="return confirm('${message(code:'query.execute.action.deleteConfirmation')}');"><span class="fa fa-trash-o fa-fw" aria-hidden="true"></span> <g:message code="query.execute.action.delete" /></button>
          </g:form>
        </div>
      </div>
    </div>

    <script type="text/javascript">
      $.ajax({
         url: '${createLink(controller:"query", action:"export", id:"${queryInstance?.uid}")}',
         data: {format: 'json'},
         success: function(data, textStatus) {
            //console.log(data);
            $('#json').addClass('json');
            $('#json').text(JSON.stringify(data, undefined, 2));
            $('#json').each(function(i, e) { hljs.highlightBlock(e); });
         },
         error: function(XMLHttpRequest, textStatus, errorThrown) {

           console.log(textStatus, errorThrown);
         }
      });
      $.ajax({
         url: '${createLink(controller:"query", action:"export", id:"${queryInstance?.uid}")}',
         data: {format: 'xml'},
         success: function(data, textStatus) {
            //console.log(data);
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
