<%@ page import="com.cabolabs.ehrserver.openehr.ehr.Ehr" %><%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'ehr.label', default: 'Ehr')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <style>
    tr:hover {
     background: none;
    }
    td {
      vertical-align: middle;
    }
    .filter_highlight {
      background-color: #ffff80;
    }
    input[type=text] {
      width: 100px;
    }
    ol.property-list, li {
      margin:0px;
    }
    .composition_filters {
      padding: 5px 0px 10px 0px;
      border-bottom: 1px solid #ddd;
    }
    img.ui-datepicker-trigger { /* <<<< datepicker icon adjustments */
      vertical-align: middle;
      height: 1.9em;
      padding-bottom: 6px; /* alinea con el input */
    }
    .folder {
      padding-left: 1em;
    }
    .icon {
      width: 48px;
      border: none;
    }
    </style>
    
    <asset:stylesheet src="jquery-ui-1.9.2.datepicker.min.css "/>
    <asset:javascript src="jquery.blockUI.js" />
    <asset:javascript src="jquery-ui-1.9.2.datepicker.min.js" />
    <asset:javascript src="highcharts/highcharts.js" />
    
    <script type="text/javascript">
    $(document).ready(function() {

      /* ===================================================================================== 
       * Calendars para filtros de compositions.
       */
      $("input[name=fromDate]").datepicker({
        // Icono para mostrar el calendar 
         showOn: "button",
         buttonImage: "${assetPath(src:'calendar.gif')}",
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
         buttonImage: "${assetPath(src:'calendar.gif')}",
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
      /* ===================================================================================== */
      
      // Muestra modal con el contenido de la composition tal cual fue devuelto por el servidor EHR
      // Esta es la forma de on() para que funcione como live() si nuevos links .showCompo se
      // agregan de forma dinamica al DOM, sino no funciona el bind del click.
      // la DIV #ehrContributiosn es fija y los A .showCompo varian usando AJAX.
      $('#ehrContributions').on('click', '.showCompo', function(e) {
        
          e.preventDefault();
          
          modal = $('#composition_modal');
          
          modal.children()[0].src = this.href;
          
          $.blockUI({
            message: modal,
            css: {
               width: '94%',
               height: '94%',
               top : '3%',
               left: '3%',
               padding: '10px'
            },
            onOverlayClick: $.unblockUI
          });
      });
    });
    
    var highlight_filtered_data = function()
    {
      console.log('highlight_filtered_data');
      archId = $('select[name=qarchetypeId]').val();
      if (archId != '')
      {
        $('td.contribution_data_archid').each( function(i,td){
          
          td = $(td);
          console.log('td ' + td);
          console.log('td ' + td.text());
          if (td.text() == archId)
          {
            td.addClass('filter_highlight');
          }
        });
      }
    };
    </script>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1 class="hidden_uid">EHR</h1>
      </div>
    </div>
    
    <div class="row">
      <div class="col-lg-12">
        <g:render template="/person/patientData" model="[person: ehr.subject.person]" />
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div>
        </g:if>
        
        <div class="control-group">
          <label class="control-label"><g:message code="ehr.uid.label" default="UID" /></label>
          <div class="controls">
            <p class="form-control-static">${ehr.uid}</p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="ehr.dateCreated.label" default="Date Created" /></label>
          <div class="controls">
            <p class="form-control-static"><g:formatDate date="${ehr?.dateCreated}" /></p>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label"><g:message code="ehr.systemId.label" default="System Id" /></label>
          <div class="controls">
            <p class="form-control-static"><g:fieldValue bean="${ehr}" field="systemId"/></p>
          </div>
        </div>
 

        <h2><g:message code="ehr.show.contributions" /></h2>

        <div class="composition_filters">
          <g:form id="${ehr.id}">
            <input type="text" name="fromDate" placeholder="${message(code:'filter.fromDate')}" readonly="readonly" />
            <input type="text" name="toDate" placeholder="${message(code:'filter.toDate')}" readonly="readonly" />
             
            <g:message code="filter.rootArchetypeId" />
            <g:select name="qarchetypeId"
                      from="${CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }"
                      noSelection="['':'']" />
                            
            <g:submitToRemote
               url="[action:'ehrContributions', id:ehr.id]"
               update="ehrContributions"
               value="${message(code:'filer.action.apply')}"
               onSuccess="highlight_filtered_data()"
               class="btn btn-default btn-md" />
             
            <input type="reset" value="${message(code:'form.action.reset')}" onclick="javascript:location.reload()" class="btn btn-default btn-md" />
          </g:form>
        </div>
        
        
        <div id="ehrContributions">
          <g:include action="ehrContributions" id="${ehr.id}" />
        </div>
        


        <h2><g:message code="ehr.show.directory" /></h2>
        <g:ehr_directory directory="${ehr.directory}" />


        <script type="text/javascript">
          $(document).ready(function() {
             
            $('#add_versioned_objects').on('click', function () {

              var versioned_object_uids = [];
              $.each( $('input[name="versioned_object.uid"]:checked'), function () {
                versioned_object_uids.push($(this).val());
              });
              var folder_id = $('input[name="folder.id"]:checked').val();

              if (versioned_object_uids.length == 0)
              {
                alert('Select one or more documents');
                return;
              }
              if (folder_id == undefined)
              {
                alert('Select a folder');
                return;
              }
              
              console.log(versioned_object_uids, folder_id);

              $.ajax({
                url: '${createLink(controller:"folder", action:"addItems")}',
                type: "POST",
                traditional: true, // Avoids adding suffix [] to the versioned_object_uids param.
                data: {id: folder_id, versioned_object_uids: versioned_object_uids},
                success: function(data, textStatus) {
                    
                  console.log(data);

                  if (data == "ok") location.reload();

                  // TODO: update the directory panel to see the added versioned_objects.

                  /*
                    $('#json').addClass('json');
                    $('#json').text(JSON.stringify(data, undefined, 2));
                    $('#json').each(function(i, e) { hljs.highlightBlock(e); });
                  */
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    
                  console.log(textStatus, errorThrown);
                }
              });
            });
          });
        </script>
        
        <div class="btn-toolbar" role="toolbar">
          <a href="javascript:void(0);" id="add_versioned_objects">
            <button type="button" class="btn btn-default btn-md">
              <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> Add documents to folders
            </button>
          </a>
        </div>

      </div>
    </div>
  </body>
</html>
