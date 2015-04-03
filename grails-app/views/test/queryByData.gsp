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
    </style>
    <asset:javascript src="xml_utils.js" /><!-- xmlToString -->
    <g:javascript>
      
      /**
       * FIXME: formRemote no me deja hacer validacion e impedir el submit, necesito usar el jQuery Form Plugin y listo.
       */
      var findCompositionsBefore = function()
      {
         /*
         console.log($(this));
         console.log( $('#criteria > tr').length );
         return false;
         */
      };
      
      /**
       * Handler success para consulta por ajax de remoteForm.
       */
      var findCompositionsSuccess = function(data, textStatus)
	   {
         console.log(data);
	      // Si devuelve HTML
	      if ($('select[name=showUI]').val()=='true')
	      {
	        $('#findCompositionsSuccess').html( data );
	      }
	      else // Si devuelve el XML
	      {
	        $('#findCompositionsSuccess').empty();
	      
	        // el append devuelve la DIV no el PRE, chidren tiene el PRE
	        var pre = $('#findCompositionsSuccess').append('<pre></pre>').children()[0];
	        $(pre).text( formatXml( xmlToString(data) ) );
	      }
	   };
	   
	   /**
       * Handler failure para consulta por ajax de remoteForm.
       * FIXME: verificar el nombre de los parametros (no es data).
       */
	   var findCompositionsFailure = function(XMLHttpRequest, textStatus, errorThrown)
	   {
         console.log(XMLHttpRequest, textStatus, errorThrown);
         alert("error: " + errorThrown);
         
	      //$('#findCompositionsFailure').text( xmlToString(data) );
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
        }); // click en select sarchetypeId
        
        

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
          if ( $('select[name=soperand]').val() == null )
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
            '<td>'+ $('select[name=sarchetypeId]').val() +'</td>'+
            '<td>'+ $('select[name=spath]').val() +'</td>'+
            '<td>'+ $('select[name=soperand]').val() +'</td>'+
            '<td>'+ $('input[name=svalue]').val() +'</td>'+
            '<td>'+
              '<a href="#" id="removeCriteria">[-]</a>'+
              '<input type="hidden" name="archetypeId" value="'+$('select[name=sarchetypeId]').val()+'" />'+
              '<input type="hidden" name="path" value="'+$('select[name=spath]').val()+'" />'+
              '<input type="hidden" name="operand" value="'+$('select[name=soperand]').val()+'" />'+
              '<input type="hidden" name="value" value="'+$('input[name=svalue]').val()+'" />'+
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
    <div class="content scaffold-list" role="main">
      <h1>Search Compositions by Data</h1>
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
      
	         <g:select name="sarchetypeId" size="3"
	                   from="${dataIndexes.archetypeId.unique()}"
	                   noSelection="['':'Elija arquetipo']" />
          </td>
        </tr>
        <tr>
          <td><label>path</label></td>
          <td>
            <%-- Se setean las options al elegir un arquetipo --%>
		      <select name="spath" size="5"></select>
          </td>
        </tr>
        <tr>
          <td><label>operand</label></td>
          <td>
            <%-- TODO: sacar de restriccion inList de DataCriteria.operand --%>
            <select name="soperand" size="5">
		        <option value="">Elija operador</option>
		        <option value="=">=</option>
		        <option value="<>">!=</option>
		        <option value=">">&gt;</option>
		        <option value="<">&lt;</option>
		      </select>
          </td>
        </tr>
        <tr>
          <td><label>value</label></td>
          <td>
            <input type="text" name="svalue" />
          </td>
        </tr>
        <tr>
          <td><label>add criteria</label></td>
          <td><a href="#" id="addCriteria">[+]</a></td>
        </tr>
      </table>

      <%-- JS Generado:
      findCompositionsBefore();
		jQuery.ajax({
		    type: 'POST',
		    data: jQuery(this).serialize(),
		    url: '/ehr/rest/queryCompositions?doit=true',
		    success: function (data, textStatus) {
		        findCompositionsSuccess(data, textStatus);
		    },
		    error: function (XMLHttpRequest, textStatus, errorThrown) {
		        findCompositionsFailure(XMLHttpRequest, textStatus, errorThrown);
		    }
		});
		return false
      --%>
      
      <div class="content_padding">
      
	     <h2>Criteria</h2>
	     <g:formRemote name="myForm"
	                  on404="alert('not found!')"
	                  url="[controller:'rest', action:'queryCompositions', params:[doit:true]]"
	                  before="findCompositionsBefore()"
	                  onSuccess="findCompositionsSuccess(data, textStatus)"
	                  onFailure="findCompositionsFailure(XMLHttpRequest, textStatus, errorThrown)">
	        
	        Indices de nivel 1:<br/><br/>
	        
	        <table>
	          <tr>
	            <td>ehrId</td>
	            <td>
		            <g:select name="qehrId" from="${ehr.Ehr.list()}" optionKey="ehrId" size="5" />
	            </td>
	          </tr>
	          <tr>
	            <td>archetypeId</td>
	            <td>
	              <!-- FIXME: busco los arquetipos de composition en los indices porque
                        el EHRServer aun no tiene repositorio de arquetipos. Cuando lo
                        tenga, esta operacion deberia usar el ArchetypeManager. -->
                        
		           <!-- solo arquetipos de composition -->
			        <g:select name="qarchetypeId" size="5"
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
	              <select name="retrieveData" size="2">
	                <option value="false" selected="selected">no</option>
	                <option value="true">yes</option>
	              </select>
	            </td>
	          </tr>
	          <tr>
	            <td>show UI?</td>
	            <td>
	              <select name="showUI" size="2">
	                <option value="false" selected="selected">no</option>
	                <option value="true">yes</option>
	              </select>
	            </td>
	          </tr>
	          <tr>
               <td>query name (for saving)</td>
               <td>
                 <input type="text" name="name" />
               </td>
             </tr>
	        </table>
	        
	        <table id="criteria">
	          <tr>
	            <th>archetypeId</th>
	            <th>path</th>
	            <th>operand</th>
	            <th>value</th>
	            <th></th>
	          </tr>
	        </table>
	        
	        <div class="actions">
	          <input type="submit" value="Find" />
	          <g:submitToRemote value="Save query" url="[action:'saveQueryByData']" />
	        </div>
	      </g:formRemote>
      
      </div>
      
      <h2>Result</h2>
      <!-- <textarea id="findCompositionsSuccess" class="out"></textarea> -->
      <div id="findCompositionsSuccess" class="out"></div>
      
    </div>
  </body>
</html>