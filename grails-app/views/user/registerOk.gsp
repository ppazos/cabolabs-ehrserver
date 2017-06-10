<%@ page import="com.cabolabs.security.User" %>
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
    
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
  </head>
  <body>
    <div class="container">
	   <div class="row">
	     <div class="col-lg-12">
	       <h1><g:message code="user.registerOk.label" /></h1>
	       <g:if test="${flash.message}">
	         <div class="alert alert-info" role="alert">${flash.message}</div>
	       </g:if>
	       <g:message code="user.registerOk.text" />
	       <p>
            <g:message code="user.registerOk.nextSteps" />
	       </p>
          <p>
            <g:link controller="login" action="auth" class="btn btn-success">login</g:link>
          </p>
	     </div>
	   </div>
	 </div>
  </body>
</html>
