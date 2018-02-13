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
    <title>CaboLabs &copy; <g:message code="login.title" /></title>
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
     
     .navbar-header img {
       max-height: 30px;
     }
     .navbar-brand {
       padding: 10px 21px;
     }
     a.active {
       font-weight: bold;
     }
     a {
      color: #4185F3;
     }
     #app_version {
       font-size: 0.8em;
       padding: 10px 5px 5px 5px;
     }
     .help-block {
      display: none;
      margin: 0;
      padding: 10px 0;
     }
    </style>
  </head>
  <body>
    <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
	   <div class="navbar-header">
        <!--
	     <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
	       <span class="sr-only">Toggle navigation</span>
	       <span class="icon-bar"></span>
	       <span class="icon-bar"></span>
	       <span class="icon-bar"></span>
	     </button>
        -->
	     <a href="https://www.cabolabs.com" class="navbar-brand" target="_blank"><asset:image src="EHRServer_alpha_72_horizontal.png" class="img-responsive" /></a>
	   </div>
	
	   <g:set var="locale" value="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)}" />
      <ul class="nav navbar-top-links navbar-right">
        <li>
          <g:link action="auth" params="[lang:'es']" class="${(locale.language == 'es')?'active':''}">ES</g:link>
        </li>
        <li>
          <g:link action="auth" params="[lang:'en']" class="${(locale.language == 'en')?'active':''}">EN</g:link>
        </li>
        <!--
        <li>
          <g:link action="auth" params="[lang:'pt']" class="${(locale.language == 'pt')?'active':''}">PT</g:link>
        </li>
        -->
      </ul>
	 </nav>
  
    <div class="container">
      <div class="row">
        <div class="col-md-4 col-md-offset-4">
          <div class="login-panel panel panel-default">
            <div class="panel-heading">
              <h3 class="panel-title"><g:message code="login.title" /></h3>
            </div>
            <div class="panel-body">
              <g:if test='${flash.message}'>
                <div class='login_message'>${flash.message}</div><br/>
              </g:if>
              
              <form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>
                <fieldset>
                  <div class="form-group">
                    <label for='username'><g:message code="springSecurity.login.username.label"/></label>
                    <input type='text' class='form-control' name='j_username' id='username' required="required" />
                  </div>
                  <div class="form-group">
                    <label for='password'><g:message code="springSecurity.login.password.label"/></label>
                    <input type='password' class='form-control' name='j_password' id='password' required="required" value="" />
                  </div>
                  <div class="form-group">
                    <label for='org_number'><g:message code="springSecurity.login.org_number.label"/></label>
                    <div class="input-group">
                      <input type='text' class='form-control' name='j_organisation' id='org_number' required="required" />
                      <span class="input-group-btn">
                        <button class="btn btn-default" id="help-organization-btn" type="button"><i class="fa fa-question"></i></button>
                      </span>
                    </div>
                    <div id="help-organization" class="help-block"><g:message code="login.organization.help" /></div>
                  </div>
                  <%--
                  <div class="checkbox">
                    <label>
                      <input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
                      <g:message code="springSecurity.login.remember.me.label"/>
                    </label>
                  </div>
                  --%>
                  
                  <input type='submit' id="submit" class="btn btn-lg btn-success btn-block" value='${message(code: "springSecurity.login.button")}'/>
                  
                  <div class="form-group" style="margin:0; padding:15px 0 15px 0; text-align:center;">
                    <g:link controller="user" action="forgotPassword">
                      <g:message code="springSecurity.login.forgotPassword.label"/>
                    </g:link>
                  </div>
                  
                </fieldset>
                
                <g:if test="${grailsApplication.config.app.allow_web_user_register.toBoolean()}">
                  <fieldset>
                    <div class="form-group" style="margin:0; padding-top:15px; text-align:center; border-top:1px solid #ccc;">
                      <g:link controller="user" action="register">
                        <b><g:message code="springSecurity.login.register.label"/></b>
                      </g:link>
                    </div>
                  </fieldset>
                </g:if>
              </form>
            </div>
          </div>
          <div align="center" id="app_version">EHRServer v<g:meta name="app.version"/></div>
        </div>
      </div>
    </div>
    <script type='text/javascript'>
    (function() {
      document.forms['loginForm'].elements['j_username'].focus();
      
      $('#help-organization-btn').on('click', function (e) {
        $('#help-organization').fadeToggle('slow');
      });
    })();
    </script>
  </body>
</html>
