
<%@ page import="common.change_control.Contribution" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
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
          //console.log( modal.children()[0] );
          //console.log( this.href );
          
          modal.children()[0].src = this.href;
          
          $.blockUI({
            message: modal,
            css: {
              width: '960px',
              height: '600px',
              top: '10px',
              left:'auto',
              padding: '10px'
            },
            onOverlayClick: $.unblockUI
          });
      });
    });
    </script>
  </head>
  <body>
    <a href="#show-contribution" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="show-contribution" class="content scaffold-show" role="main">
      <h1>
        <g:message code="default.show.label" args="[entityName]" />
        <span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${contributionInstance}" field="uid"/></span>
      </h1>
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      <ol class="property-list contribution">
      
        <li class="fieldcontain">
          <span id="uid-label" class="property-label"><g:message code="contribution.uid.label" default="Uid" /></span>
          
        </li>

        <li class="fieldcontain">
          <span id="audit-label" class="property-label"><g:message code="contribution.audit.label" default="Audit details" /></span>
          <span class="property-value" aria-labelledby="audit-label">
            system id: ${contributionInstance?.audit?.systemId}<br/>
            time committed: ${contributionInstance?.audit?.timeCommitted}<br/>
            committer: ${contributionInstance?.audit?.committer?.name} ${contributionInstance?.audit?.committer?.value}
          </span>
        </li>
      
        <g:if test="${contributionInstance?.versions}">
          <li class="fieldcontain">
            <span id="versions-label" class="property-label">
              <g:message code="contribution.versions.label" default="Versions" />
              (${contributionInstance.versions.size()})
            </span>
            <table id="versions">
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
          </li>
        </g:if>
      
        <%-- Modal para mostrar el contenido de una composition --%>
        <div id="composition_modal" style="width:960px; height:600px; display:none;"><iframe src="" style="padding:0; margin:0; width:960px; height:600px; border:0;"></iframe></div>

      </ol>
    </div>
  </body>
</html>
