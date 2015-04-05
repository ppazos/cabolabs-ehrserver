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
     
     //$('#results').toggle('slow');
     $('code').toggle('slow');
   });
});