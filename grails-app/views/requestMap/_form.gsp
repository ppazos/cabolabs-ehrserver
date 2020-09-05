<%@ page import="com.cabolabs.security.Role" %>

<asset:stylesheet src="animate.css" />
<asset:stylesheet src="pnotify.custom.min.css" />
<asset:javascript src="pnotify.custom.min.js" />
<asset:javascript src="debounce.js" />

<div class="form-group ${hasErrors(bean: requestMapInstance, field: 'url', 'error')} required">
	<label for="url">
		<g:message code="requestMap.url.label" default="URL" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="url" required="" value="${requestMapInstance?.url}" class="form-control" />
</div>

<div class="form-group">
	<label for="roles">
		<g:message code="requestMap.roles.label" default="Roles" />
	</label>
   <g:select from="${roles}" noSelection="${['null':message(code:'defaut.select.selectOne')]}"
             name="role" class="form-control" />
</div>

<div class="form-group ${hasErrors(bean: requestMapInstance, field: 'configAttribute', 'error')} required">
	<label for="configAttribute">
		<g:message code="requestMap.configAttribute.label" default="Config Attribute" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="configAttribute" required="" value="${requestMapInstance?.configAttribute}" class="form-control" />
</div>

<script>

var validate = function () {

  // setup
  var valid = true;
  var valid_roles = [];
  $.each( $('[name=role] > option'), function( key, option ) {
    if (option.value != 'null') valid_roles.push( option.value );
  });

  config = $('[name=configAttribute]').val();
  items = config.split(',');
  //console.log(items);

  // check valid roles
  $.each( items, function( key, item ) {
    if (item == '')
    {
      new PNotify({
        title: 'Warning',
        text : 'Remove the extra "," from the config',
        type : 'error',
        styling: 'bootstrap3',
        history: false,
        stack: {
          "dir1": "down", "dir2": "left", "push": "bottom", "spacing1": 25, "spacing2": 25, "context": $("body"),
          modal: true, overlay_close: true
        },
        addclass: 'pnotify-center'
      });
      valid = false;
    }
    else if (valid_roles.indexOf( item ) == -1)
    {
      new PNotify({
        title: 'Warning',
        text : '"'+ item +'" is not a valid role',
        type : 'error',
        styling: 'bootstrap3',
        history: false,
        stack: {
          "dir1": "down", "dir2": "left", "push": "bottom", "spacing1": 25, "spacing2": 25, "context": $("body"),
          modal: true, overlay_close: true
        },
        addclass: 'pnotify-center'
      });
      valid = false;
    }
  });

  // return to correct current errors then check other errors
  if (!valid) return valid;

  // check duplicates entered by hand
  function _unique(value, index, self) {
    return self.indexOf(value) === index;
  }
  unique_items = items.filter( _unique );
  //console.log(unique_items);

  if (unique_items.length < items.length)
  {
    config = '';
    $.each( unique_items, function( key, role ) {
      if (config == '') config = role;
      else config += ','+ role;
    });

    $('[name=configAttribute]').val(config);

    new PNotify({
      title: 'Warning',
      text : 'Duplicates removed from config attribute',
      type : 'error',
      styling: 'bootstrap3',
      history: false,
      stack: {
        "dir1": "down", "dir2": "left", "push": "bottom", "spacing1": 25, "spacing2": 25, "context": $("body"),
        modal: true, overlay_close: true
      },
      addclass: 'pnotify-center'
    });
    
    valid = false;
  }

  return valid;
};

$(function() {

  // Add role to ocnfig on role select change
  $('[name=role]').on('change', function(e) {

    role = this.value;
    config = $('[name=configAttribute]').val();

    if (config == '') config = role;
    else if (config.indexOf(role) == -1) // adds the role if it is not there yet
    {
       config += ','+role;
    }

    $('[name=configAttribute]').val(config);

  });

  // On update of configAttribute, validate role names to avoid errors and remove uneeded ","
  $('[name=configAttribute]').on('keyup', debounce(function(e) {

    validate();
  }, 500));

  // Form submit validate
  $('form').on('submit', function (e) {

    if (!validate())
    {
      e.preventDefault();
    }
  });
});
</script>
