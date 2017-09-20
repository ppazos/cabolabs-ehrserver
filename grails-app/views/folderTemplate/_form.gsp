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
  
  <g:textField name="new_folder" class="form-control" /> <button id="add_node"><i class="fa fa-plus"></i></button>
  
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
   });
   /*
   $(document).on('dnd_stop.vakata', function (e, data) {
       setTimeout(function(){
           var json = $("#jstree_container").jstree(true).get_json();
           console.log(JSON.stringify(json));
           // Here I make an AJAX call to save the tree in database
       }, 100);
   });
   */
   
   $('#add_node').on('click', function(e) {

     $('#jstree_container').jstree(true).create_node('#', {text: $('[name=new_folder]').val()}, 'last');
     console.log( $('#jstree_container').jstree(true).last_error());
     e.preventDefault();
   });
   
   
   $('[name=create]').on('click', function(e) {
     name = $('[name=name]').val();
     if (!name) alert('Name is mandatory');
     
     description = $('[name=description]').val();
     if (!description) alert('Description is mandatory');
     
     folder_template.set_name(name);
     folder_template.set_description(description);
     folder_template.set_folders( $("#jstree_container").jstree(true).get_json() );
     
     console.log(JSON.stringify(folder_template));
     
     
      $.ajax({
         method: 'POST',
         url: '${createLink(controller:"folderTemplate", action:"save")}',
         contentType : 'application/json',
         data: JSON.stringify( {folderTemplate: folder_template} ) // JSON.parse(  avoid puting functions, just data
       })
       .done(function( data ) {
         console.log(data);
         //location.href = '${createLink("action": "show")}?uid='+ data.uid;
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