<%@ page import="directory.Folder" %>

<div class="control-group" class="fieldcontain ${hasErrors(bean: folderInstance, field: 'parent', 'has-error')}">
  <label class="control-label"><g:message code="folder.parent.label" default="Parent" /></label>
  <div class="controls">
    <p class="form-control-static"><g:select id="parent" name="parent.id" from="${directory.Folder.list()}" optionKey="id" optionValue="name" value="${folderInstance?.parent?.id}" class="many-to-one" noSelection="['': '']"/></p>
  </div>
</div>

<div class="control-group" class="fieldcontain ${hasErrors(bean: folderInstance, field: 'ehr', 'has-error')}">
  <label class="control-label"><g:message code="folder.ehr.label" default="EHR" /></label>
  <div class="controls">
    <p class="form-control-static"><g:select id="ehr" name="ehr.id" from="${ehrs}" optionKey="id" class="many-to-one" noSelection="['': '']"/></p>
  </div>
</div>

<div class="control-group" class="fieldcontain ${hasErrors(bean: folderInstance, field: 'name', 'has-error')} required">
  <label class="control-label"><g:message code="folder.name.label" default="Name" /></label>
  <div class="controls">
    <p class="form-control-static"><g:textField name="name" required="" value="${folderInstance?.name}"/></p>
  </div>
</div>

<div class="control-group" class="fieldcontain ${hasErrors(bean: folderInstance, field: 'items', 'has-error')}">
  <label class="control-label"><g:message code="folder.items.label" default="Items" /></label>
  <div class="controls">
    <p class="form-control-static">
      <g:each in="${folderInstance.items}">
        ${it}<br/>
      </g:each>
    </p>
  </div>
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
