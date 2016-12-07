<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="notification.create.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="notification.create.title" /></h1>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
         <g:if test="${flash.message}">
           <div class="message" role="status">${flash.message}</div>
         </g:if>
    
	      <g:hasErrors bean="${notificationInstance}">
	        <ul class="errors" role="alert">
	          <g:eachError bean="${notificationInstance}" var="error">
	            <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
	          </g:eachError>
	        </ul>
	      </g:hasErrors>
	      
	      <g:form url="[resource:notificationInstance, action:'save']" >
	        <fieldset class="form">
	          <g:render template="form"/>
	        </fieldset>
	        <fieldset class="buttons">
	          <g:submitButton name="create" class="save btn btn-success btn-md" value="${message(code: 'default.button.create.label')}" />
	        </fieldset>
	      </g:form>
      </div>
    </div>
  </body>
</html>
