<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'versionedComposition.label', default: 'VersionedComposition')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <style>
     .icon {
       width: 64px;
       border: none;
     }
     </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
       <g:if test="${flash.message}">
         <div class="message" role="status">${flash.message}</div>
       </g:if>

        <div class="control-group">
          <label class="control-label"><g:message code="versionedComposition.uid.label" default="UID" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${versionedCompositionInstance}" field="uid"/></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="versionedComposition.ehrUid.label" default="EHR UID" /></label>
          <div class="controls">
            <p class="form-control-static">${versionedCompositionInstance.ehr.uid}</p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="versionedComposition.isPersistent.label" default="Is persistent" /></label>
          <div class="controls">
            <p class="form-control-static"><g:formatBoolean boolean="${versionedCompositionInstance?.isPersistent}" /></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="versionedComposition.timeCreated.label" default="Time created" /></label>
          <div class="controls">
            <p class="form-control-static"><g:formatDate date="${versionedCompositionInstance?.timeCreated}" /></p>
          </div>
        </div>

        <h2>Versions</h2>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
            <tr>
		        <th>uid</th>
		        <th>creation date</th>
		        <th>type</th>
		        <th>change type</th>
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
  </body>
</html>
