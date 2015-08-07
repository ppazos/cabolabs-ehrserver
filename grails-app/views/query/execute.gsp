<%-- Copia modificada de test.gsp, en lugar de recibir los parametros por submit,
     vienen de la consulta guardada en la base --%>
<html>
  <head>
    <meta name="layout" content="main">
    <asset:stylesheet src="query_execution.css" />
    <asset:stylesheet src="jquery-ui-1.9.2.datepicker.min.css" />
    <asset:stylesheet src="highlightjs/xcode.css" />
    
    <asset:javascript src="jquery-ui-1.9.2.datepicker.min.js" />
    <asset:javascript src="jquery.form.js" />
    <asset:javascript src="xml_utils.js" /><!-- xmlToString -->
    <asset:javascript src="highcharts/highcharts.js" />
    <asset:javascript src="highlight.pack.js" /><!-- highlight xml and json -->
    
    <asset:javascript src="query_test_and_execution.js" />
    
    <script type="text/javascript">
      $(document).ready(function() {
     
        // ====================================================================
        // Submit ajax para busqueda de compositions por criterios de datos
        // ====================================================================
        
        $('#form_composition').ajaxForm({
        
          //dataType: 'json',
          url: '${createLink(controller:'rest', action:'query')}',
          data: {},
        
          beforeSubmit: function(data, form, options) {            // >>> BEFORE SUBMIT
            
            console.log('form_composition beforeSubmit');
            //console.log(data);
            
            valid = true;
            
            // Verifica que todos los valores necesarios para la query fueron ingresados
            $('input[type=text][name=value]').each( function(i, elem) {
              
              e = $(elem);
              e.removeClass('errors');
              
              //console.log($(elem).val());
              //console.log( elem.value );
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
          success: function(responseText, statusTest, req, form) { // >>> SUCCESS
            
            console.log('form_composition success');
            //console.log(responseText);
            //console.log(statusTest);
            //console.log(req);
            //console.log(form);

            
            // reset code class or highlight
            $('code').removeClass('xml json');
            
            
            // Si devuelve HTML
            if ($('select[name=showUI]').val()=='true')
            {
              console.log("showUI selected");
              console.log(responseText);
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
        });
        
        
        
        /**
         * En lugar de asociar directamente el ajaxForm, tengo que asociarlo
         * en el momento del submit porque el dataType puede cambiar segun lo
         * que seleccione el usuario.
         * Si bindeo el ajaxForm y el usuario cambia el dataType, ocurren un
         * error en el submit.
         */
        $('#form_datavalue').submit( function(e) {
        
          // Validacion
          if ($('select[name=ehrId]').val()==null)
          {
            alert('${g.message(code:'query.execute.select_ehr')}');
            return false;
          }
        
          // En lugar de bindear ajaxForm, llamo a ajaxSubmit sobre el form. 
          $(this).ajaxSubmit({
          
            dataType: $('select[name=format]').val(), // xml o json
            url: '${createLink(controller:'rest', action:'query')}',
            data: {},
          
            beforeSubmit: function(data, form, options) {
            
              console.log('form_datavalue beforeSubmit');
            },
            
            success: function(responseText, statusTest, req, form) {
              
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
              //$('html,body').animate({scrollTop:$('#results').offset().top+400}, 500);
              $('html,body').animate({scrollTop:$('#code').offset().top+400}, 500);
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
      
    </script>
  </head>
  <body>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list">Consultas</g:link></li>
        <li><g:link class="show" action="show" id="${query.id}">Detalles de la consulta</g:link></li>
      </ul>
    </div>
    
    <h1><g:message code="query.execute.title" args="['"'+query.name+'"']" /></h1>
      
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>
    
    <g:if test="${type == 'composition'}">
      
      <h2><g:message code="query.execute.queryByData" /></h2>
      <form id="form_composition" method="post">
        <input type="hidden" name="queryUid" value="${query.uid}" />
        <h3><g:message code="query.execute.criteria" /></h3>
        <table>
          <tr>
            <th>archetypeId</th>
            <th>path</th>
            <th>operand</th>
            <th>value</th>
          </tr>
          <g:each in="${query.where}" var="criteria" status="i">
            <tr>
              <td>
                ${criteria.archetypeId}
                <input type="hidden" name="archetypeId" value="${criteria.archetypeId}" />
              </td>
              <td>
                ${criteria.path}
                <input type="hidden" name="path" value="${criteria.path}" />
              </td>
              <td>
                ${criteria.toSQL()}
                <input type="hidden" name="operand" value="${criteria.operand}" />
              </td>
              <td>
                <%-- sino hay valor, se solicita --%>
                <g:if test="${criteria.value}">
                  ${criteria.value}
                  <input type="hidden" name="value" value="${criteria.value}" />
                </g:if>
                <g:else>
                  <input type="text" name="value" />
                </g:else>
              </td>
            </tr>
          </g:each>
        </table>
          
        <h3><g:message code="query.create.filters" /></h3>
        <table>
          <tr>
            <td>ehrId</td>
            <td>
              <g:select name="ehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" />
            </td>
          </tr>
          
          <%-- No used for now
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
          --%>
          
          <tr>
            <td>dates</td>
            <td>
              <input type="text" name="fromDate" placeholder="${message(code:'filter.fromDate')}" readonly="readonly" />
              <input type="text" name="toDate" placeholder="${message(code:'filter.toDate')}" readonly="readonly" />
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
          <input type="submit" value="${message(code:'query.execute.action.execute')}" />
        </fieldset>
      </form>
    </g:if>
    <g:else>
    
      <h2><g:message code="query.execute.queryData" /></h2>
      <form id="form_datavalue" method="post">
        <input type="hidden" name="queryUid" value="${query.uid}" />
        <h3><g:message code="query.execute.selectedDataPoints" /></h3>
        <table>
          <tr>
            <th>archetypeId</th>
            <th>path</th>
          </tr>
          <g:each in="${query.select}" var="dataGet" status="i">
            <tr>
              <td>
                ${dataGet.archetypeId}
                <input type="hidden" name="archetypeId" value="${dataGet.archetypeId}" />
              </td>
              <td>
                ${dataGet.path}
                <input type="hidden" name="path" value="${dataGet.path}" />
              </td>
            </tr>
          </g:each>
        </table>
          
        <h3><g:message code="query.create.filters" /></h3>
        <table>
          <tr>
            <td>ehrId</td>
            <td>
              <g:select name="ehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="4" />
            </td>
          </tr>
          
          <%-- No used for now
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
          --%>
          
          <tr>
            <td>dates</td>
            <td>
              <input type="text" name="fromDate" placeholder="${message(code:'filter.fromDate')}" readonly="readonly" />
              <input type="text" name="toDate" placeholder="${message(code:'filter.toDate')}" readonly="readonly" />
            </td>
          </tr>
          <tr>
            <td>default format</td>
            <td>
              <select name="format" size="2">
                <option value="xml" ${((query.format=='xml') ? 'selected="selected"':'')}>XML</option>
                <option value="json"${((query.format=='json') ? 'selected="selected"':'')}>JSON</option>
              </select>
            </td>
          </tr>
          <tr>
            <td>default group</td>
            <td>
              <select name="group" size="3">
                <option value="none" ${((query.group=='none') ? 'selected="selected"':'')}>none</option>
                <option value="composition" ${((query.group=='composition') ? 'selected="selected"':'')}>composition</option>
                <option value="path" ${((query.group=='path') ? 'selected="selected"':'')}>path</option>
              </select>
            </td>
          </tr>
        </table>
          
        <fieldset class="buttons">
          <input type="submit" value="${message(code:'query.execute.action.execute')}" />
        </fieldset>
      </form>
    </g:else>
      
    <h2><g:message code="query.execute.results" /></h2>
    <a href="#" id="show_data"><g:message code="query.execute.showData" /></a>
    <div id="results" class="out"></div>
    <pre><code id="code"></code></pre>
    <div id="chartContainer"></div>
  </body>
</html>