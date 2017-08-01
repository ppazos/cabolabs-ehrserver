<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="versionedComposition.show.title" /></title>
    <style>
    .icon {
      width: 48px;
      border: none;
    }
    </style>
    <asset:javascript src="jquery.blockUI.js" />
    <script type="text/javascript">
    $(document).ready(function() {
    
      $('#versions').on('click', '.showCompo', function(e) {
        
          e.preventDefault();
          
          modal = $('#composition_modal');
          
          modal.children()[0].src = this.href;
          
          $.blockUI({
            message: modal,
            css: {
              width: '94%',
              height: '94%',
              top : '3%',
              left: '3%',
              padding: '10px'
            },
            onOverlayClick: $.unblockUI
          });
      });
    });
    </script>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="versionedComposition.show.title" /></h1>
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
              <th><g:message code="versionedComposition.attr.uid" /></th>
              <td><g:fieldValue bean="${versionedCompositionInstance}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="versionedComposition.attr.ehr" /></th>
              <td><g:link controller="ehr" action="show" params="[uid: versionedCompositionInstance.ehr.uid]">${versionedCompositionInstance.ehr.uid}</g:link></td>
            </tr>
            <tr>
              <th><g:message code="versionedComposition.attr.isPersistent" /></th>
              <td><g:formatBoolean boolean="${versionedCompositionInstance?.isPersistent}" /></td>
            </tr>
            <tr>
              <th><g:message code="versionedComposition.attr.timeCreated" /></th>
              <td><g:formatDate date="${versionedCompositionInstance?.timeCreated}" /></td>
            </tr>
          </tbody>
        </table>

        <h2>Versions</h2>
        <div class="table-responsive" id="versions">
          <table class="table table-striped table-bordered table-hover">
            <tr>
		        <th><g:message code="version.attr.uid" /></th>
		        <th><g:message code="composition.attr.startTime" /></th>
		        <th><g:message code="composition.attr.archetypeId" /></th>
		        <th><g:message code="audit.attr.changeType" /></th>
		        <th></th>
		      </tr>
            <g:each in="${versionedCompositionInstance.allVersions}" var="version">
              <%-- ${version.uid}<br/> --%><!-- TODO: version row template + diff -->
              <g:render template="../version/versionRow" model="[version:version]"/>
            </g:each>
          </table>
        </div>
      </div>
    </div>
    
    <%-- Modal para mostrar el contenido de una composition --%>
    <div id="composition_modal" style="width:100%; height:100%; display:none;"><iframe src="" style="padding:0; margin:0; width:100%; height:100%; border:0;"></iframe></div>
   
  </body>
</html>
