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
  <asset:javascript src="jquery.blockUI.js" />
  
  <!-- Bootstrap Core CSS -->
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
  
  <!-- MetisMenu CSS -->
  <asset:link rel="stylesheet" href="metisMenu.min.css" type="text/css" />
  <asset:javascript src="metisMenu.min.js" />
  
  <asset:link rel="stylesheet" href="font-awesome.min.css" type="text/css" />
  
  
  <!-- Bootstrap DateTime Picker https://github.com/smalot/bootstrap-datetimepicker -->
  <asset:link rel="stylesheet" href="bootstrap-datetimepicker.min.css" type="text/css" />
  <asset:javascript src="bootstrap-datetimepicker.min.js" />
  
  
  <!-- Custom CSS -->
  <asset:link rel="stylesheet" href="sb-admin-2.css" type="text/css" />
  <asset:javascript src="sb-admin-2.js" />
  
  <!-- Global notifications -->
  <asset:javascript src="notification.js" />
  <asset:link rel="stylesheet" href="notification.css" type="text/css" />
  
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
  ul.navbar-top-links {
    text-align: center;
  }
  ul.navbar-top-links > li:first-child {
    margin: 0;
    padding: 15px 0 15px 15px;
    margin-left: 15px;
  }
  
  /** Adding vertical space between rows when needed **/
  /* usage <div class="row row-grid"> */
  .row.row-grid {
    margin-top: 15px;
  }
  h1 {
    margin: 10px 0 10px 0;
  }
  
  /**
   * Style for arrow to active sortable column.
   */
  tr > th.sortable.sorted.asc > a,
  tr > th.sortable.sorted.desc > a {
    margin-right: 5px;
  }
  
  #powby {
    width: 180px;
    text-align: center;
    position: fixed;
    bottom: 10px;
    cursor: pointer;
    color: #337ab7;
  }
  #app_version {
    font-size: 0.8em;
    padding: 5px;
  }
  
  .menu_vertical_separator {
    border-bottom: 3px solid #ddd;
  }
  </style>
  <g:javascript>
    // Used to access the assets root from JS code.
    // http://stackoverflow.com/questions/24048628/how-can-i-access-images-from-javascript-using-grails-asset-pipeline-plugin
    window.grailsSupport = {
     assetsRoot : '${ raw(asset.assetPath(src: '')) }', // /ehr/assets/
     baseURL : '${ g.createLink(uri:"/") }' // URL relative to / e.g. '/ehr/'
    };
    
    $(function() {
     /**
      * Add arrow to active sortable column.
      */
     $('tr > th.sortable.sorted.asc').append('<span class="glyphicon glyphicon-triangle-bottom"></span>');
     $('tr > th.sortable.sorted.desc').append('<span class="glyphicon glyphicon-triangle-top"></span>');
     
     /**
      * Get notifications.
      */
      notification_get(
        '${createLink(controller:'notification', action:'newNotifications')}',
        '${createLink(controller:'notification', action:'dismiss')}',
        '${controllerName}');
	 });
   </g:javascript>
  </head>
  <body>	  
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
	      <sec:ifLoggedIn>
	        <ul class="nav navbar-top-links navbar-right">
	         <li>
	          <g:message code="layout.welcome_user" /> <g:link controller="user" action="show" id="${sec.loggedInUserInfo(field:'id')}" style="padding: 0; display: inline;"><sec:username/></g:link>!
	          (${session.organization.name})
	         </li>
	         <li class="dropdown">
	           <a class="dropdown-toggle" data-toggle="dropdown" href="#">
	             <i class="fa fa-user fa-fw"></i> <i class="fa fa-caret-down"></i>
	           </a>
	           <ul class="dropdown-menu dropdown-user">
	             <!--
	             <li>
	               <a href="#" onclick="alert('not avilable yet')"><i class="fa fa-user fa-fw"></i> User Profile</a>
	             </li>
	             <li>
	               <a href="#" onclick="alert('not avilable yet')"><i class="fa fa-gear fa-fw"></i> Settings</a>
	             </li>
	             <li class="divider"></li>
	             -->
	             <li>
	               <g:link controller="logout"><i class="fa fa-sign-out fa-fw"></i> <g:message code="layout.action.logout" /></g:link>
	             </li>
	           </ul>
	           <!-- /.dropdown-user -->
	         </li>
	         <!-- /.dropdown -->
	        </ul>
	         
	         
	         <!-- LEFT MENU -->
	         <div class="navbar-default sidebar" role="navigation">
	           <div class="sidebar-nav navbar-collapse">
	             <ul class="nav" id="side-menu">
	               <li class="menu_vertical_separator">
	                 <g:link controller="app" action="index"><i class="fa fa-dashboard"></i> Dashboard</g:link>
	               </li>
                  
                  <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
	                 <li>
	                   <g:link controller="organization" action="index" class="${(controllerName=='organization')?'active':''}"><i class="fa fa-sitemap"></i> <g:message code="desktop.organization" /></g:link>
	                 </li>
	               </sec:ifAnyGranted>
                  
	               <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
	                 <li>
	                   <g:link controller="user" action="index" class="${(controllerName=='user')?'active':''}"><i class="fa fa-user"></i> <g:message code="desktop.user" /></g:link>
	                 </li>
	               </sec:ifAnyGranted>
                  
	               <sec:ifAnyGranted roles="ROLE_ADMIN">
	                 <li class="menu_vertical_separator">
	                   <g:link controller="role" action="index" class="${(controllerName=='role')?'active':''}"><i class="fa fa-check-square"></i> <g:message code="desktop.role" /></g:link>
	                 </li>
	               </sec:ifAnyGranted>
                  
                  <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
	                 <li>
	                   <g:link controller="ehr" action="list" class="${(controllerName=='ehr')?'active':''}"><i class="fa fa-book"></i> <g:message code="desktop.ehrs" /></g:link>
	                 </li>
	                 <li>
	                   <g:link controller="contribution" action="list" class="${(controllerName=='contribution')?'active':''}"><i class="fa fa-arrows-v"></i> <g:message code="desktop.contributions" /></g:link>
	                 </li>
                    <li>
                      <g:link controller="versionedComposition" action="index" class="${(controllerName=='versionedComposition')?'active':''}"><i class="glyphicon glyphicon-file"></i> <g:message code="desktop.versionedCompositions" /></g:link>
                    </li>
	                 <li class="menu_vertical_separator">
	                   <g:link controller="folder" action="index" class="${(controllerName=='folder')?'active':''}"><i class="fa fa-folder-open"></i> <g:message code="desktop.directory" /></g:link>
	                 </li>
                  </sec:ifAnyGranted>
                  
                  <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
	                 <li class="menu_vertical_separator">
	                   <g:link controller="query" action="list" class="${(controllerName=='query')?'active':''}"><i class="glyphicon glyphicon-search"></i> <g:message code="desktop.queries" /></g:link>
	                 </li>
                  </sec:ifAnyGranted>
                  
	               <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
	                 <li class="menu_vertical_separator">
	                   <g:link controller="operationalTemplate" action="list" class="${(controllerName=='operationalTemplate')?'active':''}"><i class="fa fa-cubes"></i> <g:message code="desktop.templates" /></g:link>
	                 </li>
	               </sec:ifAnyGranted>
                  
	               <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
                    <li class="menu_vertical_separator">
                      <g:link controller="notification" action="index" class="${(controllerName=='notification')?'active':''}"><i class="fa fa-bell"></i> <g:message code="desktop.notification" /></g:link>
                    </li>
                  </sec:ifAnyGranted>
	             </ul>
	             <div align="center" id="app_version">EHRServer v<g:meta name="app.version"/></div>
	             <p id="powby">Powered by CaboLabs</p>
	           </div>
	           <!-- /.sidebar-collapse -->
	         </div>
	      <!-- /.navbar-static-side -->
	      </sec:ifLoggedIn>
	   </nav>
	
	   <!-- BODY -->
	   <div id="page-wrapper">
	     <g:layoutBody/>
	     <div class="footer" role="contentinfo"></div>
	     <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
	   </div>
	   <!-- /BODY -->
	 </div>
	 
	 <div id="license_notice" style="display:none; cursor: default;"> 
      <h1 align="center">License Notice</h1> 
      <p>Copyright 2011-2017 <a href="http://cabolabs.com" target="_blank">CaboLabs Health Informatics</a></p>
      <p>The EHRServer was designed and developed by Pablo Pazos Gutierrez &lt;pablo.pazos@cabolabs.com&gt; at CaboLabs Health Informatics (<a href="http://cabolabs.com" target="_blank">www.cabolabs.com</a>).</p>
      <p>You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.</p>
		<p>Any modifications to the provided source code can be stated below this notice.
		<p>Licensed under the Apache License, Version 2.0 (the "License");
		you may not use this file except in compliance with the License.
		You may obtain a copy of the License at</p>
		<p align="center"><a href="http://www.apache.org/licenses/LICENSE-2.0 target="_blank">http://www.apache.org/licenses/LICENSE-2.0</a><p>
		<p>Unless required by applicable law or agreed to in writing, software
		distributed under the License is distributed on an "AS IS" BASIS,
		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
		See the License for the specific language governing permissions and
		limitations under the License.</p>
      <p align="center"><input type="button" id="close" value="Close" /></p> 
    </div>
    
    <script type="text/javascript"> 
    $(document).ready(function() { 

       $('#powby').click(function() { 
         $.blockUI({
           message: $('#license_notice'),
           centerY: true,
           centerX: true,
           css: {
             width: '80%',
             textAlign: 'left',
             left: '10%',
             top: '5%',
             padding: '10px'
           }
         }); 
       });

       $('#close').click(function() { 
         $.unblockUI(); 
         return false; 
       });
    }); 
    </script> 
    
  </body>
</html>
