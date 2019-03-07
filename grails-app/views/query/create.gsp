<%@ page import="com.cabolabs.ehrserver.query.Query" %><%@ page import="grails.converters.JSON" %><%@ page import="com.cabolabs.ehrserver.ehr.clinical_documents.OperationalTemplateIndex" %><%@ page import="com.cabolabs.util.QueryUtils" %>
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

      /* displays criteria_builder GUI of ul/li as a tree */
      #criteria_builder, #criteria_builder ul, #criteria_builder li {
        position: relative;
      }
      #criteria_builder li {
        padding-bottom: 10px;
      }
      #criteria_builder ul {
        list-style: none;
        padding-left: 32px;
      }
      #criteria_builder li::before, #criteria_builder li::after {
        content: "";
        position: absolute;
        left: -20px;
      }
      #criteria_builder li::before {
        border-top: 1px solid #000;
        top: 9px;
        width: 15px;
        height: 0;
      }
      #criteria_builder li::after {
        border-left: 1px solid #000;
        height: 100%;
        width: 0px;
        top: 2px;
      }
      #criteria_builder ul > li:last-child::after {
        height: 8px;
      }
      #criteria_builder table {
        margin: 0;
      }
      /* removes the tree from the root nodes */
      #criteria_builder > ul > li::before, #criteria_builder > ul > li::after {
        border: 0;
      }

      .datavalue {
        /*background: lime;*/
        font-weight: bold;
      }
    </style>

    <!-- query test -->
    <asset:stylesheet src="query_execution.css" />
    <asset:stylesheet src="highlightjs/xcode.css" />
    <asset:stylesheet src="animate.css" />
    <asset:stylesheet src="pnotify.custom.min.css" />

    <asset:javascript src="jquery.form.js" /><!-- ajax form -->
    <asset:javascript src="xml_utils.js" /><!-- xmlToString -->
    <asset:javascript src="highcharts/highcharts.js" />
    <asset:javascript src="highlight.pack.js" /><!-- highlight xml and json -->

    <asset:javascript src="pnotify.custom.min.js" />

    <script type="text/javascript">
      // globals
      var session_lang = "${session.lang}"; // needed by query_test_and_execution.js
      var get_concepts_url = '${createLink(controller:"query", action:"getConcepts")}';
      //var get_archetypes_in_template_url = '${createLink(controller:"query", action:"getArchetypesInTemplate")}';
      //var get_archetypes_in_template_url = '${createLink(controller:"query", action:"getTemplateJson")}';
      var get_archetypes_in_template_url = '${createLink(controller:"query", action:"getArchetypesInTemplate2")}';
      var current_criteria_spec; // set by getCriteriaSpec

      <%-- Another way to get current locale ${org.springframework.context.i18n.LocaleContextHolder.locale.language} --%>
    </script>
    <asset:javascript src="query_test_and_execution.js" />
    <!-- /query test -->

    <script type="text/javascript">

      // temporal holder for invalid complex criteria while it is edited and becomes valid
      var criteria_builder = {
        items: [], // list of trees, each tree is a sub-expression
        id_gen: 0,

        // the name param is optional and passed only on the edit while rendering the criteria
        // is needed to show the name of the path for each single criteria
        add_criteria: function (archetype_id, path, rm_type_name, criteria, allow_any_archetype_version, name)
        {
          this.id_gen++;

          var c = Object.assign({
                   _type: 'COND',
                   cid: this.id_gen,
                   archetypeId: archetype_id,
                   path: path,
                   rmTypeName: rm_type_name,
                   class: 'DataCriteria'+rm_type_name,
                   allowAnyArchetypeVersion: allow_any_archetype_version,
                   name: name
                 }, criteria.conditions); // copies the attributes in conditions to the c object

          // copy other attributes
          c['spec'] = criteria.spec;
          c['attributes'] = criteria.attributes; // array of names of attrs in conditions

          this.items.push( c );
          //this.where[this.id_gen - 1] = c; // uses the id as index, but -1 to start in 0

          // when items are removed and then added, there are undefined entries in the array
          // this cleans the undefined items so the server doesnt receive empty values.
          //this.where = this.where.filter(function(n){ return n != undefined });

          return this.id_gen;
        },
        add_complex_criteria: function (criteria_1_id, criteria_2_id, operator) // operator == AND | OR
        {
          // get c1 and c2 from root
          var c1 = this.items.find(function(e){ return e.cid == criteria_1_id });
          var c2 = this.items.find(function(e){ return e.cid == criteria_2_id });

          this.id_gen++;

          // create and/or complex criteria item
          var ccriteria = {
            _type: operator,
            left: c1,
            right: c2,
            cid: this.id_gen
          }

          // remove c1 and c2 from root
          var c1idx = this.items.findIndex( function(e){ return e.cid == criteria_1_id } );
          this.items.splice(c1idx, 1);
          var c2idx = this.items.findIndex( function(e){ return e.cid == criteria_2_id } );
          this.items.splice(c2idx, 1);

          // add and criteria item to root
          this.items.push(ccriteria);

          return this.id_gen;
        },
        is_valid: function (criteria_1_id, criteria_2_id)
        {
          return this.items.length <= 1; // no criteria is also a valid criteria
        },
        remove_criteria: function (criteria_id) // removes complex or simple criteria on root, is complex, children are added to root
        {
          //var c = this.items.find(function(e){ return e.cid == criteria_id });
          var cidx = this.items.findIndex( function(e){ return e.cid == criteria_id } );
          var c = this.items[cidx];

          this.items.splice(cidx, 1); // remove criteria

          if (c._type != 'COND') // complex criteria
          {
            this.items.push(c.left); // add criteria children to root
            this.items.push(c.right);
          }
        },
        get_criteria_tree: function()
        {
          if (!this.is_valid()) throw "Can't return invalid criteria from criteria_builder"; // TOOD: I18N
          return this.items[0];
        },
        render: function(container_id) // displays the create/edit DOM for the current state of the criteria builder
        {
          var container = $(container_id)
          var ul = container.find('> ul');

          //console.log('ul', ul);

          // since items can contain many trees, the recursive render is for each tree
          // this happens while the criteria builder contains a non valid criteria
          // when the criteria is valid, there is only one tree
          this.items.forEach(function(criteria, idx){
            criteria_builder.render_recursive(criteria, ul);
          });

          // show checkboxes only at root level
          ul.find('> li > :checkbox').show();
        },
        criteria_to_string: function(criteria)
        {
          /* criteria
          {
            "_type": "COND",
            "cid": 1,
            "archetypeId": "openEHR-EHR-OBSERVATION.test_all_datatypes.v1",
            "path": "/data[at0001]/events[at0002]/data[at0003]/items[at0011]/value",
            "rmTypeName": "DV_COUNT",
            "class": "DataCriteriaDV_COUNT",
            "allowAnyArchetypeVersion": false,
            "magnitudeValue": "33",
            "magnitudeOperand": "eq",
            "magnitudeNegation": false,
            "spec": 0,
            "attributes": [
              "magnitude"
            ]
          }

          result "magnitude eq 33"
          */
          var criteria_str = "";
          criteria.attributes.forEach(function(attr, idx){

            var valueAttr = attr+'Value'
            var operantAttr = attr+'Operand'
            var negationAttr = attr+'Negation'
            if (criteria[negationAttr]) criteria_str += 'NOT '

            criteria_str += attr +' ';
            criteria_str += criteria[operantAttr] +' ';

            // value can be an array
            var values = criteria[valueAttr];
            if (Array.isArray(values))
            {
              values.forEach(function(val, idx){
                criteria_str += val + ', ';
              });

              criteria_str = criteria_str.slice(0, -2);
            }
            else // single value
            {
              criteria_str += values;
            }

            criteria_str += ' AND ';
          });

          criteria_str = criteria_str.slice(0, -5);

          return criteria_str;
        },
        render_recursive: function(criteria, parent_ul)
        {
          if (criteria._type == 'COND')
          {
            // note, the checkbox should be hidden for all but the items at root
            parent_ul.append(
              '<li><input type="checkbox" name="criteria_builder_element_selector" data-cid="'+ criteria.cid +'" style="display:none" />'+
              '<table class="table table-striped table-bordered table-hover" data-id="'+ criteria.cid +'">'+
                '<tr>'+
                '<th><g:message code="query.create.archetype_id" /></th>'+
                '<th><g:message code="query.create.path" /></th>'+
                '<th><g:message code="query.create.name" /></th>'+
                '<th><g:message code="query.create.type" /></th>'+
                '<th><g:message code="query.create.criteria" /></th>'+
                '</tr>'+
                '<tr>'+
                  '<td>'+ criteria.archetypeId +'</td>'+
                  '<td>'+ criteria.path +'</td>'+
                  '<td>'+ criteria.name +'</td>'+
                  '<td>'+ criteria.rmTypeName +'</td>'+
                  '<td>'+ this.criteria_to_string(criteria) +'</td>'+
                '</tr>'+
              '</table></li>'
            );
          }
          else // complex criteria, follows the recursion
          {
            var binary_cond_ui = $('<li><input type="checkbox" name="criteria_builder_element_selector" data-cid="'+ criteria.cid +'" style="display:none" /><span>'+ criteria._type +'</span></li>');
            var subcriteria_ui = $('<ul/>');

            // these append the generated li to the provided ul
            this.render_recursive(criteria.left, subcriteria_ui);
            this.render_recursive(criteria.right, subcriteria_ui);

            binary_cond_ui.append(subcriteria_ui);
            parent_ul.append(binary_cond_ui);
          }
        }
      };

      // criteria builder UI events

      // on root checkbox selector click, if two are selected, enable AND/OR add
      $(document).on('click', 'input[type=checkbox]', function(e) {

        var checked = $('#criteria_builder input:checked:visible').length;

        if ( checked == 2 )
        {
          //console.log($('#criteria_builder_add_and'), $('#criteria_builder_add_or'));
          $('#criteria_builder_add_and')[0].disabled = false;
          $('#criteria_builder_add_or')[0].disabled = false;
        }
        else // disable if enabled
        {
          $('#criteria_builder_add_and')[0].disabled = true;
          $('#criteria_builder_add_or')[0].disabled = true;
        }

        if (checked == 1)
        {
          $('#criteria_builder_remove_criteria')[0].disabled = false;
        }
        else
        {
          $('#criteria_builder_remove_criteria')[0].disabled = true;
        }
      });

      // This handler process clicks on +AND +OR buttons and handles UI and model
      // changes. event.data.type has value AND/OR
      var add_criteria_item_handler = function (event)
      {
        var c1_dom = $('#criteria_builder input:checked')[0];
        var c2_dom = $('#criteria_builder input:checked')[1];

        // updates model
        var cid = criteria_builder.add_complex_criteria($(c1_dom).data('cid'), $(c2_dom).data('cid'), event.data.type);


        // update dom
        // remove criteria items UI from DOM
        var li_c1 = $(c1_dom).parent().detach();
        var li_c2 = $(c2_dom).parent().detach();

        // uncheck and hide checkboxes from li_cx
        // will be used later if the AND/OR is deleted, to know the criteria id from it's data-cid attribute
        // and will be shown again when the AND/OR containing these criteria's is removed
        $('[type=checkbox]', li_c1).prop('checked', false).hide(); //.detach();
        $('[type=checkbox]', li_c2).prop('checked', false).hide(); //.detach();

        // first li is the criteria item for the root ul
        var binary_cond_ui = $('<li><input type="checkbox" name="criteria_builder_element_selector" data-cid="'+ cid +'" /><span>'+ event.data.type +'</span></li>');
        var subcriteria_ui = $('<ul/>');
        subcriteria_ui.append(li_c1);
        subcriteria_ui.append(li_c2);
        binary_cond_ui.append(subcriteria_ui);
        $('#criteria_builder > ul').append(binary_cond_ui);

        // AND/OR buttons back to disabled
        $('#criteria_builder_add_and')[0].disabled = true;
        $('#criteria_builder_add_or')[0].disabled = true;
      };

      // these events should be binded on ready because this JS runs before the DOM is ready
      $(function() {
        $('#criteria_builder_add_and').on('click', {type:'AND'}, add_criteria_item_handler);
        $('#criteria_builder_add_or').on('click', {type:'OR'}, add_criteria_item_handler);
      });

      var query = {
        id: undefined, // used for edit/update
        id_gen: 0,
        name: undefined,
        type: undefined,
        isPublic: false,
        isCount: false,
        format: undefined,
        template_id: undefined,
        where: undefined, //[], // DataCriteria
        select: [], // DataGet
        queryGroup: undefined,
        group: 'none',
        reset:        function () {
           this.id = undefined;
           this.id_gen = 0;
           this.format = undefined;
           this.template_id = undefined;
           this.where = [];
           this.select = [];
           this.group = 'none';
        },
        set_id:       function (id) { this.id = id; }, // for edit/update
        set_type:     function (type) { this.type = type; }, // composition or datavalue
        get_type:     function () { return this.type; }, // composition or datavalue
        set_public:   function () { this.isPublic = true; },
        set_private:  function () { this.isPublic = false; },
        set_is_count: function (is_count) { this.isCount = is_count; },
        set_name:     function (name) { this.name = name; },
        get_name:     function () { return this.name; },
        set_format:   function (format) { this.format = format; },
        set_query_group: function (query_group) { this.queryGroup = query_group; },
        set_group:    function (group) { this.group = group; },
        set_template_id: function (template_id) {
          if (template_id != null) this.template_id = template_id;
        },
        set_criteria: function (criteria_tree) // set from criteria builder
        {
           return this.where = criteria_tree;
        },
        has_criteria: function ()
        {
           return this.where != undefined;
        },
        has_projections: function ()
        {
           return this.select.length != 0;
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
        this.attributes = [];
        this.spec = spec;

        this.add_condition = function (attr, operand, values, negation) {

          if (values.length > 1)
            this.conditions[attr+'Value'] = values;
          else
            this.conditions[attr+'Value'] = values[0];

          this.conditions[attr+'Operand'] = operand;
          this.conditions[attr+'Negation'] = negation;
          this.attributes.push(attr); // needed for JS render of the conditions
        };
      };


      // ==============================================================================================
      // SAVE OR UPDATE
      // TODO: put these methods in the query object
      // ==============================================================================================

      var save_or_update_query = function(action) {

        // does validation first
        if (action != 'save' && action != 'update') throw "Action is not save or update";

        query.set_name($('input[name=name]').val());

        if (!query.get_name())
        {
           alert('${g.message(code:"query.create.pleaseSpecifyQueryName")}'); // TODO: bootstrap alerts
           return;
        }

        if (query.get_type() == 'composition')
        {
          if (!criteria_builder.is_valid())
          {
            alert('${g.message(code:"query.create.invalidCriteria")}'); // TODO: bootstrap alerts
            return;
          }

          // set criteria only for composition queries
          var criteria = criteria_builder.get_criteria_tree();
          query.set_criteria(criteria);
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

           query.set_is_count( $('input[name=isCount]').is(':checked') );
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

        if (!criteria_builder.is_valid())
        {
          alert('${g.message(code:"query.create.invalidCriteria")}'); // TODO: bootstrap alerts
          return;
        }

        var criteria = criteria_builder.get_criteria_tree();
        query.set_criteria(criteria);

        // query management
        query.set_name($('input[name=name]').val());
        query.set_query_group($('select[name=queryGroup]').val());
        query.set_format( $('select[name=composition_format]').val() );
        query.set_template_id( $('select[name=templateId]').val() );

        query.set_is_count( $('input[name=isCount]').is(':checked') );

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

              // Rendering the table for group by composition doesnt depend if there is a qehrId value
              if ($('select[name=group]').val() == 'composition')
              {
                queryDataRenderTable(res);
              }

              if (qehrId != null)
              {
                if ($('select[name=group]').val() == 'path')
                {
                  queryDataRenderChart(res);
                }
              }
              else
              {
                // chart grouped by ehr?
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

         if (action == 'save' || action == 'update')
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

        // FIXME: Criteria should have a function to generate the criteria_str from
        // it's data, so other functions can use it, not

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


        cid = criteria_builder.add_criteria(archetype_id, path, type, criteria, allow_any_archetype_version);


        // shows openEHR-EHR-...* instead of .v1
        if (allow_any_archetype_version)
        {
           archetype_id = archetype_id.substr(0, archetype_id.lastIndexOf(".")) + ".*";
        }

        // displays simple criteria on criteria builder component
        // the selector on the li element is to select for adding AND/OR
        /*
        ul < criteria builder
          li < criteria item <<<< inserting item here as a table (criteria item single)
            table < criteria item single
            ul < criteria item and / or
              li < and left (criteria item, single or and/or)
              li < and right (criteria item, single or and/or)
        */
        $('#criteria_builder > ul').append(
          '<li><input type="checkbox" name="criteria_builder_element_selector" data-cid="'+ cid +'" />'+
          '<table class="table table-striped table-bordered table-hover" data-id="'+ cid +'">'+
            '<tr>'+
            '<th><g:message code="query.create.archetype_id" /></th>'+
            '<th><g:message code="query.create.path" /></th>'+
            '<th><g:message code="query.create.name" /></th>'+
            '<th><g:message code="query.create.type" /></th>'+
            '<th><g:message code="query.create.criteria" /></th>'+
            '</tr>'+
            '<tr>'+
              '<td>'+ archetype_id +'</td>'+
              '<td>'+ path +'</td>'+
              '<td>'+ name +'</td>'+
              '<td>'+ type +'</td>'+
              '<td>'+ criteria_str +'</td>'+
            '</tr>'+
          '</table></li>'
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
      var is_dv = function(rm_type_name){
        // FIXME: missing dvs!
        return ['DV_TEXT', 'DV_CODED_TEXT', 'DV_DATE', 'DV_DATE_TIME', 'DV_TIME',
                'DV_COUNT', 'DV_QUANTITY', 'DV_PROPORTION', 'DV_ORDINAL', 'DV_DURATION',
                'DV_BOOLEAN', 'DV_IDENTIFIER', 'DV_PARSABLE', 'DV_MULTIMEDIA',
                'String'].includes(rm_type_name);
      };

      var process_archetype_obj = function(obj, level, select)
      {
        //console.log(obj.rm_type_name);

        // primitive don't have attributes and their constraints are included in the criteria already, no need to add their path
        if (obj.type == "C_PRIMITIVE_OBJECT")
        {
          return;
        }

        select.append(
          /*
          '<option value="'+ didx.path +'" data-type="'+ didx.rmTypeName +'" data-name="'+ didx.name[session_lang] +'"'+
          (didx.path.endsWith('/null_flavour')?'class="null_flavour"':'') +'>'+
          didx.name[session_lang] +' ('+ didx.rmTypeName + ')</option>'
          */
          '<option value="'+ obj.path +'" data-type="'+ obj.rm_type_name +'" '+
                   (is_dv(obj.rm_type_name) ? 'class="datavalue '+ (obj.path.endsWith('/null_flavour')?'null_flavour':'') +'"' : (obj.path.endsWith('/null_flavour')?'class="null_flavour"':'')) +
                   'data-name="'+ obj.text +'">'+
            '\xA0'.repeat(level+(level > 0 ? level : 0)) + (level > 0 ? '\u2514 ' : '') + obj.text +' ('+ obj.rm_type_name +')</option>'
        );

        // avoid further processing for DVs since the criteria includes those fields, there is no need to select a subpath of a DV
        if (is_dv(obj.rm_type_name)) return;

        obj.attributes.forEach(function(attr){
          process_archetype_attr(attr, level+1, select);
        });
      };
      var process_archetype_attr = function(attr, level, select)
      {
        attr.children.forEach(function(obj){
          //console.log(obj.type);
          // do not process archetype roots since current process is for the current archetypes not for children
          // do not process slots because are not supported on the query
          if (obj.type != "C_ARCHETYPE_ROOT" && obj.type != "ARCHETYPE_SLOT")
          {
            process_archetype_obj(obj, level, select);
          }
        });
      };

      var get_and_render_archetype_paths = function (template_id, archetype_id) {

        $.ajax({
          url: '${createLink(controller:"query", action:"getArchetypePaths2")}',
          data: {template_id: template_id, archetypeId: archetype_id, datatypesOnly: true},
          dataType: 'json',
          success: function(data, textStatus) {

            //console.log(data);
            var level = 0;
            var select = $('select[name=view_archetype_path]');

            // Saca las options que haya
            select.empty();

            // Agrega las options con las paths del arquetipo seleccionado
            select.append('<option value="">${g.message(code:"query.create.please_select_datapoint")}</option>');

            process_archetype_obj(data, level, select);

            /*
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
            */
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
                  case 'DV_ORDINAL':
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

                //console.log(datatype, possible_values, conditions);
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
                      console.log(datatype, conditions[cond]);
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
                          // this case happens only for DV_ORDINAL, where the value is a number, and we show a list
                          // of possible values then between min and max should be the list of values.
                          criteria += '<select name="value" class="value min '+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'">';
                          for (k in possible_values)
                          {
                            criteria += '<option value="'+ k +'">'+ possible_values[k] +'</option>';
                          }
                          criteria += '</select>';

                          criteria += '..';

                          criteria += '<select name="value" class="value max '+ ((i==0)?' selected':'') +' '+ attr +' form-control input-sm '+ class_type +'">';
                          for (k in possible_values)
                          {
                            criteria += '<option value="'+ k +'">'+ possible_values[k] +'</option>';
                          }
                          criteria += '</select>';
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
          println '$("select[name=queryGroup]").val("'+ queryInstance.queryGroup?.uid +'");'

          println 'query.set_id("'+ queryInstance.id +'");'
          println 'query.set_name("'+ queryInstance.name +'");'
          println 'query.set_query_group("'+ queryInstance.queryGroup?.uid +'");'
          println 'query.set_type("'+ queryInstance.type +'");'

          if (queryInstance.isPublic)
            println 'query.set_public();' // FIXME is not checking the is public checkbox?

          println 'query.set_format("'+ queryInstance.format +'");'
          println 'query.set_group("'+ queryInstance.group +'");'

          if (queryInstance.templateId)
            println 'query.set_template_id("'+ queryInstance.templateId +'");'

          if (queryInstance.type == 'composition')
          {
             println '$("select[name=templateId]").val("'+ queryInstance.templateId +'");'

             // generates the code to setup the criteria_builder state
             // then we need to render the criteria_builder state to update the GUI
             println g.query_criteria_edit(query: queryInstance)

             if (queryInstance.isCount)
             {
               println 'query.set_is_count(true);'
               println '$("input[name=isCount]").prop("checked", "true");'
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
         $("#criteria_builder_remove_criteria").on("click", function(e) {

            //e.preventDefault();

            var c_dom = $('#criteria_builder input:checked')[0];
            var criteria_id = $(c_dom).data('cid');

            // remove criteria item UI from DOM
            var li_c = $(c_dom).parent().detach();

            // if the li has an ul, it is DOM of a complex criteria,
            // if not, it contains just the table of a simple criteria
            // for complex criteria need to put all the ul > lis from the c_dom,
            // inside the root #criteria_builder > ul
            // finds direct ul children, not all ul descendants
            var child_ul_c = li_c.find("> ul"); //$('ul', li_c);

            if (child_ul_c.length > 0)
            {
              console.log('complex DOM');

              // show selection checkbox to new li's on root
              //<input type="checkbox" name="criteria_builder_element_selector" data-cid="'+ cid +'" />

              // show selector checkboxes in each li to be able to manipulate the criteria
              child_ul_c.children().each(function (idx, li) {
                $(li).find('> [type=checkbox]').show(); // show checkboxes on direct children, avoid showing on descendants
              });

              $("#criteria_builder > ul").append( child_ul_c.children() );
            }
            else
            {
              console.log('simple DOM');
            }

            criteria_builder.remove_criteria(criteria_id);

            // remove buton back to disabled
            $("#criteria_builder_remove_criteria")[0].disabled = true;
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

          if (template_id == "") return;

          get_and_render_archetype_paths(template_id, archetype_id);

        }); // click en select view_archetype_id


        /**
         * Clic en una path de la lista (select[view_archetype_path])
         */
        $('select[name=view_archetype_path]').change(function() {
          var option = $(this).find(':selected');
          if (option.hasClass('datavalue'))
          {
            var datatype = option.data('type');
            get_criteria_specs(datatype);
          }
          else
          {
            alert('Select a simple type node to define the criteria');
          }

        }); // click en select view_archetype_path
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
                            optionKey="uid" optionValue="name" class="form-control input-sm" />
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

            <h2><g:message code="query.create.options"/></h2>
            <label><input type="checkbox" name="isCount" value="true" /> <g:message code="query.create.isCount" default="Is count?" /></label>

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
            <h2><g:message code="query.create.criteria" /></h2>

            <!--
            ul < criteria builder
              li < criteria item
                table < criteria item single
                ul < criteria item and / or
                  li < and left (criteria item, single or and/or)
                  li < and right (criteria item, single or and/or)
            -->
            <div class="table-responsive" id="criteria_builder">
              <button type="button" class="btn btn-success" id="criteria_builder_add_and" disabled="true">+ AND</button>
              <button type="button" class="btn btn-success" id="criteria_builder_add_or" disabled="true">+ OR</button>
              <button type="button" class="btn btn-danger" id="criteria_builder_remove_criteria" disabled="true">Remove</button>
              <ul>
              </ul>
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
