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
   <div class="table-responsive">
     <table class="table table-striped table-bordered table-hover">
	    <tr>
	      <th><g:message code="contribution.attr.timeCommitted" /></th>
	      <th><g:message code="contribution.attr.committer" /></th>
	    </tr>
	    <g:render template="../contribution/contributionRow" model="[contribution:contribution]"/>
	  </table>
  </div>
</g:each>


    <script type="text/javascript">
    $(document).ready(function() { // same code as versionedComposition show, TODO: refactor
    
      $('.showCompo').on('click', function(e) {
        
        e.preventDefault();
          
        iframe = $('iframe', '#html_modal');
        iframe[0].src = this.href;
          
        $('#html_modal').modal();
      });
      
      $('#html_modal').on('hidden.bs.modal', function (event) {
        iframe = $('iframe', '#html_modal');
        iframe[0].src = '';
      });
      
      $('.compoXml').on('click', function(e) {
        
        e.preventDefault();
        
        $.ajax({
          url: this.href,
          dataType: 'xml',
          success: function(xml, textStatus)
          {
            console.log('xml', xml);
            $('#xml').addClass('xml');
            $('#xml').text(formatXml( xmlToString(xml) ));
            $('#xml').each(function(i, e) { hljs.highlightBlock(e); });
            
            $('#xml_modal').modal();
          }
        });
        
      });
      
      $('#xml_modal').on('hidden.bs.modal', function (event) {
        $('#xml').text('');
      });
    });
    </script>
    <div class="modal fade" id="xml_modal" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
         <div class="modal-body">
           <pre><code id="xml"></code></pre>
         </div>
        </div>
      </div>
    </div>
    
    <div class="modal fade" id="html_modal" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-body">
            <iframe src="" style="padding:0; margin:0; width:100%; height:540px; border:0;"></iframe>
          </div>
        </div>
      </div>
    </div>

<script type="text/javascript">

//$(document).ready(function() {

   var series = [];
   var serie = { name: '${message(code:"contribution.list.title")}', data: [] };

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
        text: '${message(code:"contribution.list.title")}'
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
          text: '${message(code:"contribution.list.title")}'
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
