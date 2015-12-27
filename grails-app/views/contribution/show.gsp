<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.Contribution" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'contribution.label', default: 'Contribution')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
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
	     <h1>
	       <g:message code="default.show.label" args="[entityName]" />
	     </h1>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
	      <g:if test="${flash.message}">
	        <div class="message" role="status">${flash.message}</div>
	      </g:if>
	      
	      <div class="control-group">
            <label class="control-label"><g:message code="contribution.uid.label" default="Uid" /></label>
            <div class="controls">
                <p class="form-control-static"><g:fieldValue bean="${contributionInstance}" field="uid"/></p>
            </div>
         </div>
         
         <h2><g:message code="contribution.audit.label" default="Audit details" /></h2>
         
         <div class="control-group">
            <label class="control-label"><g:message code="contribution.systemId.label" default="System id" /></label>
            <div class="controls">
                <p class="form-control-static">${contributionInstance?.audit?.systemId}</p>
            </div>
         </div>
         <div class="control-group">
            <label class="control-label"><g:message code="contribution.timeCommitted.label" default="Time committed" /></label>
            <div class="controls">
                <p class="form-control-static">${contributionInstance?.audit?.timeCommitted}</p>
            </div>
         </div>
         <div class="control-group">
            <label class="control-label"><g:message code="contribution.committer.label" default="Committer" /></label>
            <div class="controls">
                <p class="form-control-static">${contributionInstance?.audit?.committer?.name} ${contributionInstance?.audit?.committer?.value}</p>
            </div>
         </div>
	      
	      <h2><g:message code="contribution.versions.label" default="Committed versions" /></h2>
	      
	      <div class="control-group">
            <label class="control-label"><g:message code="contribution.versions.label" default="Versions" /></label>
            <div class="controls">
                <p class="form-control-static">${contributionInstance.versions.size()}</p>
            </div>
         </div>

	      <g:if test="${contributionInstance?.versions}">
           <div class="table-responsive" id="versions">
             <table class="table table-striped table-bordered table-hover">
               <tr>
                 <th>uid</th>
                 <th>start time</th>
                 <th>type</th>
                 <th>change type</th>
                 <th></th>
               </tr>
               <g:each in="${contributionInstance.versions}" var="version">
				     <g:render template="../version/versionRow" model="[version:version]"/>
				   </g:each>
             </table>
           </div>
         </g:if>
      </div>
    </div>
    
    <%-- Modal para mostrar el contenido de una composition --%>
    <div id="composition_modal" style="width:100%; height:100%; display:none;"><iframe src="" style="padding:0; margin:0; width:100%; height:100%; border:0;"></iframe></div>
   
  </body>
</html>
