<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="versionedComposition.show.title" /></title>
    <!-- highlight xml and json -->
    <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:javascript src="highlight.pack.js" />
    <!-- xmlToString -->
    <asset:javascript src="xml_utils.js" />
    <style>
    .icon {
      width: 48px;
      border: none;
    }
    </style>
    <script type="text/javascript">
    $(document).ready(function() {
    
      $('.showCompo').on('click', function(e) {
        
        e.preventDefault();
          
        iframe = $('iframe', '#html_modal');
        iframe[0].src = this.href;
          
        $('#html_modal').modal();
      });
      
      $('#html_modal').on('hidden.bs.modal', function (event) {
        iframe = $('iframe', '#html_modal');
        iframe[0].src = '';
      });
      
      $('.compoXml').on('click', function(e) {
        
        e.preventDefault();
        
        $.ajax({
          url: this.href,
          dataType: 'xml',
          success: function(xml, textStatus)
          {
            console.log('xml', xml);
            $('#xml').addClass('xml');
            $('#xml').text(formatXml( xmlToString(xml) ));
            $('#xml').each(function(i, e) { hljs.highlightBlock(e); });
            
            $('#xml_modal').modal();
          }
        });
        
      });
      
      $('#xml_modal').on('hidden.bs.modal', function (event) {
        $('#xml').text('');
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
              <th><g:message code="composition.attr.templateId" /></th>
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
    
    <div class="modal fade" id="xml_modal" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
         <!--
         <div class="modal-header">
           <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
           <h4 class="modal-title" id="exampleModalLabel">New message</h4>
         </div>
         -->
         <div class="modal-body">
           <pre><code id="xml"></code></pre>
         </div>
         <!--
         <div class="modal-footer">
           <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
           <button type="button" class="btn btn-primary">Send message</button>
         </div>
         -->
        </div>
      </div>
    </div>
    
    <div class="modal fade" id="html_modal" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-body">
            <iframe src="" style="padding:0; margin:0; width:100%; height:540px; border:0;"></iframe>
          </div>
        </div>
      </div>
    </div>
   
  </body>
</html>
