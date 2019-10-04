<%@ page import="com.cabolabs.ehrserver.openehr.directory.Folder" %>

<div class="form-group ${hasErrors(bean: folderInstance, field: 'parent', 'has-error')}">
  <label class="control-label"><g:message code="folder.parent.label" default="Parent" /></label>
  <g:select id="parent" name="parent.id" from="${Folder.list()}" optionKey="id" optionValue="name" value="${folderInstance?.parent?.id}" class="many-to-one form-control" noSelection="['': '']"/>
</div>

<div class="form-group ${hasErrors(bean: folderInstance, field: 'ehr', 'has-error')}">
  <label class="control-label"><g:message code="folder.ehr.label" default="EHR" /></label>
  <g:select id="ehr" name="ehr.id" from="${ehrs}" optionKey="id" class="many-to-one form-control" noSelection="['': '']"/>
</div>

<div class="form-group ${hasErrors(bean: folderInstance, field: 'name', 'has-error')} required">
  <label class="control-label"><g:message code="folder.name.label" default="Name" /></label>
  <g:textField name="name" required="" value="${folderInstance?.name}" class="form-control" />
</div>

<g:if test="${actionName == 'edit'}">
   <div class="form-group ${hasErrors(bean: folderInstance, field: 'items', 'has-error')}">
     <label class="control-label"><g:message code="folder.items.label" default="Items" /></label>
     <g:if test="${folderInstance.items.size() == 0}">
       <div>There are no items in the folder</div>
     </g:if>
     <g:each in="${folderInstance.items}">
       ${it}<br/>
     </g:each>
   </div>
</g:if>

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
