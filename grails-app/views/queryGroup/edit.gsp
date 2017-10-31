<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="queryGroup.edit.label" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="queryGroup.edit.label" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${queryGroupInstance}">
          <ul class="errors" role="alert">
            <g:eachError bean="${queryGroupInstance}" var="error">
              <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
            </g:eachError>
          </ul>
        </g:hasErrors>
        <g:form url="[params: [uid: queryGroupInstance.uid], action:'editGroup']" >
          <fieldset class="form">
            <g:render template="/queryGroup/form"/>
            <div style="text-align:right;">
              <g:submitButton name="doit" class="save btn btn-success btn-md" value="${message(code: 'default.button.edit.label')}" />
            </div>
          </fieldset>
        </g:form>
      </div>
    </div>
  </body>
</html>
