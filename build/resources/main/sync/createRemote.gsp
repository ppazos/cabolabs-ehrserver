<%@ page import="com.cabolabs.ehrserver.account.ApiKey" %><!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="sync.createRemote.title" /></title>
    <style>
    .checkbox .cr{
      margin-top: 5px;
    }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="sync.createRemote.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
          <g:if test="${flash.message}">
            <div class="alert alert-info" role="alert">${flash.message}</div>
          </g:if>
          <g:form action="saveRemote" >
            <fieldset class="form">
              <div class="form-group required">
                <label for="remoteServerName">
                  <g:message code="remote.attr.remoteServerName" />
                  <span class="required-indicator">*</span>
                </label>
                <g:textField name="remoteServerName" required="" class="form-control" />
              </div>

              <div class="form-group required">
                <label for="remoteAPIKey">
                  <g:message code="remote.attr.remoteAPIKey" />
                  <span class="required-indicator">*</span>
                </label>
                <g:textArea name="remoteAPIKey" required="" class="form-control" />
              </div>

              <div class="form-group required">
                <label for="remoteServerIP">
                  <g:message code="remote.attr.remoteServerIP" />
                  <span class="required-indicator">*</span>
                </label>
                <g:textField name="remoteServerIP" required="" class="form-control" />
              </div>

              <div class="form-group required">
                <label for="remoteServerPort">
                  <g:message code="remote.attr.remoteServerPort" />
                  <span class="required-indicator">*</span>
                </label>
                <g:textField name="remoteServerPort" required="" class="form-control" />
              </div>

              <div class="form-group required">
                <label for="remoteServerPath">
                  <g:message code="remote.attr.remoteServerPath" />
                  <span class="required-indicator">*</span>
                </label>
                <g:textField name="remoteServerPath" required="" class="form-control" />
              </div>

              <div class="form-group checkbox">
                <label for="isActive">
                  <g:message code="remote.attr.isActive" />
                  <g:checkBox name="isActive" />
                  <span class="cr"><i class="cr-icon fa fa-check"></i></span>
                </label>
              </div>

              <div style="text-align:right;">
                <g:submitButton name="doit" class="save btn btn-success btn-md" value="${message(code: 'default.button.create.label')}" />
              </div>
            </fieldset>
          </g:form>
      </div>
    </div>
  </body>
</html>
