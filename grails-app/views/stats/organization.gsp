<%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex" %><%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.Contribution" %><%@ page import="com.cabolabs.ehrserver.query.Query" %><%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %>

<%-- partial view for organization/show --%>

<script>
  var ref_date = ${from}; // initial date to visualize the stats, default is current date in milliseconds from Jan 1 1970.
  console.log('from', new Date(${from}).toUTCString());
  console.log('to', new Date(${to}).toUTCString());
</script>

<style>
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
<g:if test="${!plan}">
  <div class="row content">
    <div class="col-md-12">
      <g:message code="stats.no_active_plan" />
    </div>
  </div>
</g:if>
<g:else>
 <div class="row content">
   <div class="col-lg-4">
     <div class="panel panel-primary">
       <div class="panel-heading">
         <div class="row">
           <div class="col-xs-3">
             <i class="fa fa-exchange fa-4x"></i>
           </div>
           <div class="col-xs-9 text-right">
             <div class="big">${transactions}</div>
             <div><g:message code="stats.transactions" /></div>
           </div>
         </div>
       </div>
       <g:link controller="contribution" action="list" params="[orgUid: params.uid]">
         <div class="panel-footer">
           <span class="pull-left"><g:message code="desktop.view_details" /></span>
           <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
           <div class="clearfix"></div>
         </div>
       </g:link>
     </div>
   </div>
   <div class="col-lg-4">
     <div class="panel panel-green">
       <div class="panel-heading">
         <div class="row">
           <div class="col-xs-3">
             <i class="fa fa-file-o fa-4x"></i>
           </div>
           <div class="col-xs-9 text-right">
             <div class="big">${documents}</div>
             <div><g:message code="stats.documents" /></div>
           </div>
         </div>
       </div>
       <g:link controller="versionedComposition" action="index" params="[orgUid: params.uid]">
         <div class="panel-footer">
           <span class="pull-left"><g:message code="desktop.view_details" /></span>
           <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
           <div class="clearfix"></div>
         </div>
       </g:link>
     </div>
   </div>
   <div class="col-lg-4">
     <div class="panel panel-yellow">
       <div class="panel-heading">
         <div class="row">
           <div class="col-xs-3">
             <i class="fa fa-database fa-4x"></i>
           </div>
           <div class="col-xs-9 text-right">
             <div class="big">${(size/(1024*1024)).setScale(1,0)} / ${(plan.repo_total_size_in_kb/1024).setScale(1,0)} MB</div>
             <div><g:message code="stats.size" /></div>
           </div>
         </div>
       </div>
       <g:link controller="versionedComposition" action="index" params="[orgUid: params.uid]">
         <div class="panel-footer">
           <span class="pull-left"><g:message code="desktop.view_details" /></span>
           <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
           <div class="clearfix"></div>
         </div>
       </g:link>
     </div>
   </div>
 </div>
</g:else>
