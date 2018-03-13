<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="plan.create.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="plan.create.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
	     <g:if test="${flash.message}">
	       <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>

        <g:hasErrors bean="${account}">
          <ul class="errors" role="alert">
            <g:eachError bean="${account}" var="error">
              <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
            </g:eachError>
          </ul>
        </g:hasErrors>

        <g:form url="[resource:planInstance, action:'save']" >
          <fieldset class="form">
            <g:render template="form"/>
            <div class="btn-toolbar" role="toolbar">
              <g:submitButton name="create" class="save" class="save btn btn-default btn-md" value="${message(code: 'default.button.create.label', default: 'Create')}" />
            </div>
          </fieldset>
        </g:form>
      </div>
    </div>
  </body>
</html>
