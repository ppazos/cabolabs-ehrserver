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
    <title>CaboLabs &copy;</title>
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
   
    <style type='text/css' media='screen'>
     #login {
       margin: 15px 0px;
       padding: 0px;
       text-align: center;
     }
     #login .inner {
       width: 340px;
       padding-bottom: 6px;
       margin: 60px auto;
       text-align: left;
       border: 1px solid #aab;
       background-color: #f0f0fa;
       -moz-box-shadow: 2px 2px 2px #eee;
       -webkit-box-shadow: 2px 2px 2px #eee;
       -khtml-box-shadow: 2px 2px 2px #eee;
       box-shadow: 2px 2px 2px #eee;
     }
     #login .inner .fheader {
       padding: 18px 26px 14px 26px;
       background-color: #f7f7ff;
       margin: 0px 0 14px 0;
       color: #2e3741;
       font-size: 18px;
       font-weight: bold;
     }
     #login .inner .cssform p {
       clear: left;
       margin: 0;
       padding: 4px 0 3px 0;
       padding-left: 105px;
       margin-bottom: 20px;
       height: 1%;
     }
     #login .inner .cssform input[type='text'] {
       width: 120px;
     }
     #login .inner .cssform label {
       font-weight: bold;
       float: left;
       text-align: right;
       margin-left: -105px;
       width: 110px;
       padding-top: 3px;
       padding-right: 10px;
     }
     #login #remember_me_holder {
       padding-left: 120px;
     }
     #login #submit {
       margin-left: 15px;
     }
     #login #remember_me_holder label {
       float: none;
       margin-left: 0;
       text-align: left;
       width: 200px
     }
     #login .inner .login_message {
       padding: 6px 25px 20px 25px;
       color: #c33;
     }
     #login .inner .text_ {
       width: 120px;
     }
     #login .inner .chk {
       height: 12px;
     }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="row">
        <div class="col-md-4 col-md-offset-4">
          <div class="login-panel panel panel-default">
            <div class="panel-heading">
              <h3 class="panel-title">Please Reset your password</h3>
            </div>
            <div class="panel-body">
              <g:if test='${flash.message}'>
                <div class="alert alert-info" role="alert">${flash.message}</div><br/>
              </g:if>
              
              <g:form url="[action:'resetPassword']" method='POST' id="form" class='cssform' autocomplete='off'>
                <input type="hidden" name="token" value="${params.token}" />
                <fieldset>
                  <div class="form-group">
                    <label for='password'><g:message code="springSecurity.resetPassword.newPassword.label"/>:</label>
                    <input type='password' class='form-control' name='newPassword' id='password' required="required" value="" />
                  </div>
                  <div class="form-group">
                    <label for='confirmPassword'><g:message code="springSecurity.resetPassword.confirmNewPassword.label"/>:</label>
                    <input type='password' class='form-control' name='confirmNewPassword' id='confirmPassword' required="required" value="" />
                  </div>
                  <input type='submit' id="submit" class="btn btn-lg btn-success btn-block" value='${message(code: "springSecurity.reset.button")}'/>
                </fieldset>
                <fieldset>
                  <div class="form-group" style="margin:0; padding-top:15px; text-align:center; border-top:1px solid #ccc;">
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
    <script type='text/javascript'>
    (function() {
      document.forms['form'].elements['newPassword'].focus();
    })();
    </script>
  </body>
</html>
