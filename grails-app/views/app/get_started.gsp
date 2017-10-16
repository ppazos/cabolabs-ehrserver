<%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex" %><%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.Contribution" %><%@ page import="com.cabolabs.ehrserver.query.Query" %><%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <style type="text/css">
    </style>
    <asset:stylesheet src="jquery.qtip.min.css" />
    <asset:stylesheet src="tourist.css" />
    <asset:javascript src="underscore-min.js" />
    <asset:javascript src="backbone-min.js" />
    <asset:javascript src="jquery.qtip.min.js" />
    <asset:javascript src="jquery-ui-effects.min.js" />
    <asset:javascript src="tourist.min.js" />
    <script type="text/javascript">
    $(function(){
      STEPS = [
        {
          content: '<p>Create some EHRs to commit clinical documents, and then execute some queries. Also, EHRs can be created from the REST API.</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#menu-ehrs'),
          my: 'left center',
          at: 'center right'
         },
         {
          content: '<p>Queries are created from the Web Console, and can be executer from the REST API. No need of programming or writing SQL!</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#menu-queries'),
          my: 'left center',
          at: 'center right'
         },
         {
          content: '<p>From here you can send feedback, ask a question or send an improvement idea, and logout from the Web Console</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#top-user-menu'),
          my: 'top right',
          at: 'bottom left'
         }
      ];
      
      TOUR = new Tourist.Tour({
       stepOptions: {},
       steps: STEPS,
       //cancelStep: @finalQuit
       //successStep: @finalSucceed
       tipClass: 'QTip',
       tipOptions:{
         style: {
           classes: 'qtip-tour qtip-bootstrap'
         }
       }
      });
      
      // TODO: press start tour button
      TOUR.start();
    });
    </script>
  </head>
  <body>
    <div class="row content">
      <!--
      <div class="col-lg-3 col-md-6">
      </div>
      <div class="col-lg-3 col-md-6">
      </div>
      -->
    </div>
  </body>
</html>
