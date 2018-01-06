
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
      //console.log(data);
      callback(data);
      //return data;
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

var render_concepts = function(conainer_select_id, concept_filter_templateid, concept_filter_archetypeid, concepts) {

  var select = document.getElementById(conainer_select_id); // view_archetype_id
  if (select)
  {
    templates = [];
    archetypes = [];
    
    concepts.forEach( function(aii) {
      
      //console.log(aii);
      
/* this should be done when view_template_id is changed
      option = document.createElement("option");
      option.value = aii.archetypeId;
      // aii.name = {lang:name, lang:name}
      option.text = aii.name['ISO_639-1::'+ session_lang] +' ('+ aii.archetypeId +')';

      // data attrs to filter concepts
      templateids = '';
      archetypeids = '';
*/

      aii.parentOpts.forEach( function(opt)
      {
/*
        templateids += opt.templateId+'|';
        archetypeids += opt.archetypeId+'|';
*/
        templates.push(opt.templateId);
        archetypes.push(opt.archetypeId);
      });

/*
      // setting all the compo archetypes and owning OPTs on each item of select view_archetype_id
      // this is used to filter by archetype id
      option.setAttribute('data-templateid', templateids);
      option.setAttribute('data-archetypeid', archetypeids);
      
      select.add(option);
*/
    });
    
    
    // populate filters
    templateid_select = document.getElementById(concept_filter_templateid);
    templates.unique().forEach( function(templateid) {
      
      option = document.createElement("option");
      option.value = templateid;
      option.text = templateid;
      
      templateid_select.add(option);
    });
    
    /* archetype id filter should be populated when a template is selected on view_template_id
    concept_filter_archetypeid_select = document.getElementById(concept_filter_archetypeid);
    archetypes.unique().forEach( function(archetypeid) {
      
      option = document.createElement("option");
      option.value = archetypeid;
      option.text = archetypeid;
      
      concept_filter_archetypeid_select.add(option);
    });
    */
  }
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
var concept_filter_archetypeid_select = document.getElementById('concept_filter_archetypeid');

templateid_select.onchange = function (ev) {

  console.log(this);
  get_archetypes_in_template( $(this).val(), function(aiis) {
  
    console.log(aiis);
    
    // remder archetypes
    var select = document.getElementById('view_archetype_id'); // view_archetype_id
    
    select.innerHTML = ''; // removes current archids
    
    aiis.forEach( function(aii) {
      
      option = document.createElement("option");
      option.value = aii.archetypeId;
      // aii.name = {lang:name, lang:name}
      option.text = aii.name['ISO_639-1::'+ session_lang] +' ('+ aii.archetypeId +')';
      
      select.add(option);
    });
  });
};

/*
concept_filter_templateid_select.onchange = function (ev) {

  // reset the other filter
  concept_filter_archetypeid_select.selectedIndex = 0;
  
  var concepts_select = document.getElementById('view_archetype_id');
  for (i = 0; i < concepts_select.children.length; i++)
  {
    option = concepts_select.children[i];
    $(option).show(); // show from previous hidden
    
    if (this.value != '') // show all if no filter is selected
    {
      if (option.getAttribute('data-templateid').indexOf(this.value) == -1)
      {
        $(option).hide();
      }
    }
  };
};
*/

concept_filter_archetypeid_select.onchange = function (ev) {
   
  // reset the other filter
  //concept_filter_templateid_select.selectedIndex = 0;
  
  var concepts_select = document.getElementById('view_archetype_id');
  for (i = 0; i < concepts_select.children.length; i++)
  {
    option = concepts_select.children[i];
    $(option).show(); // show from previous hidden
    
    if (this.value != '') // show all if no filter is selected
    {
      if (option.getAttribute('data-archetypeid').indexOf(this.value) == -1)
      {
        $(option).hide();
      }
    }
  };
};

// populate concepts select and filters
var concept_callback = render_concepts.bind(null, 'view_archetype_id', 'view_template_id', 'concept_filter_archetypeid');
get_concepts(concept_callback);

// ====================================================
// /Concepts and filters module
// ====================================================
