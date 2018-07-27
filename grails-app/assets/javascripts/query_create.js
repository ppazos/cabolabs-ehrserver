
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
