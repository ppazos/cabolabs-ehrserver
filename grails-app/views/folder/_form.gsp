<%@ page import="directory.Folder" %>

<div class="fieldcontain ${hasErrors(bean: folderInstance, field: 'uid', 'error')} required">
   <label for="uid">
      <g:message code="folder.uid.label" default="Uid" />
      <span class="required-indicator">*</span>
   </label>
   <g:textField name="uid" required="" value="${folderInstance?.uid}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: folderInstance, field: 'parent', 'error')} ">
	<label for="parent">
		<g:message code="folder.parent.label" default="Parent" />
	</label>
	<g:select id="parent" name="parent.id" from="${directory.Folder.list()}" optionKey="id" value="${folderInstance?.parent?.id}" class="many-to-one" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: folderInstance, field: 'ehr', 'error')} ">
   <label for="parent">
      <g:message code="folder.ehr.label" default="EHR" />
   </label>
   <g:select id="ehr" name="ehr.id" from="${ehr.Ehr.findAllByDirectoryIsNull()}" optionKey="id" value="${folderInstance?.ehr?.id}" class="many-to-one" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: folderInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="folder.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${folderInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: folderInstance, field: 'items', 'error')} ">
	<label for="items">
		<g:message code="folder.items.label" default="Items" />
	</label>
</div>

<script type="text/javascript">
  $(document).ready(function() {

     /**
      * folder.ehr and folder.parent should be XORed.
      * if the folder doesnt have a parent, should be the EHR.directory (root), it has a ehr.
      * if the folder has a parent, it should not have ehr because the rel EHR/Folder is bidirectional and EHR.directory can only reference to the root folder.
      */
     $('select[name="parent.id"]').change( function() {

        if (this.value != '')
        {
           $('select[name="ehr.id"]').prop('disabled', 'disabled');
        }
        else $('select[name="ehr.id"]').prop('disabled', false);
     });

     $('select[name="ehr.id"]').change( function() {

        if (this.value != '')
        {
           $('select[name="parent.id"]').prop('disabled', 'disabled');
        }
        else $('select[name="parent.id"]').prop('disabled', false);
     });
  });
</script>
