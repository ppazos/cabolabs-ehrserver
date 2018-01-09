
// ====================================================
// Concepts and filters module
// ====================================================

/**
 * Gets ArchetypeIndexItems to fill the concept select.
 */
var get_concepts = function(callback) {
  
  $.ajax({
    url: get_concepts_url,
    data: {},
    dataType: 'json',
    success: function(data, textStatus)
    {
      callback(data);
    },
    error: function(xhr, textStatus, errorThrown)
    {
      console.log(xhr, textStatus, errorThrown);
    }
  });
};

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

var render_templates = function(concept_filter_templateid, aiis) {

  templates = [];
    
  aiis.forEach( function(aii) {

    aii.parentOpts.forEach( function(opt)
    {
      templates.push(opt.templateId);
    });
  });
    
    
  // populate filters
  templateid_select = document.getElementById(concept_filter_templateid);
  templates.unique().forEach( function(templateid) {
      
    option = document.createElement("option");
    option.value = templateid;
    option.text = templateid;
    
    templateid_select.add(option);
  });
};

// to filter unique template ids and archetype ids for the filters
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
      option.text = aii.name['ISO_639-1::'+ session_lang] +' ('+ aii.archetypeId +')';
      
      
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


// populate concepts select and filters
var concept_callback = render_templates.bind(null, 'view_template_id');
get_concepts(concept_callback);

// ====================================================
// /Concepts and filters module
// ====================================================
