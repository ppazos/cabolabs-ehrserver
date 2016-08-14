<%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex" %><%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.Contribution" %><%@ page import="com.cabolabs.ehrserver.query.Query" %><%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <style type="text/css">
      html,body{
        height:100%;
        background-color:#efefef;
        font-family: arial;
        background-image: -moz-linear-gradient(center top, #ddd, #efefef);
        background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #ddd), color-stop(1, #efefef));
        background-image: linear-gradient(top, #ddd, #efefef);
        filter: progid:DXImageTransform.Microsoft.gradient(startColorStr = '#dddddd', EndColorStr = '#efefef');
        background-repeat: no-repeat;
        height: 100%;
        /* change the box model to exclude the padding from the calculation of 100% height (IE8+) */
        -webkit-box-sizing: border-box;
           -moz-box-sizing: border-box;
                box-sizing: border-box;
        
        margin: 0;
        padding: 0;
        padding-bottom: 15px;
      }
      
      .gi-3x{font-size: 3em;}
      .gi-4x{font-size: 4em;}
      .gi-5x{font-size: 5em;}
      
      #awrapper{
        height:100%;
        width:100%;
        display:table;
        vertical-align:middle;
        margin-top: 15px;
      }
      #outer {
        display:table-cell;
        vertical-align:middle;
      }
      #formwrap {
        position:relative;
        left:50%;
        float:left;
      }
      #inner {
        border: 2px solid #999;
        padding: 15px 0 5px 0;
        position: relative;
        text-align: center;
        left: -50%;
        background-color: #fff;
        -moz-box-shadow:    2px 3px 5px 1px #ccc;
        -webkit-box-shadow: 2px 3px 5px 1px #ccc;
        box-shadow:         2px 3px 5px 1px #ccc;
        -webkit-border-radius: 8px;
        -moz-border-radius: 8px;
        border-radius: 8px;
        
        width: 540px;
      }
      
      h1 {
        background-color: #ddd;
      }
      
      .error {
        /* TODO: meter icono de error ! */
        border: 1px solid #f00;
        background-color: #f99;
        padding: 2px;
        margin-bottom: 3px;
      }
      .error ul {
        list-style:none;
        margin:0;
        padding:0;
      }
      img {
        border: 0px;
      }
      

      /* Test mostrar icon */
      #index_menu a {
        padding-left: 4px; /* ancho de la imagen de fondo + un margen */
        padding-right: 4px;
        padding-top: 4px;
        padding-bottom: 4px;
        
        margin: 0.8em;
        
        /*height: 48px; no funciona */
        line-height: 1.6em; /* deja el texto en el medio vertical del icono de fondo */
        font-size: 1.6em;
        
        display: inline-block;
        
        text-decoration: none;
      }
      
      .access:hover {
        background-color: #ddddff;
      }
      
      #panel {
        overflow: visible;
        display: inline-block;
      }
      
      div.content > div {
        margin-top: 15px;
      }
    </style>
    <!--[if lt IE 8]>
      <style type="text/css">
        #formwrap {top:50%}
        #inner {top:-50%;}
      </style>
     <![endif]-->
     <!--[if IE 7]>
       <style type="text/css">
        #wrapper{
         position:relative;
         overflow:hidden;
        }
      </style>
    <![endif]-->
  </head>
  <body>
    <div class="row content">
      <div class="col-lg-3 col-md-6">
          <div class="panel panel-primary">
              <div class="panel-heading">
                  <div class="row">
                      <div class="col-xs-3">
                          <i class="fa fa-book fa-4x"></i>
                      </div>
                      <div class="col-xs-9 text-right">
                          <div class="huge">${count_ehrs}</div>
                          <div><g:message code="desktop.ehrs" /></div>
                      </div>
                  </div>
              </div>
              <g:link controller="ehr" action="list">
                  <div class="panel-footer">
                      <span class="pull-left">View Details</span>
                      <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
                      <div class="clearfix"></div>
                  </div>
              </g:link>
          </div>
      </div>
      <div class="col-lg-3 col-md-6">
          <div class="panel panel-green">
              <div class="panel-heading">
                  <div class="row">
                      <div class="col-xs-3">
                          <i class="fa fa-arrows-v fa-4x"></i>
                      </div>
                      <div class="col-xs-9 text-right">
                          <div class="huge">${count_contributions}</div>
                          <div><g:message code="desktop.contributions" /></div>
                      </div>
                  </div>
              </div>
              <g:link controller="contribution" action="list">
                  <div class="panel-footer">
                      <span class="pull-left">View Details</span>
                      <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
                      <div class="clearfix"></div>
                  </div>
              </g:link>
          </div>
      </div>
      <div class="col-lg-3 col-md-6">
          <div class="panel panel-yellow">
              <div class="panel-heading">
                  <div class="row">
                      <div class="col-xs-3">
                          <i class="glyphicon glyphicon-search gi-4x"></i>
                      </div>
                      <div class="col-xs-9 text-right">
                          <div class="huge">${Query.count()}</div>
                          <div><g:message code="desktop.queries" /></div>
                      </div>
                  </div>
              </div>
              <g:link controller="query" action="list">
                  <div class="panel-footer">
                      <span class="pull-left">View Details</span>
                      <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
                      <div class="clearfix"></div>
                  </div>
              </g:link>
          </div>
      </div>
      <div class="col-lg-3 col-md-6">
          <div class="panel panel-red">
              <div class="panel-heading">
                  <div class="row">
                      <div class="col-xs-3">
                          <i class="glyphicon glyphicon-file gi-4x"></i>
                      </div>
                      <div class="col-xs-9 text-right">
                          <div class="huge">${OperationalTemplateIndex.count()}</div>
                          <div><g:message code="desktop.templates" /></div>
                      </div>
                  </div>
              </div>
              <g:link controller="operationalTemplate" action="list">
                  <div class="panel-footer">
                      <span class="pull-left">View Details</span>
                      <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
                      <div class="clearfix"></div>
                  </div>
              </g:link>
          </div>
      </div>
      <g:each in="${version_repo_sizes}" var="org_repo_size">
        <div class="col-lg-3 col-md-6">
          <div class="panel panel-default">
            <div class="panel-heading">
              <div class="row">
                <div class="col-xs-3">
                  <i class="glyphicon glyphicon-hdd gi-4x"></i>
                </div>
                <div class="col-xs-9 text-right">
                  <div class="huge">${(org_repo_size.value / 1024).setScale(2,0)} KB</div>
                  <div><g:link controller="organization" action="show" id="${org_repo_size.key.uid}">${org_repo_size.key.name}</g:link></div>
                </div>
              </div>
            </div>
            <%--
            <g:link controller="operationalTemplate" action="list">
              <div class="panel-footer">
                <span class="pull-left">View Details</span>
                <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
                <div class="clearfix"></div>
              </div>
            </g:link>
            --%>
          </div>
        </div>
      </g:each>
    </div>
  </body>
</html>
