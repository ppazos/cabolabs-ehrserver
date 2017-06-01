<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="query.share.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="query.share.title" /></h1>
        </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div><br/>
        </g:if>

        <p>The share with the current organization wont be deleted if you unselect it here, 
        because the query can't be accessed if it is not shared with the current organization..</p>

        <g:form controller="resource" action="saveSharesQuery">
          <input type="hidden" name="uid" value="${query.uid}" />
          <g:selectWithCurrentUserOrganizations name="organizationUid" value="${organizations.uid}" class="form-control" />
          <br/>
          <div class="btn-toolbar" role="toolbar">
            <input type="submit" class="btn btn-default btn-md" value="Update shares" />
            <g:link controller="query" action="show" params="[uid:query?.uid]"><button type="button" class="btn btn-default btn-md"><g:message code="query.execute.action.back" /></button></g:link>
          </div>
        </g:form>
      </div>
    </div>
    <%--${g.message(code:'query.execute.action.delete')}--%>
  </body>
</html>
