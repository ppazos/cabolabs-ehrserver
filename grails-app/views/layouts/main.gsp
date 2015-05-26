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
    <asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
    <asset:link rel="apple-touch-icon" href="apple-touch-icon.png" />
    <asset:link rel="apple-touch-icon" sizes="114x114" href="apple-touch-icon-retina.png" />
    <asset:link rel="stylesheet" href="main.css" type="text/css" />
    <asset:link rel="stylesheet" href="mobile.css" type="text/css" />
    <g:javascript library="jquery" plugin="jquery" />
    <g:layoutHead/>
    <style type="text/css">
    #main_menu {
      text-align: center;
    }
    
    #main_menu .active {
      background-color: #efefef;
      box-shadow: 0 -2px 2px 0px #aaaaaa; /* x_offset y_offset blur spread_distance color */
    }
    
    </style>
    
    <g:javascript>
      // Used to access the assets root from JS code.
      // http://stackoverflow.com/questions/24048628/how-can-i-access-images-from-javascript-using-grails-asset-pipeline-plugin
	   window.grailsSupport = {
	      assetsRoot : '${ raw(asset.assetPath(src: '')) }', // /ehr/assets/
	      baseURL : '${ g.createLink(uri:"/") }' // URL relative to / e.g. '/ehr/'
	   };
	 </g:javascript>
  </head>
  <body>
    <div id="grailsLogo" role="banner">
      <span id="cb_logo"><assets:image src="ehr_logo.png" /></span>
      <span id="cb_app">EHR Server</span>
      <span id="cb_link"><a href="http://cabolabs.com" target="_blank"><asset:image src="cabolabs_logo.png" /></a></span>
    </div>

    <!-- main menu, same as desktop -->
    <div id="main_menu">
      <g:link controller="person" action="list" class="access_icon ${(controllerName=='person')?'active':''}"><g:message code="person.list.title" /></g:link>
      <g:link controller="ehr" action="list" class="ehr_icon ${(controllerName=='ehr')?'active':''}"><g:message code="desktop.ehrs" /></g:link>
      <g:link controller="contribution" action="list" class="contributions_icon ${(controllerName=='contribution')?'active':''}"><g:message code="desktop.contributions" /></g:link>
      <a href="#" class="directory_icon ${(controllerName=='folder')?'active':''}"><g:message code="desktop.directory" /></a>
      <g:link controller="query" action="list" class="query_icon ${(controllerName=='query')?'active':''}"><g:message code="desktop.queries" /></g:link>
      <g:link controller="indexDefinition" action="list" class="indexes_icon ${(controllerName=='indexDefinition')?'active':''}"><g:message code="desktop.indexes" /></g:link>
      <g:link controller="operationalTemplate" action="list" class="templates_icon ${(controllerName=='operationalTemplate')?'active':''}"><g:message code="desktop.templates" /></g:link>
    </div>
    <div style="clear: both"></div>
    
    <g:layoutBody/>
    <div class="footer" role="contentinfo"></div>
    <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
  </body>
</html>