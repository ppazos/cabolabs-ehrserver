<%@ page import="com.cabolabs.ehrserver.query.EhrQuery" %>

<div class="fieldcontain ${hasErrors(bean: ehrQueryInstance, field: 'name', 'error')} required">
  <label for="name">
    <g:message code="ehrQuery.name.label" default="Name" />
    <span class="required-indicator">*</span>
  </label>
  <g:textField name="name" required="" value="${ehrQueryInstance?.name}" class="form-control"/>
</div>

<div class="fieldcontain ${hasErrors(bean: ehrQueryInstance, field: 'description', 'error')} ">
  <label for="description">
    <g:message code="ehrQuery.description.label" default="Description" />
  </label>
  <g:textField name="description" value="${ehrQueryInstance?.description}" class="form-control"/>
</div>

<div class="fieldcontain ${hasErrors(bean: ehrQueryInstance, field: 'queries', 'error')} ">
  <label for="queries">
    <g:message code="ehrQuery.queries.label" default="Queries" />
  </label>
  <g:select name="queries"
            from="${com.cabolabs.ehrserver.query.Query.findAllByType('composition')}"
            multiple="multiple" optionKey="id" size="5"
            value="${ehrQueryInstance?.queries*.id}"
            class="form-control"/>
</div>



