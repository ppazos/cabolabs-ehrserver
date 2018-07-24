
// ====================================================
// Concepts and filters module
// ====================================================

var get_archetypes_in_template = function(template_id, callback) {

  $.ajax({
    url: get_archetypes_in_template_url,
    data: {template_id: template_id},
    dataType: 'json',
    success: function(data, textStatus)
    {
      callback(data); // TODO: remder on the view_archetype_id select
    },
    error: function(xhr, textStatus, errorThrown)
    {
      console.log(xhr, textStatus, errorThrown);
    }
  });
};


// to filter unique template ids and archetype ids for the filters
// I think this is not used any more
Array.prototype.unique = function() {

  return this.filter(function (value, index, self)
  {
    return self.indexOf(value) === index;
  });
};

// filters behavior
var templateid_select = document.getElementById('view_template_id');

templateid_select.onchange = function (ev) {

  get_archetypes_in_template( $(this).val(), function(aiis) {

    console.log(aiis);

    // empty paths if previous archetype was selected
    var path_select = document.getElementById('view_archetype_path');
    var default_option = path_select.children[0];
    path_select.innerHTML = '';
    path_select.add(default_option);


    // remder archetypes
    var select = document.getElementById('view_archetype_id'); // view_archetype_id

    select.innerHTML = ''; // removes current archids

    archetypes = []; // to fill the archetype filter
    archetypeids = '';
    aiis.forEach( function(aii) {

      option = document.createElement("option");
      option.value = aii.archetypeId;
      option.text = aii.name[session_lang] +' ('+ aii.archetypeId +')';


      // to be used on the archetype id filter matching
      aii.parentOpts.forEach( function(opt)
      {
        archetypeids += opt.archetypeId+'|';
      });
      option.setAttribute('data-archetypeid', archetypeids);


      select.add(option);

      // to fill filter by compo archetype id
      if (aii.rmTypeName == 'COMPOSITION')
      {
        archetypes.push(aii.archetypeId);
      }
    });
  });
};

// ====================================================
// /Concepts and filters module
// ====================================================

// ====================================================
// Complex condition builder module
// ====================================================

// CriteriaBuilderJS

