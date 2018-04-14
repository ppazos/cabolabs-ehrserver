// =======================================================================
// TEST CHART
var chart;
// =======================================================================

/**
 * functions:
 * - queryDataRenderChart
 * - renderchart
 * - queryDataRenderTable
 */

$(document).ready(function() {

   /* =====================================================================================
    * Calendars para filtros de compositions.
    */
   $("input[name=fromDate]").datepicker({
       // Icono para mostrar el calendar
       showOn: "button",
       buttonImage: window.grailsSupport.assetsRoot + "calendar.gif", // http://stackoverflow.com/questions/24048628/how-can-i-access-images-from-javascript-using-grails-asset-pipeline-plugin
       buttonImageOnly: true,
       buttonText: 'pick a date',
       // Formato
       dateFormat: 'yymmdd', // poner yy hace salir yyyy ...
       // Menus para cambiar mes y anio
       changeMonth: true,
       changeYear: true,
       // La fecha maxima es la que esta seleccionada en toDate si la hay
       //onClose: function( selectedDate ) {
       //  $( "input[name=toDate]" ).datepicker( "option", "minDate", selectedDate );
      // }
   });
   $("input[name=toDate]").datepicker({
       // Icono para mostrar el calendar
       showOn: "button",
       buttonImage: window.grailsSupport.assetsRoot + "calendar.gif", // http://stackoverflow.com/questions/24048628/how-can-i-access-images-from-javascript-using-grails-asset-pipeline-plugin
       buttonImageOnly: true,
       buttonText: 'pick a date',
       // Formato
       dateFormat: 'yymmdd', // poner yy hace salir yyyy ...
       // Menus para cambiar mes y anio
       changeMonth: true,
       changeYear: true,
       // La fecha minima es la que esta seleccionada en fromDate si la hay
       //onClose: function( selectedDate ) {
       //  $( "input[name=fromDate]" ).datepicker( "option", "maxDate", selectedDate );
       //}
   });

// ====================================================================
   // Muestra los datos crudos devueltos por el servidor
   // ====================================================================

   $('#show_data').click( function(e) {

     e.preventDefault();

     $('#code').toggle('slow');
   });
});

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
   var point;

   // FIXME: externalize point builders, it doesnt need to be created on each call.
   // El punto a graficar depende del tipo de dato, se usa
   // point_builders para resolver la construccion del punto.
   var point_builders = {
      DV_ORDINAL: function (dvi) {
         return {
            name: dvi.value +' ('+ dvi.symbol_value +')',
               y: dvi.value
         };
      },
      DV_COUNT: function (dvi) {
         return {
            name: dvi.magnitude,
               y: dvi.magnitude
         };
      },
      DV_PROPORTION: function (dvi) {
         // TODO: show proportion kind: percentage, etc.
         return {
            name: dvi.numerator+' '+dvi.denominator,
               y: dvi.numerator / dvi.denominator
         };
      },
      DV_QUANTITY: function (dvi) {
         return {
            name: dvi.magnitude+' '+dvi.units,
               y: dvi.magnitude
         };
      },
      DV_DURATION: function (dvi) {
         return {
            name: dvi.magnitude+' seconds',
               y: dvi.magnitude
         };
      }
   };

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

     //console.log('path y dviseries', path, dviseries);

     // Filter: only chart numeric data
     if ( $.inArray(dviseries.type, ['DV_QUANTITY', 'DV_COUNT', 'DV_PROPORTION', 'DV_ORDINAL', 'DV_DURATION']) == -1)
     {
        //console.log('type filtered '+ dviseries.type);
        return;
     }

     /**
      * Estructura:
      *   { name: 'John', data: [5, 7, 3] }
      *
      *   o si quiero mostrar una etiqueta en el punto:
      *   { name: 'John', data: [{name:'punto', color:'#XXX', y:5},{..},{..}] }
      */
     var name = dviseries.name[session_lang];
     if (!name) name = ""; // case that the archetype item doesnt have a name translation to the current language, this should be avoided by listing only OPTs in the current lang.
     var serie = { name: name, data: [] };
     
     $.each( dviseries.serie, function(ii, dvi) {

       //console.log('ii y dvi', ii, dvi);

       point = point_builders[dviseries.type](dvi);

       serie.data.push(point);
     });

     series.push(serie);
   });

   //console.log( series );

   // ========================================
   // Test chart
   if (series.length > 0)
     renderchart(series);
   // ========================================
}; // queryDataRenderChart

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
}; //renderchart

var queryDataRenderTable = function(data)
{
  console.log('queryDataRenderTable');
  console.log(data);

  var headers = data[0];
  var rows = data[1];
  var table = $('<table width="100%"></table>');

  // ================================================================
  // Muestra headesr y subheaders
  htmlheaders = '<tr>';
  $.each(headers, function(path, header) {

    //console.log('path y subheaders', path, subheaders);
    // TODO: deberia ser archetype+path para que sea absoluta
    // name es el nombre del ArchetypeIndexItem coorespondiente al archId y path del DataValueIndex
    htmlheaders += '<th title="'+ path +'">'+ header.name[session_lang] +' ('+ header.type +')</th>';
  });
  htmlheaders +='<th></th></tr>'; // th extra para las acciones de ver composition de cada fila

  // =================================================================
  // Muestra cada fila
  htmlrows = '';

  linkCompoXML = window.grailsSupport.baseURL + "ehr/showComposition";
  linkCompoUI = window.grailsSupport.baseURL + "ehr/showCompositionUI";

  // itera por filas
  $.each(rows, function(compoUid, data) { // data [date, uid, cols [ {type, path, attrs dep. del type}, {...}] ]

    // itera por columnas (headesrs = paths)
    htmlrows += '<tr>';
    $.each(data.cols, function(column, colvalues) { // evito attr type y path, los demas son los atributos de los subheaders que dependen del type del datavalue

      //console.log('colvalues', colvalues);
      htmlrows += '<td>';

      elem_columns = headers[colvalues.path].attrs; // units, magnitude
      //console.log('elem_columns', elem_columns);

      htmlrows += '<table width="100%"><tr>';
      $.each(elem_columns, function(jjj, colattr) {
         htmlrows += '<th>'+ colattr +'</th>';
      });
      htmlrows += '</tr>';

      // itera por atributos simples de datavalues de cada columna (subheaders)
      $.each(colvalues.values, function(iii, elem) {

        //console.log('elem', elem);
        htmlrows += '<tr>';
        $.each(elem, function(attr, value) {
          htmlrows += '<td>'+ value +'</td>';
        });
        htmlrows += '</tr>';
      });
      htmlrows += '</table>';

      htmlrows += '</td>';
    });

    // links a composition
    htmlrows += '<td>';
    htmlrows += '<a href="'+ linkCompoXML +'?uid='+ compoUid +'" target="_blank"><img src="'+ window.grailsSupport.assetsRoot +'xml.png" class="icon" /></a>';
    htmlrows += '<a href="'+ linkCompoUI  +'?uid='+ compoUid +'" target="_blank"><img src="'+ window.grailsSupport.assetsRoot +'doc.png" class="icon" /></a>';
    htmlrows += '</td></tr>';
  });

  // Uso el chartContainer para mostrar la tabla
  table.html( htmlheaders + htmlrows );
  $('#chartContainer').append(table);
}; // queryDataRenderTable
