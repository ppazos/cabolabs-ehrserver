<%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %>
<%--

in: contributions

=========================================================
          
Muestra tabla: con rowspan por los datos de la contribution,
y muestra todas las compositions de la contrib:

|-------------------------------------------------------------
|                               |                            | 
|    contrib time committed     |          committer         |
|                               |                            |
|-------------------------------------------------------------
| |--------------------------------------------------------| | 
| | version uid 1 | creation date 1 | type 1 | change type | |
| | version uid 1 | creation date 1 | type 1 | change type | |
| |--------------------------------------------------------| |
|------------------------------------------------------------|

--%>

<div id="contributionsChartContainer"></div>

<g:each in="${contributions}" var="contribution">
   <div class="table-responsive" id="versions">
     <table class="table table-striped table-bordered table-hover">
	    <tr>
	      <th>time committed</th>
	      <th>committer</th>
	    </tr>
	    <g:render template="../contribution/contributionRow" model="[contribution:contribution]"/>
	  </table>
  </div>
</g:each>


<%-- Modal para mostrar el contenido de una composition --%>
<div id="composition_modal" style="width:100%; height:100%; display:none;"><iframe src="" style="padding:0; margin:0; width:100%; height:100%; border:0;"></iframe></div>

<script type="text/javascript">

//$(document).ready(function() {

   var series = [];
   var serie = { name: 'contributions', data: [] };

   <%
   contributions.each { contrib ->
      
      def d = contrib.audit.timeCommitted
      def jsd = 'Date.UTC('+
      (d.year + 1900)    +', '+
      d.month   +', '+
      d.date    +', '+
      d.hours   +', '+
      d.minutes +', '+
      d.seconds +
      ')'
      
      def point = '['+ jsd +', 1]'
      println 'serie.data.push('+ point +');'
      
      
      println "console.log('"+ d +"');"
   }
   %>

   series.push(serie);
   

   // Charting timeline for contributions
   var chart = new Highcharts.Chart({
     chart: {
        renderTo: 'contributionsChartContainer',
        type: 'scatter',
        zoomType: 'x' // lo deja hacer zoom en el eje x, y o ambos: xy
     },
     rangeSelector : {
       selected : 1
     },
     title: {
        text: 'Contributions'
     },
     xAxis: {
        type: 'datetime',
        /*
        dateTimeLabelFormats: { // don't display the dummy year
          millisecond: '%H:%M:%S.%L',
            second: '%H:%M:%S',
            minute: '%H:%M',
            hour: '%H:%M',
            day: '%e. %b',
            week: '%e. %b',
            month: '%b \'%y',
            year: '%Y'
        },
        */
        title: {
          text: null
        }
     },
     yAxis: {
        title: {
          text: 'Contributions'
        },
        allowDecimals: false, // no decimals on y, just integers
     },
     tooltip: {
       //headerFormat: '<b>{series.name}</b><br>',
       //pointFormat: '{point.x: %Y}',
       formatter: function() {
         // http://stackoverflow.com/questions/7101464/how-to-get-highcharts-dates-in-the-x-axis
         return Highcharts.dateFormat('%Y-%m-%e %H:%M:%S', new Date(this.x));
       },
       shared: true
     },
     plotOptions: {
        scatter:  {
                marker: {
                    radius: 3,
                    states: {
                        hover: {
                            enabled: true,
                            lineColor: 'rgb(100,100,100)'
                        }
                    }
                },
                states: {
                    hover: {
                        marker: {
                            enabled: false
                        }
                    }
                }
            }
     },
     series: series
   });
   
//});
</script>
