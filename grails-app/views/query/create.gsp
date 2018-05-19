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

      /* null flavour paths hidden by default */
      option.null_flavour {
        display: none;
        background-color: #eeeeee;
      }
      .criteria_value_container, span.criteria_list_add {
        margin-bottom: 5px;
      }
      .help-block {
       margin-top: 0;
       margin-bottom: 5px;
       font-size: 0.8em;
      }
    </style>

    <!-- query test -->
    <asset:stylesheet src="query_execution.css" />
    <asset:stylesheet src="jquery-ui-1.9.2.datepicker.min.css" />
    <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:stylesheet src="animate.css" />
    <asset:stylesheet src="pnotify.custom.min.css" />

    <asset:javascript src="jquery-ui-1.9.2.datepicker.min.js" />
    <asset:javascript src="jquery.form.js" /><!-- ajax form -->
    <asset:javascript src="xml_utils.js" /><!-- xmlToString -->
    <asset:javascript src="highcharts/highcharts.js" />
    <asset:javascript src="highlight.pack.js" /><!-- highlight xml and json -->

    <asset:javascript src="pnotify.custom.min.js" />

    <script type="text/javascript">
      // globals
      var session_lang = "${session.lang}"; // needed by query_test_and_execution.js
      var get_concepts_url = '${createLink(controller:"query", action:"getConcepts")}';
      var get_archetypes_in_template_url = '${createLink(controller:"query", action:"getArchetypesInTemplate")}';
      var current_criteria_spec; // set by getCriteriaSpec

      <%-- Another way to get current locale ${org.springframework.context.i18n.LocaleContextHolder.locale.language} --%>
    </script>
    <asset:javascript src="query_test_and_execution.js" />
    <!-- /query test -->

    <script type="text/javascript">

      var query = {
        id: undefined, // used for edit/update
        id_gen: 0,
        name: undefined,
        type: undefined,
        isPublic: false,
        format: undefined,
        template_id: undefined,
        criteriaLogic: undefined,
        where: [], // DataCriteria
        select: [], // DataGet
        queryGroup: undefined,
        group: 'none',
        reset:        function () {
           this.id = undefined;
           this.id_gen = 0;
           this.format = undefined;
           this.template_id = undefined;
           this.criteriaLogic = undefined;
           this.where = [];
           this.select = [];
           this.group = 'none';
        },
        set_id:       function (id) { this.id = id; }, // for edit/update
        set_type:     function (type) { this.type = type; }, // composition or datavalue
        get_type:     function () { return this.type; }, // composition or datavalue
        set_public:   function () { this.isPublic = true; },
        set_private:  function () { this.isPublic = false; },
        set_criteria_logic: function (criteriaLogic) { this.criteriaLogic = criteriaLogic; }, // composition
        set_name:     function (name) { this.name = name; },
        get_name:     function () { return this.name; },
        set_format:   function (format) { this.format = format; },
        set_query_group: function (query_group) { this.queryGroup = query_group; },
        set_group:    function (group) { this.group = group; },
        set_template_id: function (template_id) {
          if (template_id != null) this.template_id = template_id;
        },
        has_criteria: function ()
        {
           return this.where.length != 0;
        },
        has_projections: function ()
        {
           return this.select.length != 0;
        },
        add_criteria: function (archetype_id, path, rm_type_name, criteria, allow_any_archetype_version)
        {
          if (this.type != 'composition') return false;

          this.id_gen++;

          var c = {
                   cid: this.id_gen,
                   archetypeId: archetype_id,
                   path: path,
                   rmTypeName: rm_type_name,
                   class: 'DataCriteria'+rm_type_name,
                   allowAnyArchetypeVersion: allow_any_archetype_version
                  };

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
        add_projection: function (archetype_id, path, rm_type_name, allow_any_archetype_version)
        {
          if (this.type != 'datavalue') return false;

          this.id_gen++;

          this.select[this.id_gen - 1] = {
                                          pid: this.id_gen,
                                          archetype_id: archetype_id,
                                          path: path,
                                          rmTypeName: rm_type_name,
                                          allow_any_archetype_version: allow_any_archetype_version
                                         };

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

      /**
       * usage: array_clean(my_array, [null, undefined, ""])
       */
      function array_clean(arr, empty_values)
      {
        for (var i = 0; i < arr.length; i++)
        {
          if (empty_values.includes( arr[i] ))
          {
            arr.splice(i, 1);
            i--;
          }
        }
        return arr;
      };

      function Criteria(spec) {

        this.conditions = [];
        this.spec = spec;

        this.add_condition = function (attr, operand, values, negation) {

          if (values.length > 1)
            this.conditions[attr+'Value'] = values;
          else
            this.conditions[attr+'Value'] = values[0];

          this.conditions[attr+'Operand'] = operand;

          this.conditions[attr+'Negation'] = negation;
        };
      };


      // ==============================================================================================
      // SAVE OR UPDATE
      // TODO: put these methods in the query object
      // ==============================================================================================

      var save_or_update_query = function(action) {

        if (action != 'save' && action != 'update') throw "Action is not save or update";

        query.set_name($('input[name=name]').val());

        if (!query.get_name())
        {
           alert('${g.message(code:"query.create.pleaseSpecifyQueryName")}');
           return;
        }

        query.set_query_group($('select[name=queryGroup]').val());

        if (!query.has_criteria() && !query.has_projections())
        {
           if (query.get_type() == 'datavalue')
           {
              alert('${g.message(code:"query.create.emptyProjections")}');
           }
           else
           {
              alert('${g.message(code:"query.create.emptyCriteria")}');
           }
           return;
        }

        if ( $('input[name=isPublic]').is(':checked') ) query.set_public();
        else query.set_private();

        query.set_criteria_logic($('select[name=criteriaLogic]').val());

        if (query.get_type() == 'datavalue')
        {
           query.set_format( $('select[name=format]').val() );
           query.set_group( $('select[name=group]').val() ); // for datavalue query
           query.set_template_id( $('select[name=dv_templateId]').val() );
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
               //alert(JSON.stringify(data));
               location.href = '${createLink("action": "show")}?uid='+ data.uid;
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
               location.href = '${createLink("action": "show")}?uid='+ data.uid;
             })
             .fail(function(resp,status,status_msg) {

                $('body').prepend(
                   '<div class="alert alert-danger alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
                   resp.responseJSON.message +'</div>'
                );
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
        query.set_query_group($('select[name=queryGroup]').val());
        query.set_criteria_logic($('select[name=criteriaLogic]').val());
        query.set_format( $('select[name=composition_format]').val() );
        query.set_template_id( $('select[name=templateId]').val() );

        qehrId = $('select[name=qehrId]').val();
        fromDate = $('input[name=fromDate]').val();
        toDate = $('input[name=toDate]').val();
        retrieveData = $('select[name=retrieveData]').val();
        showUI = $('select[name=showUI]').val();
        format = $('select[name=composition_format]').val();
        composerUid = $('select[name=composerUid]').val();
        composerName = $('input[name=composerName]').val();

        if (showUI == 'true') format = 'html';

        var data = {
                    query: query, fromDate: fromDate, toDate: toDate,
                    retrieveData: retrieveData, showUI: showUI, format: format
                   };
        if (qehrId != null) data.qehrId = qehrId;
        if (composerUid != null) data.composerUid = composerUid;
        if (composerName != null) data.composerName = composerName;


        // removes previous alert if present
        $('#query_test_common .alert').remove();


        $.ajax({
          method: 'POST',
          url: '${createLink(controller:'rest', action:'queryCompositions')}?format='+format, // format param in url to make the withFormat work in the controller
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
             //console.log('UI');

             $('#results').html( res );
             $('#results').show('slow');
           }
           else // Si devuelve XML o JSON
           {
             //console.log('JSON OR XML');
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
        .fail(function(resp,status,status_msg) {

           //console.log(resp);
           //console.log(resp.responseXML); // XML object!
           //console.log(resp.responseXML.getElementsByTagName("result")[0]);
           //console.log(resp.responseXML.getElementsByTagName("result")[0].getElementsByTagName("message")[0]);
           //console.log(resp.responseXML.getElementsByTagName("result")[0].getElementsByTagName("message")[0].childNodes[0].nodeValue);

           //console.log(status);
           //console.log(status_msg);

           if (format == 'xml')
           {
             // show error in XML response
             $('#query_test_common').prepend(
'<div class="alert alert-danger alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
resp.responseXML.getElementsByTagName("result")[0].getElementsByTagName("message")[0].childNodes[0].nodeValue +'</div>'
             );
           }
           else
           {
              $('#query_test_common').prepend(
'<div class="alert alert-danger alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
resp.responseJSON.result.message +'</div>'
             );
           }
        });

        //console.log('after ajax submit');
      };


      var test_query_datavalue = function () {

        //console.log('test datavalue query');

        // query management
        query.set_name($('input[name=name]').val());
        query.set_query_group($('select[name=queryGroup]').val());
        query.set_format($('select[name=format]').val());
        query.set_group($('select[name=group]').val()); // for datavalue query

        query.set_template_id( $('select[name=dv_templateId]').val() );

        qehrId = $('select[name=qehrId]').val();
        fromDate = $('input[name=fromDate]').val();
        toDate = $('input[name=toDate]').val();
        format = $('select[name=format]').val(); // xml o json
        group = $('select[name=group]').val();
        composerUid = $('select[name=composerUid]').val();
        composerName = $('input[name=composerName]').val();

        var data = {
                    query: query, fromDate: fromDate, toDate: toDate,
                    format: format, group: group
                   };
        if (qehrId != null) data.qehrId = qehrId;
        if (composerUid != null) data.composerUid = composerUid;
        if (composerName != null) data.composerName = composerName;

        // removes previous alert if present
        $('#query_test_common .alert').remove();


        $.ajax({
           method: 'POST',
           url: '${createLink(controller:"rest", action:"queryData")}?format='+format, // format param in url to make the withFormat work in the controller
           contentType : 'application/json',
           dataType: format,
           data: JSON.stringify( data )
         })
         .done(function( res ) {

            // Vacia donde se va a mostrar la tabla o el chart
            $('#chartContainer').empty();

            //console.log('form_datavalue success 2');

            var code = $('#code');

            // reset code class or highlight
            code.removeClass('xml json');

            // Si devuelve JSON (verifica si pedi json)
            if (format == 'json')
            {
              //console.log('form_datavalue success json');

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
              //console.log('form_datavalue success XML');

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
         })
         .fail(function(resp,status,status_msg) {

           if (format == 'xml')
           {
             // show error in XML response
             $('#query_test_common').prepend(
'<div class="alert alert-danger alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
resp.responseXML.getElementsByTagName("result")[0].getElementsByTagName("message")[0].childNodes[0].nodeValue +'</div>'
             );
           }
           else
           {
              console.log('json', resp);

              $('#query_test_common').prepend(
'<div class="alert alert-danger alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
resp.responseJSON.result.message +'</div>'
             );
           }
         });

      }; // test_query_datavalue


      /*
       * Called from the save/update buttons
       */
      var ajax_submit_test_or_save = function (action) {

         //console.log('ajax_submit', action);

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

        //console.log('dom_add_criteria_2', $('input.value.selected', fieldset), $('input.value.selected', fieldset).length);

        // Path is not selected
        //console.log( 'check if a path is selected, value ', $('select[name=view_archetype_path]').val() );
        if ( $('select[name=view_archetype_path]').val() == null )
        {
          alert('${g.message(code:"query.create.selectDatapoint")}');
          return false;
        }

        // =======================================================================================
        // Criteria is complete? https://github.com/ppazos/cabolabs-ehrserver/issues/141
        //
        // for each value in the criteria


        // inputs or selects with values
        var criteria_fields = $(':input.value.selected', fieldset);
        var complete = true;

        if ( criteria_fields.length == 0 ) // case when no criteria spec is selected
        {
          alert('${g.message(code:"query.create.selectCriteria")}');
          return false;
        }
        else // case when criteria spec is selected and maybe some values are not filled in
        {
          $.each( criteria_fields, function (index, value_input) {

            if ( [null, undefined, ""].includes( $(value_input).val() ) )
            {
              complete = false;
              return false; // breaks each
            }
          });
        }
        if (!complete)
        {
          alert('${g.message(code:"query.create.fillCriteria")}');
          return false;
        }
        // =======================================================================================

        var archetype_id = $('select[name=view_archetype_id]').val();
        var path = $('select[name=view_archetype_path]').val();
        var name = $('select[name=view_archetype_path] option:selected').data('name');
        var type = $('select[name=view_archetype_path] option:selected').data('type');
        var spec = $('input[name=criteria]', fieldset).data('spec');
        var allow_any_archetype_version = $('input[name=allow_any_archetype_version]')[0].checked;

        //console.log('spec', spec);

        var attribute, operand, value, values, negation;
        var criteria_str = '';

        // criteria js object
        var criteria = new Criteria(spec);

        $.each( $('.criteria_attribute', fieldset), function (i, e) {

          values = [];
          attribute = $('input[name=attribute]', e).val()
          operand = $('select[name=operand]', e).val();
          negation = $('input[name=negation]', e).prop( "checked" );

          // FIXME: are we using the hidden fields?
          if (negation) criteria_str += 'NOT '

          criteria_str += attribute +' ';
          criteria_str += operand +' ';

          // for each criteria value (can be 1 for value, 2 for range or N for list)
          // the class with the name of the attribute is needed to filter values for each attribute
          $.each( $(':input.selected.value.'+attribute, e), function (j, v) {

            value = $(v).val();

            // values are not empty or null, already checked
            values.push(value);
            criteria_str += value + ', ';
          });

          criteria_str = criteria_str.substring(0, criteria_str.length-2); // remove last ', '
          criteria_str += ' AND ';

          // query object mgt
          criteria.add_condition(attribute, operand, values, negation);
        });

        criteria_str = criteria_str.substring(0, criteria_str.length-5); // remove last ' AND '


        // query object mgt
        cid = query.add_criteria(archetype_id, path, type, criteria, allow_any_archetype_version);
        //query.log();

        // shows openEHR-EHR-...* instead of .v1
        if (allow_any_archetype_version)
        {
           archetype_id = archetype_id.substr(0, archetype_id.lastIndexOf(".")) + ".*";
        }

        // shows the criteria in the UI
        $('#criteria').append(
           '<tr data-id="'+ cid +'">'+
           '<td>'+ archetype_id +'</td>'+
           '<td>'+ path +'</td>'+
           '<td>'+ name +'</td>'+
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
          new PNotify({
            title: '${g.message(code:"query.create.condition_added")}',
            text : '<a href="#criteria">${g.message(code:"query.create.verify_condition")}</a>',
            type : 'info',
            styling: 'bootstrap3',
            history: false
          });
        }
      };

      // =================================
      // /COMPO QUERY CREATE/EDIT ========
      // =================================

      // =================================
      // DATA QUERY CREATE/EDIT =========
      // =================================

      var dom_add_selection = function (archetype_id, path, name, rm_type_name, allow_any_archetype_version) {

        // query object mgt
        pid = query.add_projection(archetype_id, path, rm_type_name, allow_any_archetype_version);
        //query.log();

        // shows openEHR-EHR-...* instead of .v1
        if (allow_any_archetype_version)
        {
           if (archetype_id.match(/\.v(\d)*/) != null) // has version? if edit it won't have it
           {
              archetype_id = archetype_id.substr(0, archetype_id.lastIndexOf(".")); // removes version
           }

           archetype_id += ".*" // generic version just for the UI
        }

        // shows the projection in the UI
        $('#selection').append(
          '<tr data-id="'+ pid +'">'+
          '<td>'+ archetype_id +'</td><td>'+ path +'</td>'+
          '<td>'+ name +'</td><td>'+ rm_type_name +'</td>'+
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

        dom_add_selection(
          $('select[name=view_archetype_id]').val(),
          $('select[name=view_archetype_path]').val(),
          $('select[name=view_archetype_path] option:selected').data('name'),
          $('select[name=view_archetype_path] option:selected').data('type'),
          $('input[name=allow_any_archetype_version]')[0].checked
        );

        new PNotify({
          title: '${g.message(code:"query.create.selection_added")}',
          text : '<a href="#selection">${g.message(code:"query.create.verify_selection")}</a>',
          type : 'info',
          styling: 'bootstrap3',
          history: false
        });
      };

      // =================================
      // /DATA QUERY CREATE/EDIT =========
      // =================================

      // =================================
      // COMMON QUERY CREATE/EDIT ========
      // =================================

      var get_and_render_archetype_paths = function (template_id, archetype_id) {

        $.ajax({
          url: '${createLink(controller:"query", action:"getArchetypePaths")}',
          data: {template_id: template_id, archetypeId: archetype_id, datatypesOnly: true},
          dataType: 'json',
          success: function(data, textStatus) {

            // Saca las options que haya
            $('select[name=view_archetype_path]').empty();

            // Agrega las options con las paths del arquetipo seleccionado
            $('select[name=view_archetype_path]').append('<option value="">${g.message(code:"query.create.please_select_datapoint")}</option>');

            // Adds options to the select
            $(data).each(function(i, didx) {

              // if there is no name for the current lang, the node is from a template that is on another language, should not be on the query create.
              if (didx.name[session_lang])
              {
                $('select[name=view_archetype_path]').append(
                  '<option value="'+ didx.path +'" data-type="'+ didx.rmTypeName +'" data-name="'+ didx.name[session_lang] +'"'+
                  (didx.path.endsWith('/null_flavour')?'class="null_flavour"':'') +'>'+
                  didx.name[session_lang] +' ('+ didx.rmTypeName + ')</option>'
                );
              }
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
      var avoid_negation_for_types = ['DV_BOOLEAN', 'DV_IDENTIFIER'];
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

            //console.log('spec', spec, spec.length);

            // set global for reference from other functions
            current_criteria_spec = spec;

            $('#composition_criteria_builder').empty();

            var criteria = '';

            // spec is an array of criteria spec
            // render criteria spec
            for (s = 0; s < spec.length; s++)
            {
              aspec = spec[s];

              global_criteria_id++;


              // 1 column for the radio button that selects the criteria
              criteria += '<div class="form-group"><div class="col-sm-1">';

              //console.log(s, aspec);

              // All fields of the same criteria will have the same id in the data-criteria attribute
              if (s == 0)
                criteria += '<input type="radio" name="criteria" data-criteria="'+ global_criteria_id +'" data-spec="'+ s +'" checked="checked" />';
              else
                criteria += '<input type="radio" name="criteria" data-criteria="'+ global_criteria_id +'" data-spec="'+ s +'" />';

              // 11 columns for the criteria
              criteria += '</div><div class="col-sm-11">';


              for (attr in aspec)
              {
                criteria += '<div class="criteria_attribute row">';
                criteria += '<div class="col-sm-2">'+ attr + '<input type="hidden" name="attribute" value="'+ attr +'" /></div>';

                conditions = aspec[attr]; // spec[0][code][eq] == value

                if (avoid_negation_for_types.indexOf(datatype) < 0)
                {
                  criteria += '<div class="col-sm-1">'
                  criteria += '${message(code:"query.create.criteria.not")} <input type="checkbox" name="negation" />'
                  criteria += '</div>'
                }

                criteria += '<div class="col-sm-4">'
                criteria += '<select class="operand '+ attr +' form-control input-sm" data-criteria="'+ global_criteria_id +'" name="operand">';


                // =======================================================================================================
                // If this has possible values from the template or another source like MM.mediaTypes is a constraint.
                // After getting the values, the attribute is removed to avoid processing it as a condition.
                var possible_values;
                switch (datatype)
                {
                  case 'DV_CODED_TEXT':
                    possible_values = conditions['codes'];
                    delete conditions['codes'];
                  break;
                  case 'DV_QUANTITY':
                    possible_values = conditions['units'];
                    delete conditions['units'];
                  break;
                  case 'DV_MULTIMEDIA':
                    possible_values = conditions['mediaTypes'];
                    delete conditions['mediaTypes'];
                  break;
                  case 'DV_PARSABLE':
                    possible_values = conditions['codes'];
                    delete conditions['codes'];
                  break;
                  case 'DV_BOOLEAN':
                    possible_values = conditions['values'];
                    delete conditions['values'];
                  break;
                }

                console.log(datatype, possible_values, conditions);

                //console.log('possible values', datatype, attr, possible_values);
                //console.log('conditions', conditions);
                // =======================================================================================================


                for (cond in conditions)
                {
                  // starts with underscore is for internal use, avoid processing,
                  // used to pass list of codes to fill the condition value criteria
                  if (cond.startsWith('_'))
                  {
                     continue;
                  }
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
                  case 'DV_MULTIMEDIA':
                    if (attr == 'size') input_type = 'number';
                  break;
                }


                // indexes of operand and value should be linked.
                criteria += '<div class="criteria_value_container col-sm-5">';
                var i = 0;
                for (cond in conditions)
                {
                  //console.log('cond', cond, 'conditions[cond]', conditions[cond]);


                  // starts with underscore is for internal use, avoid processing,
                  // used to pass list of codes to fill the condition value criteria
                  if (cond.startsWith('_'))
                  {
                     continue;
                  }


                  criteria += '<span class="criteria_value '+ attr +'" data-criteria="'+ global_criteria_id +'">';

                  if (cond == 'eq_one')
                  {
                    criteria += '<select name="value" class="value'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'">';
                    for (v in conditions[cond]) // each value from the list of possible values
                    {
                      criteria += '<option value="'+ conditions[cond][v] +'">'+ conditions[cond][v] +'</option>'; // TODO: get texts for values
                    }
                    criteria += '</select>';
                  }
                  else if (cond == 'in_snomed_exp')
                  {
                    criteria += '<textarea name="value" class="value'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" style="margin:0"></textarea>';
                    criteria += '<span class="btn btn-default btn-sm validate_snomed_expression">${message(code:"query.create.criteria.in_snomed_exp.validate")}</span>';
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
                          criteria += '<span class="help-block">${message(code:"query.create.criteria.inlist.hint")}</span>';
                          criteria += '<span class="btn btn-default btn-sm criteria_list_add"><i class="fa fa-plus"></i></span>';
                          criteria += '<div class="criteria_list_container">';
                            criteria += '<input type="'+ input_type +'" name="list" class="value list'+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'" /><!-- <span class="criteria_list_add_value">[+]</span> -->';
                          criteria += '</div>';
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
                          criteria += '<span class="help-block">${message(code:"query.create.criteria.inlist.hint")}</span>';
                          criteria += '<span class="btn btn-default btn-sm criteria_list_add"><i class="fa fa-plus"></i></span>';
                          criteria += '<div class="criteria_list_container">';
                             criteria += '<select name="list" class="value list '+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'">';
                             for (k in possible_values)
                             {
                               criteria += '<option value="'+ k +'">'+ possible_values[k] +'</option>';
                             }
                             criteria += '</select>';
                          criteria += '</div>';
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

        // http://momentjs.com/docs/#/displaying/format/
        fields.datetimepicker({
          format: "YYYY-MM-DDThh:mm:ssZ",
          ignoreReadonly: true
        });

        fields.attr('readonly', true);

        // date
        fields = $('.input_date', criteria_container)

        fields.datetimepicker({
          format: "YYYY-MM-DD",
          ignoreReadonly: true,
          viewMode: 'months'
        });

        fields.attr('readonly', true);
      };


      // Validation of snomed expressions used iin criteria values
      $(document).on('click', '.validate_snomed_expression', function(evt) {

        //console.log( $(this).prev(), $(this).prev()[0].value );

        expression_container = $(this).prev();
        expression_value = expression_container[0].value;

        $.ajax({
          url: '${createLink(controller:"query", action:"validateSnomedExpression")}',
          data: {
             snomedExpr: expression_value
          },
          dataType: 'json',
          success: function(res, textStatus) {

            //console.log('res', res);
            if (res.is_valid)
            {
               //expression_container.parent().addClass('has-success');
               expression_container.addClass('alert-success');
               setTimeout(function(){
                 expression_container.removeClass('alert-success');
               }, 2500);
            }
            else
            {
               expression_container.addClass('alert-danger');
               setTimeout(function(){
                 expression_container.removeClass('alert-danger');
               }, 2500);
            }
          },
          error: function(xhr, textStatus, errorThrown) {

            console.log(xhr, textStatus, errorThrown);
          }
        });
      });


      // Saves previous values of operand selects, needed to update the criteria area
      // after using in_snomed_exp operand.
      $(document).on('focus', 'select.operand', function(evt) {
        $(this).data('previous', this.value);
      });

      // attachs onchange for operand selects created by the 'get_criteria_specs' function.
      $(document).on('change', 'select.operand', function(evt) {

        //console.log('operand change', this.selectedIndex, $(this).data('criteria'));
        //console.log('prev', $(this).data('previous'));


        // Specific code to support in_snomed_exp as criteria operand
        //console.log(this.options[this.selectedIndex].value); // eq, in_list, in_snomed_exp, ...
        //console.log(current_criteria_spec);

        if (this.options[this.selectedIndex].value == 'in_snomed_exp')
        {
           /*
              1. set terminologyId operand on the same criteria id (data-criteria) to 'eq'
              2. save/hide current criteria value select
              3. dynamically create criteria value select for terminologyID with values current_criteria_spec[spec#][terminologyId]._snomed
           */

           // 1. setear el 'eq' en este select
           //console.log( $('select[data-criteria="'+ $(this).data('criteria') +'"].operand.terminologyId') );

           // 2.
           $('select[data-criteria="'+ $(this).data('criteria') +'"].operand.terminologyId').val('eq');

           // for each operand, there is a criteria_value, I want the index of the operand eq to select the correspondent criteria_value select
           criteria_value_index = $('select[data-criteria="'+ $(this).data('criteria') +'"].operand.terminologyId')[0].selectedIndex;

           // copiar el select o esconderlo, el que corresponda con el 'eq'
           // this selects all the selects with codes for each operand of the terminologyId (eq, contains), I want to set the eq select!
           //console.log( $('select.value.terminologyId', '[data-criteria="'+ $(this).data('criteria') +'"].criteria_value')[criteria_value_index] );

           //$('select.value.terminologyId', '[data-criteria="'+ $(this).data('criteria') +'"].criteria_value')[criteria_value_index].style.border = '1px solid red';

           // selected criteria
           selected_criteria_spec_index = $('input[name=criteria]:checked', '#query_form').data('spec');

           //console.log( current_criteria_spec[selected_criteria_spec_index].terminologyId._snomed );

           possible_values = current_criteria_spec[selected_criteria_spec_index].terminologyId._snomed;

           // select to set with the terminolgy ids from snomed (can be undefined if the terminology doesnt have a list of values)
           //select_for_terminology_operand_eq = $('select.value.terminologyId', '[data-criteria="'+ $(this).data('criteria') +'"].criteria_value')[criteria_value_index];
           //select_for_terminology_operand_eq.innerHTML = '';

           select_container = $('[data-criteria="'+ $(this).data('criteria') +'"].criteria_value.terminologyId')[criteria_value_index];

           // remove current options / criteria value
           while (select_container.hasChildNodes()) {
             select_container.removeChild(select_container.lastChild);
           }

           //select_container.removeChild(select_for_terminology_operand_eq);  // remove current select
           select_for_terminology_snomed = document.createElement("select");   // new select
           select_for_terminology_snomed.className = "value terminologyId form-control input-sm selected";
           select_container.appendChild(select_for_terminology_snomed);        // add select to container

           // add options to select for snomed versions
           for (k in possible_values)
           {
             option = document.createElement("option");
             option.value = k;
             option.text = possible_values[k];
             select_for_terminology_snomed.add(option);
           }
        }
        else
        {
           // reset the criteria_value select with the initial values because in_snomed_exp is not selected.

           // Easiest way is to call again getCriteriaSpec for the current path.
           // The problem is every time the operand is changed, it does the request and render again,
           // sinece we already have the spec, the request is no needed, just the render, but the
           // render is tied to the request in get_criteria_specs, we need to separate those processes
           // like we have on query_create.js for supporting concept filters: request and render are
           // two different methods, and the render is called from the request as a callback, but
           // the render can be called and reused from anywhere passing the model to render.

           // only updated if in_snomed_exp was used
           if ($(this).data('previous') == 'in_snomed_exp')
           {
              var datatype = $('select[name=view_archetype_path]').find(':selected').data('type');
              get_criteria_specs(datatype);
           }
        }



        var criteria_value_container = $(this).parent().next();

        // All criteria values hidden
        criteria_value_container.children().css('display', 'none');
        $(':input', criteria_value_container).removeClass('selected'); // unselect the current selected criteria values

        // criteria value [i] should be displayed
        var value_container = $(criteria_value_container.children()[this.selectedIndex]).css('display', 'inline')
        $(':input', value_container).addClass('selected'); // add selected to all the inputs, textareas and selects, this is to add the correct values to the criteria

        //console.log( $('#query_form').serialize() );


        // update previous value, needed to support gui update after using in_snomed_exp
        $(this).data('previous', this.value);
      });


      // Add/Delete multiple input values for value list criteria when enter is pressed
      $(document).on('keypress', ':input.value.list', function(evt) {

        if (!evt) evt = window.event;
        var keyCode = evt.keyCode || evt.which;

        //console.log('keypress', keyCode);

        // Enter pressed on an item from an input value list (inlist criteria condition)
        if (keyCode == '13')
        {
          $(this).after( $(this).clone().val('') );
          $(this).next().focus();
          return false;
        }
      });
      $(document).on('keyup', ':input.value.list', function(evt) {

        if (!evt) evt = window.event;
        var keyCode = evt.keyCode || evt.which;

        //console.log('keyup', keyCode);

        // Backspace or delete pressed on an item from an input value list (inlist criteria condition)
        if (keyCode == '8' || keyCode == '46')
        {
          // the latest input cant be deleted
          var __parent = $(this).parent();
          //console.log(__parent.children());
          if (__parent.children().size() == 1) return;

          $(this).remove();
          return false;
        }
      });
      // same as tje key press above but with UI buttons
      $(document).on('click', '.btn.criteria_list_add', function(evt) {
        input_to_clone = $(this).next().children()[0];
        $(input_to_clone).after( $(input_to_clone).clone().val('') );
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

        $('input[name=display_null_flavour]').on('change', function(evt) {

          if (this.checked)
          {
            $('option.null_flavour').css('display', 'block');
          }
          else
          {
            $('option.null_flavour').css('display', 'none');
          }
        });

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

          println '$("select[name=group]").val("'+ queryInstance.group +'");'
          println '$("select[name=criteriaLogic]").val("'+ queryInstance.criteriaLogic +'");'
          println '$("select[name=queryGroup]").val("'+ queryInstance.queryGroup?.uid +'");'

          println 'query.set_id("'+ queryInstance.id +'");'
          println 'query.set_name("'+ queryInstance.name +'");'
          println 'query.set_query_group("'+ queryInstance.queryGroup?.uid +'");'
          println 'query.set_type("'+ queryInstance.type +'");'

          if (queryInstance.isPublic)
            println 'query.set_public();'

          println 'query.set_format("'+ queryInstance.format +'");'
          println 'query.set_group("'+ queryInstance.group +'");'
          println 'query.set_criteria_logic("'+ queryInstance.criteriaLogic +'");'

          if (queryInstance.templateId)
            println 'query.set_template_id("'+ queryInstance.templateId +'");'


          if (queryInstance.type == 'composition')
          {
             println '$("select[name=templateId]").val("'+ queryInstance.templateId +'");'

             // similar code to dom_add_criteria_2 in JS

             def attrs, attrValueField, attrOperandField, attrNegationField, value, operand, name
             println 'var criteria;'

             queryInstance.where.each { data_criteria ->

                attrs = data_criteria.criteriaSpec(data_criteria.archetypeId, data_criteria.path)[data_criteria.spec].keySet() // attribute names of the datacriteria

                println 'criteria = new Criteria('+ data_criteria.spec +');'

                attrs.each { attr ->

                   attrValueField = attr + 'Value'
                   attrOperandField = attr + 'Operand'
                   attrNegationField = attr + 'Negation'
                   operand = data_criteria."$attrOperandField"
                   value = data_criteria."$attrValueField"

                   // DV_BOOLEAN doesn't have negation, just checking
                   if (data_criteria.hasProperty(attrNegationField))
                      negation = data_criteria."$attrNegationField"

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
                            ( value.collect{ it.format(grailsApplication.config.app.l10n.ext_datetime_utcformat_nof) } as JSON ) +', '+
                            negation +');'
                      }
                      else
                      {
                         println 'criteria.add_condition("'+
                            attr +'", "'+
                            operand +'", '+
                            ( value.collect{ it.toString() } as JSON ) +', '+ // toString to have the items with quotes on JSON, without the quotes I get an error when saving/binding the uptates to criterias.
                            negation +');'
                      }
                   }
                   else // value is an array of 1 element
                   {
                      // FIXME: if the value is not string or date, dont include the quotes
                      println 'criteria.add_condition("'+
                         attr +'", "'+
                         operand +'", [ "'+
                         value +'"], '+
                         negation +');'
                   }

                } // each attr


                println 'cid = query.add_criteria("'+
                  data_criteria.archetypeId +'", "'+
                  data_criteria.path +'", "'+
                  data_criteria.rmTypeName +'", '+
                  'criteria,'+
                  data_criteria.allowAnyArchetypeVersion +');'

                println 'var criteria_str = "'+ data_criteria.toGUI() +'";'

                // shows openEHR-EHR-...* instead of .v1
                def archetype_id = data_criteria.archetypeId
                if (data_criteria.allowAnyArchetypeVersion)
                {
                   archetype_id = archetype_id +".*";
                }

                name = data_criteria.indexItem.name[session.lang]

                println """
                  \$('#criteria').append(
                     '<tr data-id="'+ cid +'">'+
                     '<td>${archetype_id}</td>'+
                     '<td>${data_criteria.path}</td>'+
                     '<td>${name}</td>'+
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
          else // datavalue
          {
             println '$("select[name=dv_templateId]").val("'+ queryInstance.templateId +'");'

             def name
             queryInstance.select.each { data_get ->

                name = data_get.indexItem.name[session.lang]

                // Updates the UI and the query object
                println 'dom_add_selection("'+ data_get.archetypeId +'", "'+ data_get.path +'", "'+ name +'", "'+ data_get.rmTypeName +'", '+ data_get.allowAnyArchetypeVersion +');'
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
          //console.log($('.content', $(this).parent()));
          $('.content', $(this).parent()).toggle('slow');
        });


        /*
         * Change del tipo de consulta. Muestra campos dependiendo del tipo de consulta a crear.
         */
        $('select[name=type]').change( function() {

          // If the query has some data and the user doesn't confirm, the type should not be changed
          if ((query.has_criteria() || query.has_projections()) && !confirm('Changing the query type will delete the current projections or criteria. Are you sure do you want to change the type?'))
          {
             //console.log('cancel type change', this, $('.last-selected', this));
             $(this).val( $('.last-selected', this).val() ); // selects the previous value, so the select doesnt change
             return false;
          }

          // last selected is removed from the previous selected option
          $('.last-selected', this).removeClass('last-selected');

          // last selected is added to the current select
          $('option:selected', this).addClass('last-selected');

          // Limpia las tablas de criterios y seleccion cuando
          // se cambia el tipo de la query para evitar errores.
          clearCriteriaAndSelection();
          clearTest();

          // La class query_build marca las divs con campos para crear consultas,
          // tanto los comunes como los particulares para busqueda de compositions
          // o de data_values
          $('.query_build').hide();


          // Id the query has some data, it is deleted to the clean instance of query.
          query.reset();

          if (this.value != '')
          {
            show_controls(this.value);

            // query management
            // this needs to be here because it is needed to add_criteria and add_projection
            query.set_type(this.value);
          }

          // https://github.com/ppazos/cabolabs-ehrserver/issues/728
          // reset results if a previous test was executed
          $('#results').empty();
          $('#chartContainer').empty();
          var code = $('#code');
          code.removeClass('xml json');
          code.empty();
          code.hide();
        });


        /**
         * Clic en un arquetipo de la lista de arquetipos (select[view_archetype_id])
         * Lista las paths del arquetipo en select[view_archetype_path]
         */
        $('select[name=view_archetype_id]').change(function() {

          // clean the current criteria if the user defined it for another archetype/path
          $('#composition_criteria_builder').empty();

          var archetype_id = $(this).val(); // arquetipo seleccionado
          var template_id  = $('#view_template_id').val();
          get_and_render_archetype_paths(template_id, archetype_id);

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
            alert('${g.message(code:"query.create.missingCriteria")}');
            return false;
          }

          return true;
        }

        if (type == 'datavalue')
        {
          if ($('tr', '#selection').length == 1)
          {
            alert('${g.message(code:"query.create.missingProjections")}');
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
        <g:if test="${['create', 'save'].contains(actionName)}">
          <h1><g:message code="query.create.title" /></h1>
        </g:if>
        <g:else>
          <h1><g:message code="query.edit.title" /></h1>
        </g:else>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">

        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div>
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
              <tr>
                <td class="fieldcontain ${hasErrors(bean: queryInstance, field: 'name', 'error')} required">
                  <label for="name"><g:message code="query.show.name.attr" default="Name" /> *</label>
                </td>
                <td>
                  <g:textField name="name" required="" value="${queryInstance?.name}" class="form-control input-sm" />
                </td>
              </tr>
              <tr>
                <td class="fieldcontain ${hasErrors(bean: queryInstance, field: 'type', 'error')}">
                  <label for="type"><g:message code="query.show.type.attr" default="Type" /></label>
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
              <tr>
                <td class="fieldcontain ${hasErrors(bean: queryInstance, field: 'type', 'error')}">
                  <label for="type"><g:message code="query.show.queryGroup.attr" default="Query Group" /></label>
                </td>
                <td>
                  <g:select name="queryGroup" from="${queryGroups}" value="${queryInstance?.queryGroup}"
                            optionKey="uid" optionValue="name"
                            noSelection="['': '']" class="form-control input-sm" />
                </td>
              </tr>
              <sec:ifAnyGranted roles="ROLE_ADMIN">
                <tr>
                  <td>
                    <label for="isPublic">
                      <g:message code="query.show.isPublic.attr" default="Is public?" /> *
                    </label>
                  </td>
                  <td>
                    <g:checkBox name="isPublic" value="${queryInstance.isPublic}" />
                  </td>
                </tr>
              </sec:ifAnyGranted>

            </table>
          </div>


          <%-- campos comunes a ambos tipos de query --%>
          <div id="query_common" class="query_build">
            <div class="table-responsive">
              <table class="table table-striped table-bordered table-hover">
                  <tr>
                    <td>
                      <g:message code="query.create.templates" /><br/>
                      <span class="info">
                        <asset:image src="skin/information.png" />
                        <span class="content">
                          <g:message code="query.create.conceptInfo" args="[session.lang]" />
                        </span>
                      </span>
                    </td>
                    <td>
                      <%--
                      <select id="view_template_id" class="form-control" size="10">
                        <option value=""><g:message code="query.create.templates.select_template" /></option>
                      </select>
                      --%>
                      <g:select name="view_template_id"
                                size="10"
                                from="${templateIndexes}"
                                noSelection="['':message(code:"query.create.templates.select_template")]"
                                optionValue="${{it.templateId +' (v'+ it.versionNumber +')'}}"
                                optionKey="templateId"
                                class="form-control withsize"/>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <g:message code="query.create.concept" />
                    </td>
                    <td>
                      <select id="view_archetype_id" name="view_archetype_id" size="10" class="form-control withsize"></select>
                      <label><input type="checkbox" name="allow_any_archetype_version" /> <g:message code="query.create.allowAnyArchetypeVersion" /></label>
                    </td>
                  </tr>
                  <tr>
                    <td><g:message code="query.create.datapoint" /></td>
                    <td>
                      <%-- Se setean las options al elegir un arquetipo --%>
                      <select name="view_archetype_path" id="view_archetype_path" size="10" class="form-control withsize">
                        <option><g:message code="query.create.please_select_datapoint" /></option>
                      </select>
                      <div>
                        <label><input type="checkbox" name="display_null_flavour" /> <g:message code="query.create.label.displayNullFlavours" /></label>
                      </div>
                    </td>
                  </tr>
               </table>
             </div>
          </div>

          <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++ -->
          <!-- Campos de queryByData -->

          <div id="query_composition" class="query_build">

            <h2><g:message code="query.create.criteria_builder"/></h2>

            <%-- conditions depending on the data type selected from path --%>
            <div id="composition_criteria_builder" class="form-horizontal">
              <g:message code="query.create.select_concept_and_data" />
            </div>

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

            <a name="criteria"></a>
            <h3><g:message code="query.create.criteria" /></h3>

            <!-- Esta tabla almacena el criterio de busqueda que se va poniendo por JS -->
            <div class="table-responsive">
              <table class="table table-striped table-bordered table-hover" id="criteria">
                <tr>
                  <th><g:message code="query.create.archetype_id" /></th>
                  <th><g:message code="query.create.path" /></th>
                  <th><g:message code="query.create.name" /></th>
                  <th><g:message code="query.create.type" /></th>
                  <th><g:message code="query.create.criteria" /></th>
                  <th></th>
                </tr>
              </table>
            </div>

            <h2><g:message code="query.create.parameters" /></h2>

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
                    <%-- If two OPTs generated from the same template are uploaded, here we can have the samte templateId twice --%>
                    <g:select name="templateId"
                              size="5"
                              from="${templateIndexes}"
                              optionValue="${{it.templateId +' (v'+ it.versionNumber +')'}}"
                              optionKey="templateId"
                              class="form-control withsize"/>
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
                     <option value="AND" selected="selected"><g:message code="query.create.criteriaAND" /></option>
                     <option value="OR"><g:message code="query.create.criteriaOR" /></option>
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
          </div><!-- query_composition -->

          <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++ -->

          <div id="query_datavalue" class="query_build">

            <h2><g:message code="query.create.dataprojection" /></h2>

            <g:message code="query.create.select_concept_and_data_projection" />

            <div class="btn-toolbar" role="toolbar">
              <a href="#" id="addSelection">
                <button type="button" class="btn btn-default btn-md">
                  <span class="fa fa-plus-circle fa-fw" aria-hidden="true"></span> <g:message code="query.create.addProjection" default="Add projection" />
                </button>
              </a>
            </div>

            <h3><g:message code="query.create.projections" /></h3>
            <!-- Esta tabla guarda la seleccion de paths de los datavalues a obtener -->
            <a name="selection"></a>
            <div class="table-responsive">
              <table class="table table-striped table-bordered table-hover" id="selection">
                <tr>
                  <th><g:message code="query.create.archetype_id" /></th>
                  <th><g:message code="query.create.path" /></th>
                  <th><g:message code="query.create.name" /></th>
                  <th><g:message code="query.create.type" /></th>
                  <th></th>
                </tr>
              </table>
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
                    <%-- If two OPTs generated from the same template are uploaded, here we can have the samte templateId twice --%>
                    <g:select name="dv_templateId"
                              size="5"
                              from="${templateIndexes}"
                              optionValue="${{it.templateId +' (v'+ it.versionNumber +')'}}"
                              optionKey="templateId"
                              class="form-control withsize"/>
                  </td>
                </tr>
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
                <span class="fa fa-road fa-fw" aria-hidden="true"></span> <g:message code="query.create.test" default="Test" />
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

    <asset:javascript src="query_create.js" />
  </body>
</html>
