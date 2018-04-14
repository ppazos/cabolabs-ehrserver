<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>   <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>   <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>   <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--><html lang="en" class="no-js"><!--<![endif]-->
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


    <!-- Bootstrap DateTime Picker https://github.com/smalot/bootstrap-datetimepicker -->
    <asset:javascript src="moment.min.js" />
    <asset:link rel="stylesheet" href="bootstrap-datetimepicker.min.css" type="text/css" />
    <asset:javascript src="bootstrap-datetimepicker.min.js" />


    <!-- Custom CSS -->
    <asset:link rel="stylesheet" href="sb-admin-2.css" type="text/css" />
    <asset:javascript src="sb-admin-2.js" />

    <!-- Global notifications -->
    <asset:javascript src="notification.js" />
    <asset:link rel="stylesheet" href="notification.css" type="text/css" />

    <!-- ajax forms -->
    <script src="https://cdn.jsdelivr.net/jquery.form/4.2.1/jquery.form.min.js" integrity="sha384-tIwI8+qJdZBtYYCKwRkjxBGQVZS3gGozr3CtI+5JF/oL1JmPEHzCEnIKbDbLTCer" crossorigin="anonymous"></script>


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
       max-height: 30px;
     }
     .navbar-brand {
       padding: 10px 21px;
     }
     ul.navbar-top-links {
       text-align: center;
     }
     ul.navbar-top-links > li:first-child {
       margin: 0;
       padding: 15px 0 15px 15px;
       margin-left: 15px;
     }
     a, .pagination>li>a {
      color: #4185F3;
     }
     .pagination>.active>a {
      background-color: #4185F3;
      border-color: #4185F3;
     }
     .btn-primary { /* change the default bootstrap blue to our blue */
       background-color: #4185F3;
       border-color: #4185F3;
     }

     /** Adding vertical space between rows when needed **/
     /* usage <div class="row row-grid"> */
     .row.row-grid {
       margin-top: 15px;
     }
     h1 {
       margin: 10px 0 0 0;
       padding-bottom: 10px;
       border-bottom: 1px solid #eee;
     }

     /* redefinition of Hx size from boostrap to make them smaller */
     h1 { font-size: 30px; line-height: 40px; line-height:1.1; }
     h2 { font-size: 24px; line-height: 40px; margin: 10px 0 10px 0; line-height:1.1; }
     h3 { font-size: 18px; line-height: 40px; margin: 10px 0 10px 0; line-height:1.1; }
     h4 { font-size: 16px; line-height: 20px; margin: 10px 0 10px 0; line-height:1.1; }
     h5 { font-size: 14px; line-height: 20px; margin: 5px 0 5px 0; line-height:1.1; }
     h6 { font-weight: bold; margin: 5px 0 5px 0; line-height:1.1; }

     /**
      * Style for arrow to active sortable column.
      */
     tr > th.sortable.sorted.asc > a,
     tr > th.sortable.sorted.desc > a {
       margin-right: 5px;
     }

     #powby {
       cursor: pointer;
       color: #337ab7;
       font-size: 0.8em;
       padding: 5px;
     }
     #app_version {
       font-size: 0.8em;
       padding: 10px 5px 5px 5px;
     }

     .menu_vertical_separator {
       border-bottom: 3px solid #ddd;
     }

     #feedback_form {
       display: inline; /* avoids breaking the modal */
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

      /**
       * List filters.
       */
      $(function() {
        $(".btn.filter").on('click', (function() {
          $(this).toggleClass( "btn-primary" );
        }));

        $("#filter-reset").on('click', function() {
          // reset doesnt blank the fields but put the fields on the original state that might be with value, we need to blank.
          $(this).closest('form').find("input[type=text], select").val("");

          // reload to update the list without filters
          $('.form.filter')[0].submit();
        });
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
          <a href="https://www.cabolabs.com" class="navbar-brand" target="_blank"><asset:image src="EHRServer_alpha_72_horizontal.png" class="img-responsive" /></a><!-- <asset:image src="cabolabs_logo.png" class="img-responsive" /> -->
        </div>

        <!-- TOP MENU -->
        <sec:ifLoggedIn>
          <ul class="nav navbar-top-links navbar-right">
           <li style="padding-right:15px;">
             <g:link controller="user" action="show" id="${sec.loggedInUserInfo(field:'id')}" style="padding: 0; display: inline;"><sec:username/></g:link>
             &commat;
             <g:link controller="account" action="show" id="${session.organization.account.id}" style="padding: 0; display: inline;">${session.organization.account.companyName}</g:link>
             :
             <g:link controller="organization" action="show" id="${session.organization.uid}" style="padding: 0; display: inline;">${session.organization.name}</g:link>
           </li>
           <li class="dropdown" id="top-user-menu">
             <a class="dropdown-toggle" data-toggle="dropdown" href="#">
               <i class="fa fa-user fa-fw"></i> <i class="fa fa-caret-down"></i>
             </a>
             <ul class="dropdown-menu dropdown-user">
               <!--
               <li>
                 <a href="#" onclick="alert('not available yet')"><i class="fa fa-user fa-fw"></i> User Profile</a>
               </li>
               <li>
                 <a href="#" onclick="alert('not available yet')"><i class="fa fa-gear fa-fw"></i> Settings</a>
               </li>
               <li class="divider"></li>
               -->
               <li>
                 <a href="#" data-toggle="modal" data-target="#feedback_modal"><i class="fa fa-envelope"></i> <g:message code="layout.action.feedback" /></a>
               </li>
               <li class="divider"></li>
               <li>
                 <g:link controller="logout"><i class="fa fa-sign-out"></i> <g:message code="layout.action.logout" /></g:link>
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
                <li>
                  <g:link controller="app" action="get_started" class="${(controllerName=='app' && actionName=='get_started')?'active':''}"><i class="fa fa-fw fa-info"></i> <g:message code="desktop.guide" /></g:link>
                </li>
                <li class="menu_vertical_separator">
                  <g:link controller="app" action="index" class="${(controllerName=='app' && actionName=='index')?'active':''}"><i class="fa fa-fw fa-dashboard"></i> <g:message code="desktop.dashboard" /></g:link>
                </li>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                  <li id="menu-plan">
                    <g:link controller="plan" action="index" class="${(controllerName=='plan')?'active':''}"><i class="fa fa-fw fa-cog"></i> <g:message code="desktop.plans" /></g:link>
                  </li>
                  <li id="menu-accounts">
                    <g:link controller="account" action="index" class="${(controllerName=='account')?'active':''}"><i class="fa fa-fw fa-id-card"></i> <g:message code="desktop.accounts" /></g:link>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
                  <li id="menu-organizations">
                    <g:link controller="organization" action="index" class="${(controllerName=='organization')?'active':''}"><i class="fa fa-fw fa-sitemap"></i> <g:message code="desktop.organization" /></g:link>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
                  <li>
                    <g:link controller="user" action="index" class="${(controllerName=='user')?'active':''}"><i class="fa fa-fw fa-user"></i> <g:message code="desktop.user" /></g:link>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                  <li>
                    <g:link controller="role" action="index" class="${(controllerName=='role')?'active':''}"><i class="fa fa-fw fa-check-square"></i> <g:message code="desktop.role" /></g:link>
                  </li>
                  <li class="menu_vertical_separator">
                    <g:link controller="requestMap" action="index" class="${(controllerName=='requestMap')?'active':''}"><i class="fa fa-fw fa-shield"></i> <g:message code="desktop.accesscontrol" /></g:link>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
                  <li id="menu-ehrs">
                    <g:link controller="ehr" action="list" class="${(controllerName=='ehr')?'active':''}"><i class="fa fa-fw fa-book"></i> <g:message code="desktop.ehrs" /></g:link>
                  </li>
                  <li id="menu-contributions">
                    <g:link controller="contribution" action="list" class="${(controllerName=='contribution')?'active':''}"><i class="fa fa-fw fa-arrows-v"></i> <g:message code="desktop.contributions" /></g:link>
                  </li>
                  <li>
                    <g:link controller="versionedComposition" action="index" class="${(controllerName=='versionedComposition')?'active':''}"><i class="fa fa-fw fa-file"></i> <g:message code="desktop.versionedCompositions" /></g:link>
                  </li>
                  <li class="menu_vertical_separator">
                    <g:link controller="folderTemplate" action="index" class="${(controllerName=='folderTemplate')?'active':''}"><i class="fa fa-fw fa-folder-open"></i> <g:message code="desktop.folderTemplates" /></g:link>
                    <%--
                    <g:link controller="folder" action="index" class="${(controllerName=='folder')?'active':''}"><i class="fa fa-fw fa-folder-open"></i> <g:message code="desktop.directory" /></g:link>
                    --%>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
                  <li id="menu-queries">
                    <g:link controller="query" action="list" class="${(controllerName=='query')?'active':''}"><i class="fa fa-fw fa-search"></i> <g:message code="desktop.queries" /></g:link>
                  </li>
                  <li class="menu_vertical_separator">
                    <g:link controller="ehrQuery" action="index" class="${(controllerName=='ehrQuery')?'active':''}"><i class="fa fa-fw fa-search"></i> <g:message code="desktop.ehrqueries" /></g:link>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
                  <li id="menu-templates">
                    <g:link controller="operationalTemplate" action="list" class="${(controllerName=='operationalTemplate')?'active':''}"><i class="fa fa-fw fa-cubes"></i> <g:message code="desktop.templates" /></g:link>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                  <li class="menu_vertical_separator">
                    <g:link controller="dataValueIndex" action="index" class="${(controllerName=='dataValueIndex')?'active':''}"><i class="fa fa-fw fa-database "></i> <g:message code="desktop.data" /></g:link>
                  </li>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_MANAGER,ROLE_ACCOUNT_MANAGER">
                  <li>
                    <g:link controller="notification" action="index" class="${(controllerName=='notification')?'active':''}"><i class="fa fa-fw fa-bell"></i> <g:message code="desktop.notification" /></g:link>
                  </li>
                  <li class="menu_vertical_separator">
                    <g:link controller="logs" class="${(controllerName=='logs')?'active':''}"><i class="fa fa-fw fa-tasks"></i> <g:message code="desktop.logs" /></g:link>
                  </li>
                </sec:ifAnyGranted>
              </ul>
              <div align="center" id="app_version">EHRServer v<g:meta name="app.version"/></div>
              <div align="center" id="powby">
                <a href="#" data-toggle="modal" data-target="#license_notice">Powered by CaboLabs</a>
              </div>
            </div>
          </div>
        </sec:ifLoggedIn>
      </nav>

      <div id="page-wrapper">
        <g:layoutBody/>
        <div class="footer" role="contentinfo"></div>
        <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
      </div>
    </div>

    <div id="license_notice" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="license_modal_label">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="license_modal_label">License Notice</h4>
          </div>
          <div class="modal-body">
            <p>Copyright 2011-${Calendar.getInstance().get(Calendar.YEAR)} <a href="https://www.cabolabs.com" target="_blank">CaboLabs Health Informatics</a></p>
            <p>The EHRServer was designed and developed by Pablo Pazos Gutierrez &lt;pablo.pazos@cabolabs.com&gt; at CaboLabs Health Informatics (<a href="https://www.cabolabs.com" target="_blank">www.cabolabs.com</a>).</p>
            <p>You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.</p>
            <p>Any modifications to the provided source code can be stated below this notice.
            <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
            You may obtain a copy of the License at</p>
            <p align="center"><a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">http://www.apache.org/licenses/LICENSE-2.0</a><p>
            <p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
            WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
            limitations under the License.</p>
          </div>
        </div>
      </div>
    </div>

    <script type="text/javascript">
    $(function() { // ready

      $('#feedback_modal').on('submit', function(e) {
        e.preventDefault(); // prevent native submit
        $(this).ajaxSubmit({
          url: $('#feedback_form')[0].action, // without this is not taking the action as url
          type: 'post',
          success: function(data, status, response) {
            //console.log(data, response);
            alrt = '<div class="alert alert-info alert-dismissible global" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+ data.message +'</div>';
            $('body').append('<div class="global_alert_container">'+ alrt +'</div>');
          },
          error: function(a,b,c) {
            console.log(a,b,c);
            alrt = '<div class="alert alert-warning alert-dismissible global" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>An error occurred sending your feedback, please try again later.</div>';
            $('body').append('<div class="global_alert_container">'+ alrt +'</div>');
          }
        });
      });
    });
    </script>

    <!-- feedback modal form -->
    <div class="modal fade" id="feedback_modal" tabindex="-1" role="dialog" aria-labelledby="feedback_modal_label">
      <div class="modal-dialog" role="document">
        <g:form url="[controller:'messaging', action: 'feedback']" role="form" id="feedback_form">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="feedback_modal_label"><g:message code="ehrserver.messaging.feedbackform.title" /></h4>
          </div>
          <div class="modal-body">

            <div class="form-group">
              <label><g:message code="ehrserver.messaging.feedbackform.message" /></label>
              <textarea class="form-control" rows="3" name="text" required="required"></textarea>
            </div>
            <div class="form-group">
              <label><g:message code="ehrserver.messaging.feedbackform.about" /></label>
              <div class="radio">
                <label>
                  <input name="about" value="Question" checked="" type="radio" /> <g:message code="ehrserver.messaging.feedbackform.question" />
                </label>
              </div>
              <div class="radio">
                <label>
                  <input name="about" value="Issue" type="radio" /> <g:message code="ehrserver.messaging.feedbackform.issue" />
                </label>
              </div>
              <div class="radio">
                <label>
                  <input name="about" value="Improvement" type="radio" /> <g:message code="ehrserver.messaging.feedbackform.improvement" />
                </label>
              </div>
            </div>

            <div class="form-group">
              <label><g:message code="ehrserver.messaging.feedbackform.community" /></label>
              <br/>
              <i class="fa fa-facebook-square"></i> <a href="https://www.facebook.com/groups/ehrserver/" target="_blank">https://www.facebook.com/groups/ehrserver/</a>
              <br/>
              <i class="fa fa-linkedin-square"></i> <a href="https://www.linkedin.com/groups/12070397" target="_blank">https://www.linkedin.com/groups/12070397/</a>
            </div>

          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="ehrserver.messaging.feedbackform.close" /></button>
            <button type="submit" class="btn btn-primary"><g:message code="ehrserver.messaging.feedbackform.send" /></button>
          </div>
        </div>
        </g:form>
      </div>
    </div>
  </body>
</html>
