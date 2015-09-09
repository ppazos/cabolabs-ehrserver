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
    <div class="row">
      <div class="col-lg-12">
		  <div id="awrapper">
		    <div id="outer">
		      <div id="formwrap">
		        <div id="inner">
		          <div style="padding: 15px;">
		            <a href="http://cabolabs.com" target="_blank"><asset:image src="cabolabs_logo.png" class="img-responsive" /></a>
		          </div>
		            
		          <h1 align="center"><g:message code="desktop.app" /></h1>

		          <div id="index_menu">
		            <g:link controller="person" action="list"><i class="fa fa-users fa-fw"></i> <g:message code="person.list.title" /></g:link>
		            <g:link controller="ehr" action="list"><i class="fa fa-book fa-fw"></i> <g:message code="desktop.ehrs" /></g:link>
		            <g:link controller="contribution" action="list"><i class="fa fa-arrows-v fa-fw"></i> <g:message code="desktop.contributions" /></g:link>
		            <g:link controller="folder" action="index"><i class="fa fa-folder-open fa-fw"></i> <g:message code="desktop.directory" /></g:link>
		            <g:link controller="query" action="list"><i class="glyphicon glyphicon-search"></i> <g:message code="desktop.queries" /></g:link>
		            <g:link controller="indexDefinition" action="list"><i class="glyphicon glyphicon-th-list"></i> <g:message code="desktop.indexes" /></g:link>
		            <g:link controller="operationalTemplate" action="list"><i class="glyphicon glyphicon-file"></i> <g:message code="desktop.templates" /></g:link>
		          </div>
		            
		        </div>
		      </div>
		    </div>
		  </div>
      </div>
    </div>
  </body>
</html>