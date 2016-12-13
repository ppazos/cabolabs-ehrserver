<%@ page import="com.cabolabs.security.Organization" %><%@ page import="com.cabolabs.ehrserver.account.ApiKey" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="organization.show.title" /></title>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="organization.show.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
      
	     <g:if test="${flash.message}">
	       <div class="message" role="status">${flash.message}</div>
	     </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="organization.name.label" default="Name" /></th>
              <td><g:fieldValue bean="${organizationInstance}" field="name"/></td>
            </tr>
            <tr>
              <th><g:message code="organization.number.label" default="Number" /></th>
              <td><g:fieldValue bean="${organizationInstance}" field="number"/></td>
            </tr>
            <tr>
              <th><g:message code="organization.uid.label" default="UID" /></th>
              <td><g:fieldValue bean="${organizationInstance}" field="uid"/></td>
            </tr>
            <tr>
              <th><g:message code="organization.ehrs.label" default="EHRS" /></th>
              <td><g:link controller="ehr" action="list" params="[organizationUid:organizationInstance.uid]"><g:message code="common.action.display" /></g:link></td>
            </tr>
          </tbody>
        </table>
        
        <div class="btn-toolbar" role="toolbar">
          <g:form url="[resource:organizationInstance, action:'delete']" method="DELETE">
            <fieldset class="buttons">
              <g:link action="edit" id="${organizationInstance.uid}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
              <%--
              <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
              --%>
            </fieldset>
          </g:form>
        </div>
	   </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
        <h2><g:message code="organization.show.stats" /></h2>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12 stats">
        <g:include controller="stats" action="organization" params="[uid: organizationInstance.uid]" />
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12">
        <div align="center">
          <button class="prev btn btn-default btn-md"><span class="fa fa-chevron-left fa-fw" aria-hidden="true"></span></button>
          <span class="ref_date"></span>
          <button class="next btn btn-default btn-md"><span class="fa fa-chevron-right fa-fw" aria-hidden="true"></span></button>
        </div>
        <script>
            var first_day_prev_month_of = function(date)
            {
               var d = new Date(date);
               d.setUTCDate(1);
               d.setUTCMonth( d.getUTCMonth() - 1 );
               d.setUTCHours(0,0,0,0);
               return d;
            };
            var first_day_current_month_of = function(date)
            {
               var d = new Date(date);
               d.setUTCDate(1);
               d.setUTCHours(0,0,0,0);
               return d;
            };
            var last_day_prev_month_of = function(date)
            {
               var d = new Date(date);
               d.setUTCDate(0); // last day of previous month
               return d;
            };
            var first_day_next_month_of = function(date)
            {
               var d = new Date(date);
               d.setUTCDate(1);
               d.setUTCMonth( d.getUTCMonth() + 1 );
               d.setUTCHours(0,0,0,0);
               return d;
            };
            var last_day_next_month_of = function(date)
            {
               var d = new Date(date);
               d.setUTCMonth( d.getUTCMonth() + 2 ); // next next month
               d.setUTCDate(0); // last day of previous month
               return d;
            };
            var first_day_next_next_month_of = function(date)
            {
               var d = new Date(date);
               d.setUTCDate(1);
               d.setUTCMonth( d.getUTCMonth() + 2 ); // next, next month
               d.setUTCHours(0,0,0,0);
               return d;
            };
            
            console.log('now', new Date(ref_date).toUTCString());
            
            
            console.log(first_day_prev_month_of(ref_date).toUTCString());
            console.log(first_day_current_month_of(ref_date).toUTCString());
            console.log(first_day_next_month_of(ref_date).toUTCString());
            console.log(first_day_next_next_month_of(ref_date).toUTCString());

            $(function() {
              
              $('.ref_date').text( new Date(ref_date).toISOString().slice(0,7) );
              
              $('button.prev').on( "click", function() {
                var prev_from = first_day_prev_month_of(ref_date);
                var prev_to = first_day_current_month_of(ref_date);
                
                console.log(ref_date, prev_from, prev_to);
                
                var data = {'uid': '${params.uid}', 'from': prev_from.getTime(), 'to': prev_to.getTime()};

                $('.stats').load("${g.createLink(controller:'stats', action:'organization')}", data, function() {
                  $('.ref_date').text( new Date(ref_date).toISOString().slice(0,7) );
                });
              });
              
              $('button.next').on( "click", function() {
                var next_from = first_day_next_month_of(ref_date);
                var next_to = first_day_next_next_month_of(ref_date);
                
                console.log(ref_date, next_from, next_to);
                
                var data = {'uid': '${params.uid}', 'from': next_from.getTime(), 'to': next_to.getTime()};

                $('.stats').load("${g.createLink(controller:'stats', action:'organization')}", data, function() {
                  $('.ref_date').text( new Date(ref_date).toISOString().slice(0,7) );
                });
              });
            });
        </script>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
        <h2><g:message code="organization.show.apikeys" /></h2>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12 stats">
        <g:link action="generateApiKey" params="[organizationUid: params.uid]" class="create">Generate API Key</g:link>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12 stats">
        <g:set var="apikeys" value="${ApiKey.findAllByOrganization(organizationInstance)}" />
        <table>
          <g:each in="${apikeys}" var="key">
            <tr>
              <td>
                <textarea cols="80" rows="6">${key.token}</textarea>
              </td>
              <td><g:link action="deleteApiKey" id="${key.id}" class="delete">Remove</g:link></td>
            </tr>
          </g:each>
        </table>
      </div>
    </div>
  </body>
</html>
