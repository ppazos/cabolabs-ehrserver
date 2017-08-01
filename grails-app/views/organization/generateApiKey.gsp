<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="organization.generateApiKey.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="organization.generateApiKey.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
          <g:if test="${flash.message}">
            <div class="alert alert-info" role="alert">${flash.message}</div>
          </g:if>
          <g:form action="generateApiKey" >
            <input type="hidden" name="uid" value="${params.uid}" />
            <fieldset class="form">
              <div class="form-group required">
                <label for="systemId">
                  <g:message code="apikey.attr.systemId" />
                  <span class="required-indicator">*</span>
                </label>
                <g:textField name="systemId" required="" class="form-control" />
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
