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
        <h1><g:message code="template.show.title" /></h1>
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
            <tr>
              <th><g:message code="template.versionNumber.label" default="Version" /></th>
              <td>${opt.versionNumber}</td>
            </tr>
            <tr>

            </tr>
          </tbody>
        </table>

        <div class="btn-toolbar" role="toolbar">
          <fieldset class="buttons">
            <g:if test="${opt.isActive}">
              <g:link action="deactivate" params="[uid: opt.uid]" data-toggle="confirmation" data-title="Are you sure?"><button type="button" class="btn btn-danger btn-md"><span class="fa fa-ban fa-fw" aria-hidden="true"></span> <g:message code="opt.actions.deactivate" /></button></g:link>
            </g:if>
            <g:else>
              <g:link action="activate" params="[uid: opt.uid]" data-toggle="confirmation" data-title="Are you sure?"><button type="button" class="btn btn-success btn-md"><span class="fa fa-eye fa-fw" aria-hidden="true"></span> <g:message code="opt.actions.activate" /></button></g:link>
            </g:else>
            <g:link class="delete" action="delete" params="[uid: opt.uid]" data-toggle="confirmation" data-title="Are you sure?"><button type="button" class="btn btn-danger btn-md"><span class="fa fa-trash fa-fw" aria-hidden="true"></span> <g:message code="default.button.delete.label" default="Delete" /></button></g:link>
          </fieldset>
        </div>

      </div>
    </div>

    <h2><g:message code="template.versions.label" /></h2>

    <div class="table-responsive">
      <table class="table table-striped table-bordered table-hover table-org-roles">
        <thead>
          <tr>
            <th><g:message code="template.templateId.label" default="Template ID"/></th>
            <th><g:message code="template.uid.label" default="UID"/></th>
            <th><g:message code="template.versionNumber.label" default="Version" /></th>
            <th><g:message code="template.createdOn.label" /></th>
            <th><g:message code="template.isActive.label" default="Active" /></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${versions}" var="opt_version">
            <tr>
              <td>${opt_version.templateId}</td>
              <td>${opt_version.uid}</td>
              <td>${opt_version.versionNumber}</td>
              <td><g:formatDate date="${opt_version.dateCreated}" format="yyyy-MM-dd HH:mm:ss Z"/></td>
              <td>${opt_version.isActive.toString()}</td>
            </tr>
          </g:each>
        </tbody>
      </table>
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

      $('[data-toggle=confirmation]').confirmation({
        rootSelector: '[data-toggle=confirmation]',
        placement: 'left',
        btnOkClass: 'btn btn-primary',
        btnCancelClass: 'btn btn-default'
      });
    </script>
  </body>
</html>
