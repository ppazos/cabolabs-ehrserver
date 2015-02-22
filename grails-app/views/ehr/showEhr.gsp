<%@ page import="ehr.Ehr" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'ehr.label', default: 'Ehr')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <style>
    .icon {
      width: 48px;
      border: none;
    }
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
    </style>
    
    <asset:stylesheet src="jquery-ui-1.9.2.datepicker.min.css "/>
    <asset:javascript src="jquery.blockUI.js" />
    <asset:javascript src="jquery-ui-1.9.2.datepicker.min.js" />
    
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
        
          //console.log('showCompo');
        
          e.preventDefault();
          
          modal = $('#composition_modal');
          //console.log( modal.children()[0] );
          //console.log( this.href );
          
          modal.children()[0].src = this.href;
          
          $.blockUI({
            message: modal,
            css: {
              width: '960px',
              height: '600px',
              top: '10px',
              left:'auto',
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
    		console.log('archId ' + archId);
    		
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
    <a href="#show-ehr" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <%--<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>--%>
      </ul>
    </div>
    
    <g:render template="/person/patientData" model="[person: ehr.subject.person]" />
    
    <div id="show-ehr" class="content scaffold-show" role="main">
    
      <h1 class="hidden_uid">EHR <span class="uid">uid: ${ehr.ehrId}</span></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <ol class="property-list ehr">
        
        <g:if test="${ehr?.dateCreated}">
        <li class="fieldcontain">
          <span id="dateCreated-label" class="property-label"><g:message code="ehr.dateCreated.label" default="Date Created" /></span>
          <span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate date="${ehr?.dateCreated}" /></span>
        </li>
        </g:if>
      
        <g:if test="${ehr?.systemId}">
        <li class="fieldcontain">
          <span id="systemId-label" class="property-label"><g:message code="ehr.systemId.label" default="System Id" /></span>
          <span class="property-value" aria-labelledby="systemId-label"><g:fieldValue bean="${ehr}" field="systemId"/></span>
        </li>
        </g:if>
        
        <li class="fieldcontain">
        
          <h2><g:message code="ehr.show.clinicalRecords" /></h2>
          <!-- T0002.1 -->
          <div class="composition_filters">
            <g:form id="${ehr.id}">
	            <input type="text" name="fromDate" placeholder="${message(code:'filter.fromDate')}" readonly="readonly" />
	            <input type="text" name="toDate" placeholder="${message(code:'filter.toDate')}" readonly="readonly" />
	            
	            <g:message code="filter.rootArchetypeId" />
               <g:select name="qarchetypeId"
	                      from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "archetypeId"}} }"
	                      noSelection="['':'']" />
	                           
	            <g:submitToRemote
	               url="[action:'ehrContributions', id:ehr.id]"
	               update="ehrContributions"
	               value="${message(code:'filer.action.apply')}"
	               onSuccess="highlight_filtered_data()" />
               
               <input type="reset" value="${message(code:'form.action.reset')}" />
            </g:form>
          </div>
          
          <div id="ehrContributions">
            <g:include action="ehrContributions" id="${ehr.id}" />
          </div>
          
        </li>
      </ol>
      
      <!--
      <g:form>
        <fieldset class="buttons">
          <g:hiddenField name="id" value="${ehr?.id}" />
          <g:link class="edit" action="edit" id="${ehr?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
        </fieldset>
      </g:form>
      -->
    </div>
  </body>
</html>