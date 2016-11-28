<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.share.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="opt.share.title" /></h1>
        </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div><br/>
        </g:if>

        <p>The share with the current organization wont be deleted if you unselect it here, 
        because the query can't be accessed if it is not shared with the current organization..</p>

        <g:form controller="resource" action="saveSharesOpt">
          <input type="hidden" name="uid" value="${opt.uid}" />
          <g:selectWithCurrentUserOrganizations name="organizationUid" value="${organizations.uid}" class="form-control" />
          <br/>
          <div class="btn-toolbar" role="toolbar">
            <input type="submit" class="btn btn-default btn-md" value="Update shares" />
            <g:link controller="operationalTemplate" action="show" params="[uid:opt?.uid]"><button type="button" class="btn btn-default btn-md"><g:message code="common.action.back" /></button></g:link>
          </div>
        </g:form>
      </div>
    </div>
  </body>
</html>
