<%@ page import="com.cabolabs.ehrserver.reporting.ActivityLog" %><%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.CommitLog" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="activityLog.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="activityLog.show.title" /></h1>
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
              <th><g:message code="activityLog.timestamp.label" default="Timestamp" /></th>
              <td><g:formatDate date="${activityLogInstance?.timestamp}" /></td>
            </tr>
            <tr>
              <th><g:message code="user.attr.username" default="Username" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="username"/></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.organizationUid.label" default="Organization UID" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="organizationUid" /></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.action.label" default="Action" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="action"/></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.objectId.label" default="Object Id" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="objectId"/></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.objectUid.label" default="Object UID" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="objectUid"/></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.clientIp.label" default="Client Ip" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="clientIp"/></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.remoteAddr.label" default="remoteAddr" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="remoteAddr" /></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.xForwardedFor.label" default="xForwardedFor" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="xForwardedFor" /></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.referer.label" default="referer" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="referer" /></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.requestURL.label" default="URL" /></th>
              <td><g:fieldValue bean="${activityLogInstance}" field="requestURL" /></td>
            </tr>
            <tr>
              <th><g:message code="activityLog.contents.label" default="Contents" /></th>
              <td><textarea class="form-control" rows="15">${contents}</textarea></td>
            </tr>
          </tbody>
        </table>

        <g:if test="${activityLogInstance instanceof CommitLog}">
          <table class="table table-bordered" style="margin:0;">
            <tr>
              <th>EHR</th>
              <th>Contribution</th>
              <th>Type</th>
              <th>Locale</th>
              <th>Successful commit?</th>
            </tr>
            <tr>
              <td>${activityLogInstance.ehrUid}</td>
              <td>${activityLogInstance.objectUid}</td>
              <td>${activityLogInstance.contentType}</td>
              <td>${activityLogInstance.locale}</td>
              <td>${activityLogInstance.success}</td>
            </tr>
          </table>
        </g:if>

      </div>
    </div>
  </body>
</html>
