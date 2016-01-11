<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.Contribution" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'contribution.label', default: 'Contribution')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
    <asset:javascript src="highcharts/highcharts.js" />
    <script type="text/javascript">

      var series = [];
      var serie = { name: 'contributions', data: [] };
    
	   <%
      println 'var start = Date.UTC('+ (start.year + 1900) +','+ (start.month - 1) +','+ start.date +');'
      def match
	   data.each { point ->
	       
         match = (point[1].toString() =~ /(\d{4})(\d{2})/) //=match for year and month, /(\d{4})(\d{2})(\d{2})/ match for ymd

         // -1 because month is 0 based
	      println 'serie.data.push(['+
          'Date.UTC('+ match[0][1] +', (' + match[0][2] +' -1), 1), '+ point[0] +
         ']);' // point[0] is the count, point[1] is the group date
	   }
	   %>

      // avoid error on empty serie.data
      if (serie.data.length > 0) series.push(serie);

	   console.log(series);
    
	   $(function () {
	      var chart = new Highcharts.Chart({
           chart: {
              renderTo: 'contributionsChartContainer',
              type: 'column',
              zoomType: 'x'
           },
           title: {
               text: 'Contributions'
           },
           subtitle: {
               text: ''
           },
           xAxis: {
               type: 'datetime',
               dateTimeLabelFormats: {
                   second: '%H:%M:%S',
                   minute: '%H:%M',
                   hour: '%H:%M',
                   day: '%e. %b',
                   week: '%e. %b',
                   month: '%b \'%y',
                   year: '%Y'
               },
               tickInterval: 24 * 3600 * 1000 * 30 //= 1 month // 24 * 3600 * 1000 //= 1 day
           },
           yAxis: {
               min: 0,
               allowDecimals: false, // no decimals on y, just integers
               title: {
                   text: 'Count'
               }
           },
           tooltip: {
               headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
               pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                   '<td style="padding:0"><b>{point.y}</b></td></tr>',
               footerFormat: '</table>',
               shared: true,
               useHTML: true,
               xDateFormat: '%b \'%y' //'%Y-%m-%d'
           },
           plotOptions: {
               column: {
                   pointPadding: 0,
                   borderWidth: 0,
                   groupPadding: 0.1,
                   //pointStart: start
               }
           },
           series: series
	     });
	   });
    </script>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="contribution.list.title" /></h1>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
        <div id="contributionsChartContainer"></div>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
         <g:if test="${flash.message}">
	        <div class="message" role="status">${flash.message}</div>
	      </g:if>
	      <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
		        <thead>
		          <tr>
		            <g:sortableColumn property="uid" title="${message(code: 'contribution.uid.label', default: 'UID')}" />
		            <th>EHR</th>
		            <th>Time Committed</th>
		            <th># Versions</th>
		          </tr>
		        </thead>
		        <tbody>
			        <g:each in="${contributionInstanceList}" status="i" var="contributionInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td><g:link action="show" id="${contributionInstance.id}">${fieldValue(bean: contributionInstance, field: "uid")}</g:link></td>
			            <td>${contributionInstance.ehr.uid}</td>
			            <td>${contributionInstance.audit.timeCommitted}</td>
			            <td>${contributionInstance.versions.size()}</td>
			          </tr>
			        </g:each>
		        </tbody>
		     </table>
	      </div>
         <g:paginator total="${total}" />
      </div>
    </div>
  </body>
</html>
