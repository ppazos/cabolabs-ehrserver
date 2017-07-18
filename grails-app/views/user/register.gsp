<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>   <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>   <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>   <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:message code="default.register.label" /></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />

    <g:javascript library="jquery" plugin="jquery" />
    
    <!-- Bootstrap Core CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
    
    <!-- MetisMenu CSS -->
    <asset:link rel="stylesheet" href="metisMenu.min.css" type="text/css" />
    <asset:javascript src="metisMenu.min.js" />
    
    <asset:link rel="stylesheet" href="font-awesome.min.css" type="text/css" />
    
    <!-- Custom CSS -->
    <asset:link rel="stylesheet" href="sb-admin-2.css" type="text/css" />
    <asset:javascript src="sb-admin-2.js" />
  </head>
  <body>
    <div class="container">
      <div class="row">
        <div class="col-md-4 col-md-offset-4">
          <div class="login-panel panel panel-default">
            <div class="panel-heading">
              <h3 class="panel-title"><g:message code="default.register.label" /></h3>
            </div>
            <div class="panel-body">
             <g:if test="${flash.message}">
               <div class="alert alert-info" role="alert"><g:message code="${flash.message}" /></div>
             </g:if>
             
             <g:hasErrors bean="${userInstance}">
               <ul class="errors" role="alert">
                 <g:eachError bean="${userInstance}" var="error">
                   <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                 </g:eachError>
               </ul>
             </g:hasErrors>
             
             <g:form url="[resource:userInstance, action:'register']">
               <fieldset class="form">
                 <g:render template="form"/>
                 <div class="form-group ${!captchaValid ? 'error' : ''}">
                   <div style="text-align:center;">
                     <img src="${createLink(controller: 'simpleCaptcha', action: 'captcha')}" />
                   </div>
                   <label for="captcha">Type the letters in this box</label>
                   <g:textField name="captcha" class="form-control" />
                 </div>
                 <g:submitButton name="register" class="btn btn-lg btn-success btn-block" value="${message(code: 'default.button.register.label', default: 'Register')}" />
               </fieldset>
               <fieldset>
                  <div class="form-group" style="margin:15px 0 0 0; padding-top:15px; text-align:center; border-top:1px solid #ccc;">
                    <g:link controller="login" action="auth">
                      <g:message code="springSecurity.login.back.label"/>
                    </g:link>
                  </div>
                </fieldset>
             </g:form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>
