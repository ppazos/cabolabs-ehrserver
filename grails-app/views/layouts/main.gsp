<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>   <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>   <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>   <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:layoutTitle default="CaboLabs &copy;"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
    <link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
    <link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
    <g:layoutHead/>
    <r:layoutResources />
    <style type="text/css">
    #main_menu {
      text-align: center;
    }
    
    /* iconos */
    #main_menu .access {
     background-image: url('/ehr/static/images/icons/access.png');
     background-repeat: no-repeat;
     background-position: 4px 4px;
     
     padding-left: 56px; /* ancho de la imagen de fondo + un margen */
     padding-right: 8px;
     padding-top: 4px;
     padding-bottom: 4px;
     
     margin: 4px 2px -2px 0px;
     
     /*height: 48px; no funciona */
     line-height: 48px; /* deja el texto en el medio vertical del icono de fondo */
     
     /*float: left;*/
     display: inline-block;
     /*border: 1px solid #ddd;*/
    }
      
    #main_menu .active {
      background-color: #efefef;
      box-shadow: 0 -2px 2px 0px #aaaaaa; /* x_offset y_offset blur spread_distance color */
    }
    
    /* iconos */
    #main_menu .ehr {
     background-image: url('/ehr/static/images/icons/unfold-multiple.png');
    }
    #main_menu .query {
     background-image: url('/ehr/static/images/icons/zoom.png');
    }
    #main_menu .directory {
     background-image: url('/ehr/static/images/icons/book-alt-3.png');
    }
    #main_menu .contributions {
     background-image: url('/ehr/static/images/icons/ftp-alt-2.png');
    }
    #main_menu .indexes {
     background-image: url('/ehr/static/images/icons/list-ordered.png');
    }
    </style>
  </head>
  <body>
    <div id="grailsLogo" role="banner">
      <span id="cb_logo"><img src="${resource(dir: 'images', file: 'ehr_logo.png')}" /></span>
      <span id="cb_app">EHR Server</span>
      <span id="cb_link"><a href="http://cabolabs.com" target="_blank"><img src="${resource(dir: 'images', file: 'cabolabs_logo.png')}"/></a></span>
    </div>

    <!-- main menu, same as desktop -->
    <div id="main_menu">
      <g:link controller="person" action="list" class="access ${(controllerName=='person')?'active':''}"><g:message code="person.list.title" /></g:link>
      <g:link controller="ehr" action="list" class="access ehr ${(controllerName=='ehr')?'active':''}"><g:message code="desktop.ehrs" /></g:link>
      <g:link controller="contribution" action="list" class="access contributions ${(controllerName=='contribution')?'active':''}"><g:message code="desktop.contributions" /></g:link>
      <a href="#" class="access directory ${(controllerName=='folder')?'active':''}"><g:message code="desktop.directory" /></a>
      <g:link controller="query" action="list" class="access query ${(controllerName=='query')?'active':''}"><g:message code="desktop.queries" /></g:link>
      <g:link controller="dataIndex" action="list" class="access indexes ${(controllerName=='dataIndex')?'active':''}"><g:message code="desktop.indexes" /></g:link>
    </div>
    <div style="clear: both"></div>
    
    <g:layoutBody/>
    <div class="footer" role="contentinfo"></div>
    <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
    <g:javascript library="application"/>
    <r:layoutResources />
  </body>
</html>