// Criteria Expression Module
(function() {

  "use strict";

  // expression id generator
  var id = 1;

  var get_id = function () {
    return id++;
  };

  // constructor EXPR --------------------------------------------------------
  var criteria_expression = function() {
    this.id = get_id();
  };

  // API
  criteria_expression.fn = criteria_expression.prototype = {
    log: function () {
      console.log(this);
    }
  };


  // constructor COND --------------------------------------------------------
  var criteria_expression_condition = function(condition) {
    criteria_expression.call(this); // super constructor
    this._class = 'COND';
    this.condition = condition; // can be undefined
  };

  criteria_expression_condition.prototype = Object.create(criteria_expression.prototype);
  criteria_expression_condition.prototype.constructor = criteria_expression_condition;

  // API
  criteria_expression_condition.fn = criteria_expression_condition.prototype;

  criteria_expression_condition.fn.is_valid = function () {
    return !!this.condition;
  };
  criteria_expression_condition.fn.set_condition = function (cond) { // condition from simple criteria builder
    this.condition = cond;
  };

  // constructor OR --------------------------------------------------------
  var criteria_expression_or = function() {
    criteria_expression.call(this); // super constructor
    this._class = 'OR';
    this.expr_left = undefined;
    this.expr_right = undefined;
  };

  criteria_expression_or.prototype = Object.create(criteria_expression.prototype);
  criteria_expression_or.prototype.constructor = criteria_expression_or;

  // API
  criteria_expression_or.fn = criteria_expression_or.prototype;

  // add local functions
  criteria_expression_or.fn.is_valid = function () {
    return !!this.expr_left && !!this.expr_right && this.expr_left instanceof criteria_expression && this.expr_right instanceof criteria_expression && this.expr_left.is_valid() && this.expr_right.is_valid();
  };
  criteria_expression_or.fn.add_left = function (expr) { // add local func
    this.expr_left = expr;
  };
  criteria_expression_or.fn.add_right = function (expr) { // add local func
    this.expr_right = expr;
  };


  // constructor AND --------------------------------------------------------
  var criteria_expression_and = function() {
    criteria_expression.call(this); // super constructor
    this._class = 'AND';
    this.expr_left = undefined;
    this.expr_right = undefined;
  };

  criteria_expression_and.prototype = Object.create(criteria_expression.prototype);
  criteria_expression_and.prototype.constructor = criteria_expression_and;

  // API
  criteria_expression_and.fn = criteria_expression_and.prototype;

  // add local functions
  criteria_expression_and.fn.is_valid = function () {
    return !!this.expr_left && !!this.expr_right && this.expr_left instanceof criteria_expression && this.expr_right instanceof criteria_expression && this.expr_left.is_valid() && this.expr_right.is_valid();;
  };

/*
  // criteria builder -------------------------------------------------------
  // holds a temporal, maybe invalid expression, until it is valid because the
  // user edited the expression, then can be part of a query.
  var criteria_builder = function() {

    this.expressions = [];
  };

  // API
  criteria_builder.fn = criteria_builder.prototype;
  criteria_builder.fn.add_expression = function (expr) {
    this.expressions.push(expr);
  };
  criteria_builder.fn.and = function (expr_left, expr_right) {
    // TODO
  };

  // both parameters are contained in the expressions array
  // this creates and OR, sets the exprs in the OR condition
  // and removes the exprs from the epressions array
  criteria_builder.fn.or = function (expr_left, expr_right) {
    var _or = new criteria_expression_or();
    _or.add_left(expr_left);
    _or.add_right(expr_right);

    var expr_left_idx = this.expressions.findIndex( function(e){ return e.id == expr_left.id} );
    this.expressions.splice(expr_left_idx, 1);

    var expr_right_idx = this.expressions.findIndex( function(e){ return e.id == expr_right.id} );
    this.expressions.splice(expr_right_idx, 1);

    console.log(expr_left_idx, expr_right_idx, this.expressions);

    this.add_expression(_or);
  };
  criteria_builder.fn.remove_expression = function (id) {
    var expr = this.expressions.find( function(e){ return e.id == id} );
    var expr_idx = this.expressions.findIndex( function(e){ return e.id == id} );
    if (expr instanceof criteria_expressions_condition)
    {

    }
    else // AND/OR internal left and right should be stored in the expressions root
    {
      this.expressions.push(expr.expr_right);
      this.expressions.push(expr.expr_left);
    }
    this.expressions.splice(expr_idx, 1);
  };

  criteria_builder.fn.is_valid = function () {
    return this.expressions.length == 1 && this.expressions[0].is_valid();
  };

  criteria_builder.fn.get_valid_expression = function () {
    if (this.is_valid())
    {
      return this.expressions[0];
    }
    throw "Current expression is not valid";
  };
*/


  // parent can be criteria_builder / null, or an and / or expression (all the container types)
  // expr is the expression to move
  // new_parent can also be criteria_builder / null, or an and / or expression
  /*
  criteria_builder.fn.move_expression = function (parent, expr, new_parent) {
    this.expressions.push(expr);
  };
  */


  // glogals ----------------------------------------------------------------
  //window.criteria_builder = criteria_builder;
  window.criteria_expression = criteria_expression;
  window.criteria_expression_condition = criteria_expression_condition;
  window.criteria_expression_or = criteria_expression_or;
  window.criteria_expression_and = criteria_expression_and;
})();


/*
var builder = new criteria_builder();
var c1 = new criteria_expression_condition('a > 2');
var c2 = new criteria_expression_condition('b = 5');
builder.add_expression( c1 );
builder.add_expression( c2 );
builder.or(c1, c2);

console.log(builder.is_valid());
builder.get_valid_expression().log();
*/
