<%@ page import="query.Query" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
    <title><g:message code="query.create.title" /></title>
    <style>
      #query_test, #query_composition, #query_datavalue, #query_common {
        display: none;
      }
      .buttons {
        margin: 20px 0 0 0;
      }
      .buttons.test, .buttons.create {
        display: none;
      }
      tr td:last-child select, tr td:last-child input[type=text] {
        width: 100%;
      }
      tr td:first-child {
        width: 140px;
      }
      tr td:first-child {
        text-align: right;
      }
      tr td:first-child label {
        float: right;
      }
      td {
        font-size: 0.9em;
      }
      /* Notificaciones: http://www.malsup.com/jquery/block/#demos */
      div.growlUI {
        /*background: url(check48.png) no-repeat 10px 10px; */
      }
      div.growlUI h1, div.growlUI h2 {
        color: white;
        padding: 5px 10px;
        text-align: left;
        border: 0px;
      }
      .info .content {
        display: none;
        text-align: left;
      }
      .info img {
        cursor: pointer;
      }
    </style>
    <asset:javascript src="jquery.blockUI.js" />
    
    <!-- query test -->
    <asset:stylesheet src="query_execution.css" />
    <asset:stylesheet src="jquery-ui-1.9.2.datepicker.min.css" />
    <asset:stylesheet src="highlightjs/xcode.css" />
    
    <asset:javascript src="jquery-ui-1.9.2.datepicker.min.js" />
    <asset:javascript src="jquery.form.js" /><!-- ajax form -->
    <asset:javascript src="xml_utils.js" /><!-- xmlToString -->
    <asset:javascript src="highcharts/highcharts.js" />
    <asset:javascript src="highlight.pack.js" /><!-- highlight xml and json -->
    <!-- /query test -->
    
    <script type="text/javascript">

      var ajax_submit = function (action) {

         console.log('ajax_submit', action);

         if (action == 'save') {

            if ($('select[name=type]').val()=='composition')
            {
            }
            else if ($('select[name=type]').val()=='datavalue')
            {
            }
            
            $('#query_form').ajaxSubmit({
               url: '${createLink(controller:'query', action:'save')}',
               type: 'post',
               success: function(responseText, statusText, req, form) {
                  console.log(responseText);
                  // redirect to show!
                  location.href = '${createLink('action': 'show')}?id='+ responseText.id;
               },
               error: function(response, textStatus, errorThrown)
               {
                  console.log('error form_datavalue');
                  console.log(response);
                  alert(response.responseText); // lo devuelto por el servidor
               }
            });
         }
         else if (action == 'test') {

console.log('ehrid', $('select[name=qehrId]').val());
            
            // Validacion
            if ($('select[name=qehrId]').val()==null)
            {
              alert('Seleccione un EHR');
              return false;
            }

            if ($('select[name=type]').val()=='composition')
            {
               console.log('test composition query');
               console.log('query_form', $('#query_form'));
               
	            $('#query_form').ajaxSubmit({

		            // datatype = xml for composition
	               
	               url: '${createLink(controller:'rest', action:'queryCompositions')}',

	               type: 'post',
	               
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
	                
	                success: function(responseText, statusText, req, form) {  // >>> SUCCESS
	                  
	                  console.log('form_composition success');
	                  //console.log(responseText);
	                  //console.log(statusText);
	                  //console.log(req);
	                  //console.log(form);
	                  
	                  // reset code class or highlight
                     $('code').removeClass('xml json');
	
	                  // Si devuelve HTML
	                  if ($('select[name=showUI]').val()=='true')
	                  {
	                     $('#results').html( responseText );
	                     $('#results').show('slow');
	                  }
	                  else // Si devuelve el XML
	                  {
	                     // highlight
	                     $('code').addClass('xml');
	                     $('code').text(formatXml( xmlToString(responseText) ));
	                     $('code').each(function(i, e) { hljs.highlightBlock(e); });
	                     $('code').show('slow');
	                  }
	                  
	                  // Muestra el boton que permite ver los datos crudos
	                  // devueltos por el servidor
	                  $('#show_data').show();
	                },
	                
	                error: function(response, textStatus, errorThrown) {  // >>> ERROR
	                
	                  console.log(response, textStatus, errorThrown);
	                  
	                  alert(errorThrown); // lo devuelto por el servidor
	                }
	            }); // ajax_submit

	            console.log('after ajax submit');
	            
            } // query type = composition
            else // query type = datavalue
            {
               console.log('test datavalue query');
               
               $('#query_form').ajaxSubmit({
                  
                  dataType: $('select[name=format]').val(), // xml o json
                  url: '${createLink(controller:'rest', action:'queryData')}',
                  type: 'post',
                  data: {doit:true},
                
                  beforeSubmit: function(data, form, options) {
                  
                    console.log('form_datavalue beforeSubmit');
                  },
                  
                  success: function(responseText, statusText, req, form) {
                    
                    console.log('form_datavalue success');
                    
                    // Vacia donde se va a mostrar la tabla o el chart
                    $('#chartContainer').empty();
                    
                    console.log('form_datavalue success 2');

                    // reset code class or highlight
                    $('code').removeClass('xml json');

                    // Si devuelve JSON (verifica si pedi json)
                    if ($('select[name=format]').val()=='json')
                    {
                      console.log('form_datavalue success json');
                    
                      // highlight
                      $('code').addClass('json');
                      $('code').text(JSON.stringify(responseText, undefined, 2));
                      $('code').each(function(i, e) { hljs.highlightBlock(e); });
                      
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
                    
                      // highlight
                      $('code').addClass('xml');
                      $('code').text(formatXml( xmlToString(responseText) ));
                      $('code').each(function(i, e) { hljs.highlightBlock(e); });
                    }

                    $('code').show('slow');
                    
                    // Muestra el boton que permite ver los datos crudos
                    // devueltos por el servidor
                    $('#show_data').show();
                    
                    
                    // Hace scroll animado para mostrar el resultado
                    $('html,body').animate({scrollTop:$('#code').offset().top+400}, 500);
                  },
                  
                  error: function(response, textStatus, errorThrown)
                  {
                    console.log('error form_datavalue');
                    console.log(response);
                    
                    alert(response.responseText); // lo devuelto por el servidor
                  }
                });
               
            } // query type = datavalue
         }
      };
    
      $(document).ready(function() {
      
        $('.info img').click(function(e) {
          console.log($('.content', $(this).parent()));
          $('.content', $(this).parent()).toggle('slow');
        });

        
      
        /*
         * Change del tipo de consulta. Muestra campos dependiendo del tipo de consulta a crear.
         */
        $('select[name=type]').change( function() {

          // Limpia las tablas de criterios y seleccion cuando
          // se cambia el tipo de la query para evitar errores.
          clearCriteriaAndSelection();
          clearTest();
          
          // La class query_build marca las divs con campos para crear consultas,
          // tanto los comunes como los particulares para busqueda de compositions
          // o de data_values
          $('.query_build').hide();
          
          if (this.value != '')
          {
            $('#query_common').show();
            $('#query_'+ this.value).show();
            $('.buttons.create').show();
          }
        });
        
        
        /**
         * Clic en un arquetipo de la lista de arquetipos (select[view_archetype_id])
         * Lista las paths del arquetipo en select[view_archetype_path]
         */
        $('select[name=view_archetype_id]').change(function() {
        
          var archetypeId = $(this).val(); // arquetipo seleccionado
          
          // http://api.jquery.com/jQuery.ajax/
          //
          $.ajax({
              // FIXME: this action should be in QueryController
              url: '${createLink(controller:"test", action:"getIndexDefinitions")}',
              data: {archetypeId: archetypeId},
              dataType: 'json',
              success: function(data, textStatus) {
              
                /**
                 didx
                   archetypeId: "openEHR-EHR-COMPOSITION.encounter.v1"
                   name: "value"
                   path: "/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value"
                   rmTypeName: "DV_QUANTITY"
                */
                
                // Saca las options que haya
                $('select[name=view_archetype_path]').empty();
                
                // Agrega las options con las paths del arquetipo seleccionado
                $('select[name=view_archetype_path]').append('<option value="">Select a path</option>');
                
                $(data).each(function(i, didx) {
                
                  op = '<option value="'+ didx.archetypePath +'">';
                  op += didx.name +' {'+ didx.rmTypeName + '}' //+' {'+ ((didx.name != null) ? didx.name +': ' : '') + didx.rmTypeName + '}';
                  op += '</option>';
                  
                  $('select[name=view_archetype_path]').append(op);
                });
              },
              error: function(XMLHttpRequest, textStatus, errorThrown) {
                
                console.log(textStatus, errorThrown);
              }
          });
        }); // click en select view_archetype_id
        
        
        /*
         * ======================================================
         * queryByData add and remove criteria
         * ======================================================
         */
        
        /**
         * Clic en [+]
         * Agregar una condicion al criterio de busqueda.
         */
        $('#addCriteria').click( function(e) {
        
          e.preventDefault();
          
          // TODO: verificar que todo tiene valor seleccionado
          
          if ( $('select[name=view_archetype_id]').val() == null )
          {
            alert('seleccione un concepto'); // TODO: I18N
            return;
          }
          if ( $('select[name=view_archetype_path]').val() == null )
          {
            alert('seleccione una parte'); // TODO: I18N
            return;
          }
          /*
          if ( $('select[name=soperand]').val() == null )
          {
            alert('seleccione un operador');
            return;
          }
          */
          if ( $('input[name=soperand]:checked').val() == null )
          {
            alert('seleccione un operador');
            return;
          }
          if ( $('input[name=svalue]').val() == null )
          {
            alert('ingrese un valor');
            return;
          }
          
          $('#criteria').append(
            '<tr>'+
            '<td>'+ $('select[name=view_archetype_id]').val() +'</td>'+
            '<td>'+ $('select[name=view_archetype_path]').val() +'</td>'+
            //'<td>'+ $('select[name=soperand]').val() +'</td>'+
            '<td>'+ $('input[name=soperand]:checked').val() +'</td>'+
            '<td>'+ $('input[name=svalue]').val() +'</td>'+
            '<td>'+
              '<a href="#" id="removeCriteria">[-]</a>'+
              '<input type="hidden" name="archetypeId" value="'+$('select[name=view_archetype_id]').val()+'" />'+
              '<input type="hidden" name="archetypePath" value="'+$('select[name=view_archetype_path]').val()+'" />'+
              //'<input type="hidden" name="operand" value="'+$('select[name=soperand]').val()+'" />'+
              '<input type="hidden" name="operand" value="'+$('input[name=soperand]:checked').val()+'" />'+
              '<input type="hidden" name="value" value="'+$('input[name=svalue]').val()+'" />'+
            '</td></tr>'
          );
          
          
          // Notifica que la condicion fue agregada
          $.growlUI('Condici&oacute;n agregada', '<a href="#criteria">Verifique la condicion agregada</a>'); 
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
        
        /* ======================================================
         * /queryByData (query composition)
         * ======================================================
         */
        
        
        /*
         * ======================================================
         * queryData add and remove selection
         * ======================================================
         */
        /**
         * Clic en [+]
         * Agregar una condicion al criterio de busqueda.
         */
        $('#addSelection').click( function(e) {
        
          e.preventDefault();
          
          // TODO: verificar que todo tiene valor seleccionado
          
          if ( $('select[name=view_archetype_id]').val() == null )
          {
            alert('seleccione un concepto'); // TODO: I18N
            return;
          }
          if ( $('select[name=view_archetype_path]').val() == null )
          {
            alert('seleccione una parte'); // TODO: I18N
            return;
          }
          
          $('#selection').append(
            '<tr><td>'+ $('select[name=view_archetype_id]').val() +'</td>'+
            '<td>'+ $('select[name=view_archetype_path]').val() +'</td>'+
            '<td>'+
              '<a href="#" id="removeSelection">[-]</a>'+
              '<input type="hidden" name="archetypeId" value="'+$('select[name=view_archetype_id]').val()+'" />'+
              '<input type="hidden" name="archetypePath" value="'+$('select[name=view_archetype_path]').val()+'" />'+
            '</td></tr>'
          );
          
           // Notifica que la condicion fue agregada
          $.growlUI('Selecci&oacute;n agregada', '<a href="#selection">Verifique la selecci&oacute;n agregada</a>'); 
        });
        
        
        /**
         * Clic en [-]
         * Elimina un criterio de la lista de criterios de busqueda.
         */
        $(document).on("click", "#removeSelection", function(e) {
        
          e.preventDefault();
          
          // parent es la td y parent.parent es la TR a eliminar
          //console.log($(e.target).parent().parent());
          //
          $(e.target).parent().parent().remove();
        });
        
        /* ======================================================
         * /queryData (query datavalue)
         * ======================================================
         */
        
        
        /*
         * Valida antes de hacer test o guardar.
         */
        $('form[name=query_form]').submit( function(e) {
        
          // Valida que haya algun criterio o alguna seleccion,
          // sino hay, retorna false y el submit no se hace.
          // Ademas muestra un alert con el error. 
          return validate();
        });
        
      }); // ready
      
      
      /**
       * Limpia la tabla de archetypeIds y paths seleccionadas
       * cuando se cambia el tipo de la query a crear, asi se
       * evitan errores de no mezclar datos que son para criteria
       * en queryByData o para seleccion en queryData.
       */
      var clearCriteriaAndSelection = function()
      {
        //console.log( 'clearCriteriaAndSelection' );
      
        selectionTable = $('#selection');
        criteriaTable = $('#criteria');
        
        // remueve todos menos el primer TR
        removeTRsFromTable(selectionTable, 1);
        removeTRsFromTable(criteriaTable, 1);
        
        $('.buttons.create').hide();
      };
      
      /**
       * Clears the current test panel when the query type is changed.
       */
      var clearTest = function()
      {
         $('.buttons.test').hide();
         $('#query_test').hide();
      };
      
      
      /**
       * Auxiliar usada por cleanCriteriaOrSelection
       * Elimina los TRs desde from:int, si from es undefined o 0, elimina todos los TRs.
       */
      var removeTRsFromTable = function (table, from)
      {
        if (from == undefined) from = 0;
        $('tr', table).each( function(i, tr) {
          
          if (i >= from) $(tr).remove();
        });
      };
      
      
      // Validacion antes de submit a test:
      //   1. type = composition debe tener algun criterio de datos
      //   2. type = path debe tener alguna path en su seleccion
      //
      var validate = function()
      {
        type = $('select[name=type]').val();
        
        if (type == 'composition')
        {
          // Si la tabla de criterio solo tiene el tr del cabezal, no tiene criterio seleccionado
          if ($('tr', '#criteria').length == 1)
          {
            alert('Debe especificar algun criterio de busqueda');
            return false;
          }
          
          return true;
        }
        
        if (type == 'datavalue')
        {
          if ($('tr', '#selection').length == 1)
          {
            alert('Debe especificar la seleccion de valores de la busqueda');
            return false;
          }
          
          return true;
        }
      }; // validate
    </script>
  </head>
  <body>
    <a href="#create-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="query.list.title" /></g:link></li>
      </ul>
    </div>
    
    <h1><g:message code="query.create.title" /></h1>
      
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>
      
    <g:hasErrors bean="${queryInstance}">
      <ul class="errors" role="alert">
        <g:eachError bean="${queryInstance}" var="error">
          <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
        </g:eachError>
      </ul>
    </g:hasErrors>
    
    
    <g:form name="query_form" controller="query">

      <%-- campos comunes a ambos tipos de query --%>
      <table>
        <%-- nombre de la query --%>
        <tr>
          <td class="fieldcontain ${hasErrors(bean: queryInstance, field: 'name', 'error')} required">
            <label for="name">
              <g:message code="query.name.label" default="Name" /> *
            </label>
          </td>
          <td>
            <g:textField name="name" required="" value="${queryInstance?.name}"/>
          </td>
        </tr>
          
        <%-- se hace como wizard, primero poner el tipo luego va el contenido --%>
        <%-- type de la query, el contenid va a depender del tipo --%>
        <tr>
          <td class="fieldcontain ${hasErrors(bean: queryInstance, field: 'type', 'error')}">
            <label for="type">
              <g:message code="query.type.label" default="Type" />
            </label>
            <span class="info">
              <asset:image src="skin/information.png" />
              <span class="content">
                <ul>
                  <li>composition: find clinical documents by criteria over data points</li>
                  <li>datavalue: find data points by context based criteria</li>
                </ul>
              </span>
            </span>
          </td>
          <td>
            <g:select name="type" from="${queryInstance.constraints.type.inList}" value="${queryInstance?.type}" valueMessagePrefix="query.type" noSelection="['': '']"/>
          </td>
        </tr>
      </table>

      
      <!-- Aqui van los campos comunes a ambos tipos de query -->
      <div id="query_common" class="query_build">
        <table>
          <tr>
            <th>attribute</th>
            <th>value</th>
          </tr>
          <tr>
            <td>concept</td>
            <td>
              <g:set var="concepts" value="${dataIndexes.archetypeId.unique()}" />
              
              <%-- optionKey="archetypeId" optionValue="name" --%>
              <%-- This select is used just to create the condition or projection,
                   is not saved in the query directly --%>
              <g:select name="view_archetype_id" size="3" from="${concepts}"
                        noSelection="['':'Choose a concept']" />
            </td>
          </tr>
          <tr>
            <td>part</td>
            <td>
              <%-- Se setean las options al elegir un arquetipo --%>
              <select name="view_archetype_path" size="5"></select>
            </td>
          </tr>
        </table>
      </div>
        
      <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++ -->
      <!-- Campos de queryByData -->

      <div id="query_composition" class="query_build">
        <table>
          <tr>
            <td>operand</td>
            <td>
              <%-- TODO: sacar de restriccion inList de DataCriteria.operand 
              Elija operador (TODO: hacerlo con grupo de radio buttons en lugar de selects, hay que corregir el JS)
              --%>
              <label><input type="radio" name="soperand" value="eq" />=</label>
              <label><input type="radio" name="soperand" value="neq" />!=</label>
              <label><input type="radio" name="soperand" value="gt" />&gt;</label>
              <label><input type="radio" name="soperand" value="lt" />&lt;</label>
            </td>
          </tr>
          <tr>
            <td>value</td>
            <td><input type="text" name="svalue" /></td>
          </tr>
          <tr>
            <td>add criteria</td>
            <td><a href="#" id="addCriteria">[+]</a></td>
          </tr>
        </table>
        
        <!--
        value puede especificarse aqui como filtro o puede ser un
        parametro de la query sino se especifica aqui.
        
        ehrId y rangos de fechas son parametros de la query
        
        archetypeId se puede especificar como filtro (tipo de documento), 
        sino se especifica aqui puede pasarse como parametro de la query
        -->
        
        <h2><g:message code="query.create.criteria" /></h2>
         
        <!-- Indices de nivel 1 -->
        <table>
          <%-- Removed for now...
          <tr>
            <td>
              composition archetypeId
              <span class="info">
                <asset:image src="skin/information.png" />
                 <span class="content">
                   <ul>
                     <li>
                       Selecting an archetype here will narrow the query to get only data for this archetype id.
                       This makes sense if criteria is defined over archetypes that are not the root composition archetype.
                       Right now, criteria is defined over root compositions archetypes, so this archetype should not be selected.
                       This is here only for demo/test purposes.
                     </li>
                   </ul>
                 </span>
              </span>
            </td>
            <td>
              <!--
               FIXME:
               busco los arquetipos de composition en los indices porque
               el EHRServer aun no tiene repositorio de arquetipos. Cuando
               lo tenga, esta operacion deberia usar el ArchetypeManager.
              -->
                      
              <!-- solo arquetipos de composition -->
              <g:select name="qtemplateId" size="5"
                        from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "templateId"}} }" />
            </td>
          </tr>
          --%>
          <tr>
            <td>
              show UI?
              <span class="info">
                <asset:image src="skin/information.png" />
                <span class="content">
                  <ul>
                    <li>
                      Select between showing the clinical document as a web view or retrieve it as XML.
                    </li>
                  </ul>
                </span>
              </span>
            </td>
            <td>
              <select name="showUI">
                <option value="false" selected="selected">no</option>
                <option value="true">yes</option>
              </select>
            </td>
          </tr>
        </table>
        
        <a name="criteria"></a>
        <h3><g:message code="query.create.conditions" /></h3>
        <!-- Esta tabla almacena el criterio de busqueda que se va poniendo por JS -->
        <table id="criteria">
          <tr>
            <th>archetypeId</th>
            <th>path</th>
            <th>operand</th>
            <th>value</th>
            <th></th>
          </tr>
        </table>
      </div><!-- query_composition -->
        
      <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++ -->
        
      <div id="query_datavalue" class="query_build">

        <!--
        Aqui van los campos de queryData
        -->
        
        <table>
          <tr>
            <td><label>add projection</label></td>
            <td><a href="#" id="addSelection">[+]</a></td>
          </tr>
        </table>

        <h2><g:message code="query.create.filters" /></h2>

        <g:message code="query.create.level1indexes" /><br/><br/>

        <!--
        ehrId, archetypeId (tipo de doc), rango de fechas, formato
        y agrupacion son todos parametros de la query.
        
        Aqui se pueden fijar SOLO algunos de esos parametros
        a modo de filtro.

        TODO: para los que no se pueden fijar aqui, inluir en la
        definicion de la query si son obligatorios o no.
        -->
        
        <table>
          <%-- Removed for now...
          <tr>
            <td>
              composition templateId
              <span class="info">
                <asset:image src="skin/information.png" />
                <span class="content">
                  <ul>
                    <li>
                      Selecting an archetype here will narrow the query to get only data for this archetype id.
                      This makes sense if criteria is defined over archetypes that are not the root composition archetype.
                      Right now, criteria is defined over root compositions archetypes, so this archetype should not be selected.
                      This is here only for demo/test purposes.
                    </li>
                  </ul>
                </span>
              </span>
            </td>
            
            <td>
              <!--
              FIXME:
              busco los arquetipos de composition en los indices porque
              el EHRServer aun no tiene repositorio de arquetipos. Cuando
              lo tenga, esta operacion deberia usar el ArchetypeManager.
              -->
              <!-- solo arquetipos de composition -->
              <g:select name="qtemplateId" size="5"
                        from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections{distinct "templateId"}} }" />
            </td>
          </tr>
          --%>
          
          <tr>
            <td>default format</td>
            <td>
              <select name="format">
                <option value="xml" selected="selected">XML</option>
                <option value="json">JSON</option>
              </select>
            </td>
          </tr>
          <tr>
            <td>default group</td>
            <td>
              <select name="group" size="3">
                <option value="" selected="selected">none</option>
                <option value="composition">composition</option>
                <option value="path">path</option>
              </select>
            </td>
          </tr>
        </table>
        
        <h3><g:message code="query.create.projections" /></h3>
        <!-- Esta tabla guarda la seleccion de paths de los datavalues a obtener -->
        <a name="selection"></a>
        <table id="selection">
          <tr>
            <th>archetypeId</th>
            <th>path</th>
            <th></th>
          </tr>
        </table>
        
      </div><!-- query_datavalue -->
      
      <fieldset class="buttons create">
        <script>
          // Toggles the query test on and off.
          var toggle_test = function() { 
            
            // Test options for each type of query
            if ( $('select[name=type]').val() == 'composition' )
            {
               $('div#query_test_composition').show();
               $('div#query_test_datavalue').hide();
            }
            else
            {
               $('div#query_test_composition').hide();
               $('div#query_test_datavalue').show();
            }
            
            $('#query_test').toggle('slow');
            $('.buttons.test').toggle('slow');
          };
        </script>
        <a href="javascript:void(0);" onclick="javascript:toggle_test();" id="test_query">${message(code:'default.button.test.label', default: 'Test')}</a>
        
        <a href="javascript:void(0);" onclick="javascript:ajax_submit('save');" >${message(code:'default.button.create.label', default: 'Save')}</a>
      </fieldset>
      
      <!-- test panel -->
      <div id="query_test">
	      <g:include action="test" />
	   </div>
	   
	   <fieldset class="buttons test">
	     <a href="javascript:void(0);" onclick="javascript:ajax_submit('test');" >${message(code:'default.button.execute.label', default: 'Execute')}</a>
      </fieldset>
    </g:form>
  </body>
</html>