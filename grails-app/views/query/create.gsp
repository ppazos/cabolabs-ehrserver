<%@ page import="com.cabolabs.ehrserver.query.Query" %><%@ page import="grails.converters.JSON" %><%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
    <title><g:message code="query.create.title" /></title>
    <style>
      #query_test, #query_composition, #query_datavalue, #query_common {
        display: none;
      }
      .btn-toolbar.bottom {
        display: none;
      }
      tr td:last-child select, tr td:last-child input[type=text] {
        width: 100%;
      }
      
      table#query_setup tr td:first-child {
        width: 140px;
      }
      /**
      tr td:first-child {
        text-align: right;
      }
      tr td:first-child label {
        float: right;
      }
      **/
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
      #update_button {
        display: none;
      }
      fieldset {
        border: 1px solid #ddd;
      }
      /* hide all criteria vales but the first */
      .criteria_value {
        display: none;
      }
      .criteria_value:first-child {
        display: inline;
      }
      
      /* Smaller selects with size > 1 because input-sm is not supported for that case */
      select.withsize {
        padding: 5px 10px;
        font-size: 12px;
        border-radius: 3px;
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
    
    <asset:javascript src="query_test_and_execution.js" />
    <!-- /query test -->
    
    <script type="text/javascript">

      var query = {
        id: undefined, // used for edit/update
        id_gen: 0,
        name: undefined,
        type: undefined,
        format: undefined,
        template_id: undefined,
        criteriaLogic: undefined,
        where: [], // DataCriteria
        select: [], // DataGet
        group: 'none',
        set_id:       function (id) { this.id = id; }, // for edit/update
        set_type:     function (type) { this.type = type; }, // composition or datavalue
        get_type:     function () { return this.type; }, // composition or datavalue
        set_criteria_logic: function (criteriaLogic) { this.criteriaLogic = criteriaLogic; }, // composition
        set_name:     function (name) { this.name = name; },
        set_format:   function (format) { this.format = format; },
        set_group:    function (group) { this.group = group; },
        set_template_id: function (template_id) {
          if (template_id != null) this.template_id = template_id;
        },
        add_criteria: function (archetype_id, path, rm_type_name, criteria)
        {
          if (this.type != 'composition') return false;

          this.id_gen++;

          var c = {cid: this.id_gen, archetypeId: archetype_id, path: path, rmTypeName: rm_type_name, class: 'DataCriteria'+rm_type_name};

          // copy attributes
          for (a in criteria.conditions) c[a] = criteria.conditions[a];

          c['spec'] = criteria.spec;
          
          //this.where.push( c );
          this.where[this.id_gen - 1] = c; // uses the id as index, but -1 to start in 0

          // when items are removed and then added, there are undefined entries in the array
          // this cleans the undefined items so the server doesnt receive empty values.
          this.where = this.where.filter(function(n){ return n != undefined }); 
          
          return this.id_gen;
        },
        add_projection: function (archetype_id, path)
        {
          if (this.type != 'datavalue') return false;

          this.id_gen++;
          
          this.select[this.id_gen - 1] = {pid: this.id_gen, archetype_id: archetype_id, path: path};

          // when items are removed and then added, there are undefined entries in the array
          // this cleans the undefined items so the server doesnt receive empty values.
          this.select = this.select.filter(function(n){ return n != undefined }); 
          
          return this.id_gen;
        },
        remove_criteria: function (id)
        {
          // lookup: TODO put this in array prototype
          //var result = $.grep(myArray, function(e){ return e.id == id; });
          var pos;
          for (var i = 0, len = this.where.length; i < len; i++) {
             if (this.where[i].cid == id)
             {
                pos = i;
                break;
             }
          }

          this.where.splice(pos, 1); // removes 1 item in from the position
        },
        remove_projection: function (id)
        {
           // lookup: TODO put this in array prototype
           //var result = $.grep(myArray, function(e){ return e.id == id; });
           var pos;
           for (var i = 0, len = this.select.length; i < len; i++) {
              if (this.select[i].pid == id)
              {
                 pos = i;
                 break;
              }
           }
           
           this.select.splice(pos, 1); // removes 1 item in from the position
        },
        log: function () { console.log(this); }
      };

      function Criteria(spec) {

        this.conditions = [];
        this.spec = spec;

        this.add_condition = function (attr, operand, values) {

          if (values.length > 1)
            this.conditions[attr+'Value'] = values;
          else
            this.conditions[attr+'Value'] = values[0];
          
          this.conditions[attr+'Operand'] = operand;
        };
      };

      
      // ==============================================================================================
      // SAVE OR UPDATE
      // TODO: put these methods in the query object
      // ==============================================================================================
    
      var save_or_update_query = function(action) {

        if (action != 'save' && action != 'update') throw "Action is not save or update";
      
        query.set_name($('input[name=name]').val());
        query.set_criteria_logic($('select[name=criteriaLogic]').val());

        if (query.get_type() == 'datavalue')
        {
           query.set_format( $('select[name=format]').val() );
           query.set_group( $('select[name=group]').val() ); // for datavalue query
        }
        else
        {
           query.set_format( $('select[name=composition_format]').val() );
           query.set_template_id( $('select[name=templateId]').val() );
        }
        
        // We need these functions because the URLs are created on the server.
        var s_o_u = {
           save_query: function() {
             $.ajax({
               method: 'POST',
               url: '${createLink(controller:"query", action:"save")}',
               contentType : 'application/json',
               data: JSON.stringify( {query: query} ) // JSON.parse(  avoid puting functions, just data
             })
             .done(function( data ) {
               //console.log(data);
               location.href = '${createLink("action": "show")}?id='+ data.id;
             });
           },
           update_query: function() {
             $.ajax({
               method: 'POST',
               url: '${createLink(controller:"query", action:"update")}',
               contentType : 'application/json',
               data: JSON.stringify( {query: query} ) // JSON.parse(  avoid puting functions, just data
             })
             .done(function( data ) {
               //console.log(data);
               location.href = '${createLink("action": "show")}?id='+ data.id;
             });
           }
        };
        
        // save_query() or update_query()
        s_o_u[action+'_query']();
      };
      
      
      // ==============================================================================================
      // TEST COMPOSITION and DATAVALUE
      // ==============================================================================================

      var test_query_composition = function () {

        //console.log('test composition query');
        //console.log('query_form', $('#query_form'));

        
        // query management
        query.set_name($('input[name=name]').val());
        query.set_criteria_logic($('select[name=criteriaLogic]').val());
        query.set_format( $('select[name=composition_format]').val() );
        query.set_template_id( $('select[name=templateId]').val() );

        qehrId = $('select[name=qehrId]').val();
        fromDate = $('input[name=fromDate]').val();
        toDate = $('input[name=toDate]').val();
        retrieveData = $('select[name=retrieveData]').val();
        showUI = $('select[name=showUI]').val();
        format = $('select[name=composition_format]').val();
        organizationUid = $('select[name=organizationUid]').val();

        if (showUI == 'true') format = 'html';

        var data = {
                    query: query, fromDate: fromDate, toDate: toDate,
                    retrieveData: retrieveData, showUI: showUI, format: format,
                    organizationUid: organizationUid
                   };
        if (qehrId != null) data.qehrId = qehrId;
        
        
        $.ajax({
          method: 'POST',
          url: '${createLink(controller:'rest', action:'queryCompositions')}',
          dataType: format, // xml o json
          contentType: 'application/json',
          data: JSON.stringify( data ) // JSON.parse(  avoid puting functions, just data
        })
        .done(function( res ) {
           
           //console.log("queryCompositions result", res);
           
           var code = $('#code');
           // reset code class or highlight
           code.removeClass('xml json');

           // Si devuelve HTML
           if ($('select[name=showUI]').val()=='true')
           {
             console.log('UI');
              
             $('#results').html( res );
             $('#results').show('slow');
           }
           else // Si devuelve XML o JSON
           {
             console.log('JSON OR XML');
             if (format == 'json')
             {
               code.addClass('json');
               code.text(JSON.stringify(res, undefined, 2));
               code.each(function(i, e) { hljs.highlightBlock(e); });
             }
             else
             { 
               // highlight
               code.addClass('xml');
               code.text(formatXml( xmlToString(res) ));
               code.each(function(i, e) { hljs.highlightBlock(e); });
             }

             code.show('slow');
           }
           
           // Muestra el boton que permite ver los datos crudos
           // devueltos por el servidor
           $('#show_data').show();
        })
        .fail(function(a,b,c) {
           
           console.log(a,b,c);
        });

        console.log('after ajax submit');
      };


      var test_query_datavalue = function () {

        console.log('test datavalue query');

        // query management
        query.set_name($('input[name=name]').val());
        query.set_format($('select[name=format]').val());
        query.set_group($('select[name=group]').val()); // for datavalue query

        qehrId = $('select[name=qehrId]').val();
        fromDate = $('input[name=fromDate]').val();
        toDate = $('input[name=toDate]').val();
        format = $('select[name=format]').val(); // xml o json
        group = $('select[name=group]').val();
        organizationUid = $('select[name=organizationUid]').val();

        var data = {
                    query: query, fromDate: fromDate, toDate: toDate, format: format,
                    group: group, organizationUid: organizationUid
                   };
        if (qehrId != null) data.qehrId = qehrId;
        
        
        $.ajax({
           method: 'POST',
           url: '${createLink(controller:"rest", action:"queryData")}',
           contentType : 'application/json',
           dataType: format,
           data: JSON.stringify( data )
         })
         .done(function( res ) {
         
            console.log(res);
            
            // Vacia donde se va a mostrar la tabla o el chart
            $('#chartContainer').empty();
            
            console.log('form_datavalue success 2');

            var code = $('#code');
            
            // reset code class or highlight
            code.removeClass('xml json');

            // Si devuelve JSON (verifica si pedi json)
            if (format == 'json')
            {
              console.log('form_datavalue success json');
              
              // highlight
              code.addClass('json');
              code.text(JSON.stringify(res, undefined, 2));
              code.each(function(i, e) { hljs.highlightBlock(e); });
              
              if (qehrId != null) 
              {
                // =================================================================
                // Si agrupa por composition (muestra tabla)
                //
                if ($('select[name=group]').val() == 'composition')
                {
                  queryDataRenderTable(res);
                }
                else if ($('select[name=group]').val() == 'path')
                {
                  queryDataRenderChart(res);
                }
              }
              else
              {
                // chart grouped by ehr
              }
            }
            else // Si devuelve el XML
            {
              console.log('form_datavalue success XML');
            
              // highlight
              code.addClass('xml');
              code.text(formatXml( xmlToString(res) ));
              code.each(function(i, e) { hljs.highlightBlock(e); });
            }

            code.show('slow');
            
            // Muestra el boton que permite ver los datos crudos
            // devueltos por el servidor
            $('#show_data').show();
            
            
            // Hace scroll animado para mostrar el resultado
            $('html,body').animate({scrollTop:code.offset().top+400}, 500);
         });

      }; // test_query_datavalue
    
      
      /*
       * Called from the save/update buttons
       */
      var ajax_submit_test_or_save = function (action) {

         console.log('ajax_submit', action);

         if (action == 'save')
         {
            save_or_update_query(action);
         }
         else if (action == 'update')
         {
            save_or_update_query(action);
         }
         else if (action == 'test')
         {
            console.log('ehrUid', $('select[name=qehrId]').val());
            
            // Validacion
            /*
            if ($('select[name=qehrId]').val()==null)
            {
              alert('Please select an EHR');
              return false;
            }
            */

            if ($('select[name=type]').val()=='composition')
            {
               test_query_composition();
            }
            else // query type = datavalue
            {
               test_query_datavalue();
            }
         } // test query
         
      }; // ajax_submit

      // ==================================================================
      // / TEST QUERIES
      // ==================================================================


      // =================================
      // COMPO QUERY CREATE/EDIT =========
      // =================================

      /**
       * Creates the criteria, adds it to the query, updates the UI.
       */
      var dom_add_criteria_2 = function (fieldset) {

        console.log('dom_add_criteria_2', $('input.value.selected', fieldset), $('input.value.selected', fieldset).length);
        
        // =======================================================================================
        // Criteria is complete? https://github.com/ppazos/cabolabs-ehrserver/issues/141
        //
        // for each value in the criteria
        
        
        // inputs or selects with values
        criteria_fields = $(':input.value.selected', fieldset);
        
        if ( criteria_fields.length == 0 ) // case when no criteria spec is selected
        {
          alert('Please select a datapoint to define a query criteria');
          return false;
        }
        else // case when criteria spec is selected and maybe some values are not filled in
        {
          complete = true;
           $.each( criteria_fields, function (index, value_input) {
            
             if ( $(value_input).val() == '' )
             {
               complete = false;
               return false; // breaks each
             }
           });
        }
        if (!complete)
        {
          alert('Please fill all the criteria values');
          return false;
        }
        // =======================================================================================

        var archetype_id = $('select[name=view_archetype_id]').val();
        var path = $('select[name=view_archetype_path]').val();
        var type = $('select[name=view_archetype_path] option:selected').data('type');
        var spec = $('input[name=criteria]', fieldset).data('spec');
        
        console.log('spec', spec);

        var attribute, operand, value, values;
        var criteria_str = '';
        
        // criteria js object
        var criteria = new Criteria(spec);
        
        $.each( $('.criteria_attribute', fieldset), function (i, e) {

          values = [];
          attribute = $('input[name=attribute]', e).val()
          operand = $('select[name=operand]', e).val();
          
          // FIXME: are we using the hidden fields?
          criteria_str += attribute +' ';
          criteria_str += operand +' ';

          // for each criteria value (can be 1 for value, 2 for range or N for list)
          // the class with the name of the attribute is needed to filter values for each attribute
          $.each( $(':input.selected.value.'+attribute, e), function (j, v) {

             console.log(v);

             value = $(v).val();
             values.push(value);
             
             criteria_str += value + ', ';
          });

          criteria_str = criteria_str.substring(0, criteria_str.length-2); // remove last ', '
          criteria_str += ' AND ';
          
          
          // query object mgt
          criteria.add_condition(attribute, operand, values);
        });
        
        criteria_str = criteria_str.substring(0, criteria_str.length-5); // remove last ' AND '
        
        
        // query object mgt
        cid = query.add_criteria(archetype_id, path, type, criteria);
        query.log();


        // shows the criteria in the UI
        $('#criteria').append(
           '<tr data-id="'+ cid +'">'+
           '<td>'+ archetype_id +'</td>'+
           '<td>'+ path +'</td>'+
           '<td>'+ type +'</td>'+
           '<td>'+ criteria_str +'</td>'+
           '<td>'+
             '<div class="btn-toolbar" role="toolbar">'+
               '<a href="#" class="removeCriteria">'+
                 '<button type="button" class="btn btn-default btn-sm">'+
                   '<span class="fa fa-minus-circle fa-fw" aria-hidden="true"></span>'+
                 '</button>'+
               '</a>'+
             '</div>'+
           '</td></tr>'
        );
        
        return true;
        
      }; // dom_add_criteria_2


      var query_composition_add_criteria_2 = function () {

        // data for the selected criteria (input[name=criteria] is the radio button)
        ok = dom_add_criteria_2(
          $('input[name=criteria]:checked', '#query_form').closest('.form-group') // container of the selected criteria
        );
        
        if (ok)
        {
          // Notifica que la condicion fue agregada
          $.growlUI(
            '${g.message(code:"query.create.condition_added")}',
            '<a href="#criteria">${g.message(code:"query.create.verify_condition")}</a>'
          );
        }
      };

      // =================================
      // /COMPO QUERY CREATE/EDIT ========
      // =================================
      
      // =================================
      // DATA QUERY CREATE/EDIT =========
      // =================================
      
      var dom_add_selection = function (archetype_id, path) {

        // query object mgt
        pid = query.add_projection(archetype_id, path);
        query.log();

        // shows the projection in the UI
        $('#selection').append(
          '<tr data-id="'+ pid +'">'+
          '<td>'+ archetype_id +'</td><td>'+ path +'</td>'+
          '<td>'+
            '<div class="btn-toolbar" role="toolbar">'+
              '<a href="#" class="removeSelection">'+
                '<button type="button" class="btn btn-default btn-sm">'+
                  '<span class="fa fa-minus-circle fa-fw" aria-hidden="true"></span>'+
                '</button>'+
              '</a>'+
            '</div>'+
          '</td></tr>'
        );
      };
      
      var query_datavalue_add_selection = function () {

        // TODO: verificar que todo tiene valor seleccionado
         
        if ( $('select[name=view_archetype_id]').val() == null )
        {
          alert('${g.message(code:"query.create.please_select_concept")}');
          return;
        }
        if ( $('select[name=view_archetype_path]').val() == null )
        {
          alert('${g.message(code:"query.create.please_select_datapoint")}');
          return;
        }

        dom_add_selection($('select[name=view_archetype_id]').val(), $('select[name=view_archetype_path]').val());
        
        
         // Notifica que la condicion fue agregada
        $.growlUI('${g.message(code:"query.create.selection_added")}', '<a href="#selection">${g.message(code:"query.create.verify_selection")}</a>'); 
      };

      // =================================
      // /DATA QUERY CREATE/EDIT =========
      // =================================
      
      // =================================
      // COMMON QUERY CREATE/EDIT ========
      // =================================
      
      var get_and_render_archetype_paths = function (archetype_id) {

        $.ajax({
          url: '${createLink(controller:"query", action:"getArchetypePaths")}',
          data: {archetypeId: archetype_id, datatypesOnly: true},
          dataType: 'json',
          success: function(data, textStatus) {
          
            /*
            didx
             archetypeId: 'openEHR-EHR-COMPOSITION.encounter.v1
             name: 'value'
             path: '/content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value'
             rmTypeName: "DV_QUANTITY"
            */

            // Saca las options que haya
            $('select[name=view_archetype_path]').empty();
            
            // Agrega las options con las paths del arquetipo seleccionado
            $('select[name=view_archetype_path]').append('<option value="">${g.message(code:"query.create.please_select_datapoint")}</option>');
            
            // Adds options to the select
            $(data).each(function(i, didx) {
            
              $('select[name=view_archetype_path]').append(
                '<option value="'+ didx.path +'" data-type="'+ didx.rmTypeName +'">'+ didx.name +' {'+ didx.rmTypeName + '}</option>'
              );
            });
          },
          error: function(XMLHttpRequest, textStatus, errorThrown) {
            
            console.log(textStatus, errorThrown);
          }
        });
      };


      /**
       * TODO: put the criteria builder functions in a criteria builder object.
       */
      // For composition criteria builder
      var global_criteria_id = 0; // used to link data for the same criteria
      var get_criteria_specs = function (datatype) {

        $.ajax({
          url: '${createLink(controller:"query", action:"getCriteriaSpec")}',
          data: {
             archetypeId: $('select[name=view_archetype_id]').val(),
             path:        $('select[name=view_archetype_path]').val(),
             datatype:    datatype
          },
          dataType: 'json',
          success: function(spec, textStatus) {

            console.log(spec);

            $('#composition_criteria_builder').empty();
            
            var criteria = '';

            // spec is an array of criteria spec
            // render criteria spec
            for (i in spec)
            {
              aspec = spec[i];
              
              global_criteria_id++;
              
              
              // 1 column for the radio button that selects the criteria
              criteria += '<div class="form-group"><div class="col-sm-1">';
              
              // All fields of the same criteria will have the same id in the data-criteria attribute
              if (i == 0)
                criteria += '<input type="radio" name="criteria" data-criteria="'+ global_criteria_id +'" data-spec="'+ i +'" checked="checked" />';
              else
                criteria += '<input type="radio" name="criteria" data-criteria="'+ global_criteria_id +'" data-spec="'+ i +'" />';
              
              // 11 columns for the criteria
              criteria += '</div><div class="col-sm-11">';
              
              
              for (attr in aspec)
              {
                criteria += '<div class="criteria_attribute row">';
                criteria += '<div class="col-sm-2">'+ attr + '<input type="hidden" name="attribute" value="'+ attr +'" /></div>';
                
                conditions = aspec[attr]; // spec[0][code][eq] == value
                criteria += '<div class="col-sm-5"><select class="operand form-control input-sm" data-criteria="'+ global_criteria_id +'" name="operand">';
                
                
                // =======================================================================================================
                // If this has possible values from the template
                var possible_values;
                switch (datatype)
                {
                  case 'DV_CODED_TEXT':
                    possible_values = conditions['codes'];
                    delete conditions['codes']; // Deletes the codes attribute
                  break;
                  case 'DV_QUANTITY':
                    possible_values = conditions['units'];
                    delete conditions['units']; // Deletes the codes attribute
                  break;
                }
                
                console.log('possible values', datatype, possible_values);
                console.log('conditions', conditions);
                // =======================================================================================================
                
                
                for (cond in conditions)
                {
                  criteria += '<option value="'+ cond +'">'+ cond +'</option>';
                }
                criteria += '</select></div>';
                
                
                
                // *******************************************************************************
                // FIXME: date* is not supported bu FF, we might need to use a JS lib / polyfill
                // *******************************************************************************
                
                // input type by datatype and attr
                var input_type = 'text';
                var class_type = ''; // date, datetime and datetime-local types don't work on all browsers, we use bootstrap datetime picker for those, applied using a class
                switch ( datatype )
                {
                  case 'DV_QUANTITY':
                    if (attr == 'magnitude') input_type = 'number';
                  break;
                  case 'DV_DATE_TIME':
                    if (attr == 'value') class_type = 'input_datetime'; //input_type = 'datetime-local';
                  break;
                  case 'DV_DATE':
                    if (attr == 'value') class_type = 'input_date'; //input_type = 'datetime-local';
                  break;
                  case 'DV_COUNT':
                    if (attr == 'magnitude') input_type = 'number';
                  break;
                  case 'DV_ORDINAL':
                    if (attr == 'value') input_type = 'number';
                  break;
                  case 'DV_DURATION':
                    if (attr == 'magnitude') input_type = 'number';
                  break;
                  case 'DV_PROPORTION':
                    if (attr == 'numerator') input_type = 'number';
                    if (attr == 'denominator') input_type = 'number';
                  break;
                }
                
                
                
                // indexes of operand and value should be linked.
                criteria += '<div class="criteria_value_container col-sm-5">';
                var i = 0;
                for (cond in conditions)
                {
                  //console.log('cond', cond, 'conditions[cond]', conditions[cond]);
                  criteria += '<span class="criteria_value">';
                  
                  if (cond == 'eq_one')
                  {
                    criteria += '<select name="value" class="value'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'">';
                    for (v in conditions[cond]) // each value from the list of possible values
                    {
                      criteria += '<option value="'+ conditions[cond][v] +'">'+ conditions[cond][v] +'</option>'; // TODO: get texts for values
                    }
                    criteria += '</select>';
                  }
                  else
                  {
                    if (possible_values == undefined)
                    {
                      // TODO: add controls depending on the cardinality of value, list should allow any number of values to be set on the UI
                      switch ( conditions[cond] )
                      {
                        case 'value':
                          criteria += '<input type="'+ input_type +'" name="value" class="value'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" />';
                        break
                        case 'list':
                          criteria += '<input type="'+ input_type +'" name="list" class="value list'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" /><!-- <span class="criteria_list_add_value">[+]</span> -->';
                        break
                        case 'range':
                          criteria += '<input type="'+ input_type +'" name="range" class="value min'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" />..<input type="'+ input_type +'" name="range" class="value max'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" />';
                        break
                      }
                    }
                    else // we have possible_values for this criteria
                    {
                      switch ( conditions[cond] )
                      {
                        case 'value':
                          criteria += '<select name="value" class="value '+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'">';
                          for (k in possible_values)
                          {
                            criteria += '<option value="'+ k +'">'+ possible_values[k] +'</option>';
                          }
                          criteria += '</select>';
                        break
                        case 'list':
                          criteria += '<select name="list" class="value list '+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'">';
                          for (k in possible_values)
                          {
                            criteria += '<option value="'+ k +'">'+ possible_values[k] +'</option>';
                          }
                          criteria += '</select>';
                        break
                        case 'range':
                          // this case deosnt happen for now...
                          //criteria += '<input type="'+ input_type +'" name="range" class="value min'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" />..<input type="'+ input_type +'" name="range" class="value max'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" />';
                        break
                      }
                    }
                  }
                  
                  criteria += '</span>'; // criteria value
                   
                  i++;
                }
                criteria += '</div>'; // criteria value container
                criteria += '</div>'; // criteria attribute
                
              } // for aspec
              
              criteria += '</div></div>'; // /col-sm-11 /row
                
            }; // for render criteria spec
            
            $('#composition_criteria_builder').append( criteria );
            
            
            
            // apply datepicker behavior for generated fields, if any is datetime picker
            apply_datetime_picker_behavior_for_criteria( $('#composition_criteria_builder') );
            
          },
          error: function(XMLHttpRequest, textStatus, errorThrown) {
            
            console.log(textStatus, errorThrown);
          }
        });
      };
      
      
      
      /**
       * Applies datepicker behavior for generated fields, if any is datetime picker.
       */
      var apply_datetime_picker_behavior_for_criteria = function (criteria_container)
      {
        // datetime
        var fields = $('.input_datetime', criteria_container)
        
        fields.datetimepicker({
          format: "yyyy-mm-ddThh:ii:ssZ",
          todayHighlight: 1,
          weekStart: 1,
          todayHighlight: true,
          minuteStep: 15,
          pickerPosition: 'bottom-left',
          autoclose: true
        });
        
        fields.attr('readonly', true);
        
        // date
        fields = $('.input_date', criteria_container)
        
        fields.datetimepicker({
          format: "yyyy-mm-dd",
          todayHighlight: 1,
          weekStart: 1,
          todayHighlight: true,
          startView: 'month', /* starts showing the days of the month */
          minView: 'month',   /* ends showing the days of the month, that's the date picker, below this starts hte time picker */
          pickerPosition: 'bottom-left',
          autoclose: true
        });
        
        fields.attr('readonly', true);
      };
      
      
      
      // attachs onchange for operand selects created by the 'get_criteria_specs' function.
      $(document).on('change', 'select.operand', function(evt) {
         
        console.log('operand change', this.selectedIndex, $(this).data('criteria'));
        
        var criteria_value_container = $(this).parent().next();
        
        // All criteria values hidden
        criteria_value_container.children().css('display', 'none');
        $(':input', criteria_value_container).removeClass('selected'); // unselect the current selected criteria values
        
        // criteria value [i] should be displayed
        var value_container = $(criteria_value_container.children()[this.selectedIndex]).css('display', 'inline')
        $(':input', value_container).addClass('selected'); // add selected to all the inputs, textareas and selects, this is to add the correct values to the criteria
        
        console.log( $('#query_form').serialize() );
      });
      
      // Add multiple input values for value list criteria when enter is pressed
      $(document).on('keypress', ':input.value.list', function(evt) {
      
          if (!evt) evt = window.event;
          var keyCode = evt.keyCode || evt.which;
          
          if (keyCode == '13') { // Enter pressed
          
            $(this).after( $(this).clone().val('') );
            $(this).next().focus();
            return false;
          }
      });
     
      
      // =================================
      // /COMMON QUERY CREATE/EDIT =======
      // =================================
      
      var show_controls = function (query_type) {

        $('#query_common').show();
        $('#query_'+ query_type).show();
        $('.btn-toolbar.bottom').show();
      };
      
    
      $(document).ready(function() {
      
        $('select[name=type]').val(""); // select empty option by default
        
        // zebra style to the selects that have the size attr
        $.each($('select[size] option'), function( i, option ) {
        
          if (i % 2 == 0) $(option).css('background-color', '#f3f3f3');
        });
      
        <%
        // =====================================================================
        // a little groovy code to setup the edit/update ...
        
        if (mode == 'edit')
        {
          // dont allow to change type
          println '$("select[name=type]").val("'+ queryInstance.type +'");'
          println '$("select[name=type]").prop("disabled", "disabled");'
          
          println 'show_controls("'+ queryInstance.type +'");'
          
          println '$("select[name=format]").val("'+ queryInstance.format +'");'
          println '$("select[name=composition_format]").val("'+ queryInstance.format +'");'
          
          
          println '$("select[name=templateId]").val("'+ queryInstance.templateId +'");'
          
          println '$("select[name=group]").val("'+ queryInstance.group +'");'
          println '$("select[name=criteriaLogic]").val("'+ queryInstance.criteriaLogic +'");'
          println 'query.set_id("'+ queryInstance.id +'");'
          println 'query.set_name("'+ queryInstance.name +'");'
          println 'query.set_type("'+ queryInstance.type +'");'
          println 'query.set_format("'+ queryInstance.format +'");'
          println 'query.set_group("'+ queryInstance.group +'");'
          println 'query.set_criteria_logic("'+ queryInstance.criteriaLogic +'");'
          
          if (queryInstance.templateId)
            println 'query.set_template_id("'+ queryInstance.templateId +'");'
          
          
          
          
          if (queryInstance.type == 'composition')
          {
             // similar code to dom_add_criteria_2 in JS
             
             def attrs, attrValueField, attrOperandField, value, operand
             println 'var criteria;'
             
             queryInstance.where.each { data_criteria ->
                
                attrs = data_criteria.criteriaSpec(data_criteria.archetypeId, data_criteria.path)[data_criteria.spec].keySet() // attribute names of the datacriteria
                
                println 'criteria = new Criteria('+ data_criteria.spec +');'
                
                attrs.each { attr ->
                
                   attrValueField = attr + 'Value' 
                   attrOperandField = attr + 'Operand'
                   operand = data_criteria."$attrOperandField"
                   value = data_criteria."$attrValueField"
                   
                   // TODO
                   // date?.format(Holders.config.app.l10n.db_datetime_format)
                   // ext_datetime_utcformat_nof = "yyyy-MM-dd'T'HH:mm:ss'Z'"
                   
                   if (value instanceof List)
                   {
                      if (value[0] instanceof Date)
                      {
                         println 'criteria.add_condition("'+
                            attr +'", "'+
                            operand +'", '+
                            ( value.collect{ it.format(grailsApplication.config.app.l10n.ext_datetime_utcformat_nof) } as JSON ) +');'
                      }
                      else
                      {
                         println 'criteria.add_condition("'+
                            attr +'", "'+
                            operand +'", '+
                            ( value.collect{ it.toString() } as JSON ) +');' // toString to have the items with quotes on JSON, without the quotes I get an error when saving/binding the uptates to criterias.
                      }
                   }
                   else // value is an array of 1 element
                   {
                      // FIXME: if the value is not string or date, dont include the quotes
                      println 'criteria.add_condition("'+
                         attr +'", "'+
                         operand +'", [ "'+
                         value +'" ]);'
                   }
                   
                } // each attr
                
                
                println 'cid = query.add_criteria("'+
                  data_criteria.archetypeId +'", "'+
                  data_criteria.path +'", "'+
                  data_criteria.rmTypeName +'", '+
                  'criteria);'
                
                println 'var criteria_str = "'+ data_criteria.toSQL() +'";'
                
                println """
                  \$('#criteria').append(
                     '<tr data-id="'+ cid +'">'+
                     '<td>${data_criteria.archetypeId}</td>'+
                     '<td>${data_criteria.path}</td>'+
                     '<td>${data_criteria.rmTypeName}</td>'+
                     '<td>'+ criteria_str +'</td>'+
                     '<td>'+
                       '<div class="btn-toolbar" role="toolbar">'+
                         '<a href="#" class="removeCriteria">'+
                           '<button type="button" class="btn btn-default btn-sm">'+
                             '<span class="fa fa-minus-circle fa-fw" aria-hidden="true"></span>'+
                           '</button>'+
                         '</a>'+
                       '</div>'+
                     '</td></tr>'
                  );
                """
                 
                criteria_str = ""
             }
          }
          else
          {
             //print 'alert("datavalue");'
             queryInstance.select.each { data_get ->
                
                // Updates the UI and the query object
                println 'dom_add_selection("'+ data_get.archetypeId +'", "'+ data_get.path +'");'
             }
          }
          
          // add id of query to form
          println '$("#query_form").append(\'<input type="hidden" name="id" value="'+ queryInstance.id +'"/>\');'
          
          // show update button, hide create button
          println '$("#update_button").show();'
          println '$("#create_button").hide();'
        }
        
        // EDIT SERVER-SIDE LOGIC
        %>
        
        
        /** ***************************************************************
         * ACTIONS ASSOCIATED TO ELEMENTS OF THE UI
         */
         
        // ========================================================
        // Los registros de eventos deben estar en document.ready
        
        /**
          * Clic en [+]
          * Agregar una condicion al criterio de busqueda.
          */
         $('#addCriteria').on('click', function(e) {
    
            e.preventDefault();
            
            query_composition_add_criteria_2();
         });
          
          
         /**
          * Clic en [-]
          * Elimina un criterio de la lista de criterios de busqueda.
          */
         $(document).on("click", "a.removeCriteria", function(e) {
          
            e.preventDefault();
            
            // parent=DIV, parent.parent = TD y parent.parent.parent = TR a eliminar
           //console.log(this); // a href=#
            //console.log($(this).parent().parent().parent());
            //
            row = $(this).parent().parent().parent();
            id = row.data('id');
            row.remove(); // deletes from DOM
            
            query.remove_criteria( id ); // updates the query
         });
         
         /**
          * Clic en [+]
          * Agregar una seleccion.
          */
         $('#addSelection').click( function(e) {
          
            e.preventDefault();
            
            query_datavalue_add_selection();
         });
          
          
         /**
          * Clic en [-]
          * Elimina una seleccion de la lista.
          */
         $(document).on("click", "a.removeSelection", function(e) {
          
            e.preventDefault();
            
            row = $(this).parent().parent().parent();
           id = row.data('id');
           row.remove(); // deletes from DOM
           
           query.remove_projection( id ); // updates the query
         });
         
         // ========================================================
          
      
      
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
            show_controls(this.value);
            
            // query management
            // this needs to be here because it is needed to add_criteria and add_projection
            query.set_type(this.value);
          }
        });
        
        
        /**
         * Clic en un arquetipo de la lista de arquetipos (select[view_archetype_id])
         * Lista las paths del arquetipo en select[view_archetype_path]
         */
        $('select[name=view_archetype_id]').change(function() {
        
          var archetype_id = $(this).val(); // arquetipo seleccionado
          get_and_render_archetype_paths(archetype_id);
          
        }); // click en select view_archetype_id
        
        
        /**
         * Clic en una path de la lista (select[view_archetype_path])
         */
        $('select[name=view_archetype_path]').change(function() {
        
          var datatype = $(this).find(':selected').data('type');
          get_criteria_specs(datatype);
          
        }); // click en select view_archetype_path
        
        
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
        
        $('.btn-toolbar.bottom').hide();
      };
      
      /**
       * Clears the current test panel when the query type is changed.
       */
      var clearTest = function()
      {
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

    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="query.create.title" /></h1>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
          
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
          <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
                <%-- nombre de la query --%>
                <tr>
                  <td class="fieldcontain ${hasErrors(bean: queryInstance, field: 'name', 'error')} required">
                    <label for="name">
                      <g:message code="query.name.label" default="Name" /> *
                    </label>
                  </td>
                  <td>
                    <g:textField name="name" required="" value="${queryInstance?.name}" class="form-control input-sm" />
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
                          <li><g:message code="query.create.help_composition" /></li>
                          <li><g:message code="query.create.help_datavalue" /></li>
                        </ul>
                      </span>
                    </span>
                  </td>
                  <td>
                    <g:select name="type" from="${queryInstance.constraints.type.inList}" value="${queryInstance?.type}" valueMessagePrefix="query.type" noSelection="['': '']" class="form-control input-sm" />
                  </td>
                </tr>
             </table>
          </div>
    
          
          <!-- Aqui van los campos comunes a ambos tipos de query -->
          <div id="query_common" class="query_build">
            <div class="table-responsive">
              <table class="table table-striped table-bordered table-hover">
                  <tr>
                    <th><g:message code="query.create.attribute" /></th>
                    <th><g:message code="query.create.value" /></th>
                  </tr>
                  <tr>
                    <td><g:message code="query.create.concept" /></td>
                    <td>
                      <%-- This select is used just to create the condition or projection, is not saved in the query directly --%>
                      <g:select name="view_archetype_id" size="10" from="${concepts}" optionKey="archetypeId" optionValue="${{it.name +' ('+ it.archetypeId +')'}}"
                                 noSelection="${['':g.message(code:'query.create.please_select_concept')]}" class="form-control withsize" />
                    </td>
                  </tr>
                  <tr>
                    <td><g:message code="query.create.datapoint" /></td>
                    <td>
                      <%-- Se setean las options al elegir un arquetipo --%>
                      <select name="view_archetype_path" size="5" class="form-control withsize"></select>
                    </td>
                  </tr>
               </table>
             </div>
          </div>
            
          <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++ -->
          <!-- Campos de queryByData -->
    
          <div id="query_composition" class="query_build">
          
            <div id="composition_criteria_builder" class="form-horizontal"></div>
            
            <div class="btn-toolbar" role="toolbar">
              <a href="#" id="addCriteria">
                <button type="button" class="btn btn-default btn-md">
                  <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="query.create.addCriteria" default="Add criteria" />
                </button>
              </a>
            </div>
            
            <!--
            value puede especificarse aqui como filtro o puede ser un
            parametro de la query sino se especifica aqui.
            
            ehrUid y rangos de fechas son parametros de la query
            
            archetypeId se puede especificar como filtro (tipo de documento), 
            sino se especifica aqui puede pasarse como parametro de la query
            -->
            
            <h2><g:message code="query.create.criteria" /></h2>
             
            <!-- Indices de nivel 1 -->
            <div class="table-responsive">
             <table class="table table-striped table-bordered table-hover" id="query_setup">
                 <tr>
                    <td>
                      <g:message code="query.create.criteria.filterByDocumentType" />
                      <span class="info">
                        <asset:image src="skin/information.png" />
                         <span class="content">
                           Selecting a document type will narrow the query to get only this type of document as a result.
                         </span>
                      </span>
                    </td>
                    <td>
                      <g:select name="templateId" size="5"
                                from="${OperationalTemplateIndex.withCriteria{ projections{ property("templateId") } } }" class="form-control withsize" />
                    </td>
                 </tr>
                 <tr>
                    <td>
                      <g:message code="query.create.show_ui" />
                      <span class="info">
                        <asset:image src="skin/information.png" />
                        <span class="content">
                          <g:message code="query.create.show_ui_help" />
                        </span>
                      </span>
                    </td>
                    <td>
                      <select name="showUI" class="form-control input-sm">
                        <option value="false" selected="selected"><g:message code="default.no" /></option>
                        <option value="true"><g:message code="default.yes" /></option>
                      </select>
                    </td>
                 </tr>
                 <tr>
                    <td>
                      <g:message code="query.create.criteria_logic" />
                      <span class="info">
                        <span class="content">
                          <g:message code="query.create.criteria_logic_help" />
                        </span>
                      </span>
                    </td>
                    <td>
                      <select name="criteriaLogic" class="form-control input-sm">
                        <option value="AND" selected="selected">AND</option>
                        <option value="OR">OR</option>
                      </select>
                    </td>
                 </tr>
                 <tr>
                    <td><g:message code="query.create.default_format" /></td>
                    <td>
                      <select name="composition_format" class="form-control input-sm">
                        <option value="xml" selected="selected">XML</option>
                        <option value="json">JSON</option>
                      </select>
                    </td>
                 </tr>
               </table>
             </div>
            
            <a name="criteria"></a>
            <h3><g:message code="query.create.conditions" /></h3>
            <!-- Esta tabla almacena el criterio de busqueda que se va poniendo por JS -->
            <div class="table-responsive">
             <table class="table table-striped table-bordered table-hover" id="criteria">
                  <tr>
                    <th><g:message code="query.create.archetype_id" /></th>
                    <th><g:message code="query.create.path" /></th>
                    <th><g:message code="query.create.type" /></th>
                    <th><g:message code="query.create.criteria" /></th>
                    <th></th>
                  </tr>
               </table>
            </div>
          </div><!-- query_composition -->
            
          <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++ -->
            
          <div id="query_datavalue" class="query_build">
    
           <div class="btn-toolbar" role="toolbar">
             <a href="#" id="addSelection">
               <button type="button" class="btn btn-default btn-md">
                 <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="query.create.addProjection" default="Add projection" />
               </button>
             </a>
           </div>
           
            <h2><g:message code="query.create.filters" /></h2>
    
            <!--
            ehrUid, archetypeId (tipo de doc), rango de fechas, formato
            y agrupacion son todos parametros de la query.
            
            Aqui se pueden fijar SOLO algunos de esos parametros
            a modo de filtro.
    
            TODO: para los que no se pueden fijar aqui, inluir en la
            definicion de la query si son obligatorios o no.
            -->
            
            <div class="table-responsive">
             <table class="table table-striped table-bordered table-hover">
              <tr>
                <td><g:message code="query.create.default_format" /></td>
                <td>
                  <select name="format" class="form-control input-sm">
                    <option value="xml" selected="selected">XML</option>
                    <option value="json">JSON</option>
                  </select>
                </td>
              </tr>
              <tr>
                <td><g:message code="query.create.default_group" /></td>
                <td>
                  <select name="group" size="3" class="form-control withsize">
                    <option value="none" selected="selected"><g:message code="query.create.none" /></option>
                    <option value="composition"><g:message code="query.create.composition" /></option>
                    <option value="path"><g:message code="query.create.path" /></option>
                  </select>
                </td>
              </tr>
              </table>
           </div>
            
            <h3><g:message code="query.create.projections" /></h3>
            <!-- Esta tabla guarda la seleccion de paths de los datavalues a obtener -->
            <a name="selection"></a>
           <div class="table-responsive">
             <table class="table table-striped table-bordered table-hover" id="selection">
              <tr>
                <th><g:message code="query.create.archetype_id" /></th>
                <th><g:message code="query.create.path" /></th>
                <th></th>
              </tr>
              </table>
           </div>
            
          </div><!-- query_datavalue -->
          

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
              };
            </script>

           <div class="btn-toolbar bottom" role="toolbar">
              <a href="javascript:void(0);" onclick="javascript:toggle_test();" id="test_query">
                <button type="button" class="btn btn-default btn-md">
                  <span class="fa fa-road fa-fw" aria-hidden="true"></span> <g:message code="default.button.test.label" default="Test" />
                </button></a>
             <a href="javascript:void(0);" onclick="javascript:ajax_submit_test_or_save('save');" id="create_button">
                <button type="button" class="btn btn-default btn-md">
                  <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="default.button.create.label" default="Save" />
                </button></a>
             <a href="javascript:void(0);" onclick="javascript:ajax_submit_test_or_save('update');" id="update_button">
                <button type="button" class="btn btn-default btn-md">
                  <span class="fa fa-check fa-fw" aria-hidden="true"></span> <g:message code="default.button.update.label" default="Update" />
                </button></a>
            </div>

          
          <!-- test panel -->
          <div id="query_test">
            <g:include action="test" />
          </div>
        </g:form>
      </div>
    </div>
  </body>
</html>
