<html>
  <head>
    <meta name="layout" content="main">
    <style>
      tr td:last-child select, tr td:last-child input {
        width: 100%;
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
    </style>
    <asset:javascript src="xml_utils.js" /><!-- xmlToString -->
    <g:javascript>
      /**
       * FIXME: formRemote no me deja hacer validacion e impedir el submit, necesito usar el jQuery Form Plugin y listo.
       */
      var queryBefore = function()
      {
         /*
         console.log($(this));
         console.log( $('#criteria > tr').length );
         return false;
         */
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
      
      /**
       * Handler success para consulta por ajax de remoteForm.
       */
      var querySuccess = function(data, textStatus)
	   {
         //console.log(data);

         // Vacia el output de data xml o json
	      $('#querySuccess').empty();
	      
	      // Vacia donde se va a mostrar la tabla o el chart
         $('#chartContainer').empty();
         
	      
	      // Si devuelve JSON (verifica si pedi json)
	      if ($('select[name=format]').val()=='json')
	      {
	        // http://stackoverflow.com/questions/4810841/json-pretty-print-using-javascript
	        var pre = $('#querySuccess').append('<pre></pre>').children()[0];
	        $(pre).text( JSON.stringify(data, undefined, 2) );
	        
	        
	        // =================================================================
	        // Verifica si pedi agrupado por composition (muestra tabla)
	        //
	        if ($('select[name=group]').val() == 'composition')
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
	            
	            $.each(subheaders.attrs, function(i, attr) {

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
	            htmlrows += '<a href="'+ linkCompoXML +'?uid='+ data.uid +'"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></a>';
               htmlrows += '<a href="'+ linkCompoUI  +'?uid='+ data.uid +'"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></a>';
	            htmlrows += '</td></tr>';
	          });
	          
	          
	          // Uso el chartContainer para mostrar la tabla
	          table.html( htmlheaders + htmlsubheaders + htmlrows );
	          $('#chartContainer').append(table);

	          
	          // Evito graficar si selecciona algun group
	          return;
	        }
	        // =================================================================
	        
	        
	        //console.log( 'select group', $('select[name=group]').val() );
	        
	        
	        // =================================================================
	        // Evito graficar si selecciona algun group
           // Verifica si pedi agrupado por path (TODO: muestra chart, hay que
           // reescribir el render del chart para considerar la estructura de
           // data agrupada por path)
           //
	        if ($('select[name=group]').val() == 'path')
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
	          
	          return;
	        }
	        // =================================================================
	        
	        
	        // =================================================================
	        // Si group es none (FIXME: deberia graficar cuando group es path)
	        //
	        var chartData = {};
	        
	        // Path por las que agrupar las series de datos
	        $('input[name=path]').each ( function (i, input) {
	          
	          path = $(input).val();
	          
	          //console.log('path', path);
	          
	          chartData[path] = [];
	          
	          $(data).each ( function (i, obj) {
           
	             //a = $.makeArray(obj);
	             //console.log(a);
	             
	             if (obj.path == path)
	             {
	               chartData[path].push( obj.magnitude ); // FIXME: esto es solo para DvQuantity
	               
	               //console.log('magnitude', obj.magnitude);
	             }
	          });
	        });
	        
	        //console.log('chartData', chartData);
	        
	        
	        /**
	         * Estructura de series de Highcharts
	         * [
            *   { name: 'systolic', // TODO: obtener de arquetipo
            *     data: []
            *   },
            *   { name: 'diastolic', // TODO: obtener de arquetipo
            *     data: []
            *   }
            * ]
	         */
	        var series = [];
	         
	        $.each( chartData, function(path, serie) {
             
             console.log(path, series);
             
             serie = {
               name: path,
               data: serie
             }
             
             series.push(serie);
           });
	        
	        //console.log('series', series);
	        
	        // ========================================
	        // Test chart
	        renderchart(series);
	        // ========================================
	        
	      }
	      else // Si devuelve el XML
	      {
	        // el append devuelve la DIV no el PRE, chidren tiene el PRE
	        var pre = $('#querySuccess').append('<pre></pre>').children()[0];
	        $(pre).text( formatXml( xmlToString(data) ) );
	      }
	   };
	   
	   
	   /**
       * Handler failure para consulta por ajax de remoteForm.
       * FIXME: verificar el nombre de los parametros (no es data).
       */
	   var queryFailure = function(XMLHttpRequest, textStatus, errorThrown)
	   {
         console.log(XMLHttpRequest, textStatus, errorThrown);
         alert("error: " + errorThrown + ' ('+ XMLHttpRequest.responseText +')');
         
	      //$('#queryFailure').text( xmlToString(data) );
      };
      
      
      
	   $(document).ready(function() {
    
        /**
         * Clic en un arquetipo de la lista de arquetipos (select[sarchetypeId])
         * Lista las paths del arquetipo en select[spath]
         */
        $('select[name=sarchetypeId]').change(function() {
        
          var archetypeId = $(this).val(); // arquetipo seleccionado
		    
		    // http://api.jquery.com/jQuery.ajax/
		    //
          $.ajax({
				  url: '${createLink(controller:"test", action:"getIndexDefinitions")}',
				  data: {archetypeId: archetypeId},
				  dataType: 'json',
				  success: function(data, textStatus) {
				  
				    // didx:
				    //   archetypeId: "openEHR-EHR-COMPOSITION.encounter.v1"
					 //   name: "value"
					 //   path: "/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value"
					 //   rmTypeName: "DV_QUANTITY"
					 
					 
		          // Saca las options que haya
		          $('select[name=spath]').empty();
		          
		          
		          // Agrega las options con las paths del arquetipo seleccionado
		          $('select[name=spath]').append('<option value="">Seleccione una path</option>');
		          
		          $(data).each(function(i, didx) {
		          
		            op = '<option value="'+didx.path+'">';
		            op += didx.path +' {'+ ((didx.name != null) ? didx.name +': ' : '') + didx.rmTypeName + '}';
		            op += '</option>';
		            
		            $('select[name=spath]').append(op);
		          });

				  },
				  error: function(XMLHttpRequest, textStatus, errorThrown) {
                
                console.log(textStatus, errorThrown);
              }
          });
        }); // selecciona archetypeId
        
        

        /**
         * Clic en [+]
         * Agregar una condicion al criterio de busqueda.
         */
        $('#addCriteria').click( function(e) {
        
          e.preventDefault();
          
          // TODO: verificar que todo tiene valor seleccionado
          
          if ( $('select[name=sarchetypeId]').val() == null )
          {
            alert('seleccione un arquetipo');
            return;
          }
          if ( $('select[name=spath]').val() == null )
          {
            alert('seleccione una path');
            return;
          }
          
          $('#criteria').append(
            '<tr>'+
            '<td>'+ $('select[name=sarchetypeId]').val() +'</td>'+
            '<td>'+ $('select[name=spath]').val() +'</td>'+
            '<td>'+
              '<a href="#" id="removeCriteria">[-]</a>'+
              '<input type="hidden" name="archetypeId" value="'+$('select[name=sarchetypeId]').val()+'" />'+
              '<input type="hidden" name="path" value="'+$('select[name=spath]').val()+'" />'+
            '</td>'+
            '</tr>'
          );
        });
        
        
        /**
         * Clic en [-]
         * Elimina un criterio de la lista de criterios de busqueda.
         */
        $(document).on("click", "#removeCriteria", function(e) {
        
          e.preventDefault();
          
          // parent es la td y parent.parent es la TR a eliminar
          //console.log($(e.target).parent().parent());
          //
          $(e.target).parent().parent().remove();
        });
        
      });
    </g:javascript>
  </head>
  <body>
    <script src="${resource(dir:'js', file:'highcharts/highcharts.js')}" type="text/javascript"></script>
  
    <div class="content scaffold-list" role="main">
    
      <h1>Search Data</h1>
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <table>
        <tr>
          <th>attribute</th>
          <th>value</th>
        </tr>
        <tr>
          <td><label>archetypeId</label></td>
          <td>
            <%-- Necesito este model para listar los arquetipos para los que hay indices --%>
            <g:set var="dataIndexes" value="${ehr.clinical_documents.IndexDefinition.list()}" />
      
	         <g:select name="sarchetypeId" size="5"
	                   from="${dataIndexes.archetypeId.unique()}"
	                   noSelection="['':'Elija arquetipo']" />
          </td>
        </tr>
        <tr>
          <td><label>path</label></td>
          <td>
            <%-- Se setean las options al elegir un arquetipo --%>
		      <select name="spath" size="5">
		      </select>
          </td>
        </tr>
        <tr>
          <td><label>add select data</label></td>
          <td><a href="#" id="addCriteria">[+]</a></td>
        </tr>
      </table>

      <%-- JS Generado:
      queryBefore();
		jQuery.ajax({
		    type: 'POST',
		    data: jQuery(this).serialize(),
		    url: '/ehr/rest/queryData?doit=true',
		    success: function (data, textStatus) {
		        querySuccess(data, textStatus);
		    },
		    error: function (XMLHttpRequest, textStatus, errorThrown) {
		        queryFailure(XMLHttpRequest, textStatus, errorThrown);
		    }
		});
		return false
      --%>
      
      <div class="content_padding">
      
	    <h2>Criteria</h2>
	    <g:formRemote name="myForm"
	                  on404="alert('not found!')"
	                  url="[controller:'rest', action:'queryData', params:[doit:true]]"
	                  before="queryBefore()"
	                  onSuccess="querySuccess(data, textStatus)"
	                  onFailure="queryFailure(XMLHttpRequest, textStatus, errorThrown)">
	        
	        Indices de nivel 1:<br/><br/>
	        
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
               <td>format</td>
               <td>
                 <select name="format" size="2">
	                <option value="xml" selected="selected">XML</option>
	                <option value="json">JSON</option>
	              </select>
               </td>
             </tr>
             <tr>
               <td>group</td>
               <td>
                 <select name="group" size="3">
                   <option value="" selected="selected">none</option>
                   <option value="composition">composition</option>
                   <option value="path">path</option>
                 </select>
               </td>
             </tr>
	        </table>
	        
	        <table id="criteria">
	          <tr>
	            <th>archetypeId</th>
	            <th>path</th>
	            <th></th>
	          </tr>
	        </table>
	        
	        <div class="actions">
	          <input type="submit" value="Find" />
	        </div>
	     </g:formRemote>
	      
	     <h2>Result</h2>
	     <!-- <textarea id="querySuccess" class="out"></textarea> -->
	     <div id="querySuccess" class="out"></div>
        <div id="chartContainer"></div>
      </div>
    </div>
  </body>
</html>