<%@ page import="query.Query" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
    <title><g:message code="query.create.title" /></title>
    <style>
      #query_composition, #query_datavalue, #query_common {
        display: none;
      }
      .buttons {
        margin: 20px 0 0 0;
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
    <%--<r:require module="jquery" />--%>
    <g:javascript src="jquery-1.8.2.min.js" />
    <g:javascript src="jquery.blockUI.js" />
    <g:javascript>
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
          
          // La class query_build marca las divs con campos para crear consultas,
          // tanto los comunes como los particulares para busqueda de compositions
          // o de data_values
          $('.query_build').hide();
          
          if (this.value != '')
          {
            $('#query_common').show();
            $('#query_'+ this.value).show();
          }
        });
        
        
        /**
         * Clic en un arquetipo de la lista de arquetipos (select[stemplateId])
         * Lista las paths del arquetipo en select[spath]
         */
        $('select[name=stemplateId]').change(function() {
        
          var templateId = $(this).val(); // arquetipo seleccionado
          
          // http://api.jquery.com/jQuery.ajax/
          //
          $.ajax({
              url: '${createLink(controller:"test", action:"getIndexDefinitions")}',
              data: {templateId: templateId},
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
                $('select[name=spath]').append('<option value="">Select a path</option>');
                
                $(data).each(function(i, didx) {
                
                  op = '<option value="'+didx.path+'">';
                  op += didx.name +' {'+ didx.rmTypeName + '}' //+' {'+ ((didx.name != null) ? didx.name +': ' : '') + didx.rmTypeName + '}';
                  op += '</option>';
                  
                  $('select[name=spath]').append(op);
                });
              },
              error: function(XMLHttpRequest, textStatus, errorThrown) {
                
                console.log(textStatus, errorThrown);
              }
          });
        }); // click en select stemplateId
        
        
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
          
          if ( $('select[name=stemplateId]').val() == null )
          {
            alert('seleccione un arquetipo');
            return;
          }
          if ( $('select[name=spath]').val() == null )
          {
            alert('seleccione una path');
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
            '<td>'+ $('select[name=stemplateId]').val() +'</td>'+
            '<td>'+ $('select[name=spath]').val() +'</td>'+
            //'<td>'+ $('select[name=soperand]').val() +'</td>'+
            '<td>'+ $('input[name=soperand]:checked').val() +'</td>'+
            '<td>'+ $('input[name=svalue]').val() +'</td>'+
            '<td>'+
              '<a href="#" id="removeCriteria">[-]</a>'+
              '<input type="hidden" name="templateId" value="'+$('select[name=stemplateId]').val()+'" />'+
              '<input type="hidden" name="path" value="'+$('select[name=spath]').val()+'" />'+
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
        $('#removeCriteria').live("click", function(e) {
        
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
          
          if ( $('select[name=stemplateId]').val() == null )
          {
            alert('seleccione un arquetipo');
            return;
          }
          if ( $('select[name=spath]').val() == null )
          {
            alert('seleccione una path');
            return;
          }
          
          $('#selection').append(
            '<tr><td>'+ $('select[name=stemplateId]').val() +'</td>'+
            '<td>'+ $('select[name=spath]').val() +'</td>'+
            '<td>'+
              '<a href="#" id="removeSelection">[-]</a>'+
              '<input type="hidden" name="templateId" value="'+$('select[name=stemplateId]').val()+'" />'+
              '<input type="hidden" name="path" value="'+$('select[name=spath]').val()+'" />'+
            '</td></tr>'
          );
          
           // Notifica que la condicion fue agregada
          $.growlUI('Selecci&oacute;n agregada', '<a href="#selection">Verifique la selecci&oacute;n agregada</a>'); 
        });
        
        
        /**
         * Clic en [-]
         * Elimina un criterio de la lista de criterios de busqueda.
         */
        $('#removeSelection').live("click", function(e) {
        
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
        $('form[name=myForm]').submit( function(e) {
        
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
    </g:javascript>
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
    
    
    <g:form name="myForm" controller="query" target="_blank">

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
              <img src="${resource(dir:"images/skin", file:"information.png")}" />
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
            <td>document type</td>
            <td>
              <g:select name="stemplateId" size="3" from="${templateIndexes}"
                        optionKey="templateId" optionValue="concept"
                        noSelection="['':'Choose document type']" />
            </td>
          </tr>
          <tr>
            <td>path</td>
            <td>
              <%-- Se setean las options al elegir un arquetipo --%>
              <select name="spath" size="5"></select>
            </td>
          </tr>
        </table>
      </div>
        
      <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++ -->

      <!-- Campos de queryByData -->
      <div id="query_composition" class="query_build">
        <table>
          <tr>
            <td><label>operand</label></td>
            <td>
              <%-- TODO: sacar de restriccion inList de DataCriteria.operand --%>
              <!--
              Elija operador (TODO: hacerlo con grupo de radio buttons en lugar de selects, hay que corregir el JS)
              -->
              <label><input type="radio" name="soperand" value="eq" />=</label>
              <label><input type="radio" name="soperand" value="neq" />!=</label>
              <label><input type="radio" name="soperand" value="gt" />&gt;</label>
              <label><input type="radio" name="soperand" value="lt" />&lt;</label>
            </td>
          </tr>
          <tr>
            <td><label>value</label></td>
            <td><input type="text" name="svalue" /></td>
          </tr>
          <tr>
            <td><label>add criteria</label></td>
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
          <tr>
            <td>
              composition archetypeId
              <span class="info">
                <img src="${resource(dir:"images/skin", file:"information.png")}" />
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
          <tr>
            <td>
              show UI?
              <span class="info">
                <img src="${resource(dir:"images/skin", file:"information.png")}" />
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
            <th>templateId</th>
            <th>path</th>
            <th>operand</th>
            <th>value</th>
            <th></th>
          </tr>
        </table>
         
        <fieldset class="buttons">
          <g:actionSubmit value="${message(code:'default.button.test.label', default: 'Test')}" action="test" />
          <g:actionSubmit value="${message(code:'default.button.create.label', default: 'Guardar')}" action="save" />
        </fieldset>
      </div>
        
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
          <tr>
            <td>
              composition templateId
              <span class="info">
                <img src="${resource(dir:"images/skin", file:"information.png")}" />
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
            <th>templateId</th>
            <th>path</th>
            <th></th>
          </tr>
        </table>

        <fieldset class="buttons">
          <g:actionSubmit value="${message(code:'default.button.test.label', default: 'Test')}" action="test" />
          <g:actionSubmit value="${message(code:'default.button.create.label', default: 'Guardar')}" action="save" />
        </fieldset>
      </div>
    
    </g:form>
  </body>
</html>