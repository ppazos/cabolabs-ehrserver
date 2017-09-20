<div class="form-group ${hasErrors(bean: folderTemplate, field: 'name', 'has-error')}">
  <label class="control-label"><g:message code="folderTemplate.attr.name" /></label>
  <g:textField name="name" value="${folderTemplate?.name}" class="form-control" />
</div>
<div class="form-group ${hasErrors(bean: folderTemplate, field: 'description', 'has-error')}">
  <label class="control-label"><g:message code="folderTemplate.attr.description" /></label>
  <g:textField name="description" value="${folderTemplate?.description}" class="form-control" />
</div>
<div class="form-group ${hasErrors(bean: folderTemplate, field: 'folders', 'has-error')}">
  <label class="control-label"><g:message code="folderTemplate.attr.folders" /></label>
  <span class="help-block"><g:message code="folderTemplate.create.folders.help" /></span>
  <div class="input-group">
    <g:textField name="new_folder" class="form-control"  placeholder="${message(code:'folderTemplate.label.folderName')}"/>
    <span class="input-group-btn">
      <button class="btn btn-default" id="add_node"><i class="fa fa-plus"></i></button>
    </span>
  </div>
  <div id="jstree_container"></div>
</div>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/jstree.min.js"></script>
    
<script type="text/javascript">

var folder_template = {
  id: undefined, // used for edit/update
  name: undefined,
  description: undefined,
  folders: [],
  set_name: function (name) {
    this.name = name;
  },
  set_description: function (description) {
    this.description = description;
  },
  set_folders: function(jstree_json) {
  
    jstree_json.forEach( function(n) {
      //console.log(n, folder_template);
      node = folder_template.set_folders_traverse(n, n.children);
      folder_template.folders.push( node );
    });
  },
  set_folders_traverse: function(parent, jstree_json_children) { // recursive
  
    var folder = {name: parent.text, folders: []};
    jstree_json_children.forEach( function(n) {
      //console.log(n);
      folder.folders.push( folder_template.set_folders_traverse(n, n.children) );
    });
    return folder;
  },
  has_folders: function() {
    return folder_template.folders.length != 0;
  }
};

 $(function () {
   $('#jstree_container').jstree({
     'core' : {
       'check_callback': true, // allows editing the tree
       'multiple' : false,
       'data' : ${foldersTree}
     },
     'plugins' : [ 'dnd' ]
   })
   .bind("ready.jstree", function (event, data) {
     $(this).jstree("open_all");
   })
   .bind("move_node.jstree", function (event, data) { /* opens the tree on drag and drop */
     $(this).jstree("open_all");
   });
   
   /*
   $(document).on('dnd_stop.vakata', function (e, data) {
     console.log('stop', e, data);
     
     setTimeout(function(){
       $(this).jstree("open_all");
       //var json = $("#jstree_container").jstree(true).get_json();
       //console.log(JSON.stringify(json));
     }, 100);
   });
   */
   
   $('#add_node').on('click', function(e) {

     var name = $('[name=new_folder]').val();
     if (!name)
     {
        alert('Please enter a name for the new folder');
        return false;
     }
     
     $('#jstree_container').jstree(true).create_node('#', {text: name}, 'last');
     //console.log( $('#jstree_container').jstree(true).last_error());
     
     $('[name=new_folder]').val('');
     
     e.preventDefault();
   });
   
   
   $('[name=create]').on('click', function(e) {
     name = $('[name=name]').val();
     if (!name)
     {
        alert("${message(code:'folderTemplate.create.feedback.nameMandatory')}");
        return false;
     }
     
     description = $('[name=description]').val();
     if (!description)
     {
        alert("${message(code:'folderTemplate.create.feedback.descriptionMandatory')}");
        return false;
     }
     
     folder_template.set_name(name);
     folder_template.set_description(description);
     folder_template.set_folders( $("#jstree_container").jstree(true).get_json() );
     
     if (!folder_template.has_folders())
     {
        alert("${message(code:'folderTemplate.create.feedback.oneFolderRequired')}");
        return false;
     }
     
     console.log(JSON.stringify(folder_template));
     
     
      $.ajax({
         method: 'POST',
         url: '${createLink(controller:"folderTemplate", action:"save")}',
         contentType : 'application/json',
         data: JSON.stringify( {folderTemplate: folder_template} ) // JSON.parse(  avoid puting functions, just data
       })
       .done(function( data ) {
         console.log(data);
         location.href = data.ref;
       })
       .fail(function(resp,status,status_msg) {
         console.log(resp,status,status_msg);
       /*
          $('body').prepend(
             '<div class="alert alert-danger alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
             resp.responseJSON.message +'</div>'
          );*/
       });
             
     
     e.preventDefault();
   });
 });
 </script>