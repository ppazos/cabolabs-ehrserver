<div class="form-group ${hasErrors(bean: queryGroupInstance, field: 'name', 'error')} required">
  <label for="name">
    <g:message code="queryGroup.attr.name" />
    <span class="required-indicator">*</span>
  </label>
  <g:textField name="name" required="" value="${queryGroupInstance?.name}" class="form-control" />
</div>
