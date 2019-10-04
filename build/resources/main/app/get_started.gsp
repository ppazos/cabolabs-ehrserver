<%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex" %><%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.Contribution" %><%@ page import="com.cabolabs.ehrserver.query.Query" %><%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="app.get_started.title" /></title>
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
          content: '<p>${message(code:"app.get_started.tour.organizations")}</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#menu-organizations'),
          my: 'left center',
          at: 'center right'
        },
        {
          content: '<p>${message(code:"app.get_started.tour.ehrs")}</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#menu-ehrs'),
          my: 'left center',
          at: 'center right'
        },
        {
          content: '<p>${message(code:"app.get_started.tour.templates")}</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#menu-templates'),
          my: 'left center',
          at: 'center right'
        },
        {
          content: '<p>${message(code:"app.get_started.tour.contributions")}</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#menu-contributions'),
          my: 'left center',
          at: 'center right'
        },
        {
          content: '<p>${message(code:"app.get_started.tour.queries")}</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#menu-queries'),
          my: 'left center',
          at: 'center right'
        },
        {
          content: '<p>${message(code:"app.get_started.tour.top_menu")}</p>',
          highlightTarget: true,
          nextButton: true,
          target: $('#top-user-menu'),
          my: 'top right',
          at: 'bottom left'
        }
      ];

      Tourist.Tip.Base.prototype.nextButtonTemplate = Tourist.Tip.Base.prototype.nextButtonTemplate.replace('Next step →', '${message(code:"app.get_started.tour.next").decodeHTML()}');
      Tourist.Tip.Base.prototype.finalButtonTemplate = Tourist.Tip.Base.prototype.finalButtonTemplate.replace('Finish up', '${message(code:"app.get_started.tour.finish").decodeHTML()}');

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

      $('#tour-btn').on('click', function(){

        TOUR.start();
      });
    });
    </script>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
	     <h1><g:message code="app.get_started.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <h2>1. <g:message code="app.get_started.tour" /></h2>
        <p><g:message code="app.get_started.tour.text" /></p>
        <button class="btn btn-primary" id="tour-btn"><g:message code="app.get_started.tour.start" /></button>
        <br/><br/>
      </div>
      <div class="col-lg-12">
        <h2>2. <g:message code="app.get_started.guides" /></h2>
        <p><g:message code="app.get_started.guides.text" /></p>
        <a href="https://cloudehrserver.com/learn" target="_blank"><g:message code="app.get_started.guides.link" /></a>
        <br/><br/>
      </div>
      <div class="col-lg-12">
        <h2>3. <g:message code="app.get_started.forum" /></h2>
        <p><g:message code="app.get_started.forum.text" /></p>
        <a href="https://www.cabolabs.com/forum/categories/ehrserver" target="_blank"><g:message code="app.get_started.forum.link" /></a>
        <br/><br/>
      </div>
      <div class="col-lg-12">
        <h2>4. <g:message code="app.get_started.groups" /></h2>
        <p><g:message code="app.get_started.groups.text" /></p>
        <a href="https://www.facebook.com/groups/ehrserver/" target="_blank"><g:message code="app.get_started.groups.fb" /></a>
        <br/>
        <a href="https://www.linkedin.com/groups/12070397" target="_blank"><g:message code="app.get_started.groups.ln" /></a>
        <br/><br/>
      </div>
    </div>
  </body>
</html>
