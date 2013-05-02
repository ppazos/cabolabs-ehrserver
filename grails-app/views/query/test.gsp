<html>
  <head>
    <meta name="layout" content="main">
    <style>
      tr td:last-child select, tr td:last-child input {
        width: 100%;
      }
      tr td:first-child {
        width: 140px;
      }
      tr td:first-child {
        text-align: right;
      }
      td {
        font-size: 0.9em;
      }
      #addCriteria {
        padding: 10px;
        text-align: right;
        display: block;
        font-weight: bold;
      }
      textarea {
          width: 98%;
          height: 300px;

          display: block;
          border: 1px solid black;
          padding: 5px;
          margin: 5px;
          font-family: courier;
          font-size: 12px;
      }
      .actions {
        text-align: right;
      }
      label {
        font-weight: bold;
      }
      .content_padding {
        padding: 10px;
      }
      .chartContainer {
        width: 100%;
        height: 400px;
      }
      .icon {
        width: 64px;
        border: none;
      }
      #results, #show_data {
        display: none;
      }
    </style>
    <g:javascript src="jquery-1.8.2.min.js" />
    <g:javascript src="jquery.form.js" /><!-- ajax form -->
    <g:javascript src="xml_utils.js" /><!-- xmlToString -->
    <script src="${resource(dir:'js', file:'highcharts/highcharts.js')}" type="text/javascript"></script>
    
    <g:javascript>
      $(document).ready(function() {
     
     
        // ====================================================================
        // Muestra los datos crudos devueltos por el servidor
        // ====================================================================
        
        $('#show_data').click( function(e) {
          
          e.preventDefault();
          $('#results').toggle('slow');
        });
        
    
        // ====================================================================
        // Submit ajax para busqueda de compositions por criterios de datos
        // ====================================================================
        
        $('#form_composition').ajaxForm({
        
          //dataType: 'json',
          url: '${createLink(controller:'query', action:'testQueryByData')}',
          //data: {doit:'true'},
        
          beforeSubmit: function(data, form, options) {            // >>> BEFORE SUBMIT
            
            console.log('form_composition beforeSubmit', data);
            
            valid = true;
            
            // Verifica que todos los valores necesarios para la query fueron ingresados
            $('input[type=text][name=value]').each( function(i, elem) {
              
              e = $(elem);
              e.removeClass('errors');
              
              //console.log($(elem).val(), elem.value);

              if (e.val() == '')
              {
                valid = false;
                e.addClass('errors');
              }
            });
            
            if (!valid)
            {
              alert('Introduzca los valores para el criterio de la consulta');
            }
            
            return valid;
          },
          
          success: function(responseText, statusTest, req, form) {  // >>> SUCCESS
            
            console.log('form_composition success');
            //console.log(responseText);
            //console.log(statusTest);
            //console.log(req);
            //console.log(form);

            // Si devuelve HTML
            if ($('select[name=showUI]').val()=='true')
            {
               $('#results').html( responseText );
            }
            else // Si devuelve el XML
            {
              //console.log("Resultado XML: form_composition.submit.success", responseText);
            
              $('#results').empty();
             
              // el append devuelve la DIV no el PRE, chidren tiene el PRE
              var pre = $('#results').append('<pre></pre>').children()[0];
              $(pre).text( formatXml( xmlToString(responseText) ) );
               
              // Como XML no hace render de tabla o grafica, muestro los datos
              // crudos como si hiciera clic en show_data.
              $('#results').show('slow');
            }
            
            // Muestra el boton que permite ver los datos crudos
            // devueltos por el servidor
            $('#show_data').show();
          }
        });
        
        
        // ====================================================================
        // Submit ajax para busqueda de datos por paths
        // ====================================================================
        
        /**
         * En lugar de asociar directamente el ajaxForm, tengo que asociarlo
         * en el momento del submit porque el dataType puede cambiar segun lo
         * que seleccione el usuario.
         * Si bindeo el ajaxForm y el usuario cambia el dataType, ocurren un
         * error en el submit.
         */
        $('#form_datavalue').submit( function(e) {
        
          // Validacion
          if ($('select[name=qehrId]').val()==null)
          {
            alert('Seleccione un EHR');
            return false;
          }
        
        
          // xml o json
          type = $('select[name=format]').val();
          
          
          // En lugar de bindear ajaxForm, llamo a ajaxSubmit sobre el form. 
          $(this).ajaxSubmit({
          
            dataType: type,
            
            // ============================================================
            // TODO: poner el codigo de test/queryData en QueryController
            // ============================================================
            
            url: '${createLink(controller:'test', action:'queryData')}',
            data: {doit:true},
          
            beforeSubmit: function(data, form, options) {
            
              console.log('form_datavalue beforeSubmit');
            },
            
            success: function(responseText, statusTest, req, form) {
              
              console.log('form_datavalue success');
              
              // Vacia el output de data xml o json
              $('#results').empty();
              
              // Vacia donde se va a mostrar la tabla o el chart
              $('#chartContainer').empty();
              
              
              console.log('form_datavalue success 2');
              
              
              // Si devuelve JSON (verifica si pedi json)
              if ($('select[name=format]').val()=='json')
              {
                console.log('form_datavalue success json');
              
              
                // http://stackoverflow.com/questions/4810841/json-pretty-print-using-javascript
                var pre = $('#results').append('<pre></pre>').children()[0];
                $(pre).text( JSON.stringify(responseText, undefined, 2) );
                
                
                // =================================================================
                // Si agrupa por composition (muestra tabla)
                //
                if ($('select[name=group]').val() == 'composition')
                {
                  queryDataRenderTable(responseText);
                }
                else if ($('select[name=group]').val() == 'path')
                {
                  queryDataRenderChart(responseText);
                }
              }
              else // Si devuelve el XML
              {
                console.log('form_datavalue success XML');
              
                // el append devuelve la DIV no el PRE, chidren tiene el PRE
                var pre = $('#results').append('<pre></pre>').children()[0];
                $(pre).text( formatXml( xmlToString(responseText) ) );
                
                
                // Como XML no hace render de tabla o grafica, muestro los datos
                // crudos como si hiciera clic en show_data.
                $('#results').toggle('slow');
              }
              
              
              // Muestra el boton que permite ver los datos crudos
              // devueltos por el servidor
              $('#show_data').show();
              
              
              // Hace scroll animado para mostrar el resultado
              $('html,body').animate({scrollTop:$('#results').offset().top+400}, 500);
            },
            
            error: function(response, textStatus, errorThrown)
            {
              console.log('error form_datavalue');
              console.log(response);
              
              alert(response.responseText); // lo devuelto por el servidor
            }
          });
          
          return false; // previene submit por defecto
          
        }); // form data_value ajax submit

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
          // name es el nombre del DataIndex coorespondiente al archId y path del DataValueIndex
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
          htmlrows += '<a href="'+ linkCompoXML +'?uid='+ data.uid +'" target="_blank"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></a>';
          htmlrows += '<a href="'+ linkCompoUI  +'?uid='+ data.uid +'" target="_blank"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></a>';
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
      
    </g:javascript>
  </head>
  <body>
    <h1>Prueba de consulta "${name}"</h1>
      
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>
      
    <g:if test="${type == 'composition'}">
      
      <h2>Búsqueda de documentos por datos</h2>
      <form id="form_composition" method="post">
      
        <h3>Criterio</h3>
        <table>
          <tr>
            <th>archetypeId</th>
            <th>path</th>
            <th>operand</th>
            <th>value</th>
          </tr>
          <g:each in="${archetypeId}" var="archId" status="i">
            <tr>
              <td>
                ${archId}
                <input type="hidden" name="archetypeId" value="${archId}" />
              </td>
              <td>
                ${path[i]}
                <input type="hidden" name="path" value="${path[i]}" />
              </td>
              <td>
                ${operand[i]}
                <input type="hidden" name="operand" value="${operand[i]}" />
              </td>
              <td>
                <%-- sino hay valor, se solicita --%>
                <g:if test="${value[i]}">
                  ${value[i]}
                  <input type="hidden" name="value" value="${value[i]}" />
                </g:if>
                <g:else>
                  <input type="text" name="value" />
                </g:else>
              </td>
            </tr>
          </g:each>
        </table>
        
        <h3>Filtros</h3>
        <table>
          <tr>
            <td>ehrId</td>
            <td>
              <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" />
            </td>
          </tr>
          <tr>
            <td>archetypeId</td>
            <td>
              <!-- FIXME: busco los arquetipos de composition en los indices porque
                     el EHRServer aun no tiene repositorio de arquetipos. Cuando lo
                     tenga, esta operacion deberia usar el ArchetypeManager. -->
                     
              <!-- solo arquetipos de composition -->
              <g:select name="qarchetypeId" size="4"
                        from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }" />
            </td>
          </tr>
          <tr>
            <td>dates (yyyymmdd)</td>
            <td>
              from <input type="text" name="fromDate" />
              to <input type="text" name="toDate" />
            </td>
          </tr>
          <tr>
            <td>retrieve data?</td>
            <td>
              <select name="retrieveData">
                <option value="false" selected="selected">no</option>
                <option value="true">yes</option>
              </select>
            </td>
          </tr>
          <tr>
            <td>show UI?</td>
            <td>
              <select name="showUI">
                <option value="false" selected="selected">no</option>
                <option value="true">yes</option>
              </select>
            </td>
          </tr>
        </table>
      
        <fieldset class="buttons">
          <input type="submit" value="Ejecutar" />
        </fieldset>
      
      </form>
        
    </g:if>
    <g:else>
    
      <h2>Búsqueda de datos</h2>
      <form id="form_datavalue" method="post">
      
        <h3>Selección</h3>
        <table>
          <tr>
            <th>archetypeId</th>
            <th>path</th>
          </tr>
          <g:each in="${archetypeId}" var="archId" status="i">
            <tr>
              <td>
                ${archId}
                <input type="hidden" name="archetypeId" value="${archId}" />
              </td>
              <td>
                ${path[i]}
                <input type="hidden" name="path" value="${path[i]}" />
              </td>
            </tr>
          </g:each>
        </table>
        
        <h3>Filtros</h3>
        <table>
          <tr>
             <td>ehrId</td>
             <td>
               <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" />
             </td>
          </tr>
          <tr>
             <td>archetypeId</td>
             <td>
               <!--
               FIXME: busco los arquetipos de composition en los indices porque
               el EHRServer aun no tiene repositorio de arquetipos. Cuando lo
               tenga, esta operacion deberia usar el ArchetypeManager.
               -->
                      
               <!-- solo arquetipos de composition -->
               <g:select name="qarchetypeId" size="4"
                         from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }" />
             </td>
          </tr>
          <tr>
             <td>dates (yyyymmdd)</td>
             <td>
               from <input type="text" name="fromDate" />
               to <input type="text" name="toDate" />
             </td>
          </tr>
          <tr>
             <td>default format</td>
             <td>
               <select name="format" size="2">
                 <option value="xml" ${((format=='xml') ? 'selected="selected"':'')}>XML</option>
                 <option value="json"${((format=='json') ? 'selected="selected"':'')}>JSON</option>
               </select>
             </td>
           </tr>
           <tr>
             <td>default group</td>
             <td>
               <select name="group" size="3">
                 <option value="" ${((group=='') ? 'selected="selected"':'')}>none</option>
                 <option value="composition" ${((group=='composition') ? 'selected="selected"':'')}>composition</option>
                 <option value="path" ${((group=='path') ? 'selected="selected"':'')}>path</option>
               </select>
             </td>
          </tr>
        </table>
      
        <fieldset class="buttons">
          <input type="submit" value="Ejecutar" />
        </fieldset>
      </form>
    </g:else>
    
    <h2>Resultado</h2>
    <a href="#" id="show_data">Ver datos</a>
    <div id="results" class="out"></div>
    <div id="chartContainer"></div>
  </body>
</html>