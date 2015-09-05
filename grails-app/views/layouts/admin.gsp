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
    <title><g:layoutTitle default="CaboLabs &copy;"/></title>
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
    
    <g:layoutHead/>
    <style type="text/css">
    #main_menu {
      text-align: center;
    }
    
    #main_menu .active {
      background-color: #efefef;
      box-shadow: 0 -2px 2px 0px #aaaaaa; /* x_offset y_offset blur spread_distance color */
    }
    .navbar-header img {
      max-height: 20px;
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
    <%--
    <!-- main menu, same as desktop -->
    <div id="main_menu">
      <g:link controller="person" action="list" class="access_icon ${(controllerName=='person')?'active':''}"><g:message code="person.list.title" /></g:link>
      <g:link controller="ehr" action="list" class="ehr_icon ${(controllerName=='ehr')?'active':''}"><g:message code="desktop.ehrs" /></g:link>
      <g:link controller="contribution" action="list" class="contributions_icon ${(controllerName=='contribution')?'active':''}"><g:message code="desktop.contributions" /></g:link>
      <g:link controller="folder" action="index" class="directory_icon ${(controllerName=='folder')?'active':''}"><g:message code="desktop.directory" /></g:link>
      <g:link controller="query" action="list" class="query_icon ${(controllerName=='query')?'active':''}"><g:message code="desktop.queries" /></g:link>
      <g:link controller="indexDefinition" action="list" class="indexes_icon ${(controllerName=='indexDefinition')?'active':''}"><g:message code="desktop.indexes" /></g:link>
      <g:link controller="operationalTemplate" action="list" class="templates_icon ${(controllerName=='operationalTemplate')?'active':''}"><g:message code="desktop.templates" /></g:link>
    </div>
    <div style="clear: both"></div>
     --%>
    
    <div id="wrapper">

        <!-- Navigation -->
        <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <!-- LOGO -->
                <a href="http://cabolabs.com" class="navbar-brand" target="_blank"><asset:image src="cabolabs_logo.png" class="img-responsive" /></a>
                <!-- /LOGO -->
            </div>
            <!-- /.navbar-header -->

            <!-- TOP MENU: TODO -->
            
            <!-- LEFT MENU -->
            <div class="navbar-default sidebar" role="navigation">
                <div class="sidebar-nav navbar-collapse">
                    <ul class="nav" id="side-menu">
                        <li>
                            <a href="/"><i class="fa fa-dashboard fa-fw"></i> Dashboard</a>
                        </li>
                        <li>
                            <a href="tables.html"><i class="fa fa-table fa-fw"></i> <g:message code="person.list.title" /></a>
                        </li>
                        <li>
                            <a href="forms.html"><i class="fa fa-edit fa-fw"></i> <g:message code="desktop.ehrs" /></a>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-wrench fa-fw"></i> <g:message code="desktop.contributions" /><span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="panels-wells.html">Panels and Wells</a>
                                </li>
                                <li>
                                    <a href="buttons.html">Buttons</a>
                                </li>
                                <li>
                                    <a href="notifications.html">Notifications</a>
                                </li>
                            </ul>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-sitemap fa-fw"></i> <g:message code="desktop.directory" /><span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="#">Second Level Item</a>
                                </li>
                                <li>
                                    <a href="#">Second Level Item</a>
                                </li>
                                <li>
                                    <a href="#">Third Level <span class="fa arrow"></span></a>
                                    <ul class="nav nav-third-level">
                                        <li>
                                            <a href="#">Third Level Item</a>
                                        </li>
                                        <li>
                                            <a href="#">Third Level Item</a>
                                        </li>
                                        <li>
                                            <a href="#">Third Level Item</a>
                                        </li>
                                        <li>
                                            <a href="#">Third Level Item</a>
                                        </li>
                                    </ul>
                                    <!-- /.nav-third-level -->
                                </li>
                            </ul>
                            <!-- /.nav-second-level -->
                        </li>
                        <li>
                            <a href="#"><i class="glyphicon glyphicon-search"></i> <g:message code="desktop.queries" /><span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="blank.html"><i class="glyphicon glyphicon-plus-sign"></i> <g:message code="query.create.title" /></a>
                                </li>
                            </ul>
                        </li>
                        <li>
                            <a href="forms.html"><i class="glyphicon glyphicon-th-list"></i> <g:message code="desktop.indexes" /></a>
                        </li>
                        <li>
                            <a href="forms.html"><i class="glyphicon glyphicon-file"></i> <g:message code="desktop.templates" /></a>
                        </li>
                    </ul>
                </div>
                <!-- /.sidebar-collapse -->
            </div>
            <!-- /.navbar-static-side -->
        </nav>

        <!-- BODY -->
        <div id="page-wrapper">
            <!--
            <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Dashboard</h1>
                </div>
            </div>
            -->
            <g:layoutBody/>
			   <div class="footer" role="contentinfo"></div>
			   <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
        </div>
        <!-- /BODY -->

    </div>
    <!-- /#wrapper -->
  </body>
</html>
