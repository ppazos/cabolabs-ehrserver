<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
    <title><g:message code="default.register.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
	     <h1><g:message code="default.register.label" args="[entityName]" /></h1>
	     
	     <g:if test="${flash.message}">
	       <div class="message" role="status"><g:message code="${flash.message}" /></div>
	     </g:if>
	     
	     <g:hasErrors bean="${userInstance}">
	       <ul class="errors" role="alert">
	         <g:eachError bean="${userInstance}" var="error">
	           <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
	         </g:eachError>
	       </ul>
	     </g:hasErrors>
	     
	     <g:form url="[resource:userInstance, action:'register']" >
          <fieldset class="form">
            <g:render template="form"/>
            
            <div class="${!captchaValid ? 'error' : ''}">
              <img src="${createLink(controller: 'simpleCaptcha', action: 'captcha')}"/>
				  <label for="captcha">Type the letters above in the box below:</label>
				  <g:textField name="captcha" />
            </div>
            
          </fieldset>
          <fieldset class="buttons">
            <g:submitButton name="register" class="save btn btn-default btn-md" value="${message(code: 'default.button.register.label', default: 'Register')}" />
          </fieldset>
          <fieldset class="buttons">
             <g:link controller="login" class="btn btn-default btn-md">${message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
           </fieldset>
	     </g:form>
      </div>
    </div>
  </body>
</html>
