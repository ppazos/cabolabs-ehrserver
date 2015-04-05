<html>
  <head>
    <%-- Modal, doesnt have layout, used from query/create to test queries before creation --%>
    
    <asset:javascript src="query_test_and_execution.js" />
    
    <script type="text/javascript">
      $(document).ready(function() {

      }); // ready
      
      
      var queryDataRenderTable = function(data)
      {
        var headers = data[0];
        var rows = data[1];
        var table = $('<table></table>');
        
        
        // ================================================================
        // Muestra headesr y subheaders
        
        htmlsubheaders = ''; // subheaders para cada header
        htmlheaders = '<tr>';
        
        $.each(headers, function(path, subheaders) {
        
          console.log('path y subheaders', path, subheaders);
          
          // TODO: deberia ser archetype+path para que sea absoluta
          // name es el nombre del IndexDefinition coorespondiente al archId y path del DataValueIndex
          htmlheaders += '<th colspan="'+ subheaders.attrs.length +'" title="'+ path +'">'+ subheaders.name +'</th>';
          
          $.each(subheaders.attrs, function(i, attr)
          {
            console.log('attr', attr);
            htmlsubheaders += '<td>'+ attr +'</td>';
          });
          
        });
        htmlheaders +='<th></th></tr>'; // th extra para las acciones de ver composition de cada fila
        
        
        // =================================================================
        // Muestra cada fila
        htmlrows = '';
        
        linkCompoXML = '${createLink(controller:"ehr", action:"showComposition")}';
        linkCompoUI = '${createLink(controller:"ehr", action:"showCompositionUI")}';
        
        // itera por filas
        $.each(rows, function(compoId, data) { // data [date, uid, cols [ {type, path, attrs dep. del type}, {...}] ]
        
          // itera por columnas (headesrs = paths)
          htmlrows += '<tr>';
          $.each(data.cols, function(ix, colvalues) { // evito attr type y path, los demas son los atributos de los subheaders que dependen del type del datavalue
          
            console.log('ix y colvalues', ix, colvalues);
          
            // itera por atributos simples de datavalues de cada columna (subheaders)
            $.each(colvalues, function(attr, value) {
            
              console.log('attr y value', attr, value);
            
              if (attr == 'path' || attr == 'type') return true;
            
              htmlrows += '<td>'+ value +'</td>';
            });
          });
          
          // links a composition
          htmlrows += '<td>';
          htmlrows += '<a href="'+ linkCompoXML +'?uid='+ data.uid +'" target="_blank"><img src="${assetPath(src:'xml.png')}" class="icon" /></a>';
          htmlrows += '<a href="'+ linkCompoUI  +'?uid='+ data.uid +'" target="_blank"><img src="${assetPath(src:'doc.png')}" class="icon" /></a>';
          htmlrows += '</td></tr>';
        });
        
        
        // Uso el chartContainer para mostrar la tabla
        table.html( htmlheaders + htmlsubheaders + htmlrows );
        $('#chartContainer').append(table);
      };
      
      
      var queryDataRenderChart = function(data)
      {
        /*
         series: [{
           name: 'Jane',
           data: [1, 0, 4]
         }, {
           name: 'John',
           data: [5, 7, 3]
         }]
         */
         var series = [];
         
         /*
         data = {
           path: {
             type: 'xx',
             name: 'yy',
             serie: [ dvi, dvi, dvi ]
           }
         }
         */
         $.each( data, function(path, dviseries) {
         
           console.log('path y dviseries', path, dviseries);
           
           /**
            * Estructura:
            *   { name: 'John', data: [5, 7, 3] }
            *
            *   o si quiero mostrar una etiqueta en el punto:
            *   { name: 'John', data: [{name:'punto', color:'#XXX', y:5},{..},{..}] }
            */
           var serie = { name: dviseries.name, data: [] };
        

           // FIXME: cuidado, esto es solo para DvQuantity!!!!!
           $.each( dviseries.serie, function(ii, dvi) {
            
             //console.log('ii y dvi', ii, dvi);
            
             // FIXME: el valor depende del tipo de dato, y para graficar se necesitan ordinales
             // TODO: ver si se pueden graficar textos y fechas
             // TODO: prevenir internar graficar tipos de datos que no se pueden graficar
             //serie.data.push( dvi.magnitude );
             
             // para que la etiqueta muestre las unidades
             point = {name: dvi.magnitude+' '+dvi.units,
                      y: dvi.magnitude}
             serie.data.push(point);

           });
           
           series.push(serie);
         });
         
         //console.log( series );
         
         // ========================================
         // Test chart
         renderchart(series);
         // ========================================
      };
      
      
      // =======================================================================
      // TEST CHART
      var chart;
      // =======================================================================
      
      
      var renderchart = function(series)
      {
        // =============================================================================
        // TEST CHART:
        //
        // TODO: necesito las fechas para ubicar valores en X
        // - las series se separan por path y se grafican juntas
        //
        chart = new Highcharts.Chart({
          chart: {
            renderTo: 'chartContainer',
            type: 'line',
            zoomType: 'x' // lo deja hacer zoom en el eje x, y o ambos: xy
          },
          /* depende de lo que este graficando!
          title: {
            text: 'Blood Pressure' // TODO: obtener del arquetipo+path en la ontologia del arquetipo
          },
          */
          xAxis: {
            categories: []
          },
          /* depende de lo que este graficando!
          yAxis: {
            title: {
              text: 'Blood Pressure mmHg' // TODO: obtener del arquetipo
            }
          },
          */
          plotOptions: {
            line: {
              dataLabels: {
                enabled: true
              }
            }
          },
          series: series
        });
      };
      
    </script>
  </head>
  <body>
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>
    
    <div id="query_test_composition">
      
      <h2>B&uacute;queda de documentos</h2>

      <h3>Filtros</h3>
      <table>
        <tr>
          <td>retrieve data?</td>
          <td>
            <select name="retrieveData">
              <option value="false" selected="selected">no</option>
              <option value="true">yes</option>
            </select>
          </td>
        </tr>
      </table>
    </div><!-- test_by_composition -->

    <div id="query_test_datavalue">
      <h2>B&uacute;queda de datos</h2>
      <h3>Filtros</h3>
    </div><!-- query_test_datavalues -->
    
    <div id="query_test_common">
      ehrId <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" /><br />
      from <input type="text" name="fromDate" />
      to <input type="text" name="toDate" /><br />
      
      <!-- FIXME: busco los arquetipos de composition en los indices porque
                  el EHRServer aun no tiene repositorio de arquetipos. Cuando lo
                  tenga, esta operacion deberia usar el ArchetypeManager. -->

      <!-- solo arquetipos de composition -->
      document type <g:select name="qarchetypeId" size="4"
                              from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }" />
    </div>
    
    <h2><g:message code="query.execute.results" /></h2>
    <a href="#" id="show_data"><g:message code="query.execute.showData" /></a>
    <div id="results" class="out"></div>
    <pre><code id="code"></code></pre>
    <div id="chartContainer"></div>
  </body>
</html>